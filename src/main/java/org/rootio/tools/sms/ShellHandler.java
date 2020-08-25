package org.rootio.tools.sms;

import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.diagnostics.DiagnosticAgent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ShellHandler implements MessageProcessor {

    private String[] messageParts;
    private String from;
    private DiagnosticAgent diagnosticsAgent;

    public ShellHandler(String from, String[] messageParts) {
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
        try {
            this.respondAsyncStatusRequest(from, executeCommand(messageParts[1]));
        } catch (IOException e) {
            this.respondAsyncStatusRequest(from, "An error occurred");
        }
    }


    private String executeCommand(String command) throws IOException {
        StringBuilder response = new StringBuilder();
        Process proc = Runtime.getRuntime().exec(command);
        try(InputStreamReader rdr = new InputStreamReader(proc.getInputStream()))
        {
            while(true)
            {
                int c = rdr.read();
                if(c < 0)
                {
                    break;
                }
                response.append((char)c);
            }
        }
        return response.toString();
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
