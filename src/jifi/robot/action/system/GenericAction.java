/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.robot.action.system;

import java.nio.ByteBuffer;
import jifi.robot.Robot;
import jifi.robot.action.Action;

/**
 *
 * @author gnome3
 */
public class GenericAction extends Action {

    byte[] msg;

    public GenericAction(boolean singleMessage, int trials, byte... msg) {
        super(singleMessage);
        if (trials <= 0) {
            setInfiniteSend(true);
        } else {
            super.setNumberOfTrials(trials);
        }
        this.msg = msg;
    }

    @Override
    public void putMessage(ByteBuffer data, Robot robot) {
        for (byte b : msg) {
            data.put(b);
        }
    }
}
