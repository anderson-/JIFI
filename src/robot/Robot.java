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

    public class InternalClock implements Device {

        private float stepTime = 0;

        @Override
        public void stop() {
        }

        @Override
        public void reset() {
        }

        @Override
        public ByteBuffer get(ByteBuffer buffer) {
            //<lê valores>
            
            //<\lê valores>
            //reseta o buffer
            buffer.clear();
            //<escreve valores>
            
            //<\escreve valores>
            //define o limite do buffer e o retorna ao inicio
            buffer.flip();
            //retorna o buffer
            return buffer;
        }

        @Override
        public void set(ByteBuffer data) {
            stepTime = data.getFloat();
            System.out.println("Tempo do ciclo: " + stepTime);
        }

        @Override
        public String getState() {
            return "";
        }

        @Override
        public byte[] request() {
            return new byte [] {4, 0, 0};
        }
    }
    private Interpreter interpreter;
    private ArrayList<Device> devices;
    private ArrayList<Connection> connections;
    private ByteBuffer mainBuffer;
    private ByteBuffer buffer;
    private int freeRam = 0;
    public final byte CMD_STOP = 1;
    public final byte CMD_ECHO = 2;
    public final byte CMD_PRINT = 3;
    public final byte CMD_GET = 4;
    public final byte CMD_SET = 5;
    public final byte CMD_ADD = 6;
    public final byte CMD_RESET = 7;
    public final byte CMD_DONE = 8;
    public final byte CMD_NO_OP = 9;
    public final byte XTRA_ALL = (byte) 222;
    public final byte XTRA_FREE_RAM = (byte) 223;

    public Robot() {
        devices = new ArrayList<>();
        connections = new ArrayList<>();
        mainBuffer = ByteBuffer.allocate(254);
        buffer = ByteBuffer.allocate(254);
        add(new InternalClock());
    }

    public final int getFreeRam() {
        return freeRam;
    }

    public final void add(Device d) {
        devices.add(d);
    }

    public final void add(Connection c) {
        c.attach(this);
        connections.add(c);
    }

//    public final Device getDevice(Class<? extends Device> c) {
//        for (Device d : devices) {
//            if (c.isInstance(d)) {
//                return d;
//            }
//        }
//        return null;
//    }
    
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
                    byte id = message.get();
                    if (id == XTRA_ALL) {
                        for (Device d : getDevices()) {
                            if (d != null) {
                                d.stop();
                            }
                        }
                    } else {
                        Device d = getDevice(id);
                        if (d != null) {
                            d.stop();
                        }
                    }
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
                    byte id = message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    //"limpa" o buffer secundario de marcações
                    buffer.clear();
                    //coloca os argumentos no buffer
                    buffer.put(args);
                    //define o limite do buffer e retorna ao inicio
                    buffer.flip();
                    ByteBuffer slice;
                    if (id == XTRA_ALL) {
                        Device d;
                        for (int i = 0; i < getDeviceListSize(); i++) {
                            d = getDevice(i);
                            if (d != null) {
                                //passa os argumentos no buffer que guardará
                                //a resposta de Device.get()
                                //IMPORTANTE: coletar os dados pertinentes na 
                                //função get e chamar clear antes de inserir
                                //novos dados!
                                //IMPORTANTE: Não esquecer de chamar flip antes
                                //de retornar a função get! Caso contrario a 
                                //mensagem será enviada incompleta ou vazia.
                                slice = d.get(buffer.slice());
                                //"limpa" o buffer principal de marcações
                                mainBuffer.clear();
                                //header do comando set
                                mainBuffer.put(CMD_SET);
                                //id
                                mainBuffer.put((byte) i);
                                //tamanho da resposta
                                mainBuffer.put((byte) slice.remaining());
                                //resposta
                                mainBuffer.put(slice);
                                //flip antes de enviar
                                mainBuffer.flip();
                                connection.send(mainBuffer);
                            }
                        }
                    } else {
                        Device d = getDevice(id);
                        if (d != null) {
                            //ver comentarios a cima
                            slice = d.get(buffer.slice());
                            mainBuffer.clear();
                            mainBuffer.put(CMD_SET);
                            mainBuffer.put(id);
                            mainBuffer.put((byte) slice.remaining());
                            mainBuffer.put(slice);
                            mainBuffer.flip();
                            connection.send(mainBuffer);
                        }
                    }
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
                            d.set(tmp);
                        }
                    }
                    break;
                }

                case CMD_ADD: {
                    byte id = message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    //TODO
                    break;
                }

                case CMD_RESET: {
                    byte id = message.get();
                    if (id == XTRA_ALL) {
                        for (Device d : getDevices()) {
                            if (d != null) {
                                d.reset();
                            }
                        }
                    } else {
                        Device d = getDevice(id);
                        if (d != null) {
                            d.reset();
                        }
                    }
                    break;
                }

                case CMD_DONE: {
                    byte cmdDone = message.get();
                    byte id = message.get();
                    byte length = message.get();
                    byte[] args = new byte[length];
                    message.get(args);
                    //TODO
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
