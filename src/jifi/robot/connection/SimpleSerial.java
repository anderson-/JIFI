/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.connection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import jifi.robot.Robot;
import jifi.robot.action.Action;
import jifi.robot.action.system.GenericAction;
import jifi.robot.connection.message.Message;
import jifi.robot.device.Compass;
import jifi.robot.device.HBridge;
import jifi.robot.device.IRProximitySensor;
import jifi.robot.device.ReflectanceSensorArray;
import jifi.robot.simulation.VirtualConnection;
import jifi.util.observable.Observer;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author gnome3
 */
public class SimpleSerial implements Connection, SerialPortEventListener {

    private ArrayList<Observer<ByteBuffer, Connection>> observers;
    private int dataRate = 9600;
    private static SerialPort serialPort;
    private String defaultPort = null;
    private static int sentPackages = 0;
    private static int receivedPackages = 0;

    /*
     * Read buffers
     */
    private byte buffer[] = new byte[128];
    private int bufferIndex;
    private int bufferLast;

    private int bufferSize = 1;  // how big before reset or event firing

    private boolean newMessage = true;
    private Queue<byte[]> messages;
    private static boolean SERIAL_DEBUG_ROBO_F = false;
    private static boolean DEBUG = false;

    public SimpleSerial(int dataRate) {
        observers = new ArrayList<>();
        this.dataRate = dataRate;
        messages = new LinkedList<byte[]>();
    }

    public int getSendedPackages() {
        return sentPackages;
    }

    public int getReceivedPackages() {
        return receivedPackages;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(String defaultPort) {
        this.defaultPort = defaultPort;
    }

    public boolean tryConnect(String port) {
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(dataRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
            serialPort.addEventListener(this);
            return true;
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Collection<String> getAvailableDevices() {
        return Arrays.asList(SerialPortList.getPortNames());
    }

    @Override
    synchronized public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {//If data is available
            /* Se a funcao retornar antes de ler toda a mensagem, o evento so sera
             * disparado apos 20ms.
             */
            try {
                while ((bufferLast - bufferIndex) < bufferSize) {//input.available() > 0) {
                    synchronized (buffer) {
                        if (bufferLast == buffer.length) {
                            byte temp[] = new byte[bufferLast << 1];
                            System.arraycopy(buffer, 0, temp, 0, bufferLast);
                            buffer = temp;
                        }
                        if (event.getEventValue() > 0) {
                            buffer[bufferLast++] = (byte) serialPort.readBytes(1)[0];
                        }
                        if ((bufferLast - bufferIndex) >= bufferSize) {
                            if (messageHandler()) {
                                break;
                            }
                        }
                    }
                }
            } catch (SerialPortException ex) {
                System.out.println("serialEvent: " + ex.getMessage());
            }
        } else if (event.isCTS()) {//If CTS line has changed state
            if (event.getEventValue() == 1) {//If line is ON
                System.out.println("CTS - ON");
            } else {
                System.out.println("CTS - OFF");
            }
        } else if (event.isDSR()) {///If DSR line has changed state
            if (event.getEventValue() == 1) {//If line is ON
                System.out.println("DSR - ON");
            } else {
                System.out.println("DSR - OFF");
            }
        }
    }

    public boolean messageHandler() {
        if (newMessage) {
            bufferSize = read() + 2; // TODO: REMOVER + 2
            newMessage = false;
            //System.out.println("\t1: " + System.currentTimeMillis());
        }
//        System.out.println("Available: " + (bufferLast - bufferIndex));
        if ((bufferLast - bufferIndex) >= bufferSize) {
            if (!observers.isEmpty()) {
                ByteBuffer message = ByteBuffer.allocate(bufferSize + 1); // TODO: REMOVER +1
                message.put((byte) bufferSize); // TODO: REMOVER
                readBytes(message);
                if (SERIAL_DEBUG_ROBO_F) {
                    removeBytesFromStart(message, 1);
                }
                message.flip();
                for (Observer<ByteBuffer, Connection> o : observers) {
                    try {
                        o.update(message.asReadOnlyBuffer(), this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    printBytes(message.array(), message.array().length);
                    if (DEBUG) {
                        System.out.print("RECEBIDO.: ");
                        printBytes(message.array(), message.array().length);
                    }
                }
            } else {
                byte[] message = new byte[bufferSize];
                readBytes(message);
                synchronized (messages) {
                    messages.add(message);
                    if (DEBUG) {
                        System.out.print("RECEBIDO: ");
                        printBytes(message, message.length);
                    }
                }
            }
            receivedPackages++;
            newMessage = true;
            bufferSize = 1;
            return true;
            //System.out.println("\t2: " + System.currentTimeMillis());
        }
        return false;
    }

    public void removeBytesFromStart(ByteBuffer bf, int n) {
        int index = 0;
        for (int i = n; i < bf.position(); i++) {
            bf.put(index++, bf.get(i));
            bf.put(i, (byte) 0);
        }
        bf.position(index);
    }

    public void buffer(int count) {
        bufferSize = count;
    }

    public void clear() {
        bufferLast = 0;
        bufferIndex = 0;
    }

    public int read() {
        if (bufferIndex == bufferLast) {
            return -1;
        }

        synchronized (buffer) {
            int outgoing = buffer[bufferIndex++] & 0xff;
            if (bufferIndex == bufferLast) {  // rewind
                bufferIndex = 0;
                bufferLast = 0;
            }
            return outgoing;
        }
    }

    public byte[] readBytes() {
        if (bufferIndex == bufferLast) {
            return null;
        }

        synchronized (buffer) {
            int length = bufferLast - bufferIndex;
            byte outgoing[] = new byte[length];
            System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

            bufferIndex = 0;  // rewind
            bufferLast = 0;
            return outgoing;
        }
    }

    public int readBytes(byte outgoing[]) {
        if (bufferIndex == bufferLast) {
            return 0;
        }

        synchronized (buffer) {
            int length = bufferLast - bufferIndex;
            if (length > outgoing.length) {
                length = outgoing.length;
            }
            System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0;  // rewind
                bufferLast = 0;
            }
            return length;
        }
    }

    public int readBytes(ByteBuffer outgoing) {
        if (bufferIndex == bufferLast) {
            return 0;
        }

        synchronized (buffer) {
            int length = bufferLast - bufferIndex;
            if (length > outgoing.capacity()) {
                length = outgoing.capacity();
            }
            outgoing.put(buffer, bufferIndex, length);

            bufferIndex += length;
            if (bufferIndex == bufferLast) {
                bufferIndex = 0;  // rewind
                bufferLast = 0;
            }
            return length;
        }
    }

    @Override
    public void send(byte[] data) {
        try {
            byte[] msg = new byte[data.length + 1];
            msg[0] = (byte) data.length;
            System.arraycopy(data, 0, msg, 1, data.length);
            if (DEBUG) {
                System.out.print("ENVIADO: ");
                printBytes(msg, msg.length);
            }
            serialPort.writeBytes(msg);
            sentPackages++;
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void send(ByteBuffer data) {
        int length = data.remaining();
        byte[] msg = new byte[length];
        data.get(msg);
        send(msg);
    }

    @Override
    public boolean available() {
        return (!messages.isEmpty());
    }

    @Override
    public int receive(byte[] b, int size) {
        synchronized (messages) {
            if (!messages.isEmpty()) {
                byte[] lastMessage = messages.poll();
                int length = lastMessage.length;
                if (length > size) {
                    length = size;
                }
                System.arraycopy(lastMessage, 0, b, 0, length);
                return length;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean establishConnection() {
        boolean isConnected;
        if (defaultPort == null) {
            System.out.println("No definido a porta");
            for (String device : getAvailableDevices()) {
                isConnected = tryConnect(device);

                if (isConnected) {
                    System.out.println(device);
                    try {
                        Thread.sleep(2000); //espera 2s para a conexão funcionar
                    } catch (InterruptedException ex) {
                    }

                    return true;
                }
            }
        } else {
            isConnected = tryConnect(defaultPort);

            if (isConnected) {
                try {
                    Thread.sleep(2000); //espera 2s para a conexão funcionar
                } catch (InterruptedException ex) {
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public void closeConnection() {
        try {
            serialPort.closePort();//Close serial port
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        if (serialPort != null) {
            return serialPort.isOpened();
        }
        return false;
    }

    @Override
    public void attach(Observer<ByteBuffer, Connection> observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer<ByteBuffer, Connection> observer) {
        observers.remove(observer);
    }

    public static void printBytes(String str) {
        byte[] array = str.getBytes();
        printBytes(array, array.length);
    }

    public static void printBytes(byte[] array, int size) {
        System.out.print("[" + size + "]{");
        for (int i = 0; i < size; i++) {
            System.out.print("," + (int) (array[i] & 0xff));
        }
        System.out.println("}");
    }

    public static void main(String[] args) {
        SimpleSerial s = new SimpleSerial(57600);
        //SERIAL_DEBUG_ROBO_F = true;
        DEBUG = true;

        Robot r = new Robot();
        r.add(new HBridge());
        r.add(new Compass());
        r.add(new IRProximitySensor());
        r.add(new ReflectanceSensorArray());
        s.attach(r);

        Message.setConnection(s);

        ArrayList<byte[]> testMessages = new ArrayList<>();

        /* PRIMEIROS TESTES */
//        testMessages.add(new byte[]{2, 11, 3, 9, 'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', '\n'});
//        
//        testMessages.add(new byte[]{3, 0, 10, 'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', '2', '\n'});
//        testMessages.add(new byte[]{4, 0, 0});//get clock
//        testMessages.add(new byte[]{4, 1, 0});//get hbridge
//        testMessages.add(new byte[]{4, 2, 0});//get compass
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{5, 1, 2, 0, 90, 5, 1, 2, 1, -90}); //rotaciona
//        testMessages.add(new byte[]{5, 1, 2, 0, (byte) 0, 5, 1, 2, 1, (byte) 0}); //para
//        testMessages.add(new byte[]{5, 1, 2, 0, -90, 5, 1, 2, 1, 90}); //rotaciona
//        testMessages.add(new byte[]{5, 1, 2, 0, (byte) 0, 5, 1, 2, 1, (byte) 0}); //para
        /* ROBO GENERICO */
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        for (byte b = 0; b < 5; b++) { //adiciona 5 leds nos pinos 9->13
//            testMessages.add(new byte[]{6, 1, 1, (byte) (b + 9)}); //o array de led começa no pino 9
//            testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//            for (int i = 0; i < 2; i++) {
//                testMessages.add(new byte[]{4, (byte) (b + 1), 0}); //get status LED b+1 (0 = clock)
//                testMessages.add(new byte[]{5, (byte) (b + 1), 1, (byte) 255}); //set LED b+1 ON
//                testMessages.add(new byte[]{4, (byte) (b + 1), 0}); //get status LED b+1 (0 = clock)
//                testMessages.add(new byte[]{5, (byte) (b + 1), 1, 0}); //set LED b+1 OFF
//            }
//        }
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
        /*
         * Resultados (bytes):
         *  Arduino MEGA (8k):
         *   FreeRam: 6977 - apenas arduino+lib+serial
         *   FreeRam: 6954 - +1 led
         *   FreeRam: 6929 - +1 led
         *  Arduino 2009 (2k):
         *   FreeRam: 1299 - apenas arduino+lib+serial
         *   FreeRam: 1272 - +1 led
         *   FreeRam: 1243 - +1 led
         */

        /* NOVAS FUNÇÕES DE GIRAR */
//        ByteBuffer bf = ByteBuffer.allocate(8);
//        bf.putChar((char) 180);
//        byte[] tmp = new byte[2];
//        bf.flip();
//        bf.order(ByteOrder.LITTLE_ENDIAN);
//        bf.get(tmp);
//
//        testMessages.add(new byte[]{4, 0, 0});//get clock
//
//        testMessages.add(new byte[]{9, 0, 2, 1, 2, 3, tmp[1], tmp[0], 10});

        /* RESETA AS FUNÇÕES E PONTE H */
//        testMessages.add(new byte[]{7, (byte) 222});//reset all

        /* RESETA O SISTEMA (funcionando) */
//        testMessages.add(new byte[]{7, (byte) 224});//reset system
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{6, 5, 1, 17});//add dist
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{7, (byte) 224});//reset system
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{6, 5, 1, 17});//add dist
//        
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//
//        for (int i = 0; i < 40; i++) {
//            testMessages.add(new byte[]{6, 5, 1, 17});//add dist
//        }

        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
        
        testMessages.add(new byte[]{4, (byte) 222, 0});//get device list
        
        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam

        testMessages.add(new byte[]{7, (byte) 222});//reset all

        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
        
        testMessages.add(new byte[]{4, (byte) 222, 0});//get device list
        
        testMessages.add(new byte[]{6, 1, 1, 2});//add LED
        
        testMessages.add(new byte[]{5, 5, 1, 1});//LED ON
        
        testMessages.add(new byte[]{4, (byte) 222, 0});//get device list
        
        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
        
        testMessages.add(new byte[]{5, 5, 1, 0});//LED OFF
//
//        for (int i = 0; i < 60; i++) {
//            testMessages.add(new byte[]{6, 5, 1, 17});//add dist
//        }
//
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//
//        for (int i = 0; i < 10; i++) {
//            testMessages.add(new byte[]{6, (byte) 222, 1, 0});//remove device
//        }
//
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam

//        testMessages.add(new byte[]{6, 1, 1, 2});//add LED
//        testMessages.add(new byte[]{5, 5, 1, 1});//LED ON
//        testMessages.add(new byte[]{5, 5, 1, 0});//LED OFF
//        testMessages.add(new byte[]{5, 5, 1, 1});//LED ON
//        testMessages.add(new byte[]{5, 5, 1, 0});//LED OFF
//        testMessages.add(new byte[]{5, 5, 1, 1});//LED ON
//        testMessages.add(new byte[]{5, 5, 1, 0});//LED OFF
        if (s.establishConnection()) {
            System.out.println("connected");
//            s.send(new byte[]{2, 1, 20});
//            try {
//                Thread.sleep(20);
//            } catch (InterruptedException ex) {
//            }
//            s.send(new byte[]{2, 1, 30});
//            try {
//                Thread.sleep(20);
//            } catch (InterruptedException ex) {
//            }
//
//            for (int i = 0; i < 10; i++) {
//                System.out.println("------------------------");
//                int rp = receivedPackages;
//                System.out.println(sentPackages + ":" + receivedPackages);
//                s.send(new byte[]{2, 1, (byte) i});
//                long t = System.currentTimeMillis();
//                while (receivedPackages <= rp && (System.currentTimeMillis() - t) < 200) {
//                    try {
//                        int w = serialPort.getInputBufferBytesCount();
////                            if (w > 0) {
////                                System.out.println(w + " " + (System.currentTimeMillis() - t));
////                            }
//                    } catch (SerialPortException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//                System.out.println(sentPackages + ":(" + rp + ")" + receivedPackages);
//                System.out.println("received after: " + (System.currentTimeMillis() - t) + "ms");
//
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ex) {
//                }
//            }
//
//            System.exit(0);

            for (int i = 0; i < 1; i++) { //repetição
                for (byte[] message : testMessages) {
//                    GenericAction ga = new GenericAction(true, 0, message);
//                    
//                    ga.begin(r);
//                    Action.run(ga, r);
//
                    s.send(message);
                    int rp = receivedPackages;
//                    System.out.println(sentPackages + ":" + receivedPackages);
                    long t = System.currentTimeMillis();
                    while (receivedPackages <= rp && (System.currentTimeMillis() - t) < 200) {
                        try {
                            int w = serialPort.getInputBufferBytesCount();
//                            if (w > 0) {
//                                System.out.println(w + " " + (System.currentTimeMillis() - t));
//                            }
                        } catch (SerialPortException ex) {
                            ex.printStackTrace();
                        }
                    }
//                    System.out.println(sentPackages + ":(" + rp + ")" + receivedPackages);
//                    System.out.println("received after: " + (System.currentTimeMillis() - t) + "ms");

//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException ex) {
//                    }
                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                }
            }
        } else {
            System.out.println("fail");
        }

        System.out.println("Fim");
        System.exit(0);
    }

}
