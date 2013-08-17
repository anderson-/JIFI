/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import java.nio.ByteBuffer;
import robot.Device;

/**
 *
 * @author antunes
 */
public class Compass extends Device {

    int alpha = 0;
    
    @Override
    public void setState(ByteBuffer data) {
        alpha = data.getChar();
    }
    
    @Override
    public String stateToString() {
        return "" + alpha;
    }
}
