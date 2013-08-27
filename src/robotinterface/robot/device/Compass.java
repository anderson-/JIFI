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
public class Compass extends Device {

    int alpha = 0;

    @Override
    public void setState(ByteBuffer data) {
        alpha = data.getChar();
        System.out.println("Angulo:" + alpha);
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
}
