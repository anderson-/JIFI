/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.SimulationPanel;
import robotinterface.util.observable.Observer;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.Robot;
import static robotinterface.robot.connection.Serial.charset;
import static robotinterface.robot.connection.Serial.printBytes;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.device.IRProximitySensor;
import robotinterface.robot.device.ReflectanceSensorArray;
import robotinterface.robot.simulation.VirtualConnection;
import robotinterface.util.ByteCharset;

/**
 *
 * @author antunes
 */
public class Serial implements Connection, SerialPortEventListener {

    private ArrayList<Observer<ByteBuffer, Connection>> observers;
    private SerialPort serialPort;
    private String defaultPort = null;
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
    public static Charset charset = new ByteCharset();
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

    public String getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(String defaultPort) {
        this.defaultPort = defaultPort;
    }

    @Override
    public void send(final byte[] data) {
        byte length = (byte) data.length;
        byte[] newdata = new byte[length + 1];
        System.arraycopy(data, 0, newdata, 1, length);
        newdata[0] = length;
        try {
            s++;
            output.write(newdata);
//            output.flush(); //trava a thread main! pq???
        } catch (IOException ex) {
            System.out.println("Send fail!");
        }
    }

//    public void send(final byte[] data, boolean sendLength) {
//        if (sendLength) {
//            byte length = (byte) data.length;
//            byte[] newdata = new byte[length + 1];
//            System.arraycopy(data, 0, newdata, 1, length);
//            newdata[0] = length;
//            send(newdata);
//        } else {
//            send(data);
//        }
//    }
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
            input = serialPort.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(input, charset));
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

    public Collection<String> getAvailableDevices() {
        ArrayList<String> avaliableDevices = new ArrayList<>();

        for (String name : PORT_NAMES) {
            if (name.contains("#")) {
                for (int i = 0; i < PORT_SEARCH; i++) {
                    String device = name.replace("#", "" + i);
                    //adiciona portas ocultas e bloqueadas (Linux)
                    System.setProperty("gnu.io.rxtx.SerialPorts", device);
                    if (tryConnect(device)) {
                        avaliableDevices.add(device);
                        closeConnection();
                    }
                }
            }
        }
        return avaliableDevices;
    }

    @Override
    public boolean establishConnection() {

        isConnected = false;

        if (defaultPort == null) {

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
        } else {
            //adiciona portas ocultas e bloqueadas (Linux)
            System.setProperty("gnu.io.rxtx.SerialPorts", defaultPort);
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
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
    public int s = 0;
    public static int r = 0;

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void attach(Observer<ByteBuffer, Connection> observer) {
        observers.add(observer);
    }
    boolean newMessage = false;

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                //define a posição como o inicio do buffer e remove a marca e o limite
                buffer.clear();
//                System.out.println("start");
                byte[] tmpByte = new byte[1];
                //preenche o buffer
                while (true) {
                    //obtem uma string delimitada por '\n', '\t' ou "\t\n"
                    String str = bufferedReader.readLine();
//                    printBytes(str);
                    r++;
                    System.out.println("S:" + s + " x R:" + r);
                    //obtem os bytes codificados na string com o Charset personalizado
                    byte[] data = str.getBytes(charset);
                    if (data.length > 1) {
                        buffer.put(data, 1, data.length - 1);
                    }
                    if (bufferedReader.ready()) {
//                        while (input.available() > 0) {
//                            input.read(tmpByte);
//                            buffer.put(tmpByte[0]);
//                        }
                        break;
                        //não remove os valores 10 presentes no meio da mensagem
                        //buffer.put((byte) '\n');
                    } else {
                        break;
                    }
                }

//                printBytes(buffer.array(), buffer.array().length);

                byte[] w = buffer.array();

                for (int i = 0; i < w.length; i++) {
                    if (w[i] == '\n') {
                        System.err.println("FUNCIONOU!!");
                        System.exit(0);
                    }
                }


//                byte [] size = new byte[1];
//                input.read(size);
//                
//                byte [] data = new byte[size[0]];
//                
//                for (int i = 0; i < size[0]; i++){
//                    
//                }




//                //sem usar string
//                while (true) {
//                    //obtem uma string delimitada por '\n', '\t' ou "\t\n"
//                    int size = input.read();
//                    System.out.println("size: "+ size);
//                    byte [] data = new byte[size];
////                    String str = bufferedReader.readLine();
////                    printBytes(str);
//                    r++;
//                    System.out.println("S:" + s + " x R:" + r);
//                    //obtem os bytes codificados na string com o Charset personalizado
//                    buffer.put(data);//str.getBytes(charset));
//                    if (bufferedReader.ready()) {
//                        //não remove os valores 10 presentes no meio da mensagem
//                        buffer.put((byte) '\n');
//                    } else {
//                        break;
//                    }
//                }

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
                e.printStackTrace();
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

    public static void main(String[] args) {
        Connection s;
        s = new Serial(57600);
//        s = new VirtualConnection(s);
//        s = new VirtualConnection();

        Robot r = new Robot();
        r.add(new HBridge());
        r.add(new Compass());
        r.add(new IRProximitySensor());
        r.add(new ReflectanceSensorArray());
        s.attach(r);

        if (s instanceof VirtualConnection) {
            VirtualConnection v = (VirtualConnection) s;
            v.setRobot(r);
        }

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
//        testMessages.add(new byte[]{4, (byte) 223, 0});//get freeRam

        /* MAPA DE PONTOS PELO SENSOR DE DISTÂNCIA - bug threads */

        testMessages.add(new byte[]{7, (byte) 224});//reset system
        testMessages.add(new byte[]{6, 5, 1, 17});//add dist
        testMessages.add(new byte[]{6, 4, 6, 0, 3, 4, 16, (byte) 200, 0});//add reflet
//        testMessages.add(new byte[]{5, 1, 2, 0, 30, 5, 1, 2, 1, -30}); //rotaciona
        for (int i = 0; i < 5000; i++) {
            testMessages.add(new byte[]{4, 2, 0, 4, 3, 0, 4, 4, 1, 0});//get compass & get dist & get reflet
        }

        SimulationPanel p = new SimulationPanel();
        p.addRobot(r);
        r.setEnvironment(p.getEnv());
        QuickFrame.create(p, "Teste Simulação").addComponentListener(p);


        /* TESTE DO RÁDIO */

        //quando uma mensagem chega é exibido "S:10 x R:10"
        //ou seja 10 mensagens enviadas e 10 recebidas
        //ATENÇÃO: trocar intervalo de tempo na linha ~389
        //coloca 100 mensagens na lista de espera
        int test = 2000;
//        for (int i = 0; i < test; i++) {
////            testMessages.add(new byte[]{3, 4, 0, 0});//get clock
//            
//            testMessages.add(new byte[]{2, });//get clock
////            testMessages.add(new byte[]{3, 4, (byte) 223, 0});//get freeRam
//        }
        if (s.establishConnection()) {
            System.out.println("connected");
            long timestamp = System.currentTimeMillis();
            for (int i = 0; i < 1; i++) { //repetição
                int send = 0;
                for (byte[] message : testMessages) {
                    send++;
                    long mtimestamp = System.currentTimeMillis();
                    s.send(message);
                    try {
                        int w = 0;
                        loop:
                        while (send != Serial.r) {
                            //tempo maximo para enviar: ~20ms da RXTXcomm + 8ms do radio
                            Thread.sleep(1); //tempo maximo para enviar: ~20ms da RXTXcomm + 8ms do radio
                            w++;
                            if (w >= 40) {
//                                s.send(message);
                                w = 0;
                                break loop;
                            }

//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(Serial.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (InterruptedException ex) {
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }

//                    } 
//                    System.out.print("Sended:   ");
//                    System.out.print(send + "\t- [" + message.length + "]{");
//                    for (byte b : message) {
//                        System.out.print("," + b);
//                    }
//                    System.out.print("}");
                    System.out.println(" @Time: " + (System.currentTimeMillis() - mtimestamp) + "ms");
                }
                System.out.println("\nAverage Time: " + (System.currentTimeMillis() - timestamp) / test + "ms");
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
