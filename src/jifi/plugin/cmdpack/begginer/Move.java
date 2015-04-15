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
 * JIFI is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JIFI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JIFI. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jifi.plugin.cmdpack.begginer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.nfunk.jep.JEP;
import jifi.algorithm.parser.FunctionToken;
import jifi.algorithm.parser.parameterparser.Argument;
import jifi.algorithm.procedure.Procedure;
import jifi.drawable.swing.DrawableProcedureBlock;
import jifi.drawable.GraphicObject;
import jifi.drawable.swing.MutableWidgetContainer;
import jifi.drawable.swing.component.TextLabel;
import jifi.drawable.swing.component.Component;
import jifi.drawable.swing.component.SubLineBreak;
import jifi.drawable.swing.component.Widget;
import jifi.drawable.swing.component.WidgetLine;
import jifi.drawable.util.QuickFrame;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.interpreter.ExecutionException;
import jifi.interpreter.ResourceManager;
import jifi.plugin.Pluggable;
import jifi.robot.Robot;
import jifi.robot.device.Device;
import jifi.robot.device.HBridge;

/**
 * Procedimento de mover o robô.
 */
public class Move extends Procedure implements FunctionToken<Move> {

    private static Color myColor = Color.decode("#47B56C");
    private Argument arg0;
    private Argument arg1;
    private HBridge hBridge = null;

    public Move() {
        arg0 = new Argument("0", Argument.NUMBER_LITERAL);
        arg1 = new Argument("0", Argument.NUMBER_LITERAL);
    }

    public Move(int m1, int m2) {
        arg0 = new Argument(m1, Argument.NUMBER_LITERAL);
        arg1 = new Argument(m2, Argument.NUMBER_LITERAL);
    }

    public Move(Argument[] args) {
        this();
        arg0.set(args[0]);
        arg1.set(args[1]);
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        setProcedure("move(" + arg0 + "," + arg1 + ")");
        super.toString(ident, sb);
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        hBridge = robot.getDevice(HBridge.class);

        if (hBridge != null) {
            JEP parser = rm.getResource(JEP.class);
            arg0.parse(parser);
            arg1.parse(parser);

            byte t1 = (byte) arg0.getDoubleValue();
            byte t2 = (byte) arg1.getDoubleValue();

            hBridge.setWaiting();
            hBridge.setFullState(t1, t2);
            robot.setRightWheelSpeed(t2);
            robot.setLeftWheelSpeed(t1);
        }
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        try {
            if (hBridge != null && hBridge.isValidRead()) {
                return true;
            }
        } catch (Device.TimeoutException ex) {
//            System.err.println("RE-ENVIANDO hBridge");
            begin(rm);
        }
        return false;
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableMove(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableMove(final Move m) {
        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Mover:", true));
                components.add(new SubLineBreak());
                Widget[] widgets = createGenericField(m, m.arg0, "V1:", 80, 25, components, container);
                JSpinner spinner = (JSpinner) widgets[0].widget;
                spinner.setModel(new SpinnerNumberModel((int) spinner.getValue(), -128, 127, 5));
                components.add(new SubLineBreak());
                widgets = createGenericField(m, m.arg1, "V1:", 80, 25, components, container);
                spinner = (JSpinner) widgets[0].widget;
                spinner.setModel(new SpinnerNumberModel((int) spinner.getValue(), -128, 127, 5));
                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                sb.append("move(");
                for (int i = 0; i < arguments.size(); i++) {
                    sb.append(arguments.get(i));
                    if (i < arguments.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(m, myColor) {
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
        Polygon tmpPoli = new Polygon();
        tmpPoli.addPoint(0, 0);
        tmpPoli.addPoint(13, 10);
        tmpPoli.addPoint(0, 20);
        myShape.add(new Area(tmpPoli));
        tmpPoli.reset();
        tmpPoli.addPoint(6, 0);
        tmpPoli.addPoint(20, 10);
        tmpPoli.addPoint(6, 20);
        myShape.exclusiveOr(new Area(tmpPoli));
        return new Item("Mover", myShape, myColor, "Envia uma mensagem para o robô mover as rodas, esquerda e direita, com velocidade v1 e v2, respectivamente. Valores diferentes fazem o robô girar ou andar para trás");
    }

    @Override
    public Object createInstance() {
        return new Move();
    }

    @Override
    public int getParameters() {
        return 2;
    }

    @Override
    public Move createInstance(Argument[] args) {
        return new Move(args);
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "move(r1,r2);", null);
        fc.setShortDescription("Move o robô, sendo r1 e r2 a roda esquerda e direita respectivamente. Cada roda recebe um valor\n"
                + "inteiro de velocidade relativa, sendo 0 a roda parada e 127 a velocidade máxima do motor.\n"
                + "Utilizando valores negativos em r1 ou r2 faz a respectiva roda girar no sentido oposto.");
        return fc;
    }

    @Override
    public String getToken() {
        return "move";
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Move) {
            ((Move) copy).arg0.set(arg0);
            ((Move) copy).arg1.set(arg1);
        }
        return copy;
    }

    public static void main(String[] args) {
        Move p = new Move();
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }
}
