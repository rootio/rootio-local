package org.rootio.services.synchronization;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class FrequencyHandler implements SynchronizationHandler {


    FrequencyHandler() {

    }

    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    /**
     * Handles information received from the cloud server pertaining to frequency for synchronization and diagnostics
     *
     * @param synchronizationResponse The JSON info containing frequency in seconds for measuring diagnostics and communicating to the cloud
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        if (synchronizationResponse != null) {
            try {
                Configuration.setProperty("diagnostics_frequency", synchronizationResponse.getString("diagnostics_frequency"));
            Configuration.setProperty("synchronization_frequency", synchronizationResponse.getString("diagnostics_frequency"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/frequency_update?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }
}