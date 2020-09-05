package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.configuration.Configuration;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class StationHandler implements SynchronizationHandler {

    public StationHandler() {

    }

    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule information
     *
     * @param synchronizationResponse The response returned from the cloud server
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        boolean isConfigChanged = false;
        //SIP settings
        try {
            JSONObject station = synchronizationResponse.getJSONObject("station");
            JSONObject sip = station.getJSONObject("sip_settings");
            isConfigChanged |= !Configuration.getProperty("sip_username").equals(sip.getString("sip_username"));
            Configuration.setProperty("sip_username", sip.getString("sip_username"));
            isConfigChanged |= !Configuration.getProperty("sip_password").equals(sip.getString("sip_password"));
            Configuration.setProperty("sip_password", sip.getString("sip_password"));
            isConfigChanged |= !Configuration.getProperty("sip_server").equals(sip.getString("sip_domain"));
            Configuration.setProperty("sip_server", sip.getString("sip_domain"));
            isConfigChanged |= !Configuration.getProperty("sip_stun_server").equals(sip.getString("sip_stun"));
            Configuration.setProperty("sip_stun_server", sip.getString("sip_stun"));
            isConfigChanged |= !Configuration.getProperty("sip_reregister_period").equals(String.valueOf(sip.getInt("sip_reregister_period")));
            Configuration.setProperty("sip_reregister_period", String.valueOf(sip.getInt("sip_reregister_period")));
            isConfigChanged |= !Configuration.getProperty("sip_port").equals(String.valueOf(sip.getInt("sip_port")));
            Configuration.setProperty("sip_port", String.valueOf(sip.getInt("sip_port")));
            isConfigChanged |= !Configuration.getProperty("sip_protocol").equals(sip.getString("sip_protocol"));
            Configuration.setProperty("sip_protocol", sip.getString("sip_protocol"));

            //sound settings
            Configuration.setProperty("media_volume", String.valueOf(station.getInt("media_volume")));
            Configuration.setProperty("name", station.getString("name"));
            Configuration.setProperty("jingle_interval", String.valueOf(station.getInt("jingle_interval")));
            Configuration.setProperty("strict_scheduling", String.valueOf(station.getBoolean("strict_scheduling")));
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[StationHandler.processJSONResponse]" : e.getMessage());
        }


        if (isConfigChanged) {
            try {
                Configuration.saveChanges();
                this.announceSIPChange();
            } catch (IOException e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[StationHandler.processJSONResponse]" : e.getMessage());
            }
        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/information?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }

    private void announceSIPChange() {
        Message m = new Message("change", "configuration",new HashMap<>());
        MessageRouter.getInstance().specicast(m, "org.rootio.service.sip.CONFIGURATION");
    }

}
