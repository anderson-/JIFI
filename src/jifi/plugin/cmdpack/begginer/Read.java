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

import jifi.algorithm.procedure.Procedure;
import jifi.drawable.util.QuickFrame;
import jifi.drawable.GraphicObject;
import jifi.drawable.swing.WidgetContainer;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import jifi.algorithm.parser.FunctionToken;
import jifi.algorithm.parser.parameterparser.Argument;
import jifi.drawable.swing.DrawableProcedureBlock;
import jifi.drawable.swing.MutableWidgetContainer;
import jifi.drawable.swing.component.TextLabel;
import jifi.drawable.graphicresource.GraphicResource;
import jifi.drawable.swing.component.Component;
import jifi.drawable.swing.component.SubLineBreak;
import jifi.drawable.swing.component.Widget;
import jifi.drawable.swing.component.WidgetLine;
import jifi.gui.panels.robot.RobotControlPanel;
import jifi.gui.panels.sidepanel.Classifiable;
import jifi.gui.panels.sidepanel.Item;
import jifi.robot.device.Device;
import jifi.robot.Robot;
import jifi.interpreter.ExecutionException;
import jifi.interpreter.ResourceManager;
import jifi.robot.connection.message.Message;

/**
 *
 * @author antunes
 */
public class Read extends Procedure implements GraphicResource, Classifiable, FunctionToken<Read> {

    private static Color myColor = Color.decode("#ED4A6A");
    private Device device;
    private int id = 2;
    private Argument arg0;
    private Argument arg1;

    public Read() {
        arg0 = new Argument("2", Argument.NUMBER_LITERAL);
        arg1 = new Argument("", Argument.SINGLE_VARIABLE);
    }

    public Read(Argument[] args) {
        this();
        arg0.set(args[0]);
        if (args.length > 1) {
            arg1.set(args[1]);
        }
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        device = robot.getDevice(id);
        if (device != null) {
            //mensagem get padrão 
            byte[] msg = device.defaultGetMessage();
            device.setWaiting();
            if (msg.length > 0) {
                //cria um buffer para a mensagem
                ByteBuffer GETmessage = ByteBuffer.allocate(64);
                //header do comando set
                GETmessage.put(Robot.CMD_GET);
                //id
                GETmessage.put(device.getID());
                //tamanho da mensagem
                GETmessage.put((byte) msg.length);
                //mensagem
                GETmessage.put(msg);
                //flip antes de enviar
                GETmessage.flip();
                robot.getMainConnection().send(GETmessage);
            } else {
                msg = new byte[]{Robot.CMD_GET, device.getID(), 0};
                robot.getMainConnection().send(msg);
            }
        }
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        try {
            if (device != null && device.isValidRead()) {
                String deviceState = device.stateToString();
                if (!(deviceState.isEmpty() || arg1.getVariableName().isEmpty())) {
                    execute(arg1.getVariableName() + " = " + deviceState, rm);
                }
                return true;
            }
        } catch (Message.TimeoutException ex) {
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

    public static MutableWidgetContainer createDrawableMove(final Read rd) {

        final JComboBox comboboxVar = new JComboBox();

        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Ler Sensor:", true));
                components.add(new SubLineBreak());

                MutableWidgetContainer.setAutoFillComboBox(comboboxVar, rd, true);

                Widget wcomboboxvar = new Widget(comboboxVar, 100, 25);

                Widget[] widgets = createGenericField(rd, rd.arg0, "ID Sensor:", 80, 25, components, container, ARG_SPINNER);
                JSpinner spinner = (JSpinner) widgets[0].widget;
                spinner.setModel(new SpinnerNumberModel((int) spinner.getValue(), 0, 20, 1));

                components.add(new SubLineBreak());
                components.add(new TextLabel("Variavel:"));
                components.add(wcomboboxvar);

                container.entangle(rd.arg1, wcomboboxvar);

                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (!rd.arg1.getVariableName().isEmpty()) {
                    sb.append("readID(").append(rd.arg0).append(",").append(rd.arg1).append(")");
                } else {
                    sb.append("readID(").append(rd.arg0).append(")");
                }
                rd.id = (int) rd.arg0.getDoubleValue();
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(rd, myColor) {
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
    public void toString(String ident, StringBuilder sb) {
        //reutiliza o metodo da classe pai
        if (!arg1.getVariableName().isEmpty()) {
            setProcedure("readID(" + arg0 + "," + arg1 + ")");
        } else {
            setProcedure("readID(" + arg0 + ")");
        }
        super.toString(ident, sb);
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();

        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(0, 0);
        tmpShape.addPoint(20, 0);
        tmpShape.addPoint(10, 18);
        myShape.add(new Area(tmpShape));

//        tmpShape.reset();
//        tmpShape.addPoint(0, 3);
//        tmpShape.addPoint(20, 3);
//        tmpShape.addPoint(20, 7);
//        tmpShape.addPoint(0, 7);
//        myShape.exclusiveOr(new Area(tmpShape));
//        tmpShape.reset();
//        tmpShape.addPoint(0, 11);
//        tmpShape.addPoint(20, 11);
//        tmpShape.addPoint(20, 15);
//        tmpShape.addPoint(0, 15);
//        myShape.exclusiveOr(new Area(tmpShape));
        tmpShape.reset();
        tmpShape.addPoint(0, 10);
        tmpShape.addPoint(20, 10);
        tmpShape.addPoint(20, 20);
        tmpShape.addPoint(0, 20);
        myShape.exclusiveOr(new Area(tmpShape));

//        tmpShape.reset();
//        tmpShape.addPoint(0, 3);
//        tmpShape.addPoint(20, 3);
//        tmpShape.addPoint(20, 5);
//        tmpShape.addPoint(0, 5);
//        myShape.subtract(new Area(tmpShape));
//        
//        tmpShape.reset();
//        tmpShape.addPoint(0, 8);
//        tmpShape.addPoint(20, 8);
//        tmpShape.addPoint(20, 10);
//        tmpShape.addPoint(0, 10);
//        myShape.subtract(new Area(tmpShape));
//        
//        tmpShape.reset();
//        tmpShape.addPoint(0, 13);
//        tmpShape.addPoint(20, 13);
//        tmpShape.addPoint(20, 15);
//        tmpShape.addPoint(0, 15);
//        myShape.subtract(new Area(tmpShape));
        return new Item("Ler Sensor ID", myShape, myColor, "Obtém o valor de um sensor e o armazena em uma variável");
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof Read) {
            ((Read) copy).arg0.set(arg0);
            ((Read) copy).arg1.set(arg1);
        }
        return copy;
    }

    @Override
    public Object createInstance() {
        return new Read();
    }

    @Override
    public int getParameters() {
        return -2;
    }

    @Override
    public Read createInstance(Argument[] args) {
        return new Read(args);
    }

    public static void main(String[] args) {
        Read p = new Read();
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "readID(", null);
        fc.setShortDescription("Função ler sensor.");
        ArrayList<ParameterizedCompletion.Parameter> params = new ArrayList<>();
        params.add(new ParameterizedCompletion.Parameter(null, "<Dispositivo>", false));
        params.add(new ParameterizedCompletion.Parameter(null, "<variavel>", true));
        fc.setParams(params);
        return fc;
    }

    @Override
    public String getToken() {
        return "readID";
    }
}
