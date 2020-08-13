package org.rootio.services.phone;

import gnu.io.*;
import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.services.SIP.CallState;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModemAgent {
    private final String port;
    private InputStream rdr;
    private OutputStream wri;
    private BroadcastReceiver broadcastReceiver;
    private SerialPort serialPort;

    public ModemAgent(String port) {
        this.port = port;
    }

    public void start() throws NoSuchPortException, UnsupportedCommOperationException {
        boolean isConnected;
        do {
            isConnected = this.connect(this.port);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(!isConnected);

        listenToSerial();
        this.prepareModem();
        this.listenForCommands();
    }

    private boolean connect(String portName) throws NoSuchPortException, UnsupportedCommOperationException {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
            return false;
        } else {
            CommPort commPort;
            try {
                commPort = portIdentifier.open(this.getClass().getName(), 2000);
            } catch (PortInUseException e) {
                return false;
            }

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                try {
                    rdr = serialPort.getInputStream();
                    wri = serialPort.getOutputStream();
                    return true;
                }
                catch(IOException ex)
                {
                    try
                    {
                        serialPort.close();
                    }
                    catch (Exception e)
                    {
                        Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ModemAgent.connect]" : e.getMessage());
                    }
                    return false;
                }
                finally {

                }
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
                return false;
            }
        }
    }

    private boolean listenToSerial()
    {
        try {
            serialPort.addEventListener((SerialPortEvent evt) ->
            {
                switch (evt.getEventType()) {
                    case SerialPortEvent.DATA_AVAILABLE:
                        routeEvent(readFromSerial(rdr));
                        break;
                    default:
                        break;
                }
            });
            serialPort.notifyOnDataAvailable(true);
            return true;
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String readFromSerial(InputStream instr) {
        StringBuilder bldr = new StringBuilder();
        Scanner scn = new Scanner(instr);
        while (scn.hasNextLine()) {
            String line = scn.nextLine();
            if(!line.equals("OK") &&!line.isEmpty())
            {
                bldr.append(line);
            }
        }
        System.out.println(bldr);
        return bldr.toString();
    }

    public void shutDown()
    {
        try
        {
            serialPort.close();
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ModemAgent.shutDown]" : e.getMessage());
        }

    }

    /**
     *
     */
    private void writeToSerial(String command) {
        try {
            this.wri.write(command.getBytes());
            this.wri.write("\r".getBytes());
            this.wri.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareModem() //prime the modem for our operations
    {
        //1) Turn off echo ATE0
        writeToSerial("ATE0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //2) Enable GPS AT+CGPSINFO
        writeToSerial("AT+CGPS=1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //3) Turn on display of calling number for calls AT+CLCC=1
        writeToSerial("AT+CLCC=1");


    }

    private void routeEvent(String data) {
        Message m ;
        HashMap<String, Object >payload = new HashMap<>();
        try {
             if (data.contains("+CLCC")) {
                String[] parts = data.split(",");
                String event = null;
                String status = parts[2];
                if (status.equals("4")) //ringing
                {
                    event = "ring";
                } else if (status.equals("0")) {
                    event = "answer";
                } else if (status.equals("6")) {
                    event = "hangup";
                }
                payload.put("b_party", parts[5].replace("\"", ""));
                MessageRouter.getInstance().specicast(new Message(event, "call", payload), "org.rootio.telephony.CALL");
            } else if (data.contains("CGPSINFO")) //+CGPSINFO: 3238.668540,N,01655.140663,W,110820,162229.0,20.5,0.0,196.2
            {
                String[] parts = data.substring(data.indexOf("+CGPSINFO: ") + 11).trim().split(",");
                payload.put("latitude", parts[0]);
                payload.put("latitude_direction", parts[1]);
                payload.put("longitude", parts[2]);
                payload.put("longitude_direction", parts[3]);
                m = new Message("read", "gps", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.telephony.GPS");
            } else if (data.contains("+CMTI")) {
                String messageId = data.split(",")[1].trim();
                payload.put("message_id", messageId);
                m = new Message("incoming", "sms", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.telephony.SMS");
            } else if (data.contains("+CNSMOD")) {
                String networkType = data.split(":")[1].split(",")[1];
                payload.put("network_type", networkType);
                m = new Message("type", "network", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
            } else if (data.contains("+COPS")) {
                String networkType = data.split(",")[2];
                payload.put("network_name", networkType);
                m = new Message("name", "network", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
            } else if (data.contains("+CSQ")) {
                payload.put("network_strength", data.split(":")[1].trim().split(",")[0]);
                m = new Message("strength", "network", payload);
                MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
            }
        }
        catch(Exception e)
        {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ModemAgent.routEvent]" : e.getMessage());

        }
    }

    private void listenForCommands() {
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Message m) {
                if(m.getCategory().equals("sms"))
                {
                    if(m.getEvent().equals("send"))
                    {
                        String to = (String)m.getPayLoad().get("to");
                        String message = (String)m.getPayLoad().get("message");
                        String command  = "AT+CMGS=\""+to+"\"\r"+message+"\u001A";
                        writeToSerial(command);
                    }
                    else if(m.getEvent().equals("read"))
                    {
                        String id = (String)m.getPayLoad().get("message_id");
                        String command  = "AT+CMGR="+id;
                        writeToSerial(command);
                    }
                }
                else if(m.getCategory().equals("gps"))
                {
                    if(m.getEvent().equals("read"))
                    {
                        String command = "AT+CGPSINFO";
                        writeToSerial(command);
                    }
                }
                else if(m.getCategory().equals("call"))
                {
                    if(m.getEvent().equals("answer"))
                    {
                        String command = "ATA";
                        writeToSerial(command);
                    }
                    else if(m.getEvent().equals("decline"))
                    {
                        String command = "AT+CHUP";
                        writeToSerial(command);
                    }
                    else if(m.getEvent().equals("status"))
                    {
                        String command = "AT+CLCC";
                        writeToSerial(command);
                    }
                }
                else if(m.getCategory().equals("network"))
                {
                    if(m.getEvent().equals("name"))
                    {
                        String command = "AT+COPS?";
                        writeToSerial(command);
                    }
                    else if(m.getEvent().equals("type"))
                    {
                        String command = "AT+CNSMOD?";
                        writeToSerial(command);
                    }
                    else if(m.getEvent().equals("strength"))
                    {
                        String command = "AT+CSQ";
                        writeToSerial(command);
                    }
                }
            }
        };
        MessageRouter.getInstance().register(broadcastReceiver, "org.rootio.phone.MODEM");
        MessageRouter.getInstance().register(broadcastReceiver, "org.rootio.services.phone.MODEM");
    }
}
