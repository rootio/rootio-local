package org.rootio.tools.logging;

import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.util.HashMap;

public class Logger extends BroadcastReceiver {


    private long log(LogRecord record)
    {
        try {
            HashMap<String, Object> values = new HashMap();
            values.put("category", record.getCategory());
            values.put("argument", record.getArgument());
            values.put("event", record.getEvent());
            values.put("eventdate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
            return DBAgent.saveData("activitylog", values);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onReceive(Message m) {
        this.log(new LogRecord(m.getCategory(), m.getArgument(), m.getEvent()));
    }

    public static class LogRecord
    {
        private final String category, argument, event;

        public LogRecord(String category, String argument, String event)
        {
            this.category = category;
            this.argument = argument;
            this.event = event;
        }

        public String getCategory() {
            return category;
        }

        public String getArgument() {
            return argument;
        }

        public String getEvent() {
            return event;
        }
    }
}


