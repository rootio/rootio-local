package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.launcher.Rootio;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchronizationDaemon implements Runnable {
    private MusicListHandler musicListHandler;
    private static SynchronizationDaemon syncDaemon;

    @Override
    public void run() {
        while (Rootio.isRunning()) {
        this.musicListHandler = new MusicListHandler();
        this.synchronize();
            try {
                Thread.sleep(getFrequency() * 1000);
            } catch (InterruptedException e) {
                Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[DiagnosticsRunner.run]" : e.getMessage());
            }
        }
    }

    private void synchronize() {
        synchronized (this) {
                SynchronizationDaemon.this.synchronize(new DiagnosticsHandler());
                SynchronizationDaemon.this.synchronize(new ProgramsHandler());
                SynchronizationDaemon.this.synchronize(new CallLogHandler());
                SynchronizationDaemon.this.synchronize(new SMSLogHandler());
                SynchronizationDaemon.this.synchronize(new WhitelistHandler());
                SynchronizationDaemon.this.synchronize(new FrequencyHandler());
                SynchronizationDaemon.this.synchronize(musicListHandler);
                SynchronizationDaemon.this.synchronize(new PlaylistHandler());
                SynchronizationDaemon.this.synchronize(new LogHandler());
                SynchronizationDaemon.this.synchronize(new StationHandler());
        }
    }

    private SynchronizationDaemon()
    {
    }

    public static SynchronizationDaemon getInstance() {
        if(syncDaemon == null)
        {
            syncDaemon = new SynchronizationDaemon();
        }
        return syncDaemon;
    }


    /**
     * Fetches the number of seconds representing the interval at which to issue
     * synchronization requests
     *
     * @return Number of seconds representing synchronization interval
     */
    private int getFrequency() {
        try {
            return Integer.parseInt(Configuration.getProperty("synchronization_interval"));
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SynchronizationDaemon]" : e.getMessage());
            return 60; // default to 1 minute
        }
    }

    public void synchronize(final SynchronizationHandler handler) {
        try {
            String synchronizationUrl = handler.getSynchronizationURL();
            if(synchronizationUrl != null) {
                HashMap<String, Object> response = Utils.doDetailedPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
                JSONObject responseJSON;
                assert response != null;
                responseJSON = new JSONObject((String) response.get("response"));
                handler.processJSONResponse(responseJSON);
                Utils.logEvent(EventCategory.SYNC, EventAction.START, String.format("length: %s, response code: %s, duration: %s, url: %s", response.get("length"), response.get("responseCode"), response.get("duration"), response.get("url")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SynchronizationDaemon.synchronize]" : e.getMessage());
        }
    }

}
