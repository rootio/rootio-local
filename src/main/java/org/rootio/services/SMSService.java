package org.rootio.services;

import org.rootio.launcher.Rootio;
import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.sms.SMSSwitch;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.yield;

public class SMSService implements RootioService, ServiceInformationPublisher {

    private boolean isRunning;
    private final int serviceId = 2;
    private Thread runnerThread, messageThread;
    private BroadcastReceiver smsReceiver;
    private ArrayBlockingQueue<Message> messageQueue;


    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "SMS Service");
        messageQueue = new ArrayBlockingQueue<Message>(50);
        listenForIncomingSMS();
        runnerThread = new Thread(() -> {
            try {
                readIncomingSMS();
                yield();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PhoneService.start]" : e.getMessage());
            }
        });
        runnerThread.start();
        messageThread = new Thread(() -> {
            processMessages();
            yield();
        });
        messageThread.start();

        new ServiceState(serviceId, "SMS", 1).save();
        while (Rootio.getServiceStatus(serviceId)) {
            try {
                runnerThread.join();
            } catch (InterruptedException e) {
                if (!Rootio.getServiceStatus(serviceId)) {
                    runnerThread.interrupt();
                    messageThread.interrupt();
                }
            }
        }
    }


    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "SMS Service");
        try {
            this.shutDownService();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[SMSService.stop]" : e.getMessage());
        }
        new ServiceState(2, "SMS", 0).save();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            //do shutdown stuff here
            this.sendEventBroadcast();
        }
    }

    private void readIncomingSMS() {
        while (Rootio.isRunning()) {
            if(messageQueue.isEmpty()) { //do not read if messages not yet processed, might result in a double read
                Message m = new Message("read", "sms", new HashMap<>());
                MessageRouter.getInstance().specicast(m, "org.rootio.phone.MODEM");
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void listenForIncomingSMS() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Message message) {
                messageQueue.add(message);
            }
        };
        MessageRouter.getInstance().register(smsReceiver, "org.rootio.telephony.SMS");
    }

    private void processMessages() {
        Message message = null;
        try {
            while (Rootio.isRunning()) {
                message = messageQueue.take();
                switch (message.getEvent()) {
                    case "header":
                        Utils.logEvent(EventCategory.SMS, EventAction.RECEIVE, (String) message.getPayLoad().get("from"));
                        SMSSwitch.switchSMS(message);
                        break;
                    case "body":
                        Utils.logEvent(EventCategory.SMS, EventAction.RECEIVE, (String) message.getPayLoad().get("body"));
                        SMSSwitch.switchSMS(message);
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
