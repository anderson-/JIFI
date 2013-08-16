/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import robot.Device;

/**
 *
 * @author antunes
 */
public class Compass implements Device {

    int alpha = 0;
    
    @Override
    public void stop() {
        
    }

    @Override
    public void reset() {
        
    }

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        return buffer;
    }

    @Override
    public void set(ByteBuffer data) {
        alpha = data.getChar();
//        System.out.println("Angulo: " + alpha);
    }
    
    
    @Override
    public String getState() {
        return "alpha = " + alpha;
    }
    
    @Override
    public byte[] request() {
        return new byte[]{4, 2, 0};
    }
    
}
