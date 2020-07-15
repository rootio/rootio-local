package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MediaLibrary {


    MediaLibrary() {
    }



    public HashSet<Media> getMedia(String value, String column) throws SQLException {
        HashSet<Media> songs = new HashSet();
        String query = "Select title, location, duration, artist from media where " + column +" = ?";
        List<String> arguments = Arrays.asList(value);
        List<List<Object>> results = DBAgent.getData(query,arguments);
        results.forEach(record -> {
            songs.add(new Media((String)record.get(0), (String)record.get(1), (long)record.get(2), (String)record.get(3)));
        });
        return songs;
    }
}
