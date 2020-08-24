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

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SIPService implements RootioService {
    private ServerSocket sck;
    private Thread runnerThread;
    private int serviceId = 6;
    private boolean isRunning;

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "SIP Service");
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
                try
                {
                    writeTwinkleConfig();
                    restartTwinkle();
                }
                catch(IOException e)
                {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SIPService.start]" : e.getMessage());
                }
                runnerThread.wait();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    stop();
                }
            }
        }
    }

    private void restartTwinkle()
    {
        try {
            Runtime.getRuntime().exec("killall -9 twinkle");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Runtime.getRuntime().exec(String.format("twinkle -f %s &", Configuration.getProperty("twinkle_profile","raspi")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTwinkleConfig() throws IOException {
        Properties twinkleConfig = new Properties();
        File twinkleConfigFile = new File(Configuration.getProperty("twinkle_config_path","~/.twinkle/raspi.cfg"));
        try(InputStream instr = new FileInputStream(twinkleConfigFile))
        {
            twinkleConfig.load(instr);
        }
        twinkleConfig.setProperty("user_name", Configuration.getProperty("sip_user_name"));
        twinkleConfig.setProperty("user_domain", Configuration.getProperty("sip_server"));
        twinkleConfig.setProperty("auth_realm", Configuration.getProperty("sip_server"));
        twinkleConfig.setProperty("auth_pass", Configuration.getProperty("sip_password"));
        twinkleConfig.setProperty("registrar", Configuration.getProperty("sip_server"));
        twinkleConfig.setProperty("sip_transport", Configuration.getProperty("sip_transport"));
        twinkleConfig.setProperty("stun_server", Configuration.getProperty("sip_stun_server"));

        //scripts called by twinkle to communicate change in status
        twinkleConfig.setProperty("script_incoming_call", Configuration.getProperty("sip_script_incoming_call"));
        twinkleConfig.setProperty("script_in_call_answered", Configuration.getProperty("sip_script_in_call_answered"));
        twinkleConfig.setProperty("script_in_call_failed", Configuration.getProperty("sip_script_in_call_failed"));
        twinkleConfig.setProperty("script_local_release", Configuration.getProperty("sip_script_local_release"));
        twinkleConfig.setProperty("script_remote_release", Configuration.getProperty("sip_script_remote_release"));

        try(FileWriter wri = new FileWriter(twinkleConfigFile))
        {
            twinkleConfig.store(wri,"Config Modified by Rootio");
        }
    }


    @Override
    public void stop() {
        if(sck != null) {
            try {
                sck.close();
            } catch (IOException e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SIPService.stop]" : e.getMessage());
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getServiceId() {
        return serviceId;
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
        while (Rootio.getServiceStatus(serviceId)) {
            try {
                Socket cli = sck.accept();
                new Thread(() -> handleSIPClientConnection(cli)).start();
            } catch (IOException e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SIPService.startListener]" : e.getMessage());
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

        } catch (IOException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SIPService.readIncomingEvent]" : e.getMessage());
        }
        return new String(buf);
    }

    private JSONObject actOnSIPEvent(String eventInfo) {
        JSONObject event = new JSONObject(eventInfo);
        JSONObject response = new JSONObject();
        switch (event.getString("event_type")) {
            case "call_ringing":
                Utils.logEvent(EventCategory.SIP_CALL, EventAction.RINGING, event.toString());
                //broadcast the state
                announceCallStatus(CallState.RINGING);
                //send back a true.
                response.put("status", "ok");
                return response;
            case "call_answer":
                Utils.logEvent(EventCategory.SIP_CALL, EventAction.RECEIVE, event.toString());
                //broadcast the state
                Rootio.setInSIPCall(true);
                announceCallStatus(CallState.INCALL);
                //send back a true.
                response.put("status", "ok");
                return response;
            case "call_release":
                Rootio.setInSIPCall(false);
                Utils.logEvent(EventCategory.SIP_CALL, EventAction.STOP, event.toString());
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
