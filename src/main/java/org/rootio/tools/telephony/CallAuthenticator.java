package org.rootio.tools.telephony;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CallAuthenticator {

    private JSONObject whiteList;

    public CallAuthenticator() {
        try {
            this.whiteList = new JSONObject(); //(String) Utils.getPreference("whitelist", String.class, this.parent));
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[CallAuthenticator.CallAuthenticator]" : e.getMessage());
        }
    }

    public boolean isWhiteListed(String phoneNumber) {
        HashSet<String> pool = new HashSet<>();
        try {
            String sanitizedPhoneNumber = this.sanitizePhoneNumber(phoneNumber);
            for(int i = 0; i < this.whiteList.getJSONArray("whitelist").length(); i++)
            {
                String sanitiziedWlNum = this.sanitizePhoneNumber(this.whiteList.getJSONArray("whitelist").getString(i));
                if(sanitizedPhoneNumber.substring(sanitizedPhoneNumber.length()>=7?sanitizedPhoneNumber.length() - 7: 0).equals(sanitiziedWlNum.substring(sanitiziedWlNum.length()>=7?sanitiziedWlNum.length() - 7: 0)))
                {
                    return true;
                }
            }
           return this.whiteList.getJSONArray("whitelist").toString().contains(sanitizedPhoneNumber); // potentially
            // problematic
        } catch (JSONException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[CallAuthenticator.isWhiteListed]" : e.getMessage());
        } catch (NullPointerException e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[CallAuthenticator.isWhiteListed]" : e.getMessage());
        }
        return false;
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        return phoneNumber.trim().replace(" ", "");
    }
}
