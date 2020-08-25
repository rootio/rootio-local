package org.rootio.tools.sms;

import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.diagnostics.DiagnosticAgent;

import java.util.HashMap;

public class ATHandler implements MessageProcessor {

    private String[] messageParts;
    private String from;
    private DiagnosticAgent diagnosticsAgent;

    public ATHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
        this.diagnosticsAgent = new DiagnosticAgent();
    }

    @Override
    public void ProcessMessage() {
        this.diagnosticsAgent.runDiagnostics();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       this.executeATCommand(messageParts[1]);
    }


    private void executeATCommand(String ATCommand) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("command", ATCommand);
        Message m = new Message("command", "at", payload);
        MessageRouter.getInstance().specicast(m, "org.rootio.phone.MODEM");
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

}
