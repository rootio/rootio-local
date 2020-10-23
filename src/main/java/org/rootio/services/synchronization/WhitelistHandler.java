package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.tools.utils.Utils;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class WhitelistHandler implements SynchronizationHandler {

    WhitelistHandler() {
    }

    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule information
     *
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        Utils.saveJSONPreferences(synchronizationResponse, "whitelist");
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/whitelist?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getBuildName(), Configuration.getBuildNumber());
    }

}
