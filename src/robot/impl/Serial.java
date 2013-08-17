/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import observable.Observer;
import robot.Connection;
import robot.Robot;
import gui.drawable.DRobot;

/**
 *
 * @author antunes
 */
public class Serial implements Connection, SerialPortEventListener {

    private ArrayList<Observer<ByteBuffer, Connection>> observers;
    private SerialPort serialPort;
    private boolean isConnected = false;
    private boolean available = false;
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyUSB#", // Linux
        "/dev/ttyACM#", // Linux USB 3.0
        "COM#", // Windows
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader converting the
     * bytes into characters making the displayed results codepage independent
     */
    private InputStream input;
    private BufferedReader bufferedReader;
    private ByteBuffer buffer;
    public static Charset charset = Charset.forName("ISO-8859-1");//new ByteCharset();
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Default bits per second for COM port.
     */
    private int dataRate = 9600;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    private static final int PORT_SEARCH = 10;

    public Serial(int dataRate) {
        observers = new ArrayList<>();
        this.dataRate = dataRate;
        buffer = ByteBuffer.allocate(256);
    }

    @Override
    public void send(final byte[] data) {
        try {
//            System.out.print("Sending:   ");
//            System.out.print("[" + data.length + "]{");
//            for (byte b : data) {
//                System.out.print("," + b);
//            }
//            System.out.println("}");
            output.write(data);
            output.flush();
        } catch (IOException ex) {
            System.out.println("Send fail!");
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
        return available;
    }

    @Override
    public int receive(byte[] b, int size) {
        available = false;
        if (available && buffer != null) {
            buffer.get(b);
            return buffer.position();
        } else {
            return 0;
        }
    }

    public boolean tryConnect(String port) {

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(port)) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            //Could not find COM port
            return false;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(dataRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            //teste web: http://www.devmedia.com.br/utilizando-a-api-rxtx-para-manipulacao-da-serial-parte-iii/7171
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            // open the streams
            bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), charset));
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (PortInUseException e) {
            //porta ocupada
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean establishConnection() {

        isConnected = false;

        for (String name : PORT_NAMES) {
            if (name.contains("#")) {
                for (int i = 0; i < PORT_SEARCH; i++) {
                    String device = name.replace("#", "" + i);
                    //adiciona portas ocultas e bloqueadas (Linux)
                    System.setProperty("gnu.io.rxtx.SerialPorts", device);

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
            }
        }
        return false;
    }

    @Override
    public void closeConnection() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void attach(Observer<ByteBuffer, Connection> observer) {
        observers.add(observer);
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                //define a posição como o inicio do buffer e remove a marca e o limite
                buffer.clear();
                //preenche o buffer
                while (true) {
                    //obtem uma string delimitada por '\n', '\t' ou "\t\n"
                    String str = bufferedReader.readLine();
//                    printBytes(str);
                    //obtem os bytes codificados na string com o Charset personalizado
                    buffer.put(str.getBytes(charset));
                    if (bufferedReader.ready()) {
                        //não remove os valores 10 presentes no meio da mensagem
                        buffer.put((byte) '\n');
                    } else {
                        break;
                    }
                }
                //define esta posição como limite e retorna para a posição 0
                //buffer.limit() retorna o tamanho da mensagem
                buffer.flip();

                /*
                 * Exemplo de como usar um ByteBuffer para leitura
                 * 
                 ByteBuffer cpy = buffer.asReadOnlyBuffer();

                 System.out.println("Tamanho:" + cpy.limit());
                 int k = 0; //contador
                 while (cpy.remaining() > 0){
                 System.out.println(k + " : " + (byte)cpy.get());
                 k++;
                 }
                 */

                //se for possivel ler um ou mais bytes
                if (buffer.remaining() > 0) {
                    //notify observers
                    for (Observer<ByteBuffer, Connection> o : observers) {
                        o.update(buffer.asReadOnlyBuffer(), this);
                    }
                    available = false;
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public static void printBytes(String str) {
        byte[] array = str.getBytes(charset);
        printBytes(array, array.length);
    }

    public static void printBytes(byte[] array, int size) {
        System.out.print("Received: ");
        System.out.print("[" + size + "]{");
        for (int i = 0; i < size; i++) {
            System.out.print("," + array[i]);
        }
        System.out.println("}");
    }

//    public static void main(String[] args) {
//        Serial s = new Serial(9600);
//
//        Robot r = new Robot();
//        r.add(new HBridge(1));
//        r.add(new Compass());
//        s.attach(r);
//
//        ArrayList<byte[]> testMessages = new ArrayList<>();
//
////        testMessages.add(new byte[]{2, 11, 3, 9, 'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', '\n'});
////        testMessages.add(new byte[]{3, 0, 10, 'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', '2', '\n'});
////        testMessages.add(new byte[]{4, 0, 0});//get clock
////        testMessages.add(new byte[]{4, 1, 0});//get hbridge
//        testMessages.add(new byte[]{4, 2, 0});//get compass
////        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam
////        testMessages.add(new byte[]{5, 1, 2, 0, 90, 5, 1, 2, 1, -90}); //rotaciona
////        testMessages.add(new byte[]{5, 1, 2, 0, (byte) 0, 5, 1, 2, 1, (byte) 0}); //para
////        testMessages.add(new byte[]{5, 1, 2, 0, -90, 5, 1, 2, 1, 90}); //rotaciona
////        testMessages.add(new byte[]{5, 1, 2, 0, (byte) 0, 5, 1, 2, 1, (byte) 0}); //para
////        byte [] msg = new byte[]{'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', 0};
////        byte j = 0;
////        testMessages.add(msg);
//        //testMessages.add(new byte [] {});
//
//        if (s.establishConnection()) {
//            System.out.println("connected");
//            long timestamp = System.currentTimeMillis();
//            for (int i = 0; i < 1000; i++) {
//                for (byte[] message : testMessages) {
////                    msg[8] = j++;
//                    s.send(message);
//                    System.out.print("Sended:   ");
//                    System.out.print("[" + message.length + "]{");
//                    for (byte b : message) {
//                        System.out.print("," + b);
//                    }
//                    System.out.print("}");
//                    System.out.println(" @Time: " + (System.currentTimeMillis() - timestamp) / 1000 + "s");
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException ex) {
//                    }
//
//                }
//            }
//        } else {
//            System.out.println("fail");
//        }
//
//        System.out.println("Fim");
//    }
}
