package org.rootio.services;

import org.rootio.tools.ipc.BroadcastReceiver;

public class ServiceStopReceiver extends BroadcastReceiver {

    ServiceStopNotifiable connectedActivity;

    public ServiceStopReceiver(ServiceStopNotifiable connectedActivity) {
        this.connectedActivity = connectedActivity;
    }

    @Override
    public void onReceive(Object o) {

    }

}
