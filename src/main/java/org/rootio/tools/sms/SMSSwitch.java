package org.rootio.tools.sms;

public class SMSSwitch {

    private String[] messageParts;
    private String from;

    public SMSSwitch(Object message) {
//        this.parent = parent;
//        this.from = message.getOriginatingAddress();
//        this.messageParts = this.getMessageParts(message.getMessageBody().toLowerCase());
    }

    /**
     * Gets a Message processor to be used to process the received message
     *
     * @return A MessageProcessor object to process the message
     */
    public MessageProcessor getMessageProcessor() {
        return this.switchSMS(this.messageParts);
    }

    /**
     * Tokenizes the message into parts that can be analyzed for actions
     *
     * @param message The message to be broken down
     * @return Array of strings representing tokens in the message
     */
    private String[] getMessageParts(String message) {
        return message.split("[|]");
     }

    /**
     * Examines the message parts and returns a suitable message processor to
     * process the message
     *
     * @param messageParts Tokens from the message to be analyzed
     * @return a MessageProcessor to process the message
     */
    private MessageProcessor switchSMS(String[] messageParts) {
        String keyword = messageParts.length > 0 ? messageParts[0] : "";
        switch(keyword.toLowerCase())
        {
            case "network": //network|<wifi | gsm >|<on | off | status>
                return new NetworkSMSHandler(from, messageParts);
            case "station":
                return new StationSMSHandler(from, messageParts);
            case "services": //services|<service_id>|<start | stop | status>
                return new ServicesSMSHandler(from, messageParts);
            case "resources":
                return new ResourcesSMSHandler(from, messageParts);
            case"sound":
                return new SoundSMSHandler(from, messageParts);
            case "whitelist":
                return new WhiteListSMSHandler(from, messageParts);
            case "ussd": //ussd|*123#
                return new USSDSMSHandler(this.from, messageParts);
            case "mark":
                return new MarkHandler(this.from, messageParts);
            default:
            return null;
        }

    }
}
