package org.rootio.tools.sms;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StationSMSHandler implements MessageProcessor {

    private final String from;
    private final String[] messageParts;

    public StationSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        if (messageParts.length != 2) {
            return;
        }

        // rebooting the phone
        if (messageParts[1].equals("reboot")) {
            try {
                this.reboot();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[StationSMSHandler.processMessage]" : e.getMessage());
            }
        }
    }

    private void reboot() {
        try {
            Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //respond
    }

}
