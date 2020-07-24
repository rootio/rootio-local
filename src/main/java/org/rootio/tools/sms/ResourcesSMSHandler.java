package org.rootio.tools.sms;

import org.rootio.tools.diagnostics.DiagnosticAgent;

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
    public boolean ProcessMessage() {
        this.diagnosticsAgent.runDiagnostics();
        return false;

    }

    /**
     * Gets the battery level of the phone
     *
     * @return String with Battery level information of the phone
     */
    private boolean getBatteryLevel() {
        try {
            String response = String.format("Battery Level: %f", this.diagnosticsAgent.getBatteryLevel());
            this.respondAsyncStatusRequest(response, from);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Gets the storage level of the phone
     *
     * @return String with storage information of the phone
     */
    private boolean getStorageLevel() {
        return false;
    }

    /**
     * Gets the memory usage of the phone
     *
     * @return String with memory utilization information of the phone
     */
    private boolean getMemoryUsage() {
        try {

            String response = String.format("Memory Utilization: %f", this.diagnosticsAgent.getMemoryStatus());
            this.respondAsyncStatusRequest(response, from);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Gets the CPU utilization of the phone
     *
     * @return String with CPU utilization information of the phone
     */
    private boolean getCPUusage() {
        try {

            String response = String.format("CPU Usage: %f", this.diagnosticsAgent.getCPUUtilization());
            this.respondAsyncStatusRequest(response, from);
            return true;

        } catch (Exception ex) {
            return false;
        }

    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send response SMS
    }

}
