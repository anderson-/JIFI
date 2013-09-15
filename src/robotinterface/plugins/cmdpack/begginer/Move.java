/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.plugins.cmdpack.begginer;

import java.awt.Color;
import java.awt.Rectangle;
import robotinterface.algorithm.Command;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.robot.device.HBridge;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Procedimento de mover o robô.
 */
public class Move extends Command implements GraphicResource{

    private byte m1, m2;

    public Move(int m1, int m2) {
        super();
        this.m1 = (byte) m1;
        this.m2 = (byte) m2;
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        HBridge hb = robot.getDevice(HBridge.class);
        hb.setFullState(m1, m2);
        hb.setWaiting();
    }

    private SimpleContainer dResource = new SimpleContainer(new Rectangle(0,0,40,20),Color.CYAN);
    
    @Override
    public Drawable getDrawableResource() {
        return dResource;
    }
}
