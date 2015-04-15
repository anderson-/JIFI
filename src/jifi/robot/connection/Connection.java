/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.connection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import jifi.util.observable.Observable;

/**
 *
 * @author antunes
 */
public interface Connection extends Observable<ByteBuffer,Connection> {

    public interface ConnectionListener {

        public void messageReceived(ByteBuffer data, Connection connection);
    }
    
    public void send(final byte[] data);
    
    public void send(ByteBuffer data);

    public boolean available();

    public int receive(byte[] b, int size);

    public boolean establishConnection();

    public void closeConnection();

    public boolean isConnected();

}
