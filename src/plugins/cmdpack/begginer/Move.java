/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.cmdpack.begginer;

import algorithm.Command;
import robot.Connection;
import robot.Robot;
import robot.impl.HBridge;
import util.Clock;

/**
 *
 * @author antunes
 */
public class Move extends Command{
    
    private byte m1, m2;
    
    public Move(int m1, int m2) {
        super();
        this.m1 = (byte)m1;
        this.m2 = (byte)m2;
    }

    @Override
    public boolean perform(Robot robot, Clock clock) {
        HBridge hb = robot.getDevice(HBridge.class);
        Connection c = robot.getMainConnection();
        hb.setState(m1,m2,c);
        return true;
    }
}
