package org.rootio.services;

import org.rootio.tools.ipc.BroadcastReceiver;

/**
 * This class is a listener for incoming SMS
 *
 * @author HP Envy
 */
public class IncomingSMSReceiver extends BroadcastReceiver {

    private IncomingSMSNotifiable incomingSMSNotifiable;

    IncomingSMSReceiver(IncomingSMSNotifiable incomingSMSNotifiable) {
        this.incomingSMSNotifiable = incomingSMSNotifiable;
    }

    @Override
    public void onReceive(Object o) {

    }

}
