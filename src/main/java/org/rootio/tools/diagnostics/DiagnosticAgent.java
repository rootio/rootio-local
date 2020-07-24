package org.rootio.tools.diagnostics;

public class DiagnosticAgent {

    private boolean isConnectedToWifi;
    private float batteryLevel;
    private boolean isConnectedToMobileNetwork;
    private int mobileSignalStrength;
    private float memoryStatus;
    private float CPUUtilization;
    private double latitude;
    private double longitude;
    private double storageStatus;
    private String telecomOperatorName, mobileNetworkType;


    public DiagnosticAgent() {
          }

    /**
     * Runs the checks for the various defined diagnostics
     */
    public void runDiagnostics() {
        this.loadSignalStrength();
        this.loadIsConnectedToMobileNetwork();
        this.loadIsConnectedToWifi();
        this.loadBatteryLevel();
        this.loadMemoryStatus();
        this.loadStorageUtilization();
        this.loadCPUutilization();
        this.loadLatitudeLongitude();
        this.loadTelecomOperatorName();
        this.loadMobileNetworkType();
     }

    private void loadMobileNetworkType() {
        this.mobileNetworkType = "";
    }

    /**
     * Loads the name of the telecom operator to which the phone is currently
     * latched
     */
    private void loadTelecomOperatorName() {
        this.telecomOperatorName = null;
    }

    /**
     * Loads the wiFI connectivity status
     */
    private void loadIsConnectedToWifi() {
        this.isConnectedToWifi = false;
    }

    /**
     * Loads the battery level of the phone
     */
    private void loadBatteryLevel() {
        batteryLevel = 0;
    }

    /**
     * Loads the mobile data connectivity status
     */
    private void loadIsConnectedToMobileNetwork() {
        this.isConnectedToMobileNetwork = false;
    }



    //TODO: actually read documentation for below method
    private void loadSignalStrength() {
        mobileSignalStrength = 0;
    }

    /**
     * Loads the percentage memory utilization of the phone
     */
    private void loadMemoryStatus() {
        memoryStatus = 0;
    }

    /**
     * Loads the percentage CPU Utilization of the phone
     */
    private void loadCPUutilization() {
        this.CPUUtilization = 0;
    }

    /**
     * Loads the GPS coordinates of the phone
     */
    private void loadLatitudeLongitude() {
            this.latitude = 0;
            this.longitude = 0;
        }

    /**
     * Loads the percentage Utilization of the phone storage
     */
    private void loadStorageUtilization() {
            this.storageStatus = 0;
    }

    /**
     * Gets the name of the telecom operator to which the phone is latched
     *
     * @return Name of the telecom operator
     */
    public String getTelecomOperatorName() {
        return this.telecomOperatorName;
    }

    /**
     * Gets whether or not the phone is connected to WiFI
     *
     * @return Boolean indicating connectivity. True: Connected, False: Not
     * connected
     */
    public boolean isConnectedToWifi() {
        return this.isConnectedToWifi;
    }

    /**
     * Gets whether or not the phone is latched onto a GSM network
     *
     * @return Boolean indicating GSM connection strength. True: Connected,
     * False: Not connected
     */
    public boolean isConnectedToMobileNetwork() {
        return this.isConnectedToMobileNetwork;
    }

    /**
     * Gets the signal strength of the GSM network
     *
     * @return GSM strength in decibels
     */
    public int getMobileSignalStrength() {
        return this.mobileSignalStrength;
    }

    /**
     * Gets memory utilization
     *
     * @return Percentage memory Utilization
     */
    public float getMemoryStatus() {
        return this.memoryStatus;
    }

    /**
     * Gets the storage status of the phone
     *
     * @return Percentage storage Utilization
     */
    public double getStorageStatus() {
        return this.storageStatus;
    }

    /**
     * Gets the CPU utilization of the phone
     *
     * @return Percentage CPU Utilization of the phone
     */
    public float getCPUUtilization() {
        return this.CPUUtilization;
    }

    /**
     * Gets the battery level of the phone
     *
     * @return Percentage battery utilization of the phone
     */
    public float getBatteryLevel() {
        return this.batteryLevel;
    }

    /**
     * Gets the latitude of the GPS position of the phone
     *
     * @return Latitude of the phone
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Gets the longitude of the GPS position of the phone
     *
     * @return Longitude of the phone
     */

    public double  getLongitude()
    {
        return this.longitude;
    }

    public String getMobileNetworkType(){
        return this.mobileNetworkType;
    }
}
