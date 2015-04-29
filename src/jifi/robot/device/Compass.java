/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.device;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import jifi.robot.Robot;
import jifi.robot.simulation.VirtualConnection;
import jifi.robot.simulation.VirtualDevice;

/**
 *
 * @author antunes
 */
public class Compass extends Device implements VirtualDevice {

    private int alpha = 0;

    @Override
    public boolean isActuator() {
        return false;
    }

    @Override
    public boolean isSensor() {
        return true;
    }

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

    public int getAlpha() {
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
    
    @Override
    public List<Object> getDescriptionData() {
        ArrayList<Object> data = new ArrayList<>();
        return data;
    }

    @Override
    public Device createDevice(List<Object> descriptionData) {
        return new Compass();
    }

    @Override
    public byte[] getBuilderMessageData() {
        return new byte[]{};
    }
}
