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

import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.swing.WidgetContainer;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JComboBox;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.drawable.swing.DrawableProcedureBlock;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.component.TextLabel;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.swing.component.Component;
import robotinterface.drawable.swing.component.SubLineBreak;
import robotinterface.drawable.swing.component.Widget;
import robotinterface.drawable.swing.component.WidgetLine;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.gui.panels.sidepanel.Classifiable;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.robot.device.Device;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;
import robotinterface.robot.connection.message.Message;

/**
 *
 * @author antunes
 */
public class ReadDevice extends Procedure implements GraphicResource, Classifiable, FunctionToken<ReadDevice> {

    private static Color myColor = Color.decode("#ED4A6A");
    private Device device;
    private Class<? extends Device> type;
    private Argument arg0;
    private Argument arg1;

    public ReadDevice() {
        arg0 = new Argument("Distancia", Argument.SINGLE_VARIABLE);
        arg1 = new Argument("", Argument.SINGLE_VARIABLE);
    }

    public ReadDevice(Argument[] args) {
        this();
        arg0.set(args[0]);
        if (args.length > 1) {
            arg1.set(args[1]);
        }
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        Robot robot = rm.getResource(Robot.class);
        device = robot.getDevice(type);
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

    public static MutableWidgetContainer createDrawableMove(final ReadDevice rd) {

        final JComboBox comboboxDev = new JComboBox();
        final JComboBox comboboxVar = new JComboBox();
        final HashMap<String, Class<? extends Device>> deviceMap = new HashMap<>();
        for (Class<? extends Device> c : RobotControlPanel.getAvailableDevices()) {
            String str = c.getSimpleName();
            try {
                str = c.newInstance().getName();
            } catch (Exception ex) {
            }
            deviceMap.put(str, c);
            comboboxDev.addItem(str);
        }

        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Ler Sensor:", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Sensor:"));

                MutableWidgetContainer.setAutoFillComboBox(comboboxVar, rd, true);

                Widget wcomboboxdev = new Widget(comboboxDev, 100, 25);
                Widget wcomboboxvar = new Widget(comboboxVar, 100, 25);
                components.add(wcomboboxdev);
                components.add(new SubLineBreak());
                components.add(new TextLabel("Variavel:"));
                components.add(wcomboboxvar);

                container.entangle(rd.arg0, wcomboboxdev);
                container.entangle(rd.arg1, wcomboboxvar);

                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (!rd.arg1.getVariableName().isEmpty()) {
                    sb.append("read(").append(rd.arg0).append(",").append(rd.arg1).append(")");
                } else {
                    sb.append("read(").append(rd.arg0).append(")");
                }
                rd.type = deviceMap.get(rd.arg0.toString());
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
            setProcedure("read(" + arg0 + "," + arg1 + ")");
        } else {
            setProcedure("read(" + arg0 + ")");
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
        return new Item("Ler Sensor", myShape, myColor);
    }

    @Override
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof ReadDevice) {
            ((ReadDevice) copy).arg0 = arg0;
            ((ReadDevice) copy).arg1 = arg1;
        }
        return copy;
    }

    @Override
    public Object createInstance() {
        return new ReadDevice();
    }

    @Override
    public int getParameters() {
        return -2;
    }

    @Override
    public ReadDevice createInstance(Argument[] args) {
        return new ReadDevice(args);
    }

    public static void main(String[] args) {
        ReadDevice p = new ReadDevice();
        p.addBefore(new Procedure("var x, y;"));
        QuickFrame.applyLookAndFeel();
        QuickFrame.drawTest(p.getDrawableResource());
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "read(", null);
        fc.setShortDescription("Função ler sensor.");
        ArrayList<ParameterizedCompletion.Parameter> params = new ArrayList<>();
        params.add(new ParameterizedCompletion.Parameter(null, "<Dispositivo>", false));
        params.add(new ParameterizedCompletion.Parameter(null, "<variavel>", true));
        fc.setParams(params);
        return fc;
    }

    @Override
    public String getToken() {
        return "read";
    }
}
