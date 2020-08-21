package org.rootio.tools.sms;

public class NetworkSMSHandler implements MessageProcessor {

    private String[] messageParts;
    private String from;

    NetworkSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        if (this.messageParts.length < 3) {
            return;
        }

        if (messageParts[1].equals("wifi")) {
            if (messageParts[2].equals("on") || messageParts[2].equals("off")) {
                this.toggleWifi(messageParts[2]);
            }

            if (messageParts[2].equals("status")) {
                this.getWifiConnection();
            }

            if (messageParts[2].equals("connect")) {
                this.connectToWifi(messageParts[3], messageParts[4]);
            }
        } else if (messageParts[1].equals("gsm")) {
            if (messageParts[2].equals("status")) {
                this.getGsmConnection();
            }
        } else if (messageParts[1].equals("data")) {
            if (messageParts[2].equals("on") || messageParts[2].equals("off")) {
                this.toggleData(messageParts[2]);
            }
        }
    }

    private boolean toggleData(String status) {
       return false;
    }

    /**
     * Sets the WiFI to the specified state
     *
     * @param state The State to which to set the WiFI. "ON" puts the WiFI on,
     *              anything else turns it off
     * @return Boolean representing whether or not the operation was successful
     */
    private boolean toggleWifi(String state) {
        return false;
    }

    /**
     * Connects to the wireless network with the specified SSID
     *
     * @param SSID     SSID of the network to which to connect
     * @param password The password of the network to which to connect
     * @return Boolean representing whether or not the operation was successful
     */
    private boolean connectToWifi(String SSID, String password) {
       return false;
    }

    /**
     * Fetches the state of the GSM connection including the name of the Telecom
     * operator and the signal strength
     *
     * @return String containing information about the GSM connection
     */
    private boolean getGsmConnection() {
        return false;
    }

    /**
     * Gets WiFI connectivity information including the network to which the
     * phone is connected and the IP address it obtained
     *
     * @return Boolean indicating whether or not getting WiFI information was
     * successful
     */
    private boolean getWifiConnection() {
       return false;
    }

    /**
     * Sets the state of the bluetooth to the specified state
     *
     * @param state The state to which to set the bluetooth. "ON" sets it on,
     *              anything else sets it off
     * @return Boolean representing whether the operation was successful
     */
    private boolean toggleBluetooth(String state) {
        return false;
    }

    /**
     * Connects to the specified bluetooth device
     *
     * @param deviceName The name of the bluetooth device to which tio connect
     * @return Boolean representing whether or not the operation was successful
     */
    private boolean connectBluetoothDevice(String deviceName) {
        return false;
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send message

    }

}
