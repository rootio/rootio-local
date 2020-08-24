package org.rootio.tools.sms;

import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.diagnostics.DiagnosticAgent;
import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ResourcesSMSHandler implements MessageProcessor {

    private String[] messageParts;
    private String from;
    private DiagnosticAgent diagnosticsAgent;

    public ResourcesSMSHandler(String from, String[] messageParts) {
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
        this.respondAsyncStatusRequest(from, getResourceStatus());
    }


    private String getResourceStatus() {

        String query = "select battery_level, memory_utilization, cpu_utilization, storage_utilization from diagnostic order by id desc limit 1";
        List<String> args = Collections.emptyList();
        List<List<Object>> data = null;
        try {
            data = DBAgent.getData(query, args);
            if (data.size() > 0) {
                return String.format("Battery: %.2f%%, Mem: %.2f%%, CPU: %.2f%%, Storage: %.2f%%", data.get(0).get(0), data.get(0).get(1), data.get(0).get(2), data.get(0).get(3));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "No resource information found";
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
