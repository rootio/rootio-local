package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogHandler implements SynchronizationHandler {

    LogHandler() {
    }

    public JSONObject getSynchronizationData() {
        return this.getRecords();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule
     * information
     *
     * @param synchronizationResponse The JSON program definition received from the cloud server
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        try {
            JSONArray objects = synchronizationResponse.getJSONArray("results");

            for (int i = 0; i < objects.length(); i++) {
                if (objects.getJSONObject(i).getBoolean("status")) {
                    this.deleteSyncedRecord(objects.getJSONObject(i).getInt("id"));
                }
            }
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[LogHandler.processJSONObject]" : e.getMessage());
        }
    }

    private long deleteSyncedRecord(int id) {
        String tableName = "activity_log";
        String whereClause = "id = ?";
        List<String> filterArgs = Collections.singletonList(String.valueOf(id));
        try {
            return DBAgent.deleteRecords(tableName, whereClause, filterArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[LogHandler.deleteSyncedRecord]" : e.getMessage());
        }
        return -1;
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/log?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getBuildName(), Configuration.getBuildNumber());
    }

    private JSONObject getRecords() {
        String query = "select id, category, argument, event, event_date  from activity_log limit 100";
        List<String> filterArgs = new ArrayList<>();
        JSONObject parent = new JSONObject();
        JSONArray logData = new JSONArray();
        try {

            List<List<Object>> results = DBAgent.getData(query, filterArgs);
            results.forEach(row -> {
                JSONObject record = new JSONObject();
                try {
                    record.put("id", row.get(0));
                    record.put("category", row.get(1));
                    record.put("argument", row.get(2));
                    record.put("event", row.get(3));
                    record.put("eventdate", row.get(4));
                    logData.put(record);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            parent.put("log_data", logData);
        } catch (SQLException | JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[LogHandler.getRecords]" : e.getMessage());
        }
        return parent;
    }
}
