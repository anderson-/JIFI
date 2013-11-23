/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.robot.device;

import java.nio.ByteBuffer;
import robotinterface.robot.Robot;
import robotinterface.robot.simulation.VirtualConnection;
import robotinterface.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class Compass extends Device implements VirtualDevice {

    private int alpha = 0;

    @Override
    public void setState(ByteBuffer data) {
        alpha = data.getChar();
//        System.out.println("Angulo:" + alpha);
    }

    @Override
    public void getState(ByteBuffer buffer, Robot robot) {
        buffer.put((byte) 2);
        char d = (char) Math.toDegrees(robot.getTheta());
        buffer.putChar(d);
    }

    @Override
    public void setState(ByteBuffer data, Robot robot) {
        int tmpAlpha = alpha;
        setState(data);
        if (tmpAlpha == alpha) {
            alpha = (int) Math.toDegrees(robot.getTheta());
        }

    }

    @Override
    public void updateRobot(Robot robot) {
        if (robot.getMainConnection() instanceof VirtualConnection) {
            if (((VirtualConnection) robot.getMainConnection()).serial()) {
                robot.setTheta(Math.toRadians(alpha));
            }
        }
    }

    @Override
    public String stateToString() {
        return "" + alpha;
    }

    @Override
    public int getClassID() {
        return 3;
    }

    public double getAlpha() {
        return alpha;
    }

    @Override
    public String getName() {
        return "Bussola";
    }

    @Override
    public void resetState() {
        alpha = 0;
    }
}
