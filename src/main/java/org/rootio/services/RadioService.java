package org.rootio.services;

import org.rootio.launcher.Rootio;
import org.rootio.tools.media.Program;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RadioService implements RootioService, ServiceInformationPublisher {

    private final int serviceId = 4;
    private boolean isRunning;
    private Thread runnerThread;
    private RadioRunner radioRunner;
    private Timer timer;

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Radio Service");
        this.timer = new Timer();
        runnerThread = new Thread(() -> runTodaySchedule());
        runnerThread.start();
        this.isRunning = true;
        this.sendEventBroadcast();
        new ServiceState(4, "Radio", 1).save();
        while (Rootio.isRunning()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                if (!Rootio.isRunning()) {
                    runnerThread.interrupt();
                }
            }
        }
    }

    private void runTodaySchedule() {
        radioRunner = RadioRunner.getInstance();
        this.scheduleNextDayAlarm();
        radioRunner.run();

    }


    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "Radio Service");
        try {
            this.shutDownService();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[RadioService.stop]" : e.getMessage());
        }
        new ServiceState(4, "Radio", 0).save();
    }

    private void shutDownService() {
        if (radioRunner != null && this.isRunning) {
            radioRunner.stop();
            this.isRunning = false;
            try {
                this.unSchedule();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[RadioService.shutDownService]" : e.getMessage());
            }
            this.sendEventBroadcast();
        }
    }

    private void unSchedule() {
        timer.cancel();
    }

    /**
     * Sends out broadcasts informing listeners of change in the status of the
     * service
     */
    @Override
    public void sendEventBroadcast() {
        //
    }

    /**
     * Gets the program slots that are defined for the current schedule
     *
     * @return An ArrayList of ProgramSlot objects each representing a slot on
     * the schedule of the radio
     */
    public ArrayList<Program> getPrograms() {
        return radioRunner == null ? new ArrayList<>() : radioRunner.getPrograms();
    }

    private void scheduleNextDayAlarm() {
        Date dt = this.getTomorrowBaseDate();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RadioService.this.runTodaySchedule();
            }
        }, dt);
    }

    private Date getTomorrowBaseDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private void setInCall(boolean inCall) {

    }

    private void setInSIPCall(boolean inSIPCall) {

    }

    private boolean getInCall() {
        return false;
    }

    private boolean getInSIPCall() {
        return false;
    }

    public RadioService() {

    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

}
