package org.rootio.services.synchronization;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DiagnosticStatistics {

    private List<Double> cpuData, memoryData, storageData, batteryData, mobileNetworkStrength, wifiConnectivityData, latitudeData, longitudeData;
    private List<Date> dateData;
    private List<Integer> idData;
    private List<String> mobileNetworkName, mobileNetworkType;
    private List<Boolean> mobileNetworkConnected;
    private int size;

    public DiagnosticStatistics() throws SQLException {
        this.LoadDiagnosticsData();
    }

    /**
     * Returns the diagnostic records written since the specified ID
     @return An array of String arrays each representing a record of
      * diagnostics
     */
    private void LoadDiagnosticsData() throws SQLException {
        String query = "select batterylevel, firstmobilenetworkname, firstmobilenetworktype, firstmobilenetworkconnected, firstmobilenetworkstrength, wificonnected, storageutilization, memoryutilization, cpuutilization, _id, diagnostictime, latitude, longitude  from diagnostic ";
        List<String> filterArgs = Arrays.asList();
        //DBAgent agent = new DBAgent(this.parent);
        List<List<Object>> results = DBAgent.getData(query, filterArgs);
        idData = new ArrayList<>();
        dateData = new ArrayList<>();
        cpuData = new ArrayList<>();
        memoryData = new ArrayList<>();
        storageData = new ArrayList<>();
        batteryData = new ArrayList<>();
        mobileNetworkStrength = new ArrayList<>();
        mobileNetworkName = new ArrayList<>();
        mobileNetworkType = new ArrayList<>();
        mobileNetworkConnected = new ArrayList<>();
        wifiConnectivityData = new ArrayList<>();
        latitudeData = new ArrayList<>();
        longitudeData = new ArrayList<>();
        results.forEach( row ->
        {
            idData.add((int)row.get(9));
            dateData.add((Date)row.get(10));
            batteryData.add((double)row.get(0));
            cpuData.add((double)row.get(8));
            storageData.add((double)row.get(6));
            memoryData.add((double)row.get(7));
            mobileNetworkStrength.add((double)row.get(4));
            mobileNetworkName.add((String)row.get(1));
            mobileNetworkType.add((String)row.get(2));
            mobileNetworkConnected.add((Boolean)row.get(3));
            wifiConnectivityData.add((double)row.get(5));
            latitudeData.add((double)row.get(11));
            longitudeData.add((double)row.get(12));
        });
    }

    /**
     * Gets The average CPU Utilization for the day
     *
     * @return Double representing average CPU Utilization for the day
     */
    public double getAverageCPUUtilization() {
        return cpuData.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the maximum recorded CPU Utilization for the day
     *
     * @return Double representing max CPU Utilization for the day
     */
    public double getMaxCPUUtilization() {
        return cpuData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the minimum recorded CPU Utilization for the day
     *
     * @return Double representing min CPU Utilization for the day
     */
    public double getMinCPUUtilization() {
        return cpuData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the average recorded Memory Utilization for the day
     *
     * @return Double representing average CPU Utilization for the day
     */
    public double getAverageMemoryUtilization() {
        return memoryData.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the maximum recorded Memory Utilization for the day
     *
     * @return Double representing max Memory Utilization for the day
     */
    public double getMaxMemoryUtilization() {
        return memoryData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the minimum recorded Memory Utilization for the day
     *
     * @return Double representing min Memory Utilization for the day
     */
    public double getMinMemoryUtilization() {
        return memoryData.stream().mapToDouble(d -> d).min().getAsDouble();
    }

    /**
     * Gets the average recorded Storage Utilization for the day
     *
     * @return Double representing average Storage Utilization for the day
     */
    public double getAverageStorageUtilization() {
        return storageData.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the maximum recorded Storage Utilization for the day
     *
     * @return Double representing max Storage Utilization for the day
     */
    public double getMaxStorageUtilization() {
        return storageData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the average recorded Storage Utilization for the day
     *
     * @return Double representing average Storage Utilization for the day
     */
    public double getMinStorageUtilization() {
        return storageData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the average recorded GSM signal strength for the day
     *
     * @return Double representing average GSM signal strength for the day
     */
    public double getAverageGSMStrength() {
        return mobileNetworkStrength.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the maximum recorded GSM signal strength for the day
     *
     * @return Double representing max GSM signal strength for the day
     */
    public double getMaxGSMStrength() {
        return mobileNetworkStrength.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the minimum recorded GSM signal strength for the day
     *
     * @return Double representing min GSM signal strength for the day
     */
    public double getMinGSMStrength() {
        return mobileNetworkStrength.stream().mapToDouble(d -> d).min().getAsDouble();
    }

    /**
     * Gets the average recorded WiFI availability for the day
     *
     * @return Double representing average WiFI availability for the day
     */
    public double getAverageWiFIAvailability() {
        return wifiConnectivityData.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the average recorded Battery level for the day
     *
     * @return Double representing average Battery level for the day
     */
    public double getAverageBatteryLevel() {
        return batteryData.stream().mapToDouble(d -> d).average().getAsDouble();
    }

    /**
     * Gets the maximum recorded Battery level for the day
     *
     * @return Double representing max Battery level for the day
     */
    public double getMaxBatteryLevel() {
        return batteryData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the minimum recorded Battery level for the day
     *
     * @return Double representing min Battery level for the day
     */
    public double getMinBatteryLevel() {
        return batteryData.stream().mapToDouble(d -> d).min().getAsDouble();
    }

    /**
     * Gets the minimum recorded latitude of the phone for the day
     *
     * @return Double representing min latitude of the phone for the day
     */
    public double getMinLatitude() {
        return latitudeData.stream().mapToDouble(d -> d).min().getAsDouble();
    }

    /**
     * Gets the minimum recorded longitude of the phone for the day
     *
     * @return Double representing min longitude of the phone for the day
     */
    public double getMinLongitude() {
        return longitudeData.stream().mapToDouble(d -> d).min().getAsDouble();
    }

    /**
     * Gets the maximum recorded latitude of the phone for the day
     *
     * @return Double representing max latitude of the phone for the day
     */
    public double getMaxLatitude() {
        return latitudeData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the maximum recorded longitude of the phone for the day
     *
     * @return Double representing max longitude of the phone for the day
     */
    public double getMaxLongitude() {
        return longitudeData.stream().mapToDouble(d -> d).max().getAsDouble();
    }

    /**
     * Gets the number of records being analyzed
     *
     * @return Integer representing the number of records being analyzed for
     * diagnostics
     */
    public int getSize() {
        return this.size;
    }

    public JSONObject getJSONRecords() {
        JSONObject parent = new JSONObject();
        JSONArray analyticData = new JSONArray();
        try {
            for (int index = 0; index < idData.size(); index++) {
                JSONObject record = new JSONObject();

                record.put("gsm_signal", mobileNetworkStrength.get(index));
                //record.put("mobilenetworkname", mobileNetworkName[index]);
                record.put("gsm_network_type_1", mobileNetworkType.get(index));
                //record.put("firstmobilenetworkconnected", mobileNetworkConnected[index]);
                record.put("wifi_connectivity", wifiConnectivityData.get(index));
                record.put("cpu_load", cpuData.get(index));
                record.put("battery_level", batteryData.get(index));
                record.put("storage_usage", storageData.get(index));
                record.put("memory_utilization", memoryData.get(index));
                record.put("gps_lat", latitudeData.get(index));
                record.put("gps_lon", longitudeData.get(index));
                record.put("record_date", dateData.get(index));
                record.put("id", idData.get(index));
                analyticData.put(record);

            }
            parent.put("analytic_data", analyticData);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return parent;
    }
}