/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.impl;

import java.nio.ByteBuffer;
import robot.Connection;
import robot.Device;

/**
 *
 * @author antunes
 */
public class HBridge implements Device {

    private byte[] msg;
    private int id;

    public HBridge(int id) {
        this.id = id;
        msg = new byte[]{5, 1, 2, 0, 0};
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

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
        
    }

    public void setMotorState(int motor, byte speed, Connection c) {
        msg[3] = (byte) motor;
        msg[4] = speed;
        c.send(msg);
    }

    public void setState(byte speedM1, byte speedM2, Connection c) {
        msg[3] = 0;
        msg[4] = speedM1;
        c.send(msg);
        msg[3] = 1;
        msg[4] = speedM2;
        c.send(msg);
    }

    @Override
    public String getState() {
        return "";
    }

    @Override
    public byte[] request() {
        return new byte[]{4, 1, 0};
    }
}
