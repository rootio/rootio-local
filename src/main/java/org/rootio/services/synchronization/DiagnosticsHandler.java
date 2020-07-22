package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiagnosticsHandler implements SynchronizationHandler {

    private DiagnosticStatistics diagnosticStatistics;

    DiagnosticsHandler() {
        try {
            this.diagnosticStatistics = new DiagnosticStatistics();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public JSONObject getSynchronizationData() {
        return this.diagnosticStatistics.getJSONRecords();
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
                    this.deleteSyncedRecord(objects.getJSONObject(i).getString("id"));
                }
            }
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ProgramHandler.processJSONObject]" : e.getMessage());
        }
    }

    private long deleteSyncedRecord(String id) throws SQLException {
        String tableName = "diagnostic";
        String whereClause = "id = ?";
        List<String> filterArgs = Arrays.asList(id);
        try {
            return DBAgent.deleteRecords(tableName, whereClause, filterArgs);
        } catch (SQLException e) {
            throw e;        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/analytics?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }
}
