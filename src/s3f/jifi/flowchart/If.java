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
package s3f.jifi.flowchart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import s3f.core.plugin.PluginManager;
import s3f.jifi.core.Command;
import s3f.jifi.core.FlowchartPanel;
import s3f.jifi.core.FlowchartPanel.TmpVar;
import s3f.jifi.core.GraphicFlowchart;
import static s3f.jifi.core.GraphicFlowchart.GF_J;
import s3f.jifi.core.interpreter.ExecutionException;
import s3f.jifi.core.interpreter.ResourceManager;
import s3f.jifi.core.parser.parameterparser.Argument;
import s3f.magenta.GraphicObject;
import s3f.magenta.graphicresource.GraphicResource;
import s3f.magenta.sidepanel.Item;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.WidgetLine;
import s3f.magenta.util.QuickFrame;

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
    public boolean perform(ResourceManager rm) throws ExecutionException {
        return true;
    }

    TmpVar ifValue = new TmpVar();

    @Override
    public Command step(ResourceManager rm) throws ExecutionException {
        if (ifValue.countObservers() == 0) {
            FlowchartPanel flowcharPanel = rm.getResource(FlowchartPanel.class);
            flowcharPanel.pushVar(ifValue);
        }
        //calcula o valor da expressão
        if (evaluate(rm)) {
            ifValue.setValue("true");
            return blockTrue.step(rm);
        } else {
            ifValue.setValue("false");
            return blockFalse.step(rm);
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

//        double w = btb.width + 2 * k + bfb.width;
//        double pbtx = -btb.width - k;
//        double pbfx = bfb.width / 2 + k;
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
        return new Item(PluginManager.getText("JIFI", "flowchart.if"), myShape, myColor, "Divide o fluxo em dois caminhos, um deles é escolhido pelo valor da condição fornecida");
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

    public static MutableWidgetContainer createSimpleIf(final Procedure p, final String def) {
        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine(20) {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel(container.getName() + ":", true));
                components.add(new SubLineBreak());
                createGenericField(p, p.getArg(0), "Condição:", 120, 25, components, container, ARG_TEXTFIELD);
                components.add(new SubLineBreak(true));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
                if (arguments.size() > 0) {
                    sb.append(arguments.get(0).toString());
                }
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(p, Color.gray) {
            private Polygon myShape = new Polygon();
            public static final int EXTENDED_HEIGHT = 15;
            public static final int SIMPLE_HEIGHT = 18;
            public static final int SIMPLE_WIDTH = 22;

            @Override
            public void updateStructure() {
                clear();
                addLine(headerLine);
            }

            @Override
            public String getBoxLabel() {
                super.getBoxLabel(); //utilizado para atualizar os argumentos a partir da caixa de texto
                p.setProcedure(p.getArg(0).toString());
                boxLabel = p.getProcedure();
                if (boxLabel.equals(def) || boxLabel.trim().isEmpty()) {
                    boxLabel = getName();
                    p.setProcedure(def);
                }
                p.getArg(0).set(p.getProcedure(), Argument.EXPRESSION);
                return boxLabel;
            }

            @Override
            public void splitBoxLabel(String original, Collection<String> splitted) {
                splitted.add(original);
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
        };

        return dcb;
    }

//    public static MutableWidgetContainer createDrawableIf(final Procedure p, final String def) {
//
//        final String[] comparadores = {"==", "!=", "<", "<=", ">", ">="};
//        final String[] proximos = {" ", "&&", "||"};
//        final int TEXTFIELD_WIDTH = 80;
//        final int TEXTFIELD_HEIGHT = 25;
//        final int COMBOBOX_WIDTH = 55;
//        final int COMBOBOX_HEIGHT = 25;
//        final int BUTTON_WIDTH = 25;
//        //HEADER LINE
//        final WidgetLine headerLine = new WidgetLine(20) {
//            @Override
//            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
//                components.add(new TextLabel(container.getName() + ":", true));
//            }
//        };
//        //LINES
//        final WidgetLine conditionalLine = new WidgetLine() {
//            @Override
//            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
//                JTextField primeiro = new JTextField();
//                JTextField segundo = new JTextField();
//                JComboBox comparacao = new JComboBox(comparadores);
//                final JComboBox proximo = new JComboBox(proximos);
//                proximo.setName("pox");
//                JComboBox combobox1 = new JComboBox();
//                JComboBox combobox2 = new JComboBox();
//                boolean exp1 = true, exp2 = true;
//
//                MutableWidgetContainer.setAutoFillComboBox(combobox1, p);
//                MutableWidgetContainer.setAutoFillComboBox(combobox2, p);
//
//                final boolean hasNext = false;
//
//                final WidgetLine thisConditionalLine = this;
//
//                proximo.addActionListener(new ActionListener() {
//                    private boolean next = hasNext;
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        Object o = proximo.getSelectedItem();
//                        if (o instanceof String) {
//                            if (!((String) o).trim().isEmpty()) {
//                                if (!next) {
//                                    container.addLine(thisConditionalLine);
//                                    next = true;
//                                }
//                            } else if (next) {
//                                int index = container.getLineIndex(thisConditionalLine);
//                                while (container.hasLine(index)) {
//                                    container.removeLine(index);
//                                    index++;
//                                }
//                                next = false;
//                            }
//                        }
//                    }
//                });
//
//                final JButton changeButton1 = new JButton();
//                final JButton changeButton2 = new JButton();
//                ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
//                changeButton1.setIcon(icon);
//                changeButton1.setToolTipText("Selecionar variável");
//                changeButton2.setIcon(icon);
//                changeButton2.setToolTipText("Selecionar variável");
//
//                final Widget wcombobox1 = new Widget(combobox1, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                final Widget wtextfield1 = new Widget(primeiro, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                components.add(wcombobox1);
//                components.add(wtextfield1);
//
//                components.add(new Widget(changeButton1, BUTTON_WIDTH, BUTTON_WIDTH));
//
//                components.add(new Widget(comparacao, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
//
//                components.add(new Widget(changeButton2, BUTTON_WIDTH, BUTTON_WIDTH));
//
//                final Widget wcombobox2 = new Widget(combobox2, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                final Widget wtextfield2 = new Widget(segundo, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
//                components.add(wcombobox2);
//                components.add(wtextfield2);
//
//                components.add(new SubLineBreak());
//
//                components.add(new Space(111));
//
//                components.add(new Widget(proximo, COMBOBOX_WIDTH, COMBOBOX_HEIGHT));
//
////                components.add(new SubLineBreak(false));
//                changeButton1.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (container.contains(wtextfield1)) {
//                            container.removeWidget(wtextfield1);
//                            container.addWidget(wcombobox1);
//                        } else {
//                            container.removeWidget(wcombobox1);
//                            container.addWidget(wtextfield1);
//                        }
//                    }
//                });
//
//                changeButton2.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (container.contains(wtextfield2)) {
//                            container.removeWidget(wtextfield2);
//                            container.addWidget(wcombobox2);
//                        } else {
//                            container.removeWidget(wcombobox2);
//                            container.addWidget(wtextfield2);
//                        }
//                    }
//                });
//
//                wtextfield1.setDynamic(true);
//                wcombobox1.setDynamic(true);
//                wtextfield2.setDynamic(true);
//                wcombobox2.setDynamic(true);
//
//                if (exp1) {
//                    container.addWidget(wtextfield1);
//                } else {
//                    container.addWidget(wcombobox1);
//                }
//
//                if (exp2) {
//                    container.addWidget(wtextfield2);
//                } else {
//                    container.addWidget(wcombobox2);
//                }
//
//            }
//
//            @Override
//            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
////                if (arguments.size() >= 6) {
////                    try {
////                        Iterator<Argument> iterator = arguments.iterator();
////                        String str;
////                        Argument tmpWidget;
////                        //JTextField 1
////                        tmpWidget = iterator.next();
////                        if (!container.contains(tmpWidget)) {
////                            tmpWidget = iterator.next();
////                        }
////                        jComponent = tmpWidget.getJComponent();
////                        if (jComponent instanceof JTextField) {
////                            str = ((JTextField) jComponent).getText();
////                            if (!str.isEmpty()) {
////                                sb.append(" ");
////                                sb.append(str.trim());
////                                sb.append(" ");
////                            } else {
////                                return def;
////                            }
////                        } else if (jComponent instanceof JComboBox) {
////                            JComboBox cb = (JComboBox) jComponent;
////                            Object o = cb.getSelectedItem();
////                            if (o != null) {
////                                sb.append(o.toString());
////                                sb.append(" ");
////                            } else {
////                                return def;
////                            }
////                        }
////                        //JButton 1
////                        tmpWidget = iterator.next();
////                        if (!(tmpWidget.getJComponent() instanceof JButton)) {
////                            iterator.next();
////                        }
////                        //JComboBox 1
////                        tmpWidget = iterator.next();
////                        jComponent = tmpWidget.getJComponent();
////                        if (jComponent instanceof JComboBox) {
////                            str = ((JComboBox) jComponent).getSelectedItem().toString();
////                            if (!str.isEmpty()) {
////                                sb.append(str);
////                                sb.append(" ");
////                            }
////                        }
////                        //JButton 2
////                        iterator.next();
////                        //JTextField 2
////                        boolean skip = false;
////                        tmpWidget = iterator.next();
////                        if (!container.contains(tmpWidget)) {
////                            tmpWidget = iterator.next();
////                            skip = true;
////                        }
////                        jComponent = tmpWidget.getJComponent();
////                        if (jComponent instanceof JTextField) {
////                            str = ((JTextField) jComponent).getText();
////                            if (!str.isEmpty()) {
////                                sb.append(str.trim());
////                                sb.append(" ");
////                            } else {
////                                return def;
////                            }
////                        } else if (jComponent instanceof JComboBox) {
////                            JComboBox cb = (JComboBox) jComponent;
////                            Object o = cb.getSelectedItem();
////                            if (o != null) {
////                                sb.append(o.toString());
////                                sb.append(" ");
////                            } else {
////                                return def;
////                            }
////                        }
////                        //JComboBox 2
////                        tmpWidget = iterator.next();
////                        if (!container.contains(tmpWidget)) {
////                            tmpWidget = iterator.next();
////                        }
////                        jComponent = tmpWidget.getJComponent();
////                        if (jComponent instanceof JComboBox) {
////                            str = ((JComboBox) jComponent).getSelectedItem().toString();
////                            if (!str.isEmpty()) {
////                                sb.append(str);
////                            }
////                        }
////                        return sb.toString().trim();
////                    } catch (NoSuchElementException e) {
////                        System.out.println("ERROR!");
////                    }
////                }
////                return def;
////            }
//                sb.append(def);
//            }
//        };
//
//        DrawableProcedureBlock dcb = new DrawableProcedureBlock(p, Color.gray) {
//            private Polygon myShape = new Polygon();
//            public static final int EXTENDED_HEIGHT = 15;
//            public static final int SIMPLE_HEIGHT = 18;
//            public static final int SIMPLE_WIDTH = 22;
//
//            {
//                boxLabel = p.getProcedure();
//                updateStructure();
//                center = true;
//            }
//
//            @Override
//            public void updateStructure() {
//                //exclui todas as linhas
//                clear();
//
//                //adiciona cabeçalho
//                addLine(headerLine);
//
//                //tenta decodificar string
//                try {
//                    //aqui é onde vai ser armazendado os dados de cada linha
//                    Object[] data = null;
//                    int op = -1;
//                    int cmp = -1;
//                    String[] expressions;
//                    boolean and;
//                    boolean andEnd;
//                    boolean or;
//                    boolean orEnd;
//
//                    for (String str0 : boxLabel.split("&&")) {
//                        and = (str0.length() == boxLabel.length());
//                        andEnd = boxLabel.endsWith(str0);
//                        for (String str : str0.split("\\|\\|")) {
//                            or = (str.length() == str0.length());
//                            orEnd = str0.endsWith(str);
//
//                            if (!or && !orEnd) {
//                                // ||
//                                op = 2;
//                            } else if (!and && !andEnd) {
//                                // &&
//                                op = 1;
//                            } else if (andEnd && orEnd) {
//                                // ??
//                                op = 0;
//                            } else {
//                                System.err.println("IF parse Error!");
//                            }
//
//                            for (int i = 0; i < comparadores.length; i++) {
//                                if (str.contains(comparadores[i])) {
//                                    cmp = i;
//                                }
//                            }
//
//                            expressions = str.split(comparadores[cmp]);
//
//                            data = new Object[]{expressions, cmp, op};
//                            //adiciona uma nova linha com dados
//                            addLine(conditionalLine);
//                        }
//                    }
//                    return;
//                } catch (Exception e) {
//                }
//
//                //adiciona uma nova linha sem dados
//                addLine(conditionalLine);
////                p.setProcedure(string);
//            }
//
//            @Override
//            public Shape updateShape(Rectangle2D bounds) {
//                myShape.reset();
//
//                if (isWidgetVisible()) {
//                    shapeStartX = 0;
//                    shapeStartY = EXTENDED_HEIGHT;
//                    myShape.addPoint((int) bounds.getCenterX(), 0);
//                    myShape.addPoint((int) bounds.getMaxX(), EXTENDED_HEIGHT);
//                    myShape.addPoint((int) bounds.getMaxX(), (int) bounds.getMaxY() + EXTENDED_HEIGHT);
//                    myShape.addPoint((int) bounds.getCenterX(), (int) bounds.getMaxY() + 2 * EXTENDED_HEIGHT);
//                    myShape.addPoint(0, (int) bounds.getMaxY() + EXTENDED_HEIGHT);
//                    myShape.addPoint(0, EXTENDED_HEIGHT);
//                } else {
//                    shapeStartX = SIMPLE_WIDTH;
//                    shapeStartY = SIMPLE_HEIGHT;
//
//                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, 0);
//                    myShape.addPoint((int) bounds.getMaxX() + 2 * SIMPLE_WIDTH, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
//                    myShape.addPoint((int) bounds.getCenterX() + SIMPLE_WIDTH, (int) bounds.getMaxY() + 2 * SIMPLE_HEIGHT);
//                    myShape.addPoint(0, (int) bounds.getCenterY() + SIMPLE_HEIGHT);
//                }
//                return myShape; //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public String getBoxLabel() {
//                String str = super.getBoxLabel();
//                if (str.trim().length() <= 2) {
//                    str = getName();
//                    p.setProcedure(def);
//                } else {
//                    p.setProcedure(str);
//                }
//                return str;
//            }
//
//            @Override
//            public void splitBoxLabel(String original, Collection<String> splitted) {
//
//                boolean and;
//                boolean andEnd;
//                boolean or;
//                boolean orEnd;
//
//                for (String str0 : boxLabel.split("&&")) {
//                    and = (str0.length() == boxLabel.length());
//                    andEnd = boxLabel.endsWith(str0);
//                    for (String str : str0.split("\\|\\|")) {
//                        or = (str.length() == str0.length());
//                        orEnd = str0.endsWith(str);
//                        str = str.trim();
//                        splitted.add(str);
//
//                        if (!or && !orEnd) {
//                            splitted.add("||");
//                        } else if (!and && !andEnd) {
//                            splitted.add("&&");
//                        }
//                    }
//                }
//            }
//        };
//
//        return dcb;
//    }
    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            MutableWidgetContainer mwc = If.createSimpleIf(this, "0");
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
            g.drawString("T", (int) bThis.getMinX() - 12, (int) bThis.getCenterY() - 5);

            path.moveTo(bThis.getCenterX(), bThis.getCenterY());
            path.lineTo(bTrueStart.getCenterX(), bThis.getCenterY());
            path.lineTo(bTrueStart.getCenterX(), bTrueB.getMinY());

            path.moveTo(bTrueEnd.getCenterX(), bTrueB.getMaxY() - 2 * GF_J);
            path.lineTo(bTrueEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            //false
            g.drawString("F", (int) bThis.getMaxX() + 5, (int) bThis.getCenterY() - 5);

            path.moveTo(bThis.getCenterX(), bThis.getCenterY());
            path.lineTo(bFalseStart.getCenterX(), bThis.getCenterY());
            path.lineTo(bFalseStart.getCenterX(), bFalseB.getMinY());

            path.moveTo(bFalseEnd.getCenterX(), bFalseB.getMaxY() - 2 * GF_J);
            path.lineTo(bFalseEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            //linha final
            path.moveTo(bTrueEnd.getCenterX(), bBlock.getMaxY() - GF_J);
            path.lineTo(bFalseEnd.getCenterX(), bBlock.getMaxY() - GF_J);

            GraphicObject d = null;
            Command c = getNext();
            if (c instanceof GraphicResource) {
                d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    path.moveTo(bThis.getCenterX(), bBlock.getMaxY() - GF_J);
                    path.lineTo(bThis.getCenterX(), bBlock.getMaxY());
                }
            }
            g.draw(path);
            drawArrow(g, bTrueEnd.getCenterX(), bTrueStart.getMinY(), ARROW_DOWN);
            drawArrow(g, bFalseEnd.getCenterX(), bFalseStart.getMinY(), ARROW_DOWN);
            if (d != null) {
                drawArrow(g, bThis.getCenterX(), bBlock.getMaxY(), ARROW_DOWN);
            }
        }
    }

    public static void main(String args[]) {
        If iiff = new If();
        QuickFrame.drawTest(iiff.getDrawableResource());

    }
}
