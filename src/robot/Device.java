/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot;

import java.nio.ByteBuffer;
import gui.drawable.DRobot;
import observable.Observable;
import observable.Observer;

/**
 *
 * @author antunes
 */
public interface Device {

    public void stop();
    
    public void reset();
    
//    public void update (Connection c);

    public ByteBuffer get(ByteBuffer buffer);

    public void set(ByteBuffer data);
    
    public String getState ();
    
    public byte [] request();
    
}
