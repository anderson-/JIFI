package robotinterface.robot.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import robotinterface.util.observable.Observer;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.device.IRProximitySensor;

/**
 *
 * @author antunes
 */
public class Serial1 implements Connection, SerialPortEventListener {

    private ArrayList<Observer<ByteBuffer, Connection>> observers;
    private SerialPort serialPort;
    private boolean isConnected = false;
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyUSB#", // Linux
        "/dev/ttyACM#", // Linux USB 3.0
        "COM#", // Windows
    };
    /**
     * Input and output streams
     */
    private InputStream input;
    private OutputStream output;

    /*
     * Read buffers
     */
    private byte buffer[] = new byte[32768];
    private int bufferIndex;
    private int bufferLast;
    private int bufferSize = 1;  // how big before reset or event firing
    private boolean bufferUntil;
    private byte bufferUntilByte;
    private boolean newMessage = true;
    private Queue<byte[]> messages;
    /**
     * Default bits per second for COM port.
     */
    private int dataRate = 9600;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    private static final int PORT_SEARCH = 1;
    public int s = 0;
    public int r = 0;

    public Serial1(int dataRate) {
        observers = new ArrayList<>();
        this.dataRate = dataRate;
        messages = new LinkedList<byte[]>();
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
    public void detach(Observer<ByteBuffer, Connection> observer) {
        observers.remove(observer);
    }

    /*
     * Some function bellow are part of PSerial library, 
     * from Processing project - http://processing.org
     */
    @Override
    synchronized public void serialEvent(SerialPortEvent serialEvent) {
        if (serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                while (input.available() > 0) {
                    synchronized (buffer) {
                        if (bufferLast == buffer.length) {
                            byte temp[] = new byte[bufferLast << 1];
                            System.arraycopy(buffer, 0, temp, 0, bufferLast);
                            buffer = temp;
                        }
                        buffer[bufferLast++] = (byte) input.read();
                        if ((bufferUntil
                                && (buffer[bufferLast - 1] == bufferUntilByte))
                                || (!bufferUntil
                                && ((bufferLast - bufferIndex) >= bufferSize))) {
                            serialHandler();
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("serialEvent: " + e.getMessage());
            }
        }
    }

    public void serialHandler() {
        if (newMessage) {
            bufferSize = read();
            newMessage = false;
        }
        //System.out.println("Available: " + (bufferLast - bufferIndex));
        if ((bufferLast - bufferIndex) >= bufferSize) {
            byte[] message = new byte[bufferSize];
            readBytes(message);
            if (message[0] == 3) {
                System.out.println("Radio: " + new String(message, 1, bufferSize - 1));
            } else {
                synchronized (messages) {
                    messages.add(message);
                }
            }
            r++;
            System.out.println("S:" + s + " x R:" + r);
            newMessage = true;
            bufferSize = 1;
        }
    }

    @Override
    public void send(byte[] data) {
        try {
            s++;
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

    public void buffer(int count) {
        bufferUntil = false;
        bufferSize = count;
    }

    public void bufferUntil(int what) {
        bufferUntil = true;
        bufferUntilByte = (byte) what;
    }

    @Override
    public boolean available() {
        return (!messages.isEmpty());
    }

    public void clear() {
        bufferLast = 0;
        bufferIndex = 0;
    }

    @Override
    public int receive(byte[] b, int size) {
        synchronized (messages) {
            if (!messages.isEmpty()) {
                byte[] lastMessage = messages.poll();
                int length = Math.min(size, lastMessage.length);
                System.arraycopy(lastMessage, 0, b, 0, length);
                return length;
            } else {
                return 0;
            }
        }
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

    public static void main(String[] args) {
        Serial s = new Serial(57600);

        Robot r = new Robot();
        r.add(new HBridge());
        r.add(new Compass());
        r.add(new IRProximitySensor());
//        s.attach(r);

        ArrayList<byte[]> testMessages = new ArrayList<>();

        /* PRIMEIROS TESTES */

//        testMessages.add(new byte[]{2, 11, 3, 9, 'a', 'n', 'd', 'e', 'r', 's', 'o', 'n', '\n'});
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

        /* MAPA DE PONTOS PELO SENSOR DE DISTÂNCIA - bug threads */

//        testMessages.add(new byte[]{6, 5, 1, 17});//add dist
//        testMessages.add(new byte[]{5, 1, 2, 0, 30, 5, 1, 2, 1, -30}); //rotaciona
//        for (int i = 0; i < 500; i++){
//            testMessages.add(new byte[]{4, 2, 0, 4, 3, 0});//get compass & get dist
//        }
//        
//        SimulationPanel p = new SimulationPanel();
//        p.addRobot(r);
//        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);
//        

        /* TESTE DO RÁDIO */

        //ATENÇÃO: trocar intervalo de tempo na linha ~435
        //coloca 100 mensagens na lista de espera
        int numMessages = 200;
        for (int i = 0; i < numMessages; i++) {
            testMessages.add(new byte[]{3, 4, (byte) 223, 0});//get freeRam
            //testMessages.add(new byte[]{3, 4, 0, 0});//get clock
            //testMessages.add(new byte[]{3, 4, (byte) (i / 256), (byte) (i)});//get clock
        }

//        if (s.establishConnection()) {
//            System.out.println("connected");
//            long timestamp = System.currentTimeMillis();
//            for (int i = 0; i < 1; i++) { //repetição
//                int send = 0;
//                for (byte[] message : testMessages) {
//                    send++;
//                    long mtimestamp = System.currentTimeMillis();
//                    s.send(message);
//                    try {
//                        int w = 0;
//                        loop:
//                        while (send != s.r) {
//                            //tempo maximo para enviar: ~20ms da RXTXcomm + 8ms do radio
//                            Thread.sleep(1); //tempo maximo para enviar: ~20ms da RXTXcomm + 8ms do radio
//                            w++;
//                            if (w >= 40){
//                                s.send(message);
//                                w = 0;
////                                break loop;
//                            }
//                            
////                    } catch (InterruptedException ex) {
////                        Logger.getLogger(Serial.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    } catch (InterruptedException ex) {
//                    }
////                    } 
//                    System.out.print("Sended:   ");
//                    System.out.print(send + "\t- [" + message.length + "]{");
//                    for (byte b : message) {
//                        System.out.print("," + b);
//                    }
//                    System.out.print("}");
//                    System.out.println(" @Time: " + (System.currentTimeMillis() - mtimestamp) + "ms");
//                }
//                System.out.println("\nAverage Time: " + (System.currentTimeMillis() - timestamp) / numMessages + "ms");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                }
//            }
//        } else {
//            System.out.println("fail");
//        }

        if (s.establishConnection()) {
            System.out.println("connected\n");

            int sended = 0;
            int failed = 0;
            byte[] buffer = new byte[20];
            long timeSum = 0;

            for (int i = 0; i < 1; i++) { //repetição
                for (byte[] message : testMessages) {
                    boolean timeout = false;
                    long timestamp = System.currentTimeMillis();

                    sended++;
                    s.send(message);
                    System.out.print("Sended [" + sended + "]:   ");
                    printBytes(message, message.length);

                    while (!s.available()) {
                        try {
                            Thread.sleep(1); //tempo maximo para enviar: ~20ms da RXTXcomm + 8ms do radio
                            if ((System.currentTimeMillis() - timestamp) >= 1000) {
                                timeout = true;
                                break;
                            }
                        } catch (InterruptedException ex) {
                        }
                    }

                    if (!timeout) {
                        int lenght = s.receive(buffer, buffer.length);
                        if (buffer[0] != 11) {
                            System.out.print("Received [" + sended + "]:   ");
                            printBytes(buffer, lenght);

                            long rtt = System.currentTimeMillis() - timestamp;
                            timeSum += rtt;
                            System.out.println(" @Time [" + sended + "]: " + rtt + "ms\n");
                        } else {
                            failed++;
                            System.out.println("Failed\n");
                        }
                    } else {
                        failed++;
                        System.out.println("Timeout\n");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }

                System.out.println("Average Time: " + timeSum / numMessages + "ms\n");
                System.out.println("Sended: " + sended + "\tFailed: " + failed);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }

        } else {
            System.out.println("fail");
        }

        System.out.println("Fim");
        System.exit(0);
    }
}