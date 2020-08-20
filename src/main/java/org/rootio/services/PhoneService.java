package org.rootio.services;

import org.rootio.configuration.Configuration;
import org.rootio.launcher.Rootio;
import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.services.SIP.CallState;
import org.rootio.services.phone.ModemAgent;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PhoneService implements RootioService {
    private ModemAgent agent;
    private int serviceId;
    private Thread runnerThread;
    private BroadcastReceiver br;
    private Process proc;

    public PhoneService() {
        agent = new ModemAgent(Configuration.getProperty("modem_port", "/dev/ttyUSB3"));
    }

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Telephone Service");
        runnerThread = new Thread(() -> {
            try {
                agent.start();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PhoneService.start]" : e.getMessage());
            }
        });
        runnerThread.start();
        registerForTelephonyEvents();

        new ServiceState(7, "Telephone", 1).save();
        while (Rootio.isRunning()) {
            try {
                runnerThread.join();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    agent.shutDown();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private void registerForTelephonyEvents() {
        this.br = new BroadcastReceiver() {
            @Override
            public void onReceive(Message m) {
                String event = m.getEvent();
                switch (event) {
                    case "ring":
                        Rootio.setInCall(true);
                        String phoneNumber = (String) m.getPayLoad().get("b_party");
                        if (isAllowed(phoneNumber)) {
                            //announce the ring
                            announceCallStatus(CallState.RINGING);
                            //sleep for 5 secs while Program fades out
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //answer the call
                            MessageRouter.getInstance().specicast(new Message("answer", "call", new HashMap<>()), "org.rootio.services.phone.MODEM");
                        }
                    case "answer":
                        announceCallStatus(CallState.INCALL);
                        playCall();
                        break;
                    case "hangup":
                        Rootio.setInCall(false);
                        announceCallStatus(CallState.IDLE);
                        stopPlayCall();
                        break;
                }
            }
        };

        MessageRouter.getInstance().register(this.br, "org.rootio.telephony.CALL");
    }

    private void announceCallStatus(CallState callState) {
        String filter = "org.rootio.services.phone.TELEPHONY";
        HashMap<String, Object> payLoad = new HashMap<>();
        payLoad.put("eventType", callState.name());
        Message message = new Message(callState.name(), "telephony", payLoad);
        MessageRouter.getInstance().specicast(message, filter);
    }

    private void playCall() {
        try {
            proc = Runtime.getRuntime().exec(String.format("%s -r 8000 -c 1 -t %s %s -b 16 -t %s %s",
                    Configuration.getProperty("sox_path", "/usr/bin/sox"),
                    Configuration.getProperty("audio_driver", "alsa"), Configuration.getProperty("audio_input_device"),
                    Configuration.getProperty("audio_driver", "alsa"), Configuration.getProperty("audio_output_device", "-d")));
        } catch (IOException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PhoneService.playCall]" : e.getMessage());
            //maybe hangup?
        }

    }

    private void stopPlayCall() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OS.indexOf("win") >= 0) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM sox.exe");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else //hopefully nix variant
        {
            try {
                Runtime.getRuntime().exec("killall -9 sox");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isAllowed(String number) {
        return true;
    }
}