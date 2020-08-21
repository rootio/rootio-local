package org.rootio.tools.sms;

import org.rootio.messaging.Message;

public class SMSSwitch {

    private static String from;

    public static void switchSMS(Message message) {
        String fr = (String)message.getPayLoad().get("from");
        if(fr != null) {
            from = fr; //a header. only set the from
        }
        String body = (String)message.getPayLoad().get("body");
        if(body != null) { //a body. process this.
            String[] messageParts = ((String) message.getPayLoad().get("body")).split("|");
            switchSMS(messageParts).ProcessMessage();
        }
    }


    /**
     * Examines the message parts and returns a suitable message processor to
     * process the message
     *
     * @param messageParts Tokens from the message to be analyzed
     * @return a MessageProcessor to process the message
     */
    private static MessageProcessor switchSMS(String[] messageParts) {
        String keyword = messageParts.length > 0 ? messageParts[0] : "";
        switch(keyword.toLowerCase())
        {
            case "network": //network|<wifi | gsm >|<on | off | status>
                return new NetworkSMSHandler(from, messageParts);
            case "station": //station|
                return new StationSMSHandler(from, messageParts);
            case "services": //services|<service_id>|<start | stop | status>
                return new ServicesSMSHandler(from, messageParts);
            case "resources": //resources
                return new ResourcesSMSHandler(from, messageParts);
            case"sound": //sound
                return new SoundSMSHandler(from, messageParts);
            case "whitelist": //whitelist
                return new WhiteListSMSHandler(from, messageParts);
            case "ussd": //ussd|*123#
                return new USSDSMSHandler(from, messageParts);
            case "mark": //mark|<...>
                return new MarkHandler(from, messageParts);
            default:
            return null;
        }

    }
}
