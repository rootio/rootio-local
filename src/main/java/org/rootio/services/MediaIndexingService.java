package org.rootio.services;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.rootio.configuration.Configuration;
import org.rootio.launcher.Rootio;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MediaIndexingService implements RootioService {

    private Thread indexingThread;
    private final int serviceId = 7;

    @Override
    public void start() {
        Utils.logEvent(EventCategory.SERVICES, EventAction.START, "Media Indexing Service");
        indexingThread = new Thread(() -> {
            while (Rootio.getServiceStatus(serviceId)) {
                if (isIndexingDue()) {
                    runIndexing();
                    if (!isNewIndexEmpty() && Rootio.getServiceStatus(serviceId)) {
                        moveIndexedMedia();
                    }
                }
                try {
                    Thread.sleep(3600 * 1000 * 5); //5 hours
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        indexingThread.start();
        new ServiceState(7,"Media Indexing", 1).save();
        while(Rootio.getServiceStatus(serviceId)) {
            try {
                    indexingThread.join();
            } catch (InterruptedException e) {
                if(!Rootio.getServiceStatus(serviceId)) {
                    indexingThread.interrupt();
                    System.out.print("interrupted...");
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getServiceId() {
        return serviceId;
    }

    private void runIndexing() {
        String[] directories = Configuration.getProperty("media_directories","").split(",");
        Arrays.stream(directories).forEach(dir -> {

            index(new File(dir.trim()));
        });
    }

    private boolean isIndexingDue() {
        Date lastIndexDate = getLastIndexDate();
        return lastIndexDate == null || Duration.between(lastIndexDate.toInstant(), Calendar.getInstance().getTime().toInstant()).toDays() > 7;
    }

    private Date getLastIndexDate() {
        String query = "select max(date_added) from media";
        List<List<Object>> result;
        try {
            result = DBAgent.getData(query, Collections.emptyList());
            if (!result.isEmpty() && result.get(0).get(0) != null) {
                return ((Date) result.get(0).get(0));
            }
        } catch (NullPointerException | SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ProgramHandler.getSynchronizationHandler]" : e.getMessage());
        }
        return null;
    }

    private void moveIndexedMedia() {
        List<String> queries = Arrays.asList("delete from media", "insert into media (select * from media_tmp)", "delete from media_tmp");
        for (String query : queries) {
            try {
                DBAgent.saveData(query, Collections.emptyList());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private boolean isNewIndexEmpty() {
        String query = "select count(*) from media_tmp";
        List<String> whereArgs = Collections.emptyList();
        List<List<Object>> result = null;
        try {
            result = DBAgent.getData(query, whereArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[ServiceState.serviceStateExists]" : e.getMessage());
        }
        return result == null || result.size() == 0 || (long) result.get(0).get(0) < 1;
    }

    private void index(File rootDir) {
        //get the files in the folder
        if(!Rootio.getServiceStatus(serviceId))
        {
            return;
        }
        File[] files = rootDir.listFiles();
        try {
            List<List<String>> records = new ArrayList<>();
            Stream.of(Objects.requireNonNull(files)).forEach(k -> {
                if (k.isDirectory()) {
                    if (!k.isHidden()) {
                        index(k.getAbsoluteFile());
                    }
                } else {
                    String fn = k.getName().toLowerCase();
                    if (fn.endsWith(".mp3") || fn.endsWith(".wav") || fn.endsWith(".flac") || fn.endsWith(".aac") || fn.endsWith(".aiff") ||
                            fn.endsWith(".wma") || fn.endsWith(".mp4") || fn.endsWith(".m4a") || fn.endsWith(".ogg") || fn.endsWith(".dsf")) {
                        AudioFile af;
                        try {
                            af = AudioFileIO.read(k.getAbsoluteFile());
                            Tag tag = af.getTag();
                            records.add(Arrays.asList(tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.ARTIST), af.getFile().getAbsolutePath(), String.valueOf(af.getAudioHeader().getTrackLength())));
                        }
                        //maybe do individual things with the exceptions
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            if (records.size() > 0) {
                saveRecords(records);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Saves the specified records to the media database
     *
     * @param records List of List<String>, each nested List representing a row
     */
    private static void saveRecords(List<List<String>> records) {
        StringBuilder query = new StringBuilder();
        List<String> args = new ArrayList<>();
        query.append("insert into media_tmp (title, artist, album, location, duration) values ");
        String data = String.join(",", records.stream().map(
                l -> { //L is a List<String> , returns (?,?,?)
                    args.addAll(l); //add to the params list
                    return "(" + String.join(",", l.stream()
                            .map(s -> "?")
                            .toArray(String[]::new)) + ")";
                }).toArray(String[]::new));

        query.append(data);
        try {
            long result = DBAgent.saveData(query.toString(), args);
            System.out.println("" + result + " songs indexed");
        } catch (SQLException throwable) {
            System.out.println("indexing failed due to SQL error");
        }
    }
}