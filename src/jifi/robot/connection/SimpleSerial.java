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
    private SerialPort serialPort;
    private String defaultPort = null;
    private int sentPackages = 0;
    private int receivedPackages = 0;

    /*
     * Read buffers
     */
    private byte buffer[] = new byte[128];
    private int bufferIndex;
    private int bufferLast;

    private int bufferSize = 1;  // how big before reset or event firing

    private boolean newMessage = true;
    private Queue<byte[]> messages;

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
            serialPort.writeBytes("This is a test string".getBytes());//Write data to port
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
        //System.out.println("Available: " + (bufferLast - bufferIndex));
        if ((bufferLast - bufferIndex) >= bufferSize) {
            if (!observers.isEmpty()) {
                ByteBuffer message = ByteBuffer.allocate(bufferSize + 1); // TODO: REMOVER +1
                message.put((byte) bufferSize); // TODO: REMOVER
                readBytes(message);
                message.flip();
                for (Observer<ByteBuffer, Connection> o : observers) {
                    o.update(message.asReadOnlyBuffer(), this);
                }
            } else {
                byte[] message = new byte[bufferSize];
                readBytes(message);
                synchronized (messages) {
                    messages.add(message);
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
            serialPort.writeBytes(data);
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
                        Thread.sleep(1000); //espera 1s para a conexão funcionar
                    } catch (InterruptedException ex) {
                    }

                    return true;
                }
            }
        } else {
            isConnected = tryConnect(defaultPort);

            if (isConnected) {
                try {
                    Thread.sleep(1000); //espera 1s para a conexão funcionar
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
        //System.out.print("Received: ");
        System.out.print("[" + size + "]{");
        for (int i = 0; i < size; i++) {
            System.out.print("," + (int) (array[i] & 0xff));
        }
        System.out.println("}");
    }

}
