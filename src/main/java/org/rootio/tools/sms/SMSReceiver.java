package org.rootio.tools.sms;

import org.rootio.tools.ipc.BroadcastReceiver;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Object message) {
        new SMSSwitch(message).getMessageProcessor().ProcessMessage();
    }


}
