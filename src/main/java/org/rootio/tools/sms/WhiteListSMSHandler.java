package org.rootio.tools.sms;

import org.rootio.tools.persistence.DBAgent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WhiteListSMSHandler implements MessageProcessor {

    private String[] messageParts;
    private final String from;

    WhiteListSMSHandler(String from, String[] messageParts) {
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public void ProcessMessage() {
        if (messageParts.length != 3) {
            return;
        }

        // adding a number
        if (messageParts[1].equals("add")) {
            try {
                boolean isNumberAdded = this.addNumberToWhitelist(messageParts[2]);
                this.respondAsyncStatusRequest(from, isNumberAdded ? String.format("The number %s was successfully added", messageParts[2]) : String.format("The number %s was not added", messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[WhiteListSMSHandler.processMessage]" : e.getMessage());
            }
        }

        // removing a number from allowed list
        if (messageParts[1].equals("remove")) {
            try {
                boolean isNumberRemoved = this.removeNumberFromWhitelist(messageParts[2]);
                this.respondAsyncStatusRequest(from, isNumberRemoved ? String.format("The number %s was successfully removed", messageParts[2]) : String.format("The number %s was not added", messageParts[2]));
            } catch (Exception e) {
                Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[WhiteListSMSHandler.processMessage]" : e.getMessage());
            }
        }
    }

    private boolean addNumberToWhitelist(String phoneNumber) {
        try {
            String tableName = "permit_list";
            HashMap<String, Object> data = new HashMap<>();
            data.put("telephone_number", phoneNumber);
            DBAgent.saveData(tableName, data);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean removeNumberFromWhitelist(String phoneNumber) {
        try {
            String tableName = "permit_list";
            String whereClause = "telephone_number = ?";
            List<String> whereArgs = Collections.singletonList(phoneNumber);
            DBAgent.deleteRecords(tableName, whereClause, whereArgs);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        // TODO Auto-generated method stub
    }

}
