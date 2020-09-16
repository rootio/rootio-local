package org.rootio.tools.utils;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utils {

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
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Utils.doPostHTTP]" : e.getMessage());
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
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[Utils.doDetailedPostHTTP]" : e.getMessage());
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
        Logger.getLogger("RootIO").logp(Level.INFO, category.toString(), action.toString(), argument);
        try {
            HashMap<String, Object> values = new HashMap<>();
            values.put("category", category.name());
            values.put("argument", argument);
            values.put("event", action.name());
            values.put("event_date", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
            return DBAgent.saveData("activity_log", values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public static void saveJSONPreferences(JSONObject jsonData, String fileName) {
        File fl = new File(Configuration.getProperty("config_directory" + "/" + fileName));
        try (FileWriter fwr = new FileWriter(fl)) {
            fwr.write(jsonData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadPreferencesFile(String fileName) {
        File fl = new File(Configuration.getProperty("config_directory" + "/" + fileName));
        StringBuilder buffer = new StringBuilder();
        try(FileReader fr = new FileReader(fl))
        {
            while(true) {
                int c = fr.read();
                if(c < 0)
                {
                    break;
                }
                buffer.append((char)c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
