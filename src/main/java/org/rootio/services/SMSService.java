package org.rootio.services;

import org.rootio.messaging.Message;
import org.rootio.tools.sms.MessageProcessor;
import org.rootio.tools.sms.SMSSwitch;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SMSService  implements RootioService, IncomingSMSNotifiable, ServiceInformationPublisher {

    private boolean isRunning;
    private final int serviceId = 2;
    private IncomingSMSReceiver incomingSMSReceiver;

    public SMSService() {
        this.incomingSMSReceiver = new IncomingSMSReceiver(this);
    }

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "SMS Service");
        this.isRunning = true;
        this.sendEventBroadcast();
        new ServiceState(2,"SMS", 1).save();
    }

    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "SMS Service");
        try {
            this.shutDownService();
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[SMSService.stop]" : e.getMessage());
        }
        new ServiceState(2,"SMS", 0).save();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            //do shutdown stuff here
            this.sendEventBroadcast();
        }
    }

    @Override
    public void notifyIncomingSMS(Message message) {
        Utils.logEvent(EventCategory.SMS, EventAction.RECEIVE, message.getOriginatingAddress()+ ">>" +message.getMessageBody());
        SMSSwitch smsSwitch = new SMSSwitch(message);
        MessageProcessor messageProcessor = smsSwitch.getMessageProcessor();
        if (messageProcessor != null) {
            messageProcessor.ProcessMessage();
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Sends out broadcasts to listeners informing them of service status
     * changes
     */
    @Override
    public void sendEventBroadcast() {
        //
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

}
