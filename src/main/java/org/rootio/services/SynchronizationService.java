package org.rootio.services;

import org.rootio.services.synchronization.SynchronizationDaemon;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchronizationService implements RootioService, ServiceInformationPublisher {

    private final int serviceId = 5;
    private boolean isRunning;

    @Override
    public boolean start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Synchronization Service");
        if (!this.isRunning) {
            SynchronizationDaemon synchronizationDaemon = SynchronizationDaemon.getInstance();
            Thread thread = new Thread(synchronizationDaemon);
            this.isRunning = true;
            thread.start();
            this.sendEventBroadcast();
        }
        new ServiceState(5,"Synchronization", 1).save();
        return true;
    }

    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "Synchronization Service");
        try {
            this.shutDownService();
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[SynchronizationService.stop]" : e.getMessage());
        }
        new ServiceState(5,"Synchronization", 0).save();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            this.sendEventBroadcast();
        }
    }

    /**
     * Sends out broadcasts informing listeners of changes in service status
     */
    public void sendEventBroadcast() {
        //send event broadcast
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

}
