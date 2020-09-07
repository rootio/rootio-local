package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

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
    private long maxDateadded;

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
                this.logMaxDateAdded(this.maxDateadded);
            }
        } catch (JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[MusicListHandler.processJSONResponse]" : e.getMessage());
        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/music?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }

    private long getMaxDateAdded() {
        return Long.parseLong(Configuration.getProperty("media_max_date_added", "0"));
    }

    private void logMaxDateAdded(long maxDate) {
        Configuration.setProperty("media_max_date_added", String.valueOf(maxDate));
    }

    private JSONObject getSongList() {
        JSONObject music = new JSONObject();
        try {
            this.maxDateadded = this.getMaxDateAdded();
            String query = "select title, album, artist, duration, date_added, date_modified from media where date_added > ? order by title asc";
            List<String> whereArgs = Collections.singletonList(String.valueOf(this.maxDateadded));
            List<List<Object>> results = DBAgent.getData(query, whereArgs);
            long dateAdded = 0;

            results.forEach(row -> {
                try {
                    JSONObject song = new JSONObject();
                    song.put("title", ((String) row.get(0)).replace("\u2019", "'").replace("\u2018", "'"));
                    song.put("duration", row.get(3));
                    song.put("date_added", row.get(4));
                    song.put("date_modified", row.get(5));
                    if (dateAdded > maxDateadded) {
                        maxDateadded = dateAdded;
                    }

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
