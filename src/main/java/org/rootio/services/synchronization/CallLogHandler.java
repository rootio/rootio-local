package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CallLogHandler implements SynchronizationHandler {


    CallLogHandler() {

    }

    @Override
    public JSONObject getSynchronizationData() {
        JSONObject data = new JSONObject();
        JSONArray calls = new JSONArray();
        List<String> sortOrder =  Arrays.asList("call_date");
        String sortDirection = "asc";
        List<String> columns = Arrays.asList("id", "b_party", "call_duration", "call_date", "call_type");
        List<String> filterColumns = Arrays.asList("id");
        List<String> args = Arrays.asList(String.valueOf(this.getMaxId()));
        try {
            List<List<Object>> results = DBAgent.getData("calls",columns,filterColumns,args,null, null,sortOrder, sortDirection, null);
            results.forEach( record -> {
                    JSONObject callRecord = new JSONObject();
                try {
                    callRecord.put("call_uuid", (long)record.get(0));
                if ((String)record.get(4) == "incoming" || (String)record.get(4) == "missed") {
                        callRecord.put("from_phonenumber", (String)record.get(1));
                        callRecord.put("to_phonenumber", "");
                    } else {
                        callRecord.put("from_phonenumber", "");
                        callRecord.put("to_phonenumber", (String)record.get(1));
                    }
                    callRecord.put("duration", (int)record.get(2));
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis((long)record.get(3));
                    callRecord.put("start_time", Utils.getDateString(cal.getTime(), "yyyy-MM-dd HH:mm:ss"));
                    calls.put(callRecord);
                } catch (JSONException e) {
                    Logger.getLogger("org.rootio").log(Level.WARNING, "CallLogHandler.getSynchronizationData: " + e.getMessage());
                }
                });
            data.put("call_data", calls);
        } catch (JSONException | SQLException e) {
            Logger.getLogger("org.rootio").log(Level.WARNING, "CallLogHandler.getSynchronizationData: " + e.getMessage());
        }
        return data;
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        JSONArray results;
        long maxCallId = this.getMaxId();
        try {
            results = synchronizationResponse.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                if (results.getJSONObject(i).getBoolean("status")) {
                    maxCallId = Math.max(results.getJSONObject(i).getLong("id"), maxCallId);
                    //this.parent.getContentResolver().delete(uri, CallLog.Calls._ID + " = ? ", new String[] { String.valueOf(results.getJSONObject(i).getLong("id")) });
                }
            }
            this.logLastId(maxCallId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void logLastId(long id) {
        Configuration.setProperty("call_id", String.valueOf(id));
    }

    private long getMaxId() {
        try {
            return Long.parseLong(Configuration.getProperty("call_id"));
        }
        catch (NumberFormatException ex)
        {
            throw ex;
        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/call?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("Station_key"), Configuration.getProperty("version_name"), Configuration.getProperty("version_code"));
    }
}
