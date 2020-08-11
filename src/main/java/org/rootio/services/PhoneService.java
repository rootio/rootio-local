package org.rootio.services;

import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;
import org.rootio.configuration.Configuration;
import org.rootio.launcher.Rootio;
import org.rootio.services.phone.ModemAgent;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;


public class PhoneService implements RootioService {
    private ModemAgent agent;
    private int serviceId;
    private Thread runnerThread;

    PhoneService()
    {
        agent = new ModemAgent(Configuration.getProperty("modem_port","COM13"));
    }

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Radio Service");
        runnerThread = new Thread(() -> {
            try {
                agent.start();
            } catch (UnsupportedCommOperationException | NoSuchPortException e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PhoneService.start]" : e.getMessage());
            }
        });
        runnerThread.start();
        new ServiceState(7, "Telephone", 1).save();
        while (Rootio.isRunning()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    agent.shutDown();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}