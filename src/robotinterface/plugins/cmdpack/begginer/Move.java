/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugins.cmdpack.begginer;

import robotinterface.algorithm.Command;
import robotinterface.robot.connection.Connection;
import robotinterface.robot.Robot;
import robotinterface.robot.device.HBridge;
import robotinterface.util.trafficsimulator.Clock;

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
        hb.setFullState(m1,m2);
        return true;
    }
}
