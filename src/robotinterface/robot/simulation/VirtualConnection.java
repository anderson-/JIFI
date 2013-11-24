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
public class VirtualConnection implements Connection, Observer<ByteBuffer, Connection> {

    private Robot robot;
    private Connection realConnection = null;
    private ArrayList<Observer<ByteBuffer, Connection>> observers;

    public VirtualConnection() {
        observers = new ArrayList<>();
    }

    public VirtualConnection(Connection realConnection) {
        observers = new ArrayList<>();
        this.realConnection = realConnection;
        realConnection.attach(this);
    }

    public boolean serial() {
        return (realConnection != null);
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    @Override
    public void update(ByteBuffer msg, Connection info) {
        if (msg.remaining() > 0) {
            //notify observers 
            for (Observer<ByteBuffer, Connection> o : observers) {
                o.update(msg.asReadOnlyBuffer(), this);
            }
        }
    }

    @Override
    public void send(byte[] data) {
        send(ByteBuffer.wrap(data));
    }

    @Override
    public void send(ByteBuffer data) {
        if (realConnection != null) {
            realConnection.send(data);
        } else {
            //TODO: colocar data em uma fila e usar uma thread para enviar
            robot.virtualRobot(data, this);
        }
    }

    @Override
    public boolean available() {
        if (realConnection != null) {
            return realConnection.available();
        } else {
            return true;
        }
    }

    @Override
    public int receive(byte[] b, int size) {
        return 0;
    }

    @Override
    public boolean establishConnection() {
        if (realConnection != null) {
            return realConnection.establishConnection();
        } else {
            return true;
        }
    }

    @Override
    public void closeConnection() {
        if (realConnection != null) {
            realConnection.closeConnection();
        }
    }

    @Override
    public boolean isConnected() {
        if (realConnection != null) {
            return realConnection.isConnected();
        } else {
            return true;
        }
    }

    @Override
    public void attach(Observer<ByteBuffer, Connection> observer) {
        observers.add(observer);
    }
    
    @Override
    public void detach(Observer<ByteBuffer, Connection> observer) {
        observers.remove(observer);
    }
}
