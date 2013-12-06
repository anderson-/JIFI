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
package robotinterface.algorithm.procedure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import robotinterface.algorithm.Command;
import static robotinterface.algorithm.Command.identChar;
import robotinterface.algorithm.GraphicFlowchart;
import static robotinterface.algorithm.GraphicFlowchart.GF_J;
import static robotinterface.algorithm.procedure.Procedure.INSET_X;
import robotinterface.drawable.DrawableCommandBlock;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.MutableWidgetContainer;
import robotinterface.drawable.MutableWidgetContainer.WidgetLine;
import robotinterface.drawable.TextLabel;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.WidgetContainer.Widget;
import robotinterface.drawable.graphicresource.GraphicResource;
import robotinterface.drawable.util.QuickFrame;
import robotinterface.gui.panels.sidepanel.Item;
import robotinterface.interpreter.ExecutionException;
import robotinterface.robot.Robot;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Divisor de fluxo.
 */
public class If extends Procedure {

    public class InnerBlock extends Block {

        @Override
        public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k) {
            Rectangle2D.Double bounds = super.getBounds(tmp, j, k);
            bounds.height += j;
            return bounds;
        }
    }

    //blocos para a divisão de fluxo
    public class BlockTrue extends InnerBlock {
    }

    public class BlockFalse extends InnerBlock {
    }
    private static Color myColor = Color.decode("#FFA500");
    private BlockTrue blockTrue;
    private BlockFalse blockFalse;
    private GraphicObject resource = null;
    private String var;

    public If() {
        blockTrue = new BlockTrue();
        blockFalse = new BlockFalse();
        blockTrue.setParent(this);
        blockFalse.setParent(this);
    }

    public If(String procedure) {
        this();
        setProcedure(procedure);
    }

    public boolean addTrue(Command c) {
        return blockTrue.add(c);
    }

    public boolean addFalse(Command c) {
        return blockFalse.add(c);
    }

    public Block getBlockTrue() {
        return blockTrue;
    }

    public Block getBlockFalse() {
        return blockFalse;
    }

    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException {
        return true;
    }

    @Override
    public Command step() throws ExecutionException {
        //calcula o valor da expressão
        if (evaluate()) {
            return blockTrue.step();
        } else {
            return blockFalse.step();
        }
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("if (").append(getProcedure()).append(")").append("{\n");
        blockTrue.toString(ident, sb);
        sb.append(ident).append("}");

        if (blockFalse.size() > 1 && !(blockFalse.get(0) instanceof DummyBlock)) {
            sb.append(" else {\n");
            blockFalse.toString(ident, sb);
            sb.append(ident).append("}\n");
        } else {
            sb.append("\n");
        }
    }

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k) {
        //tmp = Command.getBounds(this, tmp, j, k, Ix, Iy, a);
        tmp = super.getBounds(tmp, j, k);

        Rectangle2D.Double p = new Rectangle2D.Double();
        p.setRect(tmp);
        double bfh;
        double bth;
        double width;
        //false
        p = getBlockFalse().getBounds(p, j, k);
        bfh = p.height;
        tmp.x = (p.x < tmp.x) ? p.x : tmp.x;
        width = p.getMaxX();
        tmp.width = p.width;
        //true
        p = getBlockTrue().getBounds(p, j, k);
        bth = p.height;
        tmp.x = (p.x < tmp.x) ? p.x : tmp.x;
        tmp.width += p.width + 2 * k;
        width -= p.getMinX();
        //tmp.x += -(tmp.width - p.width - k) + tmp.width / 2 - p.width;

        tmp.height += GF_J * .6;
        tmp.height += (bfh > bth) ? bfh : bth;

        tmp.width = width;

        return tmp;
    }
//    @Override
//    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k, boolean a) {
//        //tmp = Command.getBounds(this, tmp, j, k, Ix, Iy, a);
//        tmp = super.getBounds(tmp, j, k, a);
//
//        Rectangle2D.Double p = new Rectangle2D.Double();
//        p.setRect(tmp);
//        //false
//        p = getBlockFalse().getBounds(p, j, k, a);
//        tmp.add(p);
//        //true
//        p = getBlockTrue().getBounds(p, j, k, a);
//        tmp.add(p);
//
//        return tmp;
//    }

    @Override
    public void ident(double x, double y, double j, double k) {

        Rectangle2D.Double btb = blockTrue.getBounds(null, j, k);
        Rectangle2D.Double bfb = blockFalse.getBounds(null, j, k);
        double w = btb.width + 2 * k + bfb.width;
        double pbtx = -btb.width - k;
        double pbfx = bfb.width / 2 + k;

        Rectangle2D.Double t = null;
        if (this instanceof GraphicResource) {
            GraphicObject d = ((GraphicResource) this).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (t != null) {
            double cw = t.width;
            double ch = t.height;

            double px = x - cw / 2;
            double py = y;

            if (this instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) this).getDrawableResource();

                if (d != null) {
                    d.setLocation(px, py);
//                    System.out.println(this + " [" + px + "," + py + "]");
                }
            }

            y += ch + j * .6;
        }

        Rectangle2D.Double btbs = blockTrue.get(0).getDrawableResource().getObjectBouds();
        Rectangle2D.Double bfbs = blockFalse.get(0).getDrawableResource().getObjectBouds();
        double pbt = Math.abs(btb.x - btbs.getCenterX());
        double pbf = Math.abs(bfb.x - bfbs.getCenterX());
        blockTrue.ident(x - (btb.width - pbt) - k / 2, y, j, k);
        blockFalse.ident(x + pbf + k / 2, y, j, k);

        y += ((bfb.height > btb.height) ? bfb.height : btb.height) + j;
        if (getNext() != null) {
            getNext().ident(x, y, j, k);
        }
    }

    @Override
    public Item getItem() {
        Area myShape = new Area();
        Polygon tmpPoli = new Polygon();
        tmpPoli.addPoint(10, 0);
        tmpPoli.addPoint(20, 10);
        tmpPoli.addPoint(10, 20);
        tmpPoli.addPoint(0, 10);
        myShape.add(new Area(tmpPoli));
        tmpPoli.reset();
        tmpPoli.addPoint(10, 3);
        tmpPoli.addPoint(17, 10);
        tmpPoli.addPoint(10, 17);
        tmpPoli.addPoint(3, 10);
        myShape.subtract(new Area(tmpPoli));
        tmpPoli.reset();
        tmpPoli.addPoint(10, 6);
        tmpPoli.addPoint(14, 10);
        tmpPoli.addPoint(10, 14);
        tmpPoli.addPoint(6, 10);
        myShape.add(new Area(tmpPoli));
        return new Item("Condicional", myShape, myColor);
    }

    @Override
    public Object createInstance() {
        return new If();
    }

    @Override
    public Procedure copy(Procedure copy) {
        Procedure p = super.copy(copy);

        if (copy instanceof If) {
            blockTrue.copy(((If) copy).blockTrue);
            blockFalse.copy(((If) copy).blockFalse);
        } else {
            System.out.println("Erro ao copiar: ");
            print();
        }

        return p;
    }

    public static MutableWidgetContainer createDrawableIf(final Procedure p, final String def) {

        final String[] comparadores = {"==", "!=", "<", "<=", ">", ">="};
        final String[] proximos = {" ", "&&", "||"};
        final int TEXTFIELD_WIDTH = 80;
        final int TEXTFIELD_HEIGHT = 25;
        final int COMBOBOX_WIDTH = 55;
        final int COMBOBOX_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine(20) {
            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {
                labels.add(new TextLabel(container.getName() + ":", 20, true));
            }
        };
        //LINES
        int textFieldLineWidth = 6 * INSET_X + 2 * TEXTFIELD_WIDTH + COMBOBOX_WIDTH + 2 * BUTTON_WIDTH;
        int textFieldLineHeight = 3 * INSET_Y + TEXTFIELD_HEIGHT + COMBOBOX_HEIGHT;
        final WidgetLine conditionalLine = new WidgetLine(textFieldLineWidth, textFieldLineHeight) {
            @Override
            protected void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container, Object data) {
                JTextField primeiro = new JTextField();
                JTextField segundo = new JTextField();
                JComboBox comparacao = new JComboBox(comparadores);
                final JComboBox proximo = new JComboBox(proximos);
                proximo.setName("pox");
                JComboBox combobox1 = new JComboBox();
                JComboBox combobox2 = new JComboBox();
                boolean exp1 = true, exp2 = true;

                MutableWidgetContainer.setAutoFillComboBox(combobox1, p);
                MutableWidgetContainer.setAutoFillComboBox(combobox2, p);

                final boolean hasNext;

                if (data instanceof Object[]) {
                    if (((Object[]) data)[0] instanceof String[]) {
                        String[] strArray = (String[]) ((Object[]) data)[0];
                        if (strArray.length == 1) {
                            String str1 = strArray[0].trim();
                            Object o = null;
                            for (int i = 0; i < combobox1.getItemCount(); i++) {
                                o = combobox1.getItemAt(i);
                                if (o != null && o.toString().equals(str1)) {
                                    combobox1.setSelectedIndex(i);
                                    exp1 = false;
                                    break;
                                } else {
                                    o = null;
                                }
                            }
                            if (o == null) {
                                primeiro.setText(str1);
                            }
                        } else if (strArray.length == 2) {
                            String str1 = strArray[0].trim();
                            Object o = null;
                            for (int i = 0; i < combobox1.getItemCount(); i++) {
                                o = combobox1.getItemAt(i);
                                if (o != null && o.toString().equals(str1)) {
                                    combobox1.setSelectedIndex(i);
                                    exp1 = false;
                                    break;
                                } else {
                                    o = null;
                                }
                            }
                            if (o == null) {
                                primeiro.setText(str1);
                            }

                            String str2 = strArray[1].trim();
                            o = null;
                            for (int i = 0; i < combobox2.getItemCount(); i++) {
                                o = combobox2.getItemAt(i);
                                if (o != null && o.toString().equals(str2)) {
                                    combobox2.setSelectedIndex(i);
                                    exp2 = false;
                                    break;
                                } else {
                                    o = null;
                                }
                            }
                            if (o == null) {
                                segundo.setText(str2);
                            }
                        }
                    }
                    if (((Object[]) data)[1] instanceof Integer) {
                        int cmp = (Integer) ((Object[]) data)[1];
                        if (cmp >= 0) {
                            comparacao.setSelectedIndex(cmp);
                        }
                    }
                    if (((Object[]) data)[2] instanceof Integer) {
                        int op = (Integer) ((Object[]) data)[2];
                        if (op >= 0) {
                            proximo.setSelectedIndex(op);
                            hasNext = true;
                        } else {
                            hasNext = false;
                        }
                    } else {
                        hasNext = false;
                    }
                } else {
                    hasNext = false;
                }

                final WidgetLine thisConditionalLine = this;

                proximo.addActionListener(new ActionListener() {
                    private boolean next = hasNext;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object o = proximo.getSelectedItem();
                        if (o instanceof String) {
                            if (!((String) o).trim().isEmpty()) {
                                if (!next) {
                                    container.addLine(thisConditionalLine, "");
                                    next = true;
                                }
                            } else if (next) {
                                int index = container.getLineIndex(thisConditionalLine);
                                while (container.hasLine(index)) {
                                    container.removeLine(index);
                                    index++;
                                }
                                next = false;
                            }
                        }
                    }
                });

                int x = INSET_X;
                int y = INSET_Y;

                final JButton changeButton1 = new JButton();
                final JButton changeButton2 = new JButton();
                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
                changeButton1.setIcon(icon);
                changeButton1.setToolTipText("Selecionar variável");
                changeButton2.setIcon(icon);
                changeButton2.setToolTipText("Selecionar variável");

                final Widget wcombobox1 = new Widget(combobox1, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final Widget wtextfield1 = new Widget(primeiro, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wcombobox1);
                widgets.add(wtextfield1);
                x += TEXTFIELD_WIDTH + INSET_X;

                widgets.add(new Widget(changeButton1, x, y, BUTTON_WIDTH, BUTTON_WIDTH));
                x += BUTTON_WIDTH + INSET_X;

                widgets.add(new Widget(comparacao, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
                x += COMBOBOX_WIDTH + INSET_X;

                widgets.add(new Widget(changeButton2, x, y, BUTTON_WIDTH, BUTTON_WIDTH));
                x += BUTTON_WIDTH + INSET_X;

                final Widget wcombobox2 = new Widget(combobox2, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                final Widget wtextfield2 = new Widget(segundo, x, y, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
                widgets.add(wcombobox2);
                widgets.add(wtextfield2);
                x += TEXTFIELD_WIDTH + INSET_X;

                x = TEXTFIELD_WIDTH + 3 * INSET_X + BUTTON_WIDTH;
                y += COMBOBOX_HEIGHT + INSET_Y;
                widgets.add(new Widget(proximo, x, y, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));

                changeButton1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (container.contains(wtextfield1)) {
                            container.removeWidget(wtextfield1);
                            container.addWidget(wcombobox1);
                        } else {
                            container.removeWidget(wcombobox1);
                            container.addWidget(wtextfield1);
                        }
                    }
                });

                changeButton2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (container.contains(wtextfield2)) {
                            container.removeWidget(wtextfield2);
                            container.addWidget(wcombobox2);
                        } else {
                            container.removeWidget(wcombobox2);
                            container.addWidget(wtextfield2);
                        }
                    }
                });

                wtextfield1.setDynamic(true);
                wcombobox1.setDynamic(true);
                wtextfield2.setDynamic(true);
                wcombobox2.setDynamic(true);

                if (exp1) {
                    container.addWidget(wtextfield1);
                } else {
                    container.addWidget(wcombobox1);
                }

                if (exp2) {
                    container.addWidget(wtextfield2);
                } else {
                    container.addWidget(wcombobox2);
                }

            }

            @Override
            public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, final MutableWidgetContainer container) {
                if (widgets.size() >= 6) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        Iterator<Widget> iterator = widgets.iterator();
                        String str;
                        Widget tmpWidget;
                        JComponent jComponent;
                        //JTextField 1
                        tmpWidget = iterator.next();
                        if (!container.contains(tmpWidget)) {
                            tmpWidget = iterator.next();
                        }
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JTextField) {
                            str = ((JTextField) jComponent).getText();
                            if (!str.isEmpty()) {
                                sb.append(" ");
                                sb.append(str.trim());
                                sb.append(" ");
                            } else {
                                return def;
                            }
                        } else if (jComponent instanceof JComboBox) {
                            JComboBox cb = (JComboBox) jComponent;
                            Object o = cb.getSelectedItem();
                            if (o != null) {
                                sb.append(o.toString());
                                sb.append(" ");
                            } else {
                                return def;
                            }
                        }
                        //JButton 1
                        tmpWidget = iterator.next();
                        if (!(tmpWidget.getJComponent() instanceof JButton)) {
                            iterator.next();
                        }
                        //JComboBox 1
                        tmpWidget = iterator.next();
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JComboBox) {
                            str = ((JComboBox) jComponent).getSelectedItem().toString();
                            if (!str.isEmpty()) {
                                sb.append(str);
                                sb.append(" ");
                            }
                        }
                        //JButton 2
                        iterator.next();
                        //JTextField 2
                        boolean skip = false;
                        tmpWidget = iterator.next();
                        if (!container.contains(tmpWidget)) {
                            tmpWidget = iterator.next();
                            skip = true;
                        }
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JTextField) {
                            str = ((JTextField) jComponent).getText();
                            if (!str.isEmpty()) {
                                sb.append(str.trim());
                                sb.append(" ");
                            } else {
                                return def;
                            }
                        } else if (jComponent instanceof JComboBox) {
                            JComboBox cb = (JComboBox) jComponent;
                            Object o = cb.getSelectedItem();
                            if (o != null) {
                                sb.append(o.toString());
                                sb.append(" ");
                            } else {
                                return def;
                            }
                        }
                        //JComboBox 2
                        tmpWidget = iterator.next();
                        if (!container.contains(tmpWidget)) {
                            tmpWidget = iterator.next();
                        }
                        jComponent = tmpWidget.getJComponent();
                        if (jComponent instanceof JComboBox) {
                            str = ((JComboBox) jComponent).getSelectedItem().toString();
                            if (!str.isEmpty()) {
                                sb.append(str);
                            }
                        }
                        return sb.toString().trim();
                    } catch (NoSuchElementException e) {
                        System.out.println("ERROR!");
                    }
                }
                return def;
            }
        };
        DrawableCommandBlock dcb = new DrawableCommandBlock(p, Color.gray) {
            private Polygon myShape = new Polygon();
            public static final int EXTENDED_HEIGHT = 15;
            public static final int SIMPLE_HEIGHT = 18;
            public static final int SIMPLE_WIDTH = 22;

            {
                string = p.getProcedure();
                updateLines();
                center = true;
            }

            @Override
            public void updateLines() {
                //exclui todas as linhas
                clear();

                //adiciona cabeçalho
                addLine(headerLine, null);

                //tenta decodificar string
                try {
                    //aqui é onde vai ser armazendado os dados de cada linha
                    Object[] data = null;
                    int op = -1;
                    int cmp = -1;
                    String[] expressions;
                    boolean and;
                    boolean andEnd;
                    boolean or;
                    boolean orEnd;

                    for (String str0 : string.split("&&")) {
                        and = (str0.length() == string.length());
                        andEnd = string.endsWith(str0);
                        for (String str : str0.split("\\|\\|")) {
                            or = (str.length() == str0.length());
                            orEnd = str0.endsWith(str);

                            if (!or && !orEnd) {
                                // ||
                                op = 2;
                            } else if (!and && !andEnd) {
                                // &&
                                op = 1;
                            } else if (andEnd && orEnd) {
                                // ??
                                op = 0;
                            } else {
                                System.err.println("IF parse Error!");
                            }

                            for (int i = 0; i < comparadores.length; i++) {
                                if (str.contains(comparadores[i])) {
                                    cmp = i;
                                }
                            }

                            expressions = str.split(comparadores[cmp]);

                            data = new Object[]{expressions, cmp, op};
                            //adiciona uma nova linha com dados
                            addLine(conditionalLine, data);
                        }
                    }
                    return;
                } catch (Exception e) {
                }

                //adiciona uma nova linha sem dados
                addLine(conditionalLine, null);
//                p.setProcedure(string);
            }

            @Override
            public Shape updateShape(Rectangle2D bounds) {
                myShape.reset();

                if (isWidgetVisible()) {
                    shapeStartX = 0;
                    shapeStartY = EXTENDED_HEIGHT;
                    myShape.addPoint((int) bounds.getCenterX(), 0);
                    myShape.addPoint((int) bounds.getMaxX(), EXTENDED_HEIGHT);
                    myShape.addPoint((int) bounds.getMaxX(), (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                    myShape.addPoint((int) bounds.getCenterX(), (int) bounds.getMaxY() + 2 * EXTENDED_HEIGHT);
                    myShape.addPoint(0, (int) bounds.getMaxY() + EXTENDED_HEIGHT);
                    myShape.addPoint(0, EXTENDED_HEIGHT);
                } else {
                    shapeStartX = SIMPLE_WIDTH;
                    shapeStartY = SIMPLE_HEIGHT;

                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, 0);
                    myShape.addPoint((int) bounds.getMaxX() + 2 * SIMPLE_WIDTH, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, (int) bounds.getMaxY() + 2 * SIMPLE_HEIGHT);
                    myShape.addPoint(0, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
                }
                return myShape; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getString() {
                String str = super.getString();
                if (str.trim().length() <= 2) {
                    str = getName();
                    p.setProcedure(def);
                } else {
                    p.setProcedure(str);
                }
                return str;
            }

            @Override
            public void splitString(String original, Collection<String> splitted) {

                boolean and;
                boolean andEnd;
                boolean or;
                boolean orEnd;

                for (String str0 : string.split("&&")) {
                    and = (str0.length() == string.length());
                    andEnd = string.endsWith(str0);
                    for (String str : str0.split("\\|\\|")) {
                        or = (str.length() == str0.length());
                        orEnd = str0.endsWith(str);
                        str = str.trim();
                        splitted.add(str);

                        if (!or && !orEnd) {
                            splitted.add("||");
                        } else if (!and && !andEnd) {
                            splitted.add("&&");
                        }
                    }
                }
            }
        };

        return dcb;
    }

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            MutableWidgetContainer mwc = If.createDrawableIf(this, "0");
            mwc.setName("If");
            mwc.setColor(myColor);
            resource = mwc;
        }
        return resource;
    }

    @Override
    public void drawLines(Graphics2D g) {
        if (resource != null) {
            Path2D.Double path = new Path2D.Double();
            Rectangle2D.Double bThis = resource.getObjectBouds();
            Rectangle2D.Double bBlock = getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            Rectangle2D.Double bTrueStart = getBlockTrue().start.getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            if (getBlockTrue().start instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) getBlockTrue().start).getDrawableResource();

                if (d != null) {
                    bTrueStart = (Rectangle2D.Double) d.getObjectBouds();
                }
            }

            Rectangle2D.Double bTrueB = getBlockTrue().getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            Rectangle2D.Double bFalseStart = getBlockFalse().start.getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            Rectangle2D.Double bTrueEnd = getBlockTrue().getEnd().getPrevious().getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            if (getBlockTrue().getEnd().getPrevious() instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) getBlockTrue().getEnd().getPrevious()).getDrawableResource();

                if (d != null) {
                    bTrueEnd = (Rectangle2D.Double) d.getObjectBouds();
                }
            }

            Rectangle2D.Double bFalseEnd = getBlockFalse().getEnd().getPrevious().getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            if (getBlockFalse().getEnd().getPrevious() instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) getBlockFalse().getEnd().getPrevious()).getDrawableResource();

                if (d != null) {
                    bFalseEnd = (Rectangle2D.Double) d.getObjectBouds();
                }
            }

            if (getBlockFalse().start instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) getBlockFalse().start).getDrawableResource();

                if (d != null) {
                    bFalseStart = (Rectangle2D.Double) d.getObjectBouds();
                }
            }

            Rectangle2D.Double bFalseB = getBlockFalse().getBounds(null,
                    GraphicFlowchart.GF_J,
                    GraphicFlowchart.GF_K);

            //true
            g.drawString("V", (int) bTrueStart.getCenterX() - 3, (int) bThis.getCenterY() - 4);

            path.moveTo(bThis.getCenterX(), bThis.getCenterY());
            path.lineTo(bTrueStart.getCenterX(), bThis.getCenterY());
            path.lineTo(bTrueStart.getCenterX(), bTrueB.getMinY());

            path.moveTo(bTrueEnd.getCenterX(), bTrueB.getMaxY() - 2 * GF_J);
            path.lineTo(bTrueEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            //false
            g.drawString("F", (int) bFalseStart.getCenterX() - 3, (int) bThis.getCenterY() - 4);

            path.moveTo(bThis.getCenterX(), bThis.getCenterY());
            path.lineTo(bFalseStart.getCenterX(), bThis.getCenterY());
            path.lineTo(bFalseStart.getCenterX(), bFalseB.getMinY());

            path.moveTo(bFalseEnd.getCenterX(), bFalseB.getMaxY() - 2 * GF_J);
            path.lineTo(bFalseEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            //linha final
            path.moveTo(bTrueEnd.getCenterX(), bBlock.getMaxY() - GF_J);
            path.lineTo(bFalseEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            Command c = getNext();
            if (c instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    path.moveTo(bThis.getCenterX(), bBlock.getMaxY() - GF_J);
                    path.lineTo(bThis.getCenterX(), bBlock.getMaxY());
                }
            }
            g.draw(path);
        }
    }

    public static void main(String args[]) {
        If iiff = new If();
        QuickFrame.drawTest(iiff.getDrawableResource());

    }
}
