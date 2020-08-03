package org.rootio.tools.radio;

import org.rootio.tools.media.ScheduleNotifiable;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduleBroadcastHandler  extends TimerTask {

    private final ScheduleNotifiable notifiable;
    private Integer scheduleIndex;


    public ScheduleBroadcastHandler(ScheduleNotifiable notifiable, int scheduleIndex) {
        this.notifiable = notifiable;
        this.scheduleIndex = scheduleIndex;
    }

    @Override
    public void run() {
        if (!this.notifiable.isExpired(scheduleIndex)) {
            Logger.getLogger("RootIO").log(Level.INFO, "Program with index "+ scheduleIndex + " not expired");
            this.notifiable.runProgram(scheduleIndex);
        } else {
            Logger.getLogger("RootIO").log(Level.INFO, "Program with index "+ scheduleIndex + " expired");
        }
    }
}
