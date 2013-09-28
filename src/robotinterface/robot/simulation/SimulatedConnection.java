/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.simulation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.Connection;
import robotinterface.util.observable.Observer;

/**
 *
 * @author antunes
 */
public class SimulatedConnection implements Connection {

    private ArrayList<Observer<ByteBuffer, Connection>> observers;
    private ByteBuffer buffer = ByteBuffer.allocate(256);

    public SimulatedConnection() {
        observers = new ArrayList<>();
    }

    public void fakeSend(ByteBuffer data) {
        for (Observer<ByteBuffer, Connection> o : observers) {
            o.update(data.asReadOnlyBuffer(), this);
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
        return true;
    }

    @Override
    public int receive(byte[] b, int size) {
        return 0;
    }

    @Override
    public boolean establishConnection() {
        return true;
    }

    @Override
    public void closeConnection() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void attach(Observer<ByteBuffer, Connection> observer) {
        observers.add(observer);
    }

    @Override
    public void send(byte[] data) {
        int offset = 0;
        int cmd;
        int id;
        int length;
        int i;
        while (offset < data.length) {
            cmd = data[offset];
            offset++;
            switch (cmd) {
                case Robot.CMD_STOP: {
//                    id = data[offset];
//                    offset++;
//                    if (id == Robot.XTRA_ALL) {
//                        for (i = 0; i < getDeviceListSize(); i++) {
//                            getDevice(i) - > stop();
//                        }
//                        for (i = 0; i < nRunning; i++) {
//                            if (running[i]) {
//                                freeParam( & running[i]);
//                            }
//                        }
//                    } else {
//                        Device * d = getDevice(id);
//                        if (d) {
//                            d - > stop();
//                        }
//                    }
//                    DONE
//                    int * tmpBuffer = buffer + size;
//                    tmpBuffer[0] = DONE;
//                    tmpBuffer[1] = STOP;
//                    tmpBuffer[2] = id;
//                    tmpBuffer[3] = 0;
//                    connection.sendMessage(tmpBuffer, 4);
//                    break;
                }
                case Robot.CMD_ECHO: {
//                    length = data[offset];
//                    offset++;
//                    connection.sendMessage(data + offset, length);
//                    offset += length;
//                    break;
                }
                case Robot.CMD_PRINT: {
//                    id = data[offset];
//                    offset++;
//                    length = data[offset];
//                    offset++;
//                    Connection * c = NULL;
//                    if (id == ALL) {
//                        for (i = 0; i < getConnectionListSize(); i++) {
//                            c = getConnection(i);
//                            if (c) {
//                                c - > sendMessage(data + offset, length);
//                            }
//                        }
//                    } else {
//                        c = getConnection(id);
//                        if (c) {
//                            c - > sendMessage(data + offset, length);
//                        }
//                    }
//                    offset += length;
//                    DONE
//                    int * tmpBuffer = buffer + size;
//                    tmpBuffer[0] = DONE;
//                    tmpBuffer[1] = PRINT;
//                    tmpBuffer[2] = id;
//                    tmpBuffer[3] = length;
//                    connection.sendMessage(tmpBuffer, 4);
//                    break;
                }
                case Robot.CMD_GET: {
//                    id = data[offset];
//                    offset++;
//                    length = data[offset];
//                    offset++;
//                     usa parte restante do buffer principal
//                    int * tmpBuffer = buffer + size;
//                    Device * device = NULL;
//                    if (id == FREE_RAM) {
//                        int free = freeRam();
//                        envia set
//                        tmpBuffer[0] = SET;
//                        tmpBuffer[1] = FREE_RAM;
//                        tmpBuffer[2] = sizeof(int);
//                        memcpy(tmpBuffer + 3,  & free, sizeof(int));
//                        connection.sendMessage(tmpBuffer, 3 + sizeof(int));
//                    } else if (id == ALL) {
//                        for (i = 0; i < getDeviceListSize(); i++) {
//                            device = getDevice(i);
//                            if (device) {
//                                device.get(...)
//                                int tmpLen = device - > get(tmpBuffer + 3, BUFFER_SIZE - size - 3);
//                                envia set
//                                tmpBuffer[0] = SET;
//                                tmpBuffer[1] = id;
//                                tmpBuffer[2] = tmpLen;
//                                connection.sendMessage(tmpBuffer, tmpLen + 3);
//                            }
//                        }
//                    } else {
//                        device = getDevice(id);
//                        if (device) {
//                            device.get(...)
//                            int tmpLen = device - > get(tmpBuffer + 3, BUFFER_SIZE - size - 3);
//                            envia set
//                            tmpBuffer[0] = Robot.CMD_SET;
//                            tmpBuffer[1] = id;
//                            tmpBuffer[2] = tmpLen;
//                            connection.sendMessage(tmpBuffer, tmpLen + 3);
//                        }
//                    }
//                    offset += length;
//                    break;
                }
                case Robot.CMD_SET: {
//                    id = data[offset];
//                    offset++;
//                    length = data[offset];
//                    offset++;
//                    Device * device = getDevice(id);
//                    if (device) {
//                        device.set
//                        device - > set(data + offset, length);
//                        offset += length;
//                        DONE
//                        int * tmpBuffer = buffer + size;
//                        tmpBuffer[0] = Robot.CMD_DONE;
//                        tmpBuffer[1] = Robot.CMD_SET;
//                        tmpBuffer[2] = id;
//                        tmpBuffer[3] = length;
//                        connection.sendMessage(tmpBuffer, 4);
//                    }
//                    break;
                }
                case Robot.CMD_ADD: {
//                    id = data[offset];
//                    offset++;
//                    length = data[offset];
//                    offset++;
//                    Device * device = createNew(id, data + offset, length);
//                    if (device) {
//                        addDevice( * device);
//                        DONE
//                        int * tmpBuffer = buffer + size;
//                        tmpBuffer[0] = DONE;
//                        tmpBuffer[1] = ADD;
//                        tmpBuffer[2] = id;
//                        tmpBuffer[3] = length;
//                        connection.sendMessage(tmpBuffer, 4);
//                        device - > begin();
//                    }
//                    offset += length;
                }
                break;
                case Robot.CMD_RESET: {
//                    id = data[offset];
//                    offset++;
//                    if (id == Robot.XTRA_ALL) {
//                        for (i = 0; i < getDeviceListSize(); i++) {
//                            getDevice(i) - > reset();
//                        }
//                        for (i = 0; i < nRunning; i++) {
//                            if (running[i]) {
//                                freeParam( & running[i]);
//                            }
//                        }
//                    } else if (id == Robot.XTRA_SYSTEM) {
//                        int * tmpBuffer = buffer + size;
//                        tmpBuffer[0] = DONE;
//                        tmpBuffer[1] = RESET;
//                        tmpBuffer[2] = SYSTEM;
//                        tmpBuffer[3] = 0;
//                        connection.sendMessage(tmpBuffer, 4);
//                        Reset_AVR();
//                    } else {
//                        Device * d = getDevice(id);
//                        if (d) {
////                            d - > reset();
//                        }
//                    }
//                    DONE
//                    int * tmpBuffer = buffer + size;
//                    tmpBuffer[0] = DONE;
//                    tmpBuffer[1] = RESET;
//                    tmpBuffer[2] = id;
//                    tmpBuffer[3] = 0;
//                    connection.sendMessage(tmpBuffer, 4);
                }
                break;
                case Robot.CMD_DONE:
//                    offset += 4; //avança o tamanho do comando DONE (4 bytes)
//                    break;
                case Robot.CMD_RUN: {
//                    id = data[offset];
//                    offset++;
//                    int deviceListSize = data[offset];
//                    offset++;
//                    int * deviceList = data + offset;
//                    offset += deviceListSize;
//                    length = data[offset];
//                    offset++;
//                    aloca espaço para os argumentos da ação, coloa a ação na lista de execução e excecuta a ação pela primeira vez
//                    com data != null
//                    startAction(actions[id], deviceList, deviceListSize, connection, data + offset, length);
//                    offset += length;
                }
                break;
                case Robot.CMD_NO_OP:
                    break;
                default:
                    return;
            }
        }
    }
    //fim
}
