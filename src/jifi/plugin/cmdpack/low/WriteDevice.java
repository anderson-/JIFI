/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.plugin.cmdpack.low;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.nfunk.jep.JEP;
import jifi.algorithm.parser.FunctionToken;
import jifi.algorithm.parser.parameterparser.Argument;
import jifi.algorithm.procedure.Procedure;
import jifi.drawable.GraphicObject;
import jifi.drawable.swing.DrawableProcedureBlock;
import jifi.drawable.swing.MutableWidgetContainer;
import jifi.drawable.swing.component.Component;
import jifi.drawable.swing.component.SubLineBreak;
import jifi.drawable.swing.component.TextLabel;
import jifi.drawable.swing.component.Widget;
import jifi.drawable.swing.component.WidgetLine;
import jifi.gui.panels.robot.RobotControlPanel;
import jifi.gui.panels.sidepanel.Item;
import jifi.interpreter.ExecutionException;
import jifi.interpreter.ResourceManager;
import jifi.plugin.cmdpack.low.decoder.SendBytesArgumentDecoder;
import jifi.robot.Robot;
import jifi.robot.device.Device;

/**
 *
 * @author antunes
 */
public class WriteDevice extends Procedure implements FunctionToken<WriteDevice> {

    private static Color myColor = Color.decode("#bd2f3f");
    private Device device;
    private byte id;
    private Argument arg0;

    public WriteDevice() {
        arg0 = new Argument("Distancia", Argument.SINGLE_VARIABLE);
    }

    private WriteDevice(Argument[] args) {
        super(eat(args, 1));
        arg0 = new Argument(args[0], Argument.SINGLE_VARIABLE);
    }

    public static Argument[] eat(Argument[] oargs, int n) {
        Argument[] args = new Argument[oargs.length - n];
        for (int i = n; i < oargs.length; i++) {
            args[i - n] = oargs[i];
        }
        return args;
    }

    public static void main(String[] args) {
        JEP parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
//        parser.setTraverse(true); //exibe debug
        parser.setImplicitMul(false);//multiplicação implicita: 2x+4
        parser.initSymTab(); // clear the contents of the symbol table

        ByteBuffer a = ByteBuffer.allocate(64);
        try {
            Object decode = new SendBytesArgumentDecoder(new ByteArrayInputStream("255[0]".getBytes("UTF-8"))).decode(a, parser);
            System.out.println(decode);
        } catch (Exception ex) {
            System.out.println("ERROR");
            ex.printStackTrace();
        }
    }

    @Override
    public void begin(ResourceManager rm) throws ExecutionException {
        JEP parser = rm.getResource(JEP.class);
        Robot robot = rm.getResource(Robot.class);
        byte[] msg = new byte[getArgSize() + 3];
        msg[0] = 5;
        msg[1] = id;
        msg[2] = (byte) getArgs().size();
        int i = 3;
        for (Argument arg : getArgs()) {
            arg.parse(parser);
            msg[i] = (byte) arg.getDoubleValue();
            i++;
        }
        robot.getMainConnection().send(msg);
    }

    @Override
    public boolean perform(ResourceManager rm) throws ExecutionException {
        return true;
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();

        Polygon tmpShape = new Polygon();
        tmpShape.addPoint(20, 20);
        tmpShape.addPoint(0, 20);
        tmpShape.addPoint(10, 2);
        myShape.add(new Area(tmpShape));

        tmpShape.reset();
        tmpShape.addPoint(0, 0);
        tmpShape.addPoint(20, 0);
        tmpShape.addPoint(20, 10);
        tmpShape.addPoint(0, 10);
        myShape.exclusiveOr(new Area(tmpShape));
        return new Item("Ativar Atuador", myShape, myColor, "Configura estado de um dispositivo do robo");
    }

    @Override
    public Object createInstance() {
        return new WriteDevice();
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "write(...);", null);
        fc.setShortDescription("");
        return fc;
    }

    @Override
    public String getToken() {
        return "write";
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append("write(").append(arg0);
        for (Argument arg : getArgs()) {
            sb2.append(", ").append(arg);
        }
        sb2.append(")");
        setProcedure(sb2.toString());
        super.toString(ident, sb);
    }

    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            resource = createDrawableSendBytes(this);
        }
        return resource;
    }

    public static MutableWidgetContainer createDrawableSendBytes(final WriteDevice W) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            private MyTableModel model = new MyTableModel();
            private Widget addButton;
            private Widget remButton;
            final JComboBox comboboxDev = new JComboBox();

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Ativar Atuador:", true));
                components.add(new SubLineBreak());
                components.add(new TextLabel("Sensor:"));

                comboboxDev.removeAllItems();
                for (Device d : RobotControlPanel.getRobot().getDevices()) {
                    if (d.isActuator()) {
                        comboboxDev.addItem(d.getName());
                    }
                }

                Widget wcomboboxdev = new Widget(comboboxDev, 100, 25);
                components.add(wcomboboxdev);
                components.add(new SubLineBreak());
                final JTable table = new JTable(model);
                table.getTableHeader().setReorderingAllowed(false);
                table.getColumnModel().getColumn(0).setMaxWidth(60);
                table.getColumnModel().getColumn(0).setCellRenderer(new RowHeaderRenderer());
                for (int i = 0; i < W.getArgSize(); i++) {
                    if (i < model.getData().size()) {
                        model.getData().set(i, W.getArg(i));
                    } else {
                        model.getData().add(W.getArg(i));
                    }
                }

                JScrollPane scrollPane = new JScrollPane(table);
                table.setFillsViewportHeight(true);
                Widget wtable = new Widget(scrollPane, 180, 120);
                components.add(wtable);

                JButton bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")));
                bTmp.setFocusable(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int size = table.getRowCount();
                        ((MyTableModel) table.getModel()).getData().add(W.addLineArg(size, Argument.UNDEFINED, ""));
                    }
                });

                addButton = new Widget(bTmp, 25, 25);

                bTmp = new JButton(new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-remove.png")));
                bTmp.setFocusable(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int r = table.getSelectedRow();
                        if (r >= 0) {
                            ArrayList list = ((MyTableModel) table.getModel()).getData();
                            list.remove(r);
                            W.removeLineArg(r);
                        }

                    }
                });

                container.entangle(W.arg0, wcomboboxdev);

                remButton = new Widget(bTmp, 25, 25);
                components.add(new SubLineBreak(true));
                components.add(addButton);
                components.add(remButton);
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                sb.append("write(").append(W.arg0);
                for (Argument arg : W.getArgs()) {
                    sb.append(", ").append(arg);
                }
                sb.append(")");

                for (Device d : RobotControlPanel.getRobot().getDevices()) {
                    if (d.isActuator()) {
                        if (W.arg0.toString().equals(d.getName())) {
                            W.id = d.getID();
                        }
                    }
                }
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(W, myColor) {
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
    public Procedure copy(Procedure copy) {
        super.copy(copy);
        if (copy instanceof WriteDevice) {
            ((WriteDevice) copy).arg0.set(arg0);
        }
        return copy;
    }

    @Override
    public int getParameters() {
        return -1;//??
    }

    @Override
    public WriteDevice createInstance(Argument[] args) {
        WriteDevice w = new WriteDevice(args);
        return w;
    }

    static class MyTableModel extends AbstractTableModel {

        private ArrayList<Argument> data;

        public MyTableModel() {
            data = new ArrayList<>();
        }

        public ArrayList getData() {
            return data;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public String getColumnName(int col) {
            return (col == 0) ? "Bytes" : "Data";
        }

        @Override
        public Object getValueAt(int row, int col) {
            return (col == 0) ? "[" + row + "]" : data.get(row).toString();
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col == 1);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data.get(rowIndex).set(aValue, Argument.UNDEFINED);
        }
    }

    static class RowHeaderRenderer extends DefaultTableCellRenderer {

        public RowHeaderRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected) {
                setFont(getFont().deriveFont(Font.BOLD));
            }

            setValue(value);
            return this;
        }
    }
}
