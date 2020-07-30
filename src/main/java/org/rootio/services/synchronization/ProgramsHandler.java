package org.rootio.services.synchronization;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProgramsHandler implements SynchronizationHandler {

   private int records=2000;

    ProgramsHandler() {
    }

    @Override
    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    private Date getTodayBaseDate() {
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        boolean hasChanges = false;
        boolean shouldRestart = false;
        boolean result = false;
        ArrayList<HashMap<String, Object>> values = new ArrayList<>();
        //long result = 0;
        JSONArray results;
        try {
            results = synchronizationResponse.getJSONArray("scheduled_programs");

            for (int i = 0; i < results.length(); i++) {
                //if the record did not exist in the DB, then it is new ;-)
                long recs = this.deleteRecord(results.getJSONObject(i).getLong("scheduled_program_id"));
                if ((recs < 1 || results.getJSONObject(i).getBoolean("deleted")) && (isCurrentOrFutureChange(results.getJSONObject(i).getString("start"), results.getJSONObject(i).getString("end")))) {
                    hasChanges = true;
                    if(results.getJSONObject(i).getInt("program_type_id") == 2 && isCurrent(results.getJSONObject(i).getString("start"), results.getJSONObject(i).getString("end")))
                    {
                       shouldRestart = true;
                    }
                }

                values.add(getContentValues(results.getJSONObject(i).getInt("scheduled_program_id"), results.getJSONObject(i).getString("name"), Utils.getDateFromString(results.getJSONObject(i).getString("start"), "yyyy-MM-dd'T'HH:mm:ss"), Utils.getDateFromString(results.getJSONObject(i).getString("end"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("structure"), Utils.getDateFromString(results.getJSONObject(i).getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("program_type_id"), results.getJSONObject(i).getBoolean("deleted")));
            }
            if(values.size() > 0) {
                result = this.saveRecords(values) > 0;
            }
            if (result && hasChanges) {
                this.announceScheduleChange(shouldRestart);
            }
            // we had a full page, maybe more records..
            this.requestSync(results.length() == this.records);
        } catch (ExecutionControl.NotImplementedException | JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ProgramHandler.processJSONObject]" : e.getMessage());
        }
    }

    private void requestSync(boolean isStarting) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("Not yet implemented");
//        Intent intent = new Intent();
//        intent.setAction("org.rootio.services.synchronization.SYNC_REQUEST");
//        intent.putExtra("category", 2);
//        intent.putExtra("sync", isStarting?"start": "end");
//        this.parent.sendBroadcast(intent);
    }

    private boolean isCurrent(String startDateStr, String endDateStr)
    {
        try {
            Date now = Calendar.getInstance().getTime();
            Date startDate = Utils.getDateFromString(startDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            Date endDate = Utils.getDateFromString(endDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            return (startDate.compareTo(now) <= 0 && endDate.compareTo(now) >= 0); //yet to begin today, or is running
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isCurrentOrFutureChange(String startDateStr, String endDateStr) {
        try {
            Date now = Calendar.getInstance().getTime();
            Date startDate = Utils.getDateFromString(startDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            Date endDate = Utils.getDateFromString(endDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            boolean isToday = now.getYear() == startDate.getYear() && now.getMonth() == startDate.getMonth() && now.getDate() == startDate.getDate();
            return (isToday && startDate.compareTo(now) >= 0) || (startDate.compareTo(now) <= 0 && endDate.compareTo(now) >= 0); //yet to begin today, or is running
        } catch (Exception ex) {
            return false;
        }
    }

    private void announceScheduleChange(boolean shouldRestart) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("Yet to be implemented");
//        Intent intent = new Intent("org.rootio.services.synchronization.SCHEDULE_CHANGE_EVENT");
//        intent.putExtra("shouldRestart", shouldRestart);
//        this.parent.sendBroadcast(intent);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/programs?api_key=%s&%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"),  Configuration.getProperty("server_key"),getSincePart(), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }

    private String getSincePart() {
        String query = "select max(updated_at) from scheduled_program";
        List<List<Object>> result;
        try {
            result = DBAgent.getData(query, Collections.emptyList());
            if (!result.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(Utils.getDateFromString((String)result.get(0).get(0), "yyyy-MM-dd HH:mm:ss"));
                cal.add(Calendar.SECOND, 1); //Add 1 second, server side compares using greater or equal
                return String.format("updated_since=%s&records=%s", Utils.getDateString(cal.getTime(), "yyyy-MM-dd'T'HH:mm:ss"), records);
            }
        } catch (NullPointerException | SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ProgramHandler.getSynchronizationHandler]" : e.getMessage());
        }
        return String.format("all=1&records=%s", this.records);
    }

    private long saveRecords(ArrayList<HashMap<String, Object>> values) {
        String tableName = "scheduled_program";
        AtomicLong result = new AtomicLong(0);

            values.forEach(row -> {
                try {
                result.addAndGet(DBAgent.saveData(tableName, row));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
        return result.get();
    }

    private HashMap<String, Object> getContentValues(int id, String name, Date start, Date end, String structure, Date updatedAt, String programTypeId, Boolean deleted) {
        HashMap<String, Object> data = new HashMap();
        data.put("id", id);
        data.put("name", name);
        data.put("start", Utils.getDateString(start, "yyyy-MM-dd HH:mm:ss"));
        data.put("end", Utils.getDateString(end, "yyyy-MM-dd HH:mm:ss"));
        data.put("structure", structure);
        data.put("program_type_id", programTypeId);
        data.put("updated_at", Utils.getDateString(updatedAt, "yyyy-MM-dd HH:mm:ss"));
        data.put("deleted", deleted);
        return data;
    }

    private long deleteRecord(long id) {
        String tableName = "scheduled_program";
        String whereClause = "id = ?";
        List<String> whereArgs = Collections.singletonList(String.valueOf(id));
        try {
            return DBAgent.deleteRecords(tableName, whereClause, whereArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ProgramHandler.deleteRecord]" : e.getMessage());
        }
        return -1;
    }
}
