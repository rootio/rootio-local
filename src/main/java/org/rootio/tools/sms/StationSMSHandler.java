package org.rootio.tools.sms;

public class StationSMSHandler implements MessageProcessor {

    private final String from;
    private final String[] messageParts;

    public StationSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length != 2) {
            return false;
        }

        // rebooting the phone
        if (messageParts[1].equals("reboot")) {
            try {
                return this.reboot();
            } catch (Exception ex) {
                return false;
            }
        }

        return false;
    }

    private boolean reboot() {
        return false;
    }


    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //respond
    }

}
