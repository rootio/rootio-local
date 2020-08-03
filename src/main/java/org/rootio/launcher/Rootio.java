package org.rootio.launcher;

import org.rootio.configuration.Configuration;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.MediaIndexingService;
import org.rootio.services.RadioService;
import org.rootio.services.SynchronizationService;

import java.io.FileNotFoundException;

public class Rootio {
    private static DiagnosticsService diagnosticsService;
    private static SynchronizationService synchronizationService;
    private static MediaIndexingService mediaIndexingService;
    private static RadioService radioService;
    private static Thread diagnosticsThread, synchronizationThread, mediaIndexingThread, radioServiceThread;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        try {
            prepareConfig("C:\\rootio\\rootio.conf");
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
            }
        }
    }

    private static void prepareConfig(String url) throws FileNotFoundException {
        Configuration.load(url);
    }

    private static void runServices() {
//        diagnosticsService = new DiagnosticsService();
//        diagnosticsThread = new Thread(() -> diagnosticsService.start());
//        diagnosticsThread.start();
//
//        synchronizationService = new SynchronizationService();
//        synchronizationThread = new Thread(() -> synchronizationService.start());
//        synchronizationThread.start();
//
//        mediaIndexingService = new MediaIndexingService();
//        mediaIndexingThread = new Thread(() -> mediaIndexingService.start());
//        mediaIndexingThread.start();

        radioService= new RadioService();
        radioServiceThread = new Thread(() -> radioService.start());
        radioServiceThread.start();
 }

    public static boolean isRunning()
    {
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
}
