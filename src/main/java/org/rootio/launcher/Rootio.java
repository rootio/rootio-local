package org.rootio.launcher;

import org.rootio.configuration.Configuration;
import org.rootio.services.*;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Rootio {
    private static DiagnosticsService diagnosticsService;
    private static SynchronizationService synchronizationService;
    private static MediaIndexingService mediaIndexingService;
    private static SIPService sipService;
    private static RadioService radioService;
    private static PhoneService phoneService;
    private static SMSService smsService;
    private static HashMap<Integer, Boolean> serviceState;
    private static HashMap<Integer, Thread> serviceThread;

    private static boolean isRunning = true;
    private static boolean inCall, inSIPCall;

    static
    {
        serviceState = new HashMap<>();
        serviceThread = new HashMap<>();
    }

    public static void main(String[] args) {
        try {
            prepareConfig(args[0]);
            runServices();
            registerShutDownHook();
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            if (!isRunning) {
                for(int i : serviceState.keySet())
                {
                    serviceState.put(i, false);
                }
                serviceThread.forEach((k, t)->t.interrupt());
            }
        }
    }

    public static void processServiceCommand(String event, int serviceId)
    {
        if(event.equals("stop") || event.equals("restart"))
        {
            serviceState.put(serviceId, false);
            serviceThread.get(serviceId).interrupt();
        }
        if(event.equals("start") || event.equals("restart"))
        {
            serviceState.put(serviceId, true);
            serviceThread.get(serviceId);
            RootioService service = null;
            switch (serviceId)
            {
                case 4:
                    service = new RadioService();
                    break;
                case 3:
                    service = new DiagnosticsService();
                    break;
                case 5:
                    service = new SynchronizationService();
                    break;
                case 7:
                    service = new MediaIndexingService();
                    break;
                case 2:
                    service = new SMSService();
                    break;
                case 1:
                    service = new PhoneService();
                    break;
                case 6:
                    service = new SIPService();
                    break;
            }
            if(service != null) {
                RootioService finalService = service;
                Thread tr = new Thread(() -> finalService.start());
                tr.start();
                serviceThread.put(serviceId, tr);
            }
        }
    }

    private static void prepareConfig(String url) throws FileNotFoundException {
        Configuration.load(url);
    }

    private static void runServices() {
        phoneService = new PhoneService();
        serviceState.put(phoneService.getServiceId(), true);
        Thread tr = new Thread(() -> phoneService.start());
        tr.start();
        serviceThread.put(phoneService.getServiceId(), tr);

        diagnosticsService = new DiagnosticsService();
        serviceState.put(diagnosticsService.getServiceId(), true);
        Thread tr1 = new Thread(() -> diagnosticsService.start());
        tr1.start();
        serviceThread.put(diagnosticsService.getServiceId(), tr1);

        synchronizationService = new SynchronizationService();
        serviceState.put(synchronizationService.getServiceId(), true);
        Thread tr2 = new Thread(() -> synchronizationService.start());
        tr2.start();
        serviceThread.put(synchronizationService.getServiceId(), tr2);

        mediaIndexingService = new MediaIndexingService();
        serviceState.put(mediaIndexingService.getServiceId(), true);
        Thread tr3 = new Thread(() -> mediaIndexingService.start());
        tr3.start();
        serviceThread.put(mediaIndexingService.getServiceId(), tr3);

        radioService = new RadioService();
        serviceState.put(radioService.getServiceId(), true);
        Thread tr4 = new Thread(() -> radioService.start());
        tr4.start();
        serviceThread.put(radioService.getServiceId(), tr4);

        sipService = new SIPService();
        serviceState.put(sipService.getServiceId(), true);
        Thread tr5 = new Thread(() -> sipService.start());
        tr5.start();
        serviceThread.put(sipService.getServiceId(), tr5);

        smsService = new SMSService();
        serviceState.put(smsService.getServiceId(), true);
        Thread tr6 = new Thread(() -> smsService.start());
        tr6.start();
        serviceThread.put(smsService.getServiceId(), tr6);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private static void registerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(Rootio::shutdown));
    }

    private static void shutdown() {
        System.out.println("Shutdown interrupt received");
        //kill all the other threads
        isRunning = false;
        synchronized(Rootio.class) {
            Rootio.class.notifyAll();
        }
    }

    public static void setInCall(boolean isInCall) {
        inCall = isInCall;
    }

    public static void setInSIPCall(boolean isInSIPCall) {
        inSIPCall = isInSIPCall;
    }

    public static boolean isInCall() {
        return inCall;
    }

    public static boolean isInSIPCall() {
        return inSIPCall;
    }

    public static boolean getServiceStatus(int parseInt) {
        return serviceState.get(parseInt);
    }
}
