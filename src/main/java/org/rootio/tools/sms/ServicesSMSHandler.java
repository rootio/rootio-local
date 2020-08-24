package org.rootio.tools.sms;

import org.rootio.launcher.Rootio;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;

import java.util.HashMap;

public class ServicesSMSHandler implements MessageProcessor{

    private final String from;
    private final String[] messageParts;

    ServicesSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        if (messageParts.length != 3) {
            return;
        }

        try {
            if (messageParts[2].equals("status")) {
                notifyServiceStatus(Integer.parseInt(messageParts[1]), Rootio.getServiceStatus(Integer.parseInt(messageParts[1])));
            } else {
                Rootio.processServiceCommand(messageParts[2], Integer.parseInt(messageParts[1]));
            }
        }
        catch (NumberFormatException ex)
        {
            respondAsyncStatusRequest(from, "Malformed command message");
        }
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send response SMS
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("to", from);
        payload.put("message", data);
        Message message = new Message("send", "sms", payload);
        MessageRouter.getInstance().specicast(message, "org.rootio.phone.MODEM");
    }


    private void notifyServiceStatus(int serviceId, boolean running) {
        this.respondAsyncStatusRequest(this.from, running ? String.format("Service %s running", serviceId) : String.format("Service %s not running", serviceId));
    }
}
