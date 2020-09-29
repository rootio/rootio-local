package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class MusicListHandler implements SynchronizationHandler {

    private int limit = 100;

    MusicListHandler() {
    }

    @Override
    public JSONObject getSynchronizationData() {
        return this.getSongList();
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        try {
            if (synchronizationResponse.getBoolean("status")) {
                this.logMaxDateAdded(synchronizationResponse.getString("date"));
            }
        } catch (JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[MusicListHandler.processJSONResponse]" : e.getMessage());
        }
    }


    @Override
    public String getSynchronizationURL() {
        if (isSyncDue()) {
            return String.format("%s://%s:%s/%s/%s/music?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
        } else {
            return null;
        }
    }

    private boolean isSyncDue() {
        List<List<Object>> result = getMediaStatus();
        if (result == null) {
            return false; //something wrong, needs looking into
        }
        String response = Utils.doPostHTTP(getPreSynchronizationURL(), "");
        JSONObject status = new JSONObject(response);

        //if the number of songs, albums or artists is different from what is on the server
        //and the sync date is different from what is on the server, a sync is in order
        boolean syncDue = !(status.getJSONObject("songs").getLong("count") == (long) result.get(0).get(0) && status.getJSONObject("songs").getString("max_date").equals(getMaxDateAdded())
                && status.getJSONObject("albums").getLong("count") == (long) result.get(0).get(1) && status.getJSONObject("albums").getString("max_date").equals(getMaxDateAdded())
                && status.getJSONObject("artists").getLong("count") == (long) result.get(0).get(2) && status.getJSONObject("artists").getString("max_date").equals(getMaxDateAdded()));
        Logger.getLogger("RootIO").log(Level.INFO, "Media Sync Due: " + syncDue +" ( " + status +" ) and " + result.get(0) +", "+getMaxDateAdded());
        return syncDue;
    }

    private String getPreSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/music_status?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }

    private String getMaxDateAdded() {
        String maxDate = Configuration.getProperty("media_max_date_added", "0");
        return maxDate.contains(".") ? maxDate.substring(0, maxDate.indexOf(".")) : maxDate;
    }

    private void logMaxDateAdded(String maxDate) {
        Configuration.setProperty("media_max_date_added", maxDate);
        try {
            Configuration.saveChanges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<List<Object>> getMediaStatus() {
        String query = "select count(distinct title), count(distinct album), count(distinct artist) from media";
        List<String> whereArgs = Collections.emptyList();
        try {
            return DBAgent.getData(query, whereArgs);
        } catch (SQLException throwables) {
            return null;
        }
    }

    private JSONObject getSongList() {
        JSONObject music = new JSONObject();
        try {
            String query = "select title, album, artist, duration, date_added, date_modified from media order by title asc";
            List<String> whereArgs = Collections.emptyList();
            List<List<Object>> results = DBAgent.getData(query, whereArgs);

            results.forEach(row -> {
                try {
                    JSONObject song = new JSONObject();
                    song.put("title", ((String) row.get(0)).replace("\u2019", "'").replace("\u2018", "'"));
                    song.put("duration", row.get(3));
                    song.put("date_added", row.get(4));
                    song.put("date_modified", row.get(5));

                    String artist = row.get(2) == null ? "unknown" : (String) row.get(2);
                    artist = artist.replace("\u2019", "'").replace("\u2018", "'");
                    if (!music.has(artist)) {
                        music.put(artist, new JSONObject());
                    }

                    String album = row.get(1) == null ? "unknown" : (String) row.get(1);
                    album = album.replace("\u2019", "'").replace("\u2018", "'");

                    if (!music.getJSONObject(artist).has(album)) {
                        music.getJSONObject(artist).put(album, new JSONObject());
                        music.getJSONObject(artist).getJSONObject(album).put("songs", new JSONArray());
                    }

                    music.getJSONObject(artist).getJSONObject(album).getJSONArray("songs").put(song);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[MusicListHandler.getSongList]" : e.getMessage());
        }
        return music;
    }

}
