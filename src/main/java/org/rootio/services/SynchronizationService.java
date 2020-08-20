package org.rootio.services;

import org.rootio.launcher.Rootio;
import org.rootio.messaging.MessageRouter;
import org.rootio.services.synchronization.SynchronizationDaemon;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchronizationService implements RootioService {

    private Thread synchronizationThread;
    private final int serviceId = 5;
    private boolean isRunning;


    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Synchronization Service");
        if (!this.isRunning) {
            SynchronizationDaemon synchronizationDaemon = SynchronizationDaemon.getInstance();
            synchronizationThread = new Thread(synchronizationDaemon);
            this.isRunning = true;
            synchronizationThread.start();
        }
        new ServiceState(5, "Synchronization", 1).save();
        while (Rootio.isRunning()) {
            try {
                synchronizationThread.join();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    synchronizationThread.interrupt();
                }
            }
        }
    }

    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "Synchronization Service");
        try {
            this.shutDownService();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[SynchronizationService.stop]" : e.getMessage());
        }
        new ServiceState(5, "Synchronization", 0).save();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }
}
