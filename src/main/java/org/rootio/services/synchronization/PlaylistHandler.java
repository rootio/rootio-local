package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class PlaylistHandler implements SynchronizationHandler {


    PlaylistHandler() {
    }


    @Override
    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }


    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        try {
            DBAgent.deleteRecords("play_list", null, Collections.EMPTY_LIST);//empty playlist info
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PlaylistHandler.processJSONObject]" : e.getMessage());
            return;
        }
        try {
            JSONArray playlists = synchronizationResponse.getJSONArray("objects");
            for (int i = 0; i < playlists.length(); i++) {
                JSONObject playlist = playlists.getJSONObject(i);
                // Save the songs
                JSONArray songs = playlist.getJSONArray("songs");
                for (int j = 0; j < songs.length(); j++) {
                    savePlaylistItem(Arrays.asList(playlist.getString("title"), songs.getJSONObject(j).getString("title")), "1");
                }
                // save the Albums
                JSONArray albums = playlist.getJSONArray("albums");
                for (int j = 0; j < albums.length(); j++) {
                    savePlaylistItem(Arrays.asList(playlist.getString("title"), albums.getJSONObject(j).getString("title")), "2");
                }
                // save the Artists
                JSONArray artists = playlist.getJSONArray("artists");
                for (int j = 0; j < artists.length(); j++) {
                    savePlaylistItem(Arrays.asList(playlist.getString("title"), artists.getJSONObject(j).getString("title")), "3");
                }
            }
        } catch (JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PlaylistHandler.processJSONObject]" : e.getMessage());
        }
    }

    private long savePlaylistItem(List<String> values, String type)
    {
        HashMap<String, Object> data = new HashMap<>();
        data.put("title", values.get(0));
        data.put("item", values.get(1));
        data.put("item_type_id", type);
        try {
            return DBAgent.saveData("play_list", data);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[PlaylistHandler.processJSONObject]" : e.getMessage());
        }
        return -1;
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/playlists?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getBuildName(), Configuration.getBuildNumber());
    }

}
