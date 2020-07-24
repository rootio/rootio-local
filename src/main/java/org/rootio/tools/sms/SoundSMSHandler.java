package org.rootio.tools.sms;

public class SoundSMSHandler implements MessageProcessor {

    private String[] messageParts;

    public SoundSMSHandler(String from, String[] messageParts) {
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length < 5) {
            return false;
        }

        // setting volume
        if (messageParts[4].equals("volume")) {
            try {
                return this.setVolume(Integer.parseInt(messageParts[5]));
            } catch (Exception ex) {
                return false;
            }
        }

        // setting the equalizer
        if (messageParts[4].equals("equalizer")) {
            try {
                return this.setEqualiser(messageParts[5]);
            } catch (Exception ex) {
                return false;
            }
        }

        // unknown option
        return false;
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
