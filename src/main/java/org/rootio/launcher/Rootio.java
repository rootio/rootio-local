package org.rootio.launcher;

import org.rootio.configuration.Configuration;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.SynchronizationService;

import java.io.FileNotFoundException;

public class Rootio {
    private static DiagnosticsService diagnosticsService;
    private static SynchronizationService synchronizationService;
    private static Thread diagnosticsThread, synchronizationThread;
    private static boolean isRunning = true;

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
