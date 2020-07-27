package org.rootio.services;

public interface RootioService {
    boolean start();

    void stop();

    boolean isRunning();
}
