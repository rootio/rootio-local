package org.rootio.tools.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.handset.R;
import org.rootio.messaging.Message;
import org.rootio.tools.persistence.DBAgent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Utils {


    public static Long getEventTimeId(long programId, Date scheduleDate, int duration) throws SQLException {
        String tableName = "eventtime";
        List<String> columns = Arrays.asList("id");
        List<String> whereClause = Arrays.asList("programid", "duration", "scheduledate");
        List<String> whereArgs = Arrays.asList(String.valueOf(programId), String.valueOf(duration), Utils.getDateString(scheduleDate, "yyyy-MM-dd HH:mm:ss"));
        //DBAgent dbAgent = new DBAgent(parent);
        List<List<Object>> results = DBAgent.getData(tableName, columns, whereClause, whereArgs, null, null, null, null);
        return results.size() > 0 ? (Long)results.get(0).get(0) : 0l;
    }

    public static void toastOnScreen(Message m) {
        //Throw a message to the logger via rabbit MQ or similar
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

    public static InetAddress parseInetAddressFromString(String input) {
        try {
            InetAddress address = InetAddress.getByName(input);
            return address;
        } catch (Exception ex) {
            return null;
        }
    }

    public static JSONObject getJSONFromFile(String fileName) throws IOException {
        FileInputStream input = null;
        try {
            File jsonFile = new File(fileName);
            input = new FileInputStream(jsonFile);
            byte[] buffer = new byte[1024];
            input.read(buffer);
            return new JSONObject(new String(buffer));
        } catch (Exception ex) {
            Logger.getLogger("org.rootio").log(Level.SEVERE, (ex.getMessage() == null) ? "NullPointerException(CallAuthenticator.isWhiteListed)" : ex.getMessage());
            throw ex;
        } finally {
            try {
                input.close();
            } catch (Exception ex) {
                // log the exception
            }
        }

    }
    }

    public static void savePreferences(HashMap<String, String> values) {
        values.forEach((k,v) -> Configuration.setProperty(k, v));
    }

    public static Object getPreference(String key, Class cls, Context context)
    {
        try{
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        if(prefs != null) {
            if (cls == String.class) {
                return prefs.getString(key, null);
            } else if (cls == int.class) {
                return prefs.getInt(key, 0);
            } else if (cls == boolean.class) {
                return prefs.getBoolean(key, false);
            } else if (cls == long.class) {
                return prefs.getLong(key, 0l);
            } else if (cls == float.class) {
                return prefs.getFloat(key, 0f);
            }
        }}
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        catch (StackOverflowError er)
        {
            er.printStackTrace();
        }
        return null;
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Object> doDetailedPostHTTP(String httpUrl, String data) {
        URL url;
        Long then  = Calendar.getInstance().getTimeInMillis();
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
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
    public static boolean isConnectedToStation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        return prefs != null && prefs.contains("station_information");
     }

     public static long logEvent(Context context, EventCategory category, EventAction action, String argument)
     {
          try {
             ContentValues values = new ContentValues();
             values.put("category", category.name());
             values.put("argument", argument);
             values.put("event", action.name());
             values.put("eventdate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
             return DBAgent.saveData("activitylog", null, values);
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
         }
         return 0;
     }

    public enum EventCategory
    {
        MEDIA, SERVICES, SYNC, SMS, CALL, SIP_CALL, DATA_NETWORK
    }

    public enum EventAction
    {
        ON, OFF, PAUSE, STOP, START, SEND, RECEIVE, REGISTRATION, RINGING
     }
}
