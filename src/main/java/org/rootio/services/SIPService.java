package org.rootio.services;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.launcher.Rootio;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.services.SIP.CallState;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class SIPService implements RootioService {
    private ServerSocket sck;
    private Thread runnerThread;
    private int serviceId = 6;
    private boolean isRunning;

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Media Indexing Service");
        runnerThread = new Thread(() -> {
            try {
                startListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        runnerThread.start();
        this.isRunning = true;

        new ServiceState(serviceId, "SIPService", 1).save();
        while (Rootio.isRunning()) {
            try {
                runnerThread.join();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    try {
                        sck.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    private void startListener() throws IOException {
        sck = new ServerSocket();
        InetSocketAddress addr = new InetSocketAddress(Integer.parseInt(Configuration.getProperty("sip_client_port", "5555")));
        boolean isBound = false;
        do {
            try {
                sck.bind(addr);
                isBound = true;
            } catch (BindException ex) {
                try {
                    Thread.sleep(30000); //Try again in 30 secs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (!isBound);
        while (Rootio.isRunning()) {
            try {
                Socket cli = sck.accept();
                new Thread(() -> handleSIPClientConnection(cli)).start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleSIPClientConnection(Socket con) {
        String incoming = readIncomingEvent(con);
        JSONObject response = actOnSIPEvent(incoming);
        sendResponse(con, response.toString());
    }

    private String readIncomingEvent(Socket con) {
        char[] buf = new char[1024]; //1KB
        try {
            InputStreamReader instr = new InputStreamReader(con.getInputStream());
            instr.read(buf, 0, buf.length);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new String(buf);
    }

    private JSONObject actOnSIPEvent(String eventInfo) {
        JSONObject event = new JSONObject(eventInfo);
        JSONObject response = new JSONObject();
        switch (event.getString("event_type")) {
            case "call_ringing":
                //broadcast the state
                announceCallStatus(CallState.RINGING);
                //send back a true.
                response.put("status", "ok");
                return response;
            case "call_answer":
                //broadcast the state
                Rootio.setInSIPCall(true);
                announceCallStatus(CallState.INCALL);
                //send back a true.
                response.put("status", "ok");
                return response;
            case "call_release":
                Rootio.setInSIPCall(false);
                //broadcast the state
                announceCallStatus(CallState.IDLE);
                //send back a true.
                response.put("status", "ok");
                return response;
        }
        response.put("status", "ok");
        return response;
    }

    private void sendResponse(Socket cli, String response) {
        try (OutputStreamWriter wri = new OutputStreamWriter(cli.getOutputStream())) {
            wri.write(response, 0, response.length());
            wri.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void announceCallStatus(CallState callState) {
        String filter = "org.rootio.services.sip.TELEPHONY";
        HashMap<String, Object> payLoad = new HashMap<>();
        payLoad.put("eventType", callState.name());
        Message message = new Message(callState.name(), "telephony", payLoad);
        MessageRouter.getInstance().specicast(message, filter);
    }
}
