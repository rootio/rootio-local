package org.rootio.services;

public interface RootioService {
    void start();

    void stop();

    boolean isRunning();

    int getServiceId();
}
