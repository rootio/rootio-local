package org.rootio.launcher;

import org.rootio.configuration.Configuration;
import org.rootio.services.*;

import java.io.FileNotFoundException;

public class Rootio {
    private static DiagnosticsService diagnosticsService;
    private static SynchronizationService synchronizationService;
    private static MediaIndexingService mediaIndexingService;
    private static SIPService sipService;
    private static RadioService radioService;
    private static Thread diagnosticsThread, synchronizationThread, mediaIndexingThread, radioServiceThread, sipServiceThread;
    private static boolean isRunning = true;
    private static boolean inCall, inSIPCall;

    public static void main(String[] args) {
        try {
            prepareConfig(args[0]);
            runServices();
            registerShutDownHook();
            Thread.currentThread().wait();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            if (!isRunning) {
                diagnosticsThread.interrupt();
                synchronizationThread.interrupt();
                mediaIndexingThread.interrupt();
                radioServiceThread.interrupt();
                sipServiceThread.interrupt();
            }
        }
    }

    private static void prepareConfig(String url) throws FileNotFoundException {
        Configuration.load(url);
    }

    private static void runServices() {
        diagnosticsService = new DiagnosticsService();
        diagnosticsThread = new Thread(() -> diagnosticsService.start());
        diagnosticsThread.start();

        synchronizationService = new SynchronizationService();
        synchronizationThread = new Thread(() -> synchronizationService.start());
        synchronizationThread.start();

        mediaIndexingService = new MediaIndexingService();
        mediaIndexingThread = new Thread(() -> mediaIndexingService.start());
        mediaIndexingThread.start();

        radioService = new RadioService();
        radioServiceThread = new Thread(() -> radioService.start());
        radioServiceThread.start();

        sipService = new SIPService();
        sipServiceThread = new Thread(() -> sipService.start());
        sipServiceThread.start();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private static void registerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(Rootio::run));
    }

    private static void run() {
        System.out.println("Shutdown interrupt received");
        //kill all the other threads
        isRunning = false;
        Thread.currentThread().notifyAll();
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
}
