package org.rootio.tools.sms;

import java.util.logging.Level;
import java.util.logging.Logger;

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

        // stopping a service
        if (messageParts[1].equals("stop")) {
            try {
                this.stopService(Integer.parseInt(messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ServicesSMSHandler.processMessage]" : e.getMessage());
            }
        }

        // starting a srevice
        if (messageParts[1].equals("start")) {
            try {
                this.startService(Integer.parseInt(messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ServicesSMSHandler.processMessage]" : e.getMessage());
            }
        }

        //restarting a service
        if (messageParts[1].equals("restart")) {
            try {
                this.restartService(Integer.parseInt(messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ServicesSMSHandler.processMessage]" : e.getMessage());
            }
        }

        // getting the service status
        if (messageParts[1].equals("status")) {
            try {
                this.getServiceStatus(Integer.parseInt(messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ServicesSMSHandler.processMessage]" : e.getMessage());
            }
        }
    }

    private boolean restartService(int i) {
        this.stopService(i);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.startService(i);
        return true;
    }

    /**
     * Starts the service whose ID is specified
     *
     * @param serviceId The ID of the service to start
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean startService(int serviceId) {
        if (serviceId == 0)//all services
        {
            this.respondAsyncStatusRequest("start all ok", from);
        } else {
            this.respondAsyncStatusRequest("start ok", from);
        }
        return true;
    }

    /**
     * Stops the Service whose ID is specified
     *
     * @param serviceId The ID of the service to be stopped
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean stopService(int serviceId) {
        if (serviceId == 0) {
            this.respondAsyncStatusRequest("stop all ok", from);
        } else {
            this.respondAsyncStatusRequest(from, "stop " + serviceId + " ok");
        }
        return true;
    }

    /**
     * Gets the status of the service whose ID is specified
     *
     * @param serviceId The ID of the service whose status to return
     * @return Boolean indicating whether or not the service is running. True:
     * Running, False: Not running
     */
    private boolean getServiceStatus(int serviceId) {
        this.bindToService(serviceId);
        return true;
    }

    /**
     * Gets the Intent to be used to communicate with the intended service
     *
     * @param serviceId The ID of the service with which to communicate
     * @return The intent to be used in communicating with the desired service
     */
    private Object getServiceIntent(int serviceId) {
        return null;
    }

    /**
     * Binds to the program service to get status of programs that are displayed
     * on the home radio screen
     */
    private void bindToService(int serviceId) {

    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {

    }


    private void notifyServiceStatus(int serviceId, boolean running) {
        this.respondAsyncStatusRequest(this.from, running ? String.format("Service %s running", serviceId) : String.format("Service %s not running", serviceId));
    }
}
