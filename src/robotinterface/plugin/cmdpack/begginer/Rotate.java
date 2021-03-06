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
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.nfunk.jep.JEP;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;
import robotinterface.robot.Robot;
import robotinterface.robot.action.RotateAction;
import robotinterface.robot.device.Compass;
import robotinterface.robot.device.HBridge;
import robotinterface.robot.simulation.VirtualConnection;

/**
 * Procedimento de mover o robô.
 */
public class Rotate extends Procedure implements GraphicResource, Classifiable, FunctionToken<Rotate> {

    private static final int THRESHOLD = 0;

    private RotateAction rotateAction = null;
    private static Color myColor = Color.decode("#FF8533");
    private Argument arg0;
    private int destAngle;
    private int lastAngle;
    private int turnRemaining;
    private GraphicObject resource = null;
    private HBridge hbridge;
    private Compass compass;

    public Rotate() {
        arg0 = new Argument("0", Argument.NUMBER_LITERAL);
    }

    public Rotate(int angle) {
        arg0 = new Argument(angle, Argument.NUMBER_LITERAL);
    }

    private Rotate(Argument[] args) {
        this();
        arg0.set(args[0]);
    }

    public int getAngle() {
        return (int) arg0.getDoubleValue();
    }

    public void setAngle(int angle) {
        arg0.set(angle, Argument.NUMBER_LITERAL);
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        setProcedure("rotate(" + arg0 + ")");
        super.toString(ident, sb);
    }

    public boolean rotate(Robot robot) {

        int currAngle = (int) Math.toDegrees(robot.getTheta());
        int diff = currAngle - lastAngle;
        if (diff < -180) {
            diff += 360;
        } else if (diff > 180) {
            diff -= 360;
        }
        turnRemaining -= diff;
        lastAngle = currAngle;

        if ((turnRemaining >= -THRESHOLD) && (turnRemaining <= THRESHOLD)) { // se ja esta dentro do erro limite
            hbridge.setFullState((byte) 0, (byte) 0);
        } else {
            byte speed;
            if (turnRemaining > THRESHOLD) { // se esta a direita do objetivo
                speed = (byte) Math.max(30, (int) (Math.min(127, turnRemaining * 0.71))); // velocidade proporcional ao erro, 0.71 = 128/180°
            } else {
                speed = (byte) Math.min(-30, (int) (Math.max(-127, turnRemaining * 0.71))); // velocidade proporcional ao erro, 0.71 = 128/180°
            }
            hbridge.setFullState(speed, (byte) -speed);
            return false;

        }

        return true;
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        VirtualConnection vc = (VirtualConnection) robot.getMainConnection();
        JEP parser = rm.getResource(JEP.class);
        arg0.parse(parser);
        if (vc.serial()) {
            rotateAction = robot.getAction(RotateAction.class);
            if (rotateAction != null) {
                rotateAction.setAngle((int) arg0.getDoubleValue());
                rotateAction.begin(robot);
            }
        } else {
            int turnAngle = (int) arg0.getDoubleValue();
            destAngle = turnAngle;
            hbridge = robot.getDevice(HBridge.class);
            compass = robot.getDevice(Compass.class);
            if (hbridge != null && compass != null) {
                turnRemaining = turnAngle;
                lastAngle = (int) Math.toDegrees(robot.getTheta());
                destAngle = lastAngle + turnAngle;
                destAngle = (destAngle + 1080) % 360; // limite máximo de +-1080
                //System.out.println( "theta = " + robot.getTheta() + 
                //					"; degrees = " + lastAngle +
                //					"; destAngle = " + destAngle);
                rotate(robot);
            }
        }
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        VirtualConnection vc = (VirtualConnection) robot.getMainConnection();
        if (vc.serial()) {
            return rotateAction.perform(robot);
        } else {
            if (hbridge != null && compass != null) {
                return rotate(robot);
            }
        }
        return true;
    }

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableRotate(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableRotate(final Rotate r) {
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Girar:", true));
                components.add(new SubLineBreak());
                Widget[] widgets = createGenericField(r, r.arg0, "Ângulo (°):", 80, 25, components, container);
                JSpinner spinner = (JSpinner) widgets[0].widget;
                spinner.setModel(new SpinnerNumberModel((int) spinner.getValue(), -360, 360, 2));
                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                sb.append("rotate(");
                for (int i = 0; i < arguments.size(); i++) {
                    sb.append(arguments.get(i));
                    if (i < arguments.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(r, myColor) {
            @Override
            public void updateStructure() {
                clear();
                addLine(headerLine);
                boxLabel = getBoxLabel();
            }
        };

        return dcb;
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();

        Shape tmpShape = new Ellipse2D.Double(0, 0, 20, 20);
        myShape.add(new Area(tmpShape));

        tmpShape = new Ellipse2D.Double(5, 5, 10, 10);
        myShape.subtract(new Area(tmpShape));

        Polygon tmpPoli = new Polygon();
        tmpPoli.addPoint(14, 8);
        tmpPoli.addPoint(14, 12);
        tmpPoli.addPoint(25, 14);
        myShape.subtract(new Area(tmpPoli));

        return new Item("Girar", myShape, myColor, "Permite rotacionar o robô em ângulos bem definidos");
    }

    @Override
    public int getParameters() {
        return 1;
    }

    @Override
    public Object createInstance() {
        return new Rotate();
    }

    @Override
    public Rotate createInstance(Argument[] args) {
        return new Rotate(args);
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "rotate(", null);
        fc.setShortDescription("Função girar.");
        ArrayList<ParameterizedCompletion.Parameter> params = new ArrayList<>();
        params.add(new ParameterizedCompletion.Parameter("var", "angulo", true));
        fc.setParams(params);
        return fc;
    }

    @Override
    public String getToken() {
        return "rotate";
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Rotate) {
            ((Rotate) copy).arg0.set(arg0);
        }
        return copy;
    }

    public static void main(String[] args) {
        Rotate p = new Rotate();
        p.addBefore(new Procedure("var x, y, z;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }
}
