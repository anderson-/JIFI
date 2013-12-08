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
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JOptionPane;
import robotinterface.util.observable.Observer;

public class Serial2 implements Connection, SerialPortEventListener {

  private static boolean FAIL_LOAD_LIBRARY = false;
  private ArrayList<Observer<ByteBuffer, Connection>> observers;
  private SerialPort serialPort;
  private String defaultPort = null;
  private boolean isConnected = false;
  private int sentPackages = 0;
  private int receivedPackages = 0;
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
  private byte buffer[] = new byte[128];
  private int bufferIndex;
  private int bufferLast;

  private int bufferSize = 1;  // how big before reset or event firing

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
  private static final int PORT_SEARCH = 10;

  public Serial2(int dataRate) {
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

    CommPortIdentifier portId = null;
    Enumeration portEnum = null;

    try {
      portEnum = CommPortIdentifier.getPortIdentifiers();
    } catch (Throwable t) {
//            t.printStackTrace();
      throw new Error();
    }

    if (portEnum == null) {
      return false;
    }

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

  public Collection<String> getAvailableDevices() {
    ArrayList<String> avaliableDevices = new ArrayList<>();

    if (FAIL_LOAD_LIBRARY) {
      return avaliableDevices;
    }

    for (String name : PORT_NAMES) {
      if (name.contains("#")) {
        for (int i = 0; i < PORT_SEARCH; i++) {
          String device = name.replace("#", "" + i);
          //adiciona portas ocultas e bloqueadas (Linux)
          System.setProperty("gnu.io.rxtx.SerialPorts", device);
          try {
            if (tryConnect(device)) {
              avaliableDevices.add(device);
              closeConnection();
            }
          } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "Falha ao carregar a biblioteca da porta serial.\nApenas simulação virtual disponivel.", "Erro", JOptionPane.ERROR_MESSAGE);
            FAIL_LOAD_LIBRARY = true;
            return avaliableDevices;
          }
        }
      }
    }
    return avaliableDevices;
  }

  @Override
  public boolean establishConnection() {

    isConnected = false;

//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                closeConnection();
//            }
//        }));
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
   * This is a modified version of serialEvent from PSerial library, 
   * from Processing project - http://processing.org
   */
  @Override
  synchronized public void serialEvent(SerialPortEvent serialEvent) {
    if (serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
      try {
        /* Se a funcao retornar antes de ler toda a mensagem, o evento so sera
         * disparado apos 20ms.
         */
        while ((bufferLast - bufferIndex) < bufferSize) {//input.available() > 0) {
          synchronized (buffer) {
            if (bufferLast == buffer.length) {
              byte temp[] = new byte[bufferLast << 1];
              System.arraycopy(buffer, 0, temp, 0, bufferLast);
              buffer = temp;
            }
            if (input.available() > 0) {
              buffer[bufferLast++] = (byte) input.read();
            }              
            if ( (bufferLast - bufferIndex) >= bufferSize ) {
              if (messageHandler())
                break;
            }
          }
        }

      } catch (IOException e) {
        System.out.println("serialEvent: " + e.getMessage());
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
        ByteBuffer message = ByteBuffer.allocate(bufferSize+1); // TODO: REMOVER +1
        message.put((byte)bufferSize); // TODO: REMOVER
        readBytes(message);
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

  @Override
  public void send(byte[] data) {
    try {
      sentPackages++;
      output.write(data.length);
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
    bufferSize = count;
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

    Serial2 s = new Serial2(57600);

    ArrayList<byte[]> testMessages = new ArrayList<>();

    /* TESTE DO RÁDIO */
    //ATENÇÃO: trocar intervalo de tempo na linha ~435
    //coloca 100 mensagens na lista de espera
    int numMessages = 100;
    for (int i = 0; i < numMessages; i++) {
      testMessages.add(new byte[]{1,2,3,4,5});
    }

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
                numMessages--;
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
            Thread.sleep(10);
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
