package org.rootio.services;

import org.rootio.tools.telephony.CallRecorder;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TelephonyService implements RootioService, ServiceInformationPublisher {

    private boolean isRunning;
    private final int serviceId = 1;
    private PhoneCallListener listener;
    private CallRecorder callRecorder;
    private boolean inCall;
    private String currentCallingNumber;

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Telephony Service");
        if (!isRunning) {
            this.waitForCalls();
            this.isRunning = true;
            this.sendEventBroadcast();
        }
        new ServiceState(1,"Telephony", 1).save();
    }

    @Override
    public void stop() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.STOP, "Telephony Service");
        try {
            this.shutDownService();
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[TelephonyService.stop]" : e.getMessage());
        }
        new ServiceState(1,"Telephony", 0).save();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            this.sendEventBroadcast();
        }
    }

    /**
     * Listens for Telephony activity coming into the phone
     */
    private void waitForCalls() {

    }

    /**
     * Answers an incoming call
     */
    private void pickCall(String frommNumber) {

    }

    /**
     * Declines an incoming call or ends an ongoing call.
     */
    private void declineCall(String fromNumber) {
    }

    /**
     * Processes a call noticed by the listener. Determines whether or not to
     * pick the phone call basing on the calling phone number * @param
     * incomingNumber
     */
    public void handleCall(final String fromNumber) {

    }


    /**
     * Class to handle telephony events received by the phone
     *
     * @author Jude Mukundane
     */
    class PhoneCallListener  {
        public void onCallStateChanged(int state, String incomingNumber) {

        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Sends out broadcasts informing listeners of change in service state
     */
    @Override
    public void sendEventBroadcast() {
       //
    }

    /**
     * Sends out broadcasts informing listeners of changes in status of the
     * Telephone
     *
     * @param isInCall Boolean indicating whether the Telephone is in a call or not.
     *                 True: in call, False: Not in call
     */
    private void sendTelephonyEventBroadcast(boolean isInCall) {
        //
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }
}
