/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot;

import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import observable.Observer;
import simulation.Interpreter;

/**
 *
 * @author antunes
 */
public class Robot implements Observer<ByteBuffer, Connection> {

    public class InternalClock extends Device {

        private float stepTime = 0;

        @Override
        public void setState(ByteBuffer data) {
            stepTime = data.getFloat();
            System.out.println("Tempo do ciclo: " + stepTime);
        }

        @Override
        public String stateToString() {
            return "" + stepTime;
        }
    }
    
    private Interpreter interpreter;
    private ArrayList<Device> devices;
    private ArrayList<Connection> connections;
    private int freeRam = 0;
    public static final byte CMD_STOP = 1;
    public static final byte CMD_ECHO = 2;
    public static final byte CMD_PRINT = 3;
    public static final byte CMD_GET = 4;
    public static final byte CMD_SET = 5;
    public static final byte CMD_ADD = 6;
    public static final byte CMD_RESET = 7;
    public static final byte CMD_DONE = 8;
    public static final byte CMD_NO_OP = 9;
    public static final byte XTRA_ALL = (byte) 222;
    public static final byte XTRA_FREE_RAM = (byte) 223;

    public Robot() {
        devices = new ArrayList<>();
        connections = new ArrayList<>();
        add(new InternalClock());
    }

    public final int getFreeRam() {
        return freeRam;
    }

    public final void add(Device d) {
        devices.add(d);
        d.setID(devices.size()-1);
    }

    public final void add(Connection c) {
        c.attach(this);
        connections.add(c);
    }
    
    public final <T> T getDevice(Class<? extends Device> c) {
        for (Device d : devices) {
            if (c.isInstance(d)) {
                return (T)d;
            }
        }
        return null;
    }

    public final Connection getConnection(Class<? extends Connection> c) {
        for (Connection con : connections) {
            if (c.isInstance(con)) {
                return con;
            }
        }
        return null;
    }

    public final  Device getDevice(int index) {
        if (index < 0 || index >= devices.size()) {
            return null;
        }
        return devices.get(index);
    }

    public final Connection getConnection(int index) {
        if (index < 0 || index >= connections.size()) {
            return null;
        }
        return connections.get(index);
    }
    
    public final Connection getMainConnection(){
        return getConnection(0);
    }

    public final List<Device> getDevices() {
        return devices;
    }

    public final List<Connection> getConnections() {
        return connections;
    }

    public final int getDeviceListSize() {
        return devices.size();
    }

    public final int getConnectionListSize() {
        return connections.size();
    }

    public final Interpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public final void update(ByteBuffer message, Connection connection) {
        message.order(ByteOrder.LITTLE_ENDIAN);
        while (message.remaining() > 0) {
            byte cmd = message.get();
            switch (cmd) {
                case CMD_STOP: {
                    //skip bytes
                    message.get();
                    break;
                }

                case CMD_ECHO: {
                    byte length = message.get();
                    byte[] bytestr = new byte[length];
                    message.get(bytestr);
                    connection.send(bytestr);
                    break;
                }

                case CMD_PRINT: {
                    byte connectionID = message.get();
                    byte length = message.get();
                    byte[] bytestr = new byte[length];
                    message.get(bytestr);
                    System.out.println(new String(bytestr)); //TODO: stdout
                    if (connectionID == XTRA_ALL) {
                        for (Connection c : getConnections()) {
                            if (c != null) {
                                c.send(bytestr);
                            }
                        }
                    } else {
                        Connection c = getConnection(connectionID);
                        if (c != null) {
                            c.send(bytestr);
                        }
                    }
                    break;
                }

                case CMD_GET: {
                    //skip bytes
                    message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    break;
                }

                case CMD_SET: {
                    byte id = message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);

                    if (id == XTRA_FREE_RAM) {
                        freeRam = ByteBuffer.wrap(args).getChar();
                        System.out.println("FreeRam: " + freeRam);
                    } else {
                        Device d = getDevice(id);
                        if (d != null) {
                            ByteBuffer tmp = ByteBuffer.wrap(args).asReadOnlyBuffer();
                            tmp.order(ByteOrder.LITTLE_ENDIAN);
                            d.setState(tmp);
                        }
                    }
                    break;
                }

                case CMD_ADD: {
                    //skip bytes
                    message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    break;
                }

                case CMD_RESET: {
                    //skip bytes
                    message.get();
                    break;
                }

                case CMD_DONE: {
                    byte cmdDone = message.get();
                    byte id = message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    //TODO: confirmação do comando enviado
                    break;
                }

                case CMD_NO_OP: {
                    break;
                }
                default:
                    if (cmd != 0) {
                        System.out.println("Erro: Comando invalido: " + cmd);
                    }
            }
        }
    }
}
