package org.rootio.tools.sms;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SoundSMSHandler implements MessageProcessor {

    private String[] messageParts;

    public SoundSMSHandler(String from, String[] messageParts) {
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        if (messageParts.length < 5) {
            return;
        }

        // setting volume
        if (messageParts[4].equals("volume")) {
            try {
                this.setVolume(Integer.parseInt(messageParts[5]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SoundSMSHandler.processMessage]" : e.getMessage());
            }
        }

        // setting the equalizer
        if (messageParts[4].equals("equalizer")) {
            try {
                this.setEqualiser(messageParts[5]);
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SoundSMSHandler.processMessage]" : e.getMessage());
            }
        }
    }

    /**
     * Sets the volume to the specified level
     *
     * @param level The level to which to set the volume. ranging from 0 - 15
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean setVolume(int level) {
        return false;
    }

    /**
     * Sets the equaliser for the audio channel
     *
     * @param equaliserName The name of the equaliser to apply to the audio channel
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean setEqualiser(String equaliserName) throws Exception {

        throw new Exception("Method not implemented");
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send response SMS
    }
}
