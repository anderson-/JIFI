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
package robotinterface.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.robot.device.Device;
import robotinterface.robot.device.HBridge;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Procedimento de mover o robô.
 */
public class Move extends Command implements GraphicResource, Classifiable, FunctionToken<Move> {

    private byte m1, m2;
    private HBridge hBridge = null;

    public Move() {
        m1 = m2 = 0;
    }

    public Move(int m1, int m2) {
        super();
        this.m1 = (byte) m1;
        this.m2 = (byte) m2;
    }

    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        hBridge = robot.getDevice(HBridge.class);
        if (hBridge != null) {
            hBridge.setWaiting();
            hBridge.setFullState(m1, m2);
        }
    }

    @Override
    public boolean perform(Robot r, Clock clock) throws ExecutionException {
        try {
            if (hBridge != null && hBridge.isValidRead()) {
//                String deviceState = device.stateToString();
//                if (!deviceState.isEmpty()) {
//                    execute(var + " = " + deviceState);
//                }
                return true;
            }
        } catch (Device.TimeoutException ex) {
            System.err.println("RE-ENVIANDO hBridge");
            begin(r, clock);
        }
        return false;
    }
    private SimpleContainer dResource = new SimpleContainer(new Rectangle(0, 0, 40, 20), Color.CYAN);

    @Override
    public GraphicObject getDrawableResource() {
        return dResource;
    }

    @Override
    public Item getItem() {
        return new Item("Move", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#80DE71"));
    }

    @Override
    public Object createInstance() {
        return new Move();
    }

    @Override
    public String getToken() {
        return "move";
    }

    @Override
    public Move createInstance(String args) {
        if (args.isEmpty()) {
            return new Move(0, 0);
        } else {
            String[] argv = args.split(",");
            if (argv.length == 1) {
                int v = Integer.parseInt(argv[0].trim());
                return new Move(v, v);
            } else if (argv.length == 2) {
                int v0 = Integer.parseInt(argv[0].trim());
                int v1 = Integer.parseInt(argv[1].trim());
                return new Move(v0, v1);
            }
        }

        return new Move(0, 0);
        //return new ParseErrorProcedure(this, args);
    }
}
