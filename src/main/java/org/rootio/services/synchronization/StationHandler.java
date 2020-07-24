package org.rootio.services.synchronization;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.configuration.Configuration;

import java.util.Iterator;

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
        var ref = new Object() {
            boolean isConfigChanged = false;
        };
        Iterator<String> iter = synchronizationResponse.keys();
        iter.forEachRemaining(entry -> {
            try {
                ref.isConfigChanged = ref.isConfigChanged || Configuration.setProperty(entry, synchronizationResponse.getString(iter.next()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        if(ref.isConfigChanged)
        {
            this.announceSIPChange();
        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/information?api_key=%s&version=%s_%s", Configuration.getProperty("server_scheme"), Configuration.getProperty("server_address"), Configuration.getProperty("http_port"), "api/station", Configuration.getProperty("station_id"), Configuration.getProperty("server_key"), Configuration.getProperty("build_version"), Configuration.getProperty("build_version"));
    }

    private void announceSIPChange()
    {
//        //This is lazy -- any station change will result in re-registration. Ideal is to extract SIP components and compare them
//        try {
//               Intent intent = new Intent("org.rootio.handset.SIP.CONFIGURATION_CHANGE");
//                this.parent.sendBroadcast(intent);
//        }
//        catch(Exception ex){
//            //todo: log this
//        }
    }

}
