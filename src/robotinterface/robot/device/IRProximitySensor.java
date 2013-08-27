/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.nio.ByteBuffer;

/**
 *
 * @author antunes
 */
public class IRProximitySensor extends Device {

    int dist = 0;
    
    @Override
    public void setState(ByteBuffer data) {
        dist = data.getChar();
        System.out.println("Distancia: " + dist);
    }
    
    @Override
    public String stateToString() {
        return "" + dist;
    }

    @Override
    public int getClassID() {
        return 5;
    }
    
    public int getDist(){
        return dist;
    }
}
