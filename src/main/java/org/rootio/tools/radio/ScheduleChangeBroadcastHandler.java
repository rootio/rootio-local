package org.rootio.tools.radio;

import org.rootio.tools.ipc.BroadcastReceiver;
import org.rootio.tools.media.ScheduleChangeNotifiable;

/**
 * Created by Jude Mukundane on 7/5/2017.
 */

public class ScheduleChangeBroadcastHandler extends BroadcastReceiver {
    private final ScheduleChangeNotifiable notifiable;
    private Integer currentIndex = null; // prevent initial assignment to 0

    public ScheduleChangeBroadcastHandler(ScheduleChangeNotifiable notifiable) {
        this.notifiable = notifiable;
    }


    @Override
    public void onReceive(Object o) {
        this.notifiable.notifyScheduleChange((boolean) o);
    }
}
