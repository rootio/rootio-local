package org.rootio.tools.utils;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Utils {


    public static Long getEventTimeId(long programId, Date scheduleDate, int duration) throws SQLException {
        String query = "select id from eventtime where program_id = ? and duration = ? and schedule_date = ?";
        List<String> whereArgs = Arrays.asList(String.valueOf(programId), String.valueOf(duration), Utils.getDateString(scheduleDate, "yyyy-MM-dd HH:mm:ss"));
        List<List<Object>> results = DBAgent.getData(query, whereArgs);
        return results.size() > 0 ? (Long) results.get(0).get(0) : 0L;
    }

    public static String getCurrentDateAsString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date now = Calendar.getInstance().getTime();
        try {
            return sdf.format(now);
        } catch (Exception ex) {
            return "";
        }
    }

    public static Date getDateFromString(String input, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(input);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getDateString(Date input, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.format(input);
        } catch (Exception ex) {
            return null;
        }
    }

    public static int parseIntFromString(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static long parseLongFromString(String input) {
        try {
            return Long.parseLong(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static double parseDoubleFromString(String input) {
        try {
            return Double.parseDouble(input);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static String doPostHTTP(String httpUrl, String data) {
        URL url;
        try {
            url = new URL(httpUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.connect();
            OutputStream outstr = httpUrlConnection.getOutputStream();
            //writeToFile(data);
            outstr.write(data.getBytes());
            outstr.flush();
            InputStream instr = httpUrlConnection.getInputStream();
            StringBuilder response = new StringBuilder();
            while (true) {
                int tmp = instr.read();
                if (tmp < 0) {
                    break;
                }
                response.append((char) tmp);
            }
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Object> doDetailedPostHTTP(String httpUrl, String data) {
        URL url;
        long then = Calendar.getInstance().getTimeInMillis();
        HashMap<String, Object> responseData = new HashMap<>();
        try {
            url = new URL(httpUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.connect();
            OutputStream outstr = httpUrlConnection.getOutputStream();
            //writeToFile(data);
            outstr.write(data.getBytes());
            outstr.flush();
            InputStream instr = httpUrlConnection.getInputStream();
            StringBuilder response = new StringBuilder();
            while (true) {
                int tmp = instr.read();
                if (tmp < 0) {
                    break;
                }
                response.append((char) tmp);
            }
            responseData.put("response", response.toString());
            responseData.put("duration", Calendar.getInstance().getTimeInMillis() - then); //ChronoUnit.MICROS.between(dt, LocalDate.now()));
            responseData.put("responseCode", httpUrlConnection.getResponseCode());
            responseData.put("length", httpUrlConnection.getContentLength());
            responseData.put("url", httpUrlConnection.getURL());
            return responseData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check to see if this phone is connected to a station in the cloud. This is done by looking for config files that are created when a station is connected
     *
     * @return True if connected, false if not connected
     */
    public static boolean isConnectedToStation() {
        return Configuration.isSet("station_name");
    }

    public static long logEvent(EventCategory category, EventAction action, String argument) {
        try {
            HashMap<String, Object> values = new HashMap<>();
            values.put("category", category.name());
            values.put("argument", argument);
            values.put("event", action.name());
            values.put("eventdate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
            return DBAgent.saveData("activitylog", values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public static void saveJSONPreferences(JSONObject jsonData, String fileName) {
        File fl = new File(Configuration.getProperty("config_direcroty" + "/" + fileName));
        try (FileWriter fwr = new FileWriter(fl)) {
            fwr.write(jsonData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
