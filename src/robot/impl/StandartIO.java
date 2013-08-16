/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import java.nio.ByteBuffer;
import observable.Observer;
import robot.Connection;

/**
 *
 * @author antunes
 */
public class StandartIO implements Connection {

    @Override
    public void send(byte[] data) {
        System.out.println(new String(data));
    }

    @Override
    public void send(ByteBuffer data) {
        System.out.println(data.toString());
    }

    @Override
    public boolean available() {
        return false;
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
        
    }
    
}
