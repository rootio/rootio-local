package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchronizationDaemon implements Runnable {
    private MusicListHandler musicListHandler;
    private HashMap<Integer, Long> syncLocks = new HashMap<>(); //replace this with locks
    private SynchronizationDaemon syncDaemon;

    @Override
    public void run() {
        this.musicListHandler = new MusicListHandler();
        this.synchronize();
    }

    private Long getCurrentTime() {
        return LocalDateTime.now().getLong(ChronoField.MILLI_OF_SECOND);
    }

    private void synchronize() {
        synchronized (this) {
            if (!SynchronizationDaemon.this.syncLocks.containsKey(1))
                SynchronizationDaemon.this.synchronize(new DiagnosticsHandler(), 1);

            //if the syncLock is greater than 5 mins old, do not consider it.
            if (!(SynchronizationDaemon.this.syncLocks.containsKey(2) && SynchronizationDaemon.this.syncLocks.get(2) < (getCurrentTime() + 300000))) {
                SynchronizationDaemon.this.syncLocks.put(2, getCurrentTime());
                SynchronizationDaemon.this.synchronize(new ProgramsHandler(), 2);
            }
            if (!SynchronizationDaemon.this.syncLocks.containsKey(3))
                SynchronizationDaemon.this.synchronize(new CallLogHandler(), 3);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(4))
                SynchronizationDaemon.this.synchronize(new SMSLogHandler(), 4);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(5))
                SynchronizationDaemon.this.synchronize(new WhitelistHandler(), 5);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(6))
                SynchronizationDaemon.this.synchronize(new FrequencyHandler(), 6);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(7))
                SynchronizationDaemon.this.synchronize(SynchronizationDaemon.this.musicListHandler, 7);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(8))
                SynchronizationDaemon.this.synchronize(new PlaylistHandler(), 8);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(9))
                SynchronizationDaemon.this.synchronize(new LogHandler(), 9);
            if (!SynchronizationDaemon.this.syncLocks.containsKey(10))
                SynchronizationDaemon.this.synchronize(new StationHandler(), 10);
        }
    }

    public void requestSync(final int category) {
        this.syncLocks.put(category, getCurrentTime()); //prevent automated sync while this is running
        final SynchronizationHandler syncHandler;
        switch (category) {
            case 1:
                syncHandler = new DiagnosticsHandler();
                break;
            case 2:
                syncHandler = new ProgramsHandler();
                break;
            case 3:
                syncHandler = new CallLogHandler();
                break;
            case 4:
                syncHandler = new SMSLogHandler();
                break;
            case 5:
                syncHandler = new WhitelistHandler();
                break;
            case 6:
                syncHandler = new FrequencyHandler();
                break;
            case 7:
                syncHandler = new StationHandler();
                break;
            case 8:
                syncHandler = new MusicListHandler();
                break;
            case 9:
                syncHandler = new PlaylistHandler();
                break;
            default:
                syncHandler = null;
                break;
        }

        Thread thread = new Thread(() -> SynchronizationDaemon.this.synchronize(syncHandler, category));
        thread.start();

    }

    private SynchronizationDaemon()
    {
    }

    public SynchronizationDaemon getInstance() {
        if(this.syncDaemon == null)
        {
            this.syncDaemon = new SynchronizationDaemon();
        }
        return this.syncDaemon;
    }

    /**
     * Causes the thread on which it is called to sleep for atleast the specified number of milliseconds
     *
     * @param milliseconds The number of milliseconds for which the thread is supposed to sleep.
     */
    private void getSomeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);// frequency is in seconds
        } catch (InterruptedException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[LogHandler.getSomeSleep]" : e.getMessage());
        }

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

    public void synchronize(final SynchronizationHandler handler, Integer id) {
        try {
            String synchronizationUrl = handler.getSynchronizationURL();
            HashMap<String, Object> response = Utils.doDetailedPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
            JSONObject responseJSON;
            assert response != null;
            responseJSON = new JSONObject((String) response.get("response"));
            handler.processJSONResponse(responseJSON);
            Utils.logEvent(EventCategory.SYNC, EventAction.START, String.format("length: %s, response code: %s, duration: %s, url: %s", response.get("length"), response.get("responseCode"), response.get("duration"), response.get("url")));
        } catch (Exception e) {
            this.syncLocks.remove(id);
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[SynchronizationDaemon.synchronize]" : e.getMessage());
        }
    }

}
