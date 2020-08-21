package org.rootio.tools.sms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class USSDSMSHandler implements MessageProcessor, UssdResultNotifiable {

    private String[] messageParts;
    private String from;

    USSDSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        //uncomment below if you are doing one-hit USSD requests and do not care for multiple requests in single session
        //if ((this.messageParts[1].startsWith("*") || this.messageParts[1].startsWith("#")) && this.messageParts[1].endsWith("#")) {
        //Utils.toastOnScreen("received " + messageParts[1]);
        this.doUSSDRequest(messageParts[1]);
        //}
        // return false;
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send response SMS
    }

    private void doUSSDRequest(String USSDString) {
        //do a USSD request
    }

    @Override
    public void notifyUssdResult(String request, String response, int resultCode) {
        this.respondAsyncStatusRequest(this.from, "%s|%s".format(response, resultCode));
    }

    class USSDSessionHandler {

        private UssdResultNotifiable client;
        private Method handleUssdRequest;
        private Object iTelephony;

        USSDSessionHandler(UssdResultNotifiable client) {
            this.client = client;
        }

        private void getUssdRequestMethod() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            //
        }

        public void doSession(String ussdRequest) {
            //do USSD session
        }
    }
}
