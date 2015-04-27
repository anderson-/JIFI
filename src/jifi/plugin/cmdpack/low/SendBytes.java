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
import jifi.gui.panels.sidepanel.Item;
import jifi.interpreter.ExecutionException;
import jifi.interpreter.ResourceManager;
import jifi.plugin.cmdpack.low.decoder.SendBytesArgumentDecoder;
import jifi.robot.Robot;

/**
 *
 * @author antunes
 */
public class SendBytes extends Procedure implements FunctionToken<SendBytes> {

    private static Color myColor = Color.decode("#99222F");

    public SendBytes() {

    }

    private SendBytes(Argument[] args) {
        super(args);
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
        byte[] msg = new byte[getArgSize()];
        int i = 0;
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
        tmpShape.addPoint(20, 20);
        tmpShape.addPoint(0, 20);
        myShape.exclusiveOr(new Area(tmpShape));
        return new Item("Enviar Bytes", myShape, myColor, "");
    }

    @Override
    public Object createInstance() {
        return new SendBytes();
    }

    @Override
    public Completion getInfo(CompletionProvider provider) {
        FunctionCompletion fc = new FunctionCompletion(provider, "sendBytes(...);", null);
        fc.setShortDescription("");
        return fc;
    }

    @Override
    public String getToken() {
        return "sendBytes";
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append("sendBytes(");
        boolean one = true;
        for (Argument arg : getArgs()) {
            if (one) {
                sb2.append(arg);
                one = false;
            } else {
                sb2.append(", ").append(arg);
            }
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

    public static MutableWidgetContainer createDrawableSendBytes(final SendBytes W) {

        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            private MyTableModel model = new MyTableModel();
            private Widget addButton;
            private Widget remButton;

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Enviar Bytes:", true));
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

                remButton = new Widget(bTmp, 25, 25);
                components.add(new SubLineBreak(true));
                components.add(addButton);
                components.add(remButton);
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                sb.append("sendBytes(");
                boolean one = true;
                for (Argument arg : W.getArgs()) {
                    if (one) {
                        sb.append(arg);
                        one = false;
                    } else {
                        sb.append(", ").append(arg);
                    }
                }
                sb.append(")");
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

//    @Override NAO PRECISA, POIS NAO POSSUI ARGUMENTOS ESPECIAIS
//    public Procedure copy(Procedure copy) {
//        super.copy(copy);
//        if (copy instanceof SendBytes) {
//            ((SendBytes) copy).arg0.set(arg0);
//        }
//        return copy;
//    }
    @Override
    public int getParameters() {
        return 0;//??
    }

    @Override
    public SendBytes createInstance(Argument[] args) {
        SendBytes w = new SendBytes(args);
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
