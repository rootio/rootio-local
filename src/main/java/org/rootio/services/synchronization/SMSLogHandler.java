package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class SMSLogHandler implements SynchronizationHandler {

    SMSLogHandler() {
    }

    @Override
    public JSONObject getSynchronizationData() {
        JSONObject data = new JSONObject();
        JSONArray sms = new JSONArray();
        String query = "select id, address, body, date, type from sms where id > ?";
        List<String> whereArgs = Collections.singletonList(String.valueOf(this.getMaxId()));

        try {
            List<List<Object>> results = DBAgent.getData(query, whereArgs);
            results.forEach(record -> {
                JSONObject smsRecord = new JSONObject();
                try {
                    smsRecord.put("message_uuid", (long) record.get(0));
                    if ((int) record.get(4) == 1) {
                        smsRecord.put("from_phonenumber", record.get(1));
                        smsRecord.put("to_phonenumber", "");
                    } else {
                        smsRecord.put("from_phonenumber", "");
                        smsRecord.put("to_phonenumber", record.get(1));
                    }
                    smsRecord.put("text", record.get(2));
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis((long) record.get(3));
                    smsRecord.put("sendtime", Utils.getDateString(cal.getTime(), "yyyy-MM-dd HH:mm:ss"));
                    sms.put(smsRecord);
                } catch (JSONException e) {
                    Logger.getLogger("org.rootio").log(Level.WARNING, "SMSLogHandler.getSynchronizationData: " + e.getMessage());
                }
            });
            data.put("message_data", sms);
        } catch (JSONException | SQLException e) {
            Logger.getLogger("org.rootio").log(Level.WARNING, "SMSLogHandler.getSynchronizationData: " + e.getMessage());
        }
        return data;
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        JSONArray results;
        long maxSmsId = getMaxId();
        try {
            results = synchronizationResponse.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                if (results.getJSONObject(i).getBoolean("status")) {
                    maxSmsId = Math.max(results.getJSONObject(i).getLong("id"), maxSmsId);
                }
            }
            this.logLastId(maxSmsId);//This is unsafe. if some messages are unsynced, they are skipped for good
        } catch (JSONException e) {
            Logger.getLogger("org.rootio").log(Level.WARNING, "SMSLogHandler.processJSONResponse: " + e.getMessage());
        }
    }

    private void logLastId(long id) {
        Configuration.setProperty("call_id", String.valueOf(id));
    }

    private long getMaxId() {
        try {
            return Long.parseLong(Configuration.getProperty("call_id"));
        } catch (NumberFormatException e) {
            Logger.getLogger("org.rootio").log(Level.WARNING, "SMSLogHandler.getMaxId: " + e.getMessage());
            return 0;
        }
    }


    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/message?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("Station_key"), Configuration.getBuildName(), Configuration.getBuildNumber());
    }
}
