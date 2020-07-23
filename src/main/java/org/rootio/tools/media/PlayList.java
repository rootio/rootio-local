package org.rootio.tools.media;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for the definition of Playlists
 *
 * @author Jude Mukundane git
 */
public class PlayList {

    private static PlayList playListInstance;
    private ArrayList<String> playlists, streams;
    private HashSet<Media> mediaList;
    private Iterator<Media> mediaIterator;
    private Iterator<String> streamIterator;
    private MediaPlayer mediaPlayer, callSignPlayer;
    private CallSignProvider callSignProvider;
    private String currentMediaUri;
    private Media currentMedia, currentCallSign;
    private Duration mediaPosition;
    private MediaLibrary mediaLib;
    private boolean isShuttingDown;
    private Thread runnerThread;
    private int maxVolume;
    private boolean foundMedia;


    private PlayList() {
        // do not instantiate
    }

    public static PlayList getInstance() {
        if (PlayList.playListInstance != null) {
            PlayList.playListInstance.stop();
        }
        PlayList.playListInstance = new PlayList();
        return playListInstance;
    }

    public void init(ArrayList<String> playlists, ArrayList<String> streams, ProgramActionType programActionType) {
        this.isShuttingDown = false;
        this.playlists = playlists;
        this.streams = streams;
        this.mediaLib = new MediaLibrary();
        this.callSignProvider = new CallSignProvider();
    }

    /**
     * Load media for this playlist from the database
     */
    public void load(boolean hard) {
        if (hard || (this.mediaList == null || mediaList.size() == 0)) {
            mediaList = loadMedia(this.playlists);
        }
        mediaIterator = mediaList.iterator();
        streamIterator = streams.iterator();
    }

    public void preload() {
        if (this.mediaList == null || mediaList.size() == 0) {
            mediaList = loadMedia(this.playlists);
        }
        mediaIterator = mediaList.iterator();
        streamIterator = streams.iterator();
    }

    /**
     * Play the media loaded in this playlist
     */
    public void play() {
        this.maxVolume = this.getMaxVolume();
        new Thread(() -> startPlayer()).start();
        //  startPlayer();
        this.callSignProvider.start();

    }

    private void startPlayer() {
        while (!foundMedia && !this.isShuttingDown) {
            try {
                if (streamIterator.hasNext()) {
                    String stream = this.streamIterator.next();
                    currentMedia = new Media("", stream, 0, null);
                    try {
                        playMedia(currentMedia.getFileLocation());
                        this.foundMedia = true;

                        new Thread(() -> Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/media_play", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version")), "")).start();
                    } catch (NullPointerException e) {
                        Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.startPLayer]" : e.getMessage());
                        this.startPlayer();
                    }

                } else if (mediaIterator.hasNext()) {

                    currentMedia = mediaIterator.next();

                    try {
                        playMedia(currentMedia.getFileLocation());
                        this.foundMedia = true;
                        new Thread(() -> Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/media_play", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version")), "")).start();

                    } catch (NullPointerException ex) {
                        //Log.e(this.parent.getString(R.string.app_name) + " PlayList.startPlayer", ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
                        this.startPlayer();
                    }
                } else {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {

                    }
                    this.load(false);
                }
            } catch (final Exception e) {
                try {
                    new Thread(() -> Utils.doPostHTTP(String.format("%s://%s:%s/%s/%s/programs?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/media_play", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version")), "")).start();
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.startPLayer]" : e.getMessage());
                    this.startPlayer();
                } catch (Exception ex1) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.startPLayer]" : e.getMessage());
                }
            }
        }
    }

    private void playMedia(String uri) {
        this.playMedia(uri, new Duration(0));
    }

    private void playMedia(String uri, Duration seekPosition) {
        this.currentMediaUri = uri;
        //begin by raising the volume
        javafx.scene.media.Media media = new javafx.scene.media.Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(() -> {
            this.foundMedia = false;
            //Utils.toastOnScreen("logging media...", this.parent);
            Utils.logEvent(EventCategory.MEDIA, EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            if (this.isShuttingDown) {
                return;
            }
            try {
                mediaPlayer.dispose();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.playMedia]" : e.getMessage());
            }
            //this.load();
            this.startPlayer();
        });
        mediaPlayer.setOnError(() -> {
            this.foundMedia = false;
            //Utils.toastOnScreen("logging media...", this.parent);
            Utils.logEvent(EventCategory.MEDIA, EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            if (this.isShuttingDown) {
                return;
            }
            try {
                mediaPlayer.dispose();
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.playMedia]" : e.getMessage());
            }
            //this.load();
            this.startPlayer();
        });
        mediaPlayer.setOnReady(() -> {
            try {
                if (this.callSignPlayer != null && this.callSignPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    this.mediaPlayer.setVolume(0.07f);
                } else {
                    this.mediaPlayer.setVolume(getMaxVolume());
                }
                //Utils.toastOnScreen("logging media...", this.parent);
                Utils.logEvent(EventCategory.MEDIA, EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            } catch (Exception ex) {
                this.mediaPlayer.setVolume(0.9F);
            }
        });

        mediaPlayer.setVolume(getMaxVolume());
        mediaPlayer.play();
    }

    /**
     * Stops the media player and disposes it.
     */
    public void stop() {
        this.isShuttingDown = true; //async nature of stuff means stuff can be called in the middle of shutdown. This flag shd be inspected...
        if (this.callSignProvider != null) {
            this.callSignProvider.stop();
            if (this.callSignPlayer != null) {
                try {
                    this.callSignPlayer.stop();
                } catch (Exception e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.stop]" : e.getMessage());
                }
            }
            try {

                Utils.logEvent(EventCategory.MEDIA, EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.stop]" : e.getMessage());
            }
            if (mediaPlayer != null) {
                try {
                    this.fadeOut();
                    mediaPlayer.stop();
                } catch (Exception e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.stop]" : e.getMessage());
                }
            }
        }
    }

    private void fadeOut() {
        //fade out in 5 secs
        double step = mediaPlayer.getVolume() / 50;
        while (mediaPlayer.getVolume() > 0) {
            //volume = volume - 0.05F;
            mediaPlayer.setVolume(mediaPlayer.getVolume() - step);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Pauses the currently playing media
     */
    public void pause(boolean soft) {
        try {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (soft) //typically these are thrown by SIP calls
                {
                    this.fadeOut();

                }

                Utils.logEvent(EventCategory.MEDIA, EventAction.PAUSE, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));

                try {
                    this.mediaPosition = this.mediaPlayer.getCurrentTime();
                    mediaPlayer.stop(); //advised that media players should never be reused, even in pause/play scenarios
                    mediaPlayer.dispose();
                } catch (Exception e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.pause]" : e.getMessage());
                }

                try {
                    //stop the call sign player as well
                    this.callSignPlayer.stop(); //this thread is sleeping! TODO: interrupt it
                    this.callSignPlayer.dispose();
                } catch (Exception e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.pause]" : e.getMessage());
                }

                //stop the callSign looper so they do not play during the call
                this.callSignProvider.stop();
            }
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.pause]" : e.getMessage());
        }
    }

    /**
     * Resumes playback after it has been paused
     *
     * @deprecated .This method should be called if you want to resume the media that was playing at the time the playlist was paused.
     * THis was deprecated in favor of stopping and restarting the playlist
     * However streams take care of themselves and for songs, another song will be chosen, so no big deal
     */
    @Deprecated

    public void resume() {
        try {
            this.playMedia(this.currentMediaUri, this.mediaPosition);
            try {

                Utils.logEvent(EventCategory.MEDIA, EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentMedia.getTitle(), currentMedia.getArtists(), currentMedia.getFileLocation()));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.resume]" : e.getMessage());
            }
            // resume the callSign provider
            this.callSignProvider.start();

        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.resume]" : e.getMessage());
        }
    }

    private HashSet<Media> loadMedia(ArrayList<String> playlists) {
        Random rand = new Random();
        HashSet<Media> media = new HashSet<>();
        for (String playlist : playlists) {
            String query = "select title, item, itemtypeid from playlist where lower(title) = ?";
            List<String> args = Arrays.asList(playlist.toLowerCase());
            List<List<Object>> data;
            try {
                data = DBAgent.getData(query, args);
            } catch (SQLException e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.loadMedia]" : e.getMessage());
                return media;
            }
            data.forEach(row -> {
                try {
                    switch ((String) row.get(2)) {
                        case "1"://songs
                            media.addAll(this.mediaLib.getMedia((String) row.get(1), "title"));
                            break;
                        case "2":// albums
                            media.addAll(this.mediaLib.getMedia((String) row.get(1), "album"));
                            break;
                        case "3":// artists
                            media.addAll(this.mediaLib.getMedia((String) row.get(1), "artist"));
                            break;
                    }
                } catch (SQLException e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.loadMedia]" : e.getMessage());
                }
            });
        }

        return media;
    }


    /**
     * Gets the media currently loaded in this playlist
     *
     * @return Array of Media objects loaded in this playlist
     */
    public HashSet<Media> getMedia() {
        return this.mediaList;
    }


    private void onReceiveCallSign(String url) {
        try {

            callSignPlayer = new MediaPlayer(new javafx.scene.media.Media(url));
            callSignPlayer.setOnEndOfMedia(() -> {
                try {
                    PlayList.this.mediaPlayer.setVolume(getMaxVolume());
                    try {
                        Utils.logEvent(EventCategory.MEDIA, EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
                    } catch (Exception e) {
                        Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.onReceiveCallSign]" : e.getMessage());
                    }
                    callSignPlayer.dispose();
                } catch (Exception e) {
                    Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.onReceiveCallSign]" : e.getMessage());
                }
            });

            this.mediaPlayer.setVolume(0.07f);
            callSignPlayer.setVolume(getMaxVolume());
            try {
                Utils.logEvent(EventCategory.MEDIA, EventAction.START, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.onReceiveCallSign]" : e.getMessage());
            }
            this.callSignPlayer.play();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.onReceiveCallSign]" : e.getMessage());
        }
    }

    @Override
    protected void finalize() {
        this.stop();
        try {
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    class CallSignProvider implements Runnable {

        private final HashSet<Media> callSigns;
        Iterator<Media> mediaIterator;
        private boolean isRunning;

        CallSignProvider() {
            ArrayList<String> jingles = new ArrayList();
            jingles.add("jingle");
            jingles.add("jingles");
            this.callSigns = PlayList.this.loadMedia(jingles);
            this.isRunning = false;
        }

        @Override
        public void run() {
            this.isRunning = true;

            this.mediaIterator = callSigns.iterator();
            while (this.isRunning) {
                try {
                    this.playCallSign();
                    Thread.sleep(PlayList.this.getJingleInterval());// 2 mins debug, 12 mins release
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void stop() {
            this.isRunning = false;
            try {
                Utils.logEvent(EventCategory.MEDIA, EventAction.STOP, String.format("Title: %s, Artist: %s, Location: %s", currentCallSign.getTitle(), currentCallSign.getArtists(), currentCallSign.getFileLocation()));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[CallSignProvider.stop]" : e.getMessage());
            }
            if (PlayList.this.runnerThread != null) {
                PlayList.this.runnerThread.interrupt(); //so the thread can exit on state change, otherwise could sleep through on->off->on
            }
        }

        private void playCallSign() {
            try {
                if (this.callSigns.size() < 1) {
                    return;
                }
                if (!this.mediaIterator.hasNext()) {
                    this.mediaIterator = callSigns.iterator(); // reset the iterator to 0 if at the end
                }
                currentCallSign = mediaIterator.next();
                onReceiveCallSign(currentCallSign.getFileLocation());
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[CallSignProvider.PlayCallSign]" : e.getMessage());
            }
        }

        public void start() {
            PlayList.this.runnerThread = new Thread(this);
            PlayList.this.runnerThread.start();

        }
    }

    private int getMaxVolume() {
        String stationVolume = Configuration.getProperty("media_volume", "8");
        try {
            return Integer.parseInt(stationVolume);
        } catch (NumberFormatException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.getMaxVolume]" : e.getMessage());
        }
        return 8;
    }

    private int getJingleInterval() {
        String jingleInterval = Configuration.getProperty("jingle_interval", "600000");
        try {
            return Integer.parseInt(jingleInterval);
        } catch (NumberFormatException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Playlist.getJingleInterval]" : e.getMessage());
        }
        return 600000;
    }

}
