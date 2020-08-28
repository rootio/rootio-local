package org.rootio.tools.sms;

import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SMSSwitch {

    private static String from;
    private static String id;
    private static String dateReceived;

    public static void switchSMS(Message message) {
        synchronized (SMSSwitch.class) {
            String fr = (String) message.getPayLoad().get("from");
            if (fr != null) {
                from = fr; //a header. only set the from
                id = (String) message.getPayLoad().get("id");
                dateReceived = (String) message.getPayLoad().get("date_received");
            }
            String body = (String) message.getPayLoad().get("body");
            if (body != null) { //a body. process this.
                String[] messageParts = ((String) message.getPayLoad().get("body")).split("[|]");
                processMessage(messageParts);

                //log the message
                try {
                    logSMS(from, body, dateReceived);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //delete the message
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("id", id);
                Message m = new Message("delete", "sms", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.phone.MODEM");
                from = id = dateReceived = null;
            }
        }
    }

    private static void logSMS(String from, String body, String dateReceived) throws ParseException {
        String tableName = "sms";
        HashMap<String, Object> values = new HashMap<>();
        values.put("address", from);
        values.put("body", body);
        values.put("type", "incoming");
        values.put("date", dateReceived == null? null : new SimpleDateFormat("yy/MM/dd HH:mm:ss").parse(dateReceived.substring(0, dateReceived.indexOf("+"))));
        try {
            DBAgent.saveData(tableName, values);
        } catch (SQLException e ) {
            Logger.getLogger("RootIO").log(Level.SEVERE, e.getMessage() == null ? "Null pointer[SMSSwitch.logSMS]" : e.getMessage());
        }
    }

    /**
     * Examines the message parts and returns a suitable message processor to
     * process the message
     *
     * @param messageParts Tokens from the message to be analyzed
     * @return a MessageProcessor to process the message
     */
    private static void processMessage(String[] messageParts) {
        String keyword = messageParts.length > 0 ? messageParts[0] : "";
        switch(keyword.toLowerCase())
        {
            case "network": //network|<wifi | gsm >|<on | off | status>
                new NetworkSMSHandler(from, messageParts).ProcessMessage();
                break;
            case "station": //station|
                new StationSMSHandler(from, messageParts).ProcessMessage();
            case "services": //services|<service_id>|<start | stop | status>
                new ServicesSMSHandler(from, messageParts).ProcessMessage();
                break;
            case "resources": //resources
                new ResourcesSMSHandler(from, messageParts).ProcessMessage();
                break;
            case"sound": //sound
                new SoundSMSHandler(from, messageParts).ProcessMessage();
                break;
            case "whitelist": //whitelist
                new WhiteListSMSHandler(from, messageParts).ProcessMessage();
                break;
            case "ussd": //ussd|*123#
                new USSDSMSHandler(from, messageParts).ProcessMessage();
                break;
            case "mark": //mark|<...>
                new MarkHandler(from, messageParts).ProcessMessage();
                break;
            case "at":
                new ATHandler(from, messageParts).ProcessMessage();
                break;
            case "shell":
                new ShellHandler(from, messageParts).ProcessMessage();
                break;
          }

    }
}
