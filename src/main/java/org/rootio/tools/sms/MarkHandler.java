package org.rootio.tools.sms;

import org.rootio.configuration.Configuration;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * THis Class handles IDs and Timestamp that are used to synchronize calls, SMS and music to the cloud
 * resetting these ids results in a sync of records with an id/date_added greater than the supplied value
 */
class MarkHandler implements MessageProcessor {
    private final String from;
    private final String[] messageParts;

    public MarkHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length != 3) {
            return false;
        }

        switch (messageParts[1]) {
            case "call":
                return setId("call_id", messageParts[2]);
            case "sms":
                return setId("sms_id", messageParts[2]);
            case "music":
                return setId("media_max_date_added", String.valueOf(getUsefulMinDate(messageParts[2])));
            default:
                return false;
        }
    }

    long getUsefulMinDate(String dateStr) throws NumberFormatException {
        Date dt = Utils.getDateFromString(dateStr, "yyyy-MM-dd HH:mm:ss");
        if (dt == null) {
            return Long.parseLong(Configuration.getProperty("media_max_date_added"));
        } else {
            return dt.getTime() / 1000;
        }

    }

    private boolean setId(String param, String value) {
        try {
            HashMap<String, Object> values = new HashMap<>();
            String tableName = "marks";
            String updateClause = param + "= ?";
            List<String> updateArgs = Collections.singletonList(value);
            DBAgent.updateRecords(tableName, updateClause, updateArgs, null, null);
            this.respondAsyncStatusRequest(this.from, "mark " + messageParts[1] + " " + messageParts[2] + " ok");
        } catch (Exception ex) {
            this.respondAsyncStatusRequest(this.from, "mark " + messageParts[1] + " " + messageParts[2] + " fail");
            return false;
        }
        return true;
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        //send SMS back
    }
}
