package org.rootio.tools.diagnostics;

import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import oshi.SystemInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiagnosticAgent {

    private SystemInfo sysInfo;
    private float wifiStrength;
    private float batteryLevel;
    private boolean isConnectedToMobileNetwork;
    private int mobileSignalStrength;
    private float memoryStatus;
    private float CPUUtilization;
    private double latitude;
    private double longitude;
    private double storageStatus;
    private String telecomOperatorName, mobileNetworkType;
    private BroadcastReceiver br;


    public DiagnosticAgent() {
        this.sysInfo = new SystemInfo();
        this.listenForNetworkInformation();
    }

    /**
     * Runs the checks for the various defined diagnostics
     */
    public void runDiagnostics() {
        this.loadWifiStrength();
        this.loadBatteryLevel();
        this.loadMemoryStatus();
        this.loadStorageUtilization();
        this.loadCPUutilization();
        this.loadPhoneNetworkProperties();
    }

    /**
     * Loads the wiFI connectivity status
     */
    private void loadWifiStrength() { //debian only. please extend as necessary
        try {
            Process proc = Runtime.getRuntime().exec("iw wlan0 link | grep signal");
            char[] result = new char[100];
            try (InputStreamReader rdr = new InputStreamReader(proc.getInputStream())) {
                rdr.read(result);
            }
            String response = new String(result);
            wifiStrength = Float.parseFloat(response.split(" ")[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | IOException ex) {
            wifiStrength = 0;
        }
    }


        /**
         * Loads the battery level of the phone
         */
        private void loadBatteryLevel () {
            batteryLevel = 0;
            Arrays.stream(sysInfo.getHardware().getPowerSources()).forEach(powerSource -> {
                if (powerSource.getName().toLowerCase().contains("battery")) {
                    batteryLevel = 100f * (float) powerSource.getRemainingCapacity();
                }
            });
        }

        /**
         * Loads the percentage memory utilization of the phone
         */
        private void loadMemoryStatus () {
            memoryStatus = 100f * (1f - (float) sysInfo.getHardware().getMemory().getAvailable() / (float) sysInfo.getHardware().getMemory().getTotal());
        }

        /**
         * Loads the percentage CPU Utilization of the phone
         */
        private void loadCPUutilization () {
            this.CPUUtilization = 100f * (float) sysInfo.getHardware().getProcessor().getSystemCpuLoad();
        }


        /**
         * Loads the percentage Utilization of the phone storage
         */
        private void loadStorageUtilization () {
            AtomicLong free = new AtomicLong(0);
            AtomicLong total = new AtomicLong(0);
            Arrays.stream(File.listRoots()).forEach(l -> {
                free.addAndGet(l.getFreeSpace());
                total.addAndGet(l.getTotalSpace());
            });
            this.storageStatus = 100 * (1f - (float) free.get() / (float) total.get());
        }

        private void loadPhoneNetworkProperties () {
            MessageRouter.getInstance().specicast(new Message("name", "network", new HashMap<>()), "org.rootio.phone.MODEM");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MessageRouter.getInstance().specicast(new Message("type", "network", new HashMap<>()), "org.rootio.phone.MODEM");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MessageRouter.getInstance().specicast(new Message("strength", "network", new HashMap<>()), "org.rootio.phone.MODEM");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MessageRouter.getInstance().specicast(new Message("read", "gps", new HashMap<>()), "org.rootio.phone.MODEM");
        }

        private void listenForNetworkInformation () {
            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Message m) {
                    try {
                        if (m.getCategory().equals("gps")) {
                            DiagnosticAgent.this.latitude = ddmmToDec((String) m.getPayLoad().get("latitude"), (String) m.getPayLoad().get("latitude_direction"));
                            DiagnosticAgent.this.longitude = ddmmToDec((String) m.getPayLoad().get("longitude"), (String) m.getPayLoad().get("longitude_direction"));
                        } else if (m.getCategory().equals("network")) {
                            switch (m.getEvent()) {
                                case "type":
                                    switch ((String) m.getPayLoad().get("network_type")) {
                                        case "0":
                                            mobileNetworkType = "no service";
                                            break;
                                        case "1":
                                            mobileNetworkType = "GSM";
                                            break;
                                        case "2":
                                            mobileNetworkType = "GPRS";
                                            break;
                                        case "3":
                                            mobileNetworkType = "EGPRS (EDGE)";
                                            break;
                                        case "4":
                                            mobileNetworkType = "WCDMA";
                                            break;
                                        case "5":
                                            mobileNetworkType = "HSDPA only(WCDMA)";
                                            break;
                                        case "6":
                                            mobileNetworkType = "HSUPA only(WCDMA)";
                                            break;
                                        case "7":
                                            mobileNetworkType = "HSPA (HSDPA and HSUPA, WCDMA)";
                                            break;
                                        case "8":
                                            mobileNetworkType = "LTE";
                                            break;
                                        case "9":
                                            mobileNetworkType = "TDS-CDMA";
                                            break;
                                        case "10":
                                            mobileNetworkType = "TDS-HSDPA only";
                                            break;
                                        case "11":
                                            mobileNetworkType = "TDS- HSUPA only";
                                            break;
                                        case "12":
                                            mobileNetworkType = "TDS- HSPA (HSDPA and HSUPA)";
                                            break;
                                        case "13":
                                            mobileNetworkType = "CDMA";
                                            break;
                                        case "14":
                                            mobileNetworkType = "EVDO";
                                            break;
                                        case "15":
                                            mobileNetworkType = "HYBRID (CDMA and EVDO)";
                                            break;
                                        case "16":
                                            mobileNetworkType = "1XLTE(CDMA and LTE)";
                                            break;
                                    }
                                    break;
                                case "strength":
                                    String signal = (String) m.getPayLoad().get("network_strength");
                                    DiagnosticAgent.this.mobileSignalStrength = (Integer.parseInt(signal));
                                    break;
                                case "name":
                                    DiagnosticAgent.this.telecomOperatorName = (String) m.getPayLoad().get("network_name");
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[DiagnosticsRunner.run]" : e.getMessage());
                    }
                }

            };
            MessageRouter.getInstance().register(br, "org.rootio.telephony.NETWORK");
            MessageRouter.getInstance().register(br, "org.rootio.telephony.GPS");

        }

        private float ddmmToDec (String ddmmLocation, String direction){
            try {
                float degrees = Float.parseFloat(ddmmLocation.substring(0, ddmmLocation.indexOf(".") - 2));
                float minutes = Float.parseFloat(ddmmLocation.substring(ddmmLocation.indexOf(".") - 2));
                degrees += minutes / 60F;
                return Arrays.asList("s", "S", "W", "w").contains(direction) ? -degrees : degrees;
            } catch (Exception e) {
                return 0f;
            }
        }

        /**
         * Gets the name of the telecom operator to which the phone is latched
         *
         * @return Name of the telecom operator
         */
        public String getTelecomOperatorName () {
            return this.telecomOperatorName;
        }

        /**
         * Gets whether or not the phone is connected to WiFI
         *
         * @return Boolean indicating connectivity. True: Connected, False: Not
         * connected
         */
        public float getWifiStrength () {

            return this.wifiStrength;
        }

        /**
         * Gets whether or not the phone is latched onto a GSM network
         *
         * @return Boolean indicating GSM connection strength. True: Connected,
         * False: Not connected
         */
        public boolean isConnectedToMobileNetwork () {
            return this.isConnectedToMobileNetwork;
        }

        /**
         * Gets the signal strength of the GSM network
         *
         * @return GSM strength in decibels
         */
        public int getMobileSignalStrength () {
            return this.mobileSignalStrength;
        }

        /**
         * Gets memory utilization
         *
         * @return Percentage memory Utilization
         */
        public float getMemoryStatus () {
            return this.memoryStatus;
        }

        /**
         * Gets the storage status of the phone
         *
         * @return Percentage storage Utilization
         */
        public double getStorageStatus () {
            return this.storageStatus;
        }

        /**
         * Gets the CPU utilization of the phone
         *
         * @return Percentage CPU Utilization of the phone
         */
        public float getCPUUtilization () {
            return this.CPUUtilization;
        }

        /**
         * Gets the battery level of the phone
         *
         * @return Percentage battery utilization of the phone
         */
        public float getBatteryLevel () {
            return this.batteryLevel;
        }

        /**
         * Gets the latitude of the GPS position of the phone
         *
         * @return Latitude of the phone
         */
        public double getLatitude () {
            return this.latitude;
        }

        /**
         * Gets the longitude of the GPS position of the phone
         *
         * @return Longitude of the phone
         */

        public double getLongitude () {
            return this.longitude;
        }

        public String getMobileNetworkType () {
            return this.mobileNetworkType;
        }


    }
