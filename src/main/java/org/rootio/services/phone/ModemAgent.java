package org.rootio.services.phone;

import gnu.io.*;
import org.rootio.messaging.BroadcastReceiver;
import org.rootio.messaging.Message;
import org.rootio.messaging.MessageRouter;
import org.rootio.tools.utils.EventAction;
import org.rootio.tools.utils.EventCategory;
import org.rootio.tools.utils.Utils;

import java.io.*;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModemAgent {
    private final String port;
    private InputStream in;
    private OutputStream out;
    private BroadcastReceiver broadcastReceiver;
    private SerialPort serialPort;

    public ModemAgent(String port) {
        this.port = port;
    }

    public void start() {
        boolean isConnected = false;
        do {
            try {
                isConnected = this.connect(this.port, 115200);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

        }
        while (!isConnected);

        this.prepareModem();
        this.listenForCommands();
    }

    boolean connect(String portName, int rate) throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
            return false;
        } else {

            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.disableReceiveTimeout();
            serialPort.enableReceiveThreshold(1);
            serialPort.setSerialPortParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            try {
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serialPort.close();
                } catch (Exception ex) {

                }
                return false;
            }

            try {
                serialPort.addEventListener((SerialPortEvent evt) ->
                {
                    switch (evt.getEventType()) {
                        case SerialPortEvent.DATA_AVAILABLE:
                            readFromSerial(in);
                            break;
                        default:
                            break;
                    }
                });
            } catch (TooManyListenersException e) {
                e.printStackTrace();
                try {
                    serialPort.close();
                } catch (Exception ex) {

                }
                return false;
            }
            serialPort.notifyOnDataAvailable(true);
            return true;
        }
    }

    private void readFromSerial(InputStream instr) {
        try {
            Scanner scn = new Scanner(instr);
            while (scn.hasNextLine()) {
                String line = scn.nextLine();
                if (!line.equals("OK") && !line.isEmpty()) {
                    new Thread(() -> routeEvent(line)).start();
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        try {
            serialPort.close();
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ModemAgent.shutDown]" : e.getMessage());
        }

    }

    /**
     *
     */
    private void writeToSerial(String command) {
        try {
            this.out.write(command.getBytes());
            this.out.write(("\r").getBytes());
            this.out.flush();
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
        Message m;
        HashMap<String, Object> payload = new HashMap<>();
        try {
            synchronized (this) {
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
                    String bParty = parts[5].replace("\"", "");
                    Utils.logEvent(EventCategory.CALL, EventAction.START, event + ": " + bParty);
                    payload.put("b_party", bParty);
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
                    //MessageRouter.getInstance().specicast(m, "org.rootio.telephony.SMS");
                } else if (data.contains("+CMGL")) {
                    String from = data.split(",")[2].replace("\"", "");
                    String id = data.split(",")[0].replaceAll("[^0-9]", "");
                    String dateReceived = data.split(",")[4].replace("\"", "") + " " + data.split(",")[5].replace("\"", "");

                    payload.put("from", from);
                    payload.put("id", id);
                    payload.put("date_received", dateReceived);
                    m = new Message("header", "sms", payload);
                    MessageRouter.getInstance().specicast(m, "org.rootio.telephony.SMS");
                } else if (data.contains("+CNSMOD")) {
                    String networkType = data.split(":")[1].split(",")[1];
                    payload.put("network_type", networkType);
                    m = new Message("type", "network", payload);
                    MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
                } else if (data.contains("+COPS")) {
                    String networkName = data.split(",")[2];
                    payload.put("network_name", networkName);
                    m = new Message("name", "network", payload);
                    MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
                } else if (data.contains("+CSQ")) {
                    payload.put("network_strength", data.split(":")[1].trim().split(",")[0]);
                    m = new Message("strength", "network", payload);
                    MessageRouter.getInstance().specicast(m, "org.rootio.telephony.NETWORK");
                } //Special SMS commands, written on their own lines, making SMS a pain to deal with.
                else if (data.startsWith("network") || data.startsWith("ussd") || data.startsWith("services") || data.startsWith("station") ||
                        data.startsWith("resources") || data.startsWith("sound") || data.startsWith("whitelist") || data.startsWith("mark") ||
                        !((data.startsWith("+") || data.startsWith("ERROR") || data.startsWith("OK") ||data.startsWith(">") || data.startsWith("ATE0"))))
                {
                    payload.put("body", data);
                    m = new Message("body", "sms", payload);
                    MessageRouter.getInstance().specicast(m, "org.rootio.telephony.SMS");
                }
            }
        } catch (Exception e) {
            Logger.getLogger("RootIO").log(Level.WARNING, e.getMessage() == null ? "Null pointer[ModemAgent.routEvent]" : e.getMessage());

        }
    }


    private void listenForCommands() {
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Message m) {
                if (m.getCategory().equals("sms")) {
                    if (m.getEvent().equals("send")) {
                        String to = (String) m.getPayLoad().get("to");
                        String message = (String) m.getPayLoad().get("message");
                        String command = "AT+CMGS=\"" + to + "\"\r\n";
                        writeToSerial(command);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        writeToSerial(message + "\u001A");
                    } else if (m.getEvent().equals("read")) {
                        String command = "AT+CMGL=\"ALL\"";
                        writeToSerial(command);
                    } else if (m.getEvent().equals("delete")) {
                        String command = "AT+CMGD=" + m.getPayLoad().get("id");
                        writeToSerial(command);
                    }
                } else if (m.getCategory().equals("gps")) {
                    if (m.getEvent().equals("read")) {
                        String command = "AT+CGPSINFO";
                        writeToSerial(command);
                    }
                } else if (m.getCategory().equals("call")) {
                    if (m.getEvent().equals("answer")) {
                        String command = "ATA";
                        writeToSerial(command);
                    } else if (m.getEvent().equals("decline")) {
                        String command = "AT+CHUP";
                        writeToSerial(command);
                    } else if (m.getEvent().equals("status")) {
                        String command = "AT+CLCC";
                        writeToSerial(command);
                    }
                } else if (m.getCategory().equals("network")) {
                    if (m.getEvent().equals("name")) {
                        String command = "AT+COPS?";
                        writeToSerial(command);
                    } else if (m.getEvent().equals("type")) {
                        String command = "AT+CNSMOD?";
                        writeToSerial(command);
                    } else if (m.getEvent().equals("strength")) {
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
