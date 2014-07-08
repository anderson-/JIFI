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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JTextField;
import s3f.jifi.core.Command;
import static s3f.jifi.core.Command.identChar;
import s3f.jifi.core.GraphicFlowchart;
import s3f.jifi.core.parser.parameterparser.Argument;
import static s3f.jifi.flowchart.DummyBlock.SHAPE_ROUND_RECTANGLE;
import static s3f.jifi.flowchart.DummyBlock.createSimpleBlock;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.GraphicObject;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.WidgetLine;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.graphicresource.GraphicResource;
import s3f.magenta.swing.component.Component;
import s3f.jifi.core.interpreter.ResourceManager;

/**
 * Função com *futuro* suporte a argumentos. <### EM DESENVOLVIMENTO ###>
 */
public class Function extends Block {

    public class FunctionEnd extends BlockEnd {

        protected FunctionEnd() {
        }

        private GraphicObject resource = null;

        @Override
        public GraphicObject getDrawableResource() {
            if (resource == null) {
                resource = createSimpleBlock(this, "   END   ", Color.black, Color.decode("#631864"), SHAPE_ROUND_RECTANGLE);
            }
            return resource;
        }

        @Override
        public Command step(ResourceManager rm) {
            return null;
        }

        @Override
        public void toString(String ident, StringBuilder sb) {
        }
    }

    private String name = "prog";
    private ArrayList<String> args;

    public Function() {
        super.setEnd(new FunctionEnd());
        //super(new FunctionEnd());
        args = new ArrayList<>();
    }

    public Function(String name, String args) {
        this();
        this.name = name;
        updateFunction(name, args, this);
    }

    private static void updateFunction(String name, String args, Function f) {
        f.setName(name);
        f.getArgs().clear();
        if (args != null) {
            for (String arg : args.split(",")) {
                arg = arg.replace("var", "").trim();
//                f.getArgs().add(arg);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public Collection<String> getArgs() {
//        return args;
//    }
    public void addArgs(Collection<String> args) {
        this.args.addAll(args);
    }

    public void removeArg(String arg) {
        args.remove(arg);
    }

    @Override
    public boolean addBefore(Command c) {
        return true;
        //return super.addBegin(c);
    }

    @Override
    public boolean addAfter(Command c) {
        return true;
        //return super.addBegin(c);
    }

    public static final int ARR_SIZE = 7;

    public static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
//        g.setStroke(new BasicStroke(2));

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform o = (AffineTransform) g.getTransform().clone();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
//        g.backDraw(0, 0, len - ARR_SIZE, 0);
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, (int) (-ARR_SIZE * .5), (int) (ARR_SIZE * .5), 0}, 4);
        at.setToIdentity();
        g.setTransform(o);
    }

    public static void drawArrow(Graphics2D g, Line2D l, boolean drawLine) {
        double dx = l.getX2() - l.getX1();
        double dy = l.getY2() - l.getY1();
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform o = (AffineTransform) g.getTransform().clone();
        AffineTransform at = AffineTransform.getTranslateInstance(l.getX1(), l.getY1());
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        if (drawLine) {
            g.drawLine(0, 0, len - ARR_SIZE, 0);
        }
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, (int) (-ARR_SIZE * .5), (int) (ARR_SIZE * .5), 0}, 4);
        at.setToIdentity();
        g.setTransform(o);
    }

    private static Command boundaryTest(Point2D p, Command it) {
        if (it.getBounds(null, GraphicFlowchart.GF_J, GraphicFlowchart.GF_K).contains(p)) {
            return it;
        }
        return null;
    }

    public static Command find(Point2D p, Block b) {
        Command it = b.start;
        while (it != null) {

            if (it instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) it).getDrawableResource();
                if (d != null) {
                    if (d.getObjectShape().contains(p)) {
                        return it;
                    }
                }
            }

            if (it instanceof Block) {
                Command c = find(p, (Block) it);

                if (c != null) {
                    return c;
                }
                //TESTE
                //c = boundaryTest(p, it);
                if (c != null) {
                    return c;
                }

            } else if (it instanceof If) {
                Command c = find(p, ((If) it).getBlockTrue());
                if (c != null) {
                    return c;
                }
                c = find(p, ((If) it).getBlockFalse());
                if (c != null) {
                    return c;
                }
                //TESTE
                //c = boundaryTest(p, it);
                if (c != null) {
                    return c;
                }
            }

            it = it.getNext();
        }

        return null;
    }

    public Command find(Point2D p) {
        if (getDrawableResource().getObjectShape().contains(p)) {
            return this;
        }
        return Function.find(p, this);
    }

    Rectangle2D.Double shape = new Rectangle2D.Double();
    static ArrayList<Rectangle2D.Double> tmpBounds = new ArrayList<>();
    static ArrayList<String> tmpBoundsName = new ArrayList<>();

    public static Shape smothConer(Line2D.Double l1, Line2D.Double l2, double i) {
        GeneralPath gp = new GeneralPath();
        double s1 = Math.sqrt((l1.x2 - l1.x1) * (l1.x2 - l1.x1) + (l1.y2 - l1.y1) * (l1.y2 - l1.y1));
        double s2 = Math.sqrt((l2.x2 - l2.x1) * (l2.x2 - l2.x1) + (l2.y2 - l2.y1) * (l2.y2 - l2.y1));
        if (s1 > s2) {
            double t = s1;
            s1 = s2;
            s2 = t;
        }
        gp.moveTo(l1.x1, l1.y1);
        gp.lineTo((1 - i) * l1.x1 + i * l1.x2, (1 - i) * l1.y1 + i * l1.y2);
        i = ((s2 - s1) / s2) * (1 - i) + i;
        gp.quadTo(l1.x2, l1.y2, (1 - i) * l2.x2 + i * l2.x1, (1 - i) * l2.y2 + i * l2.y1);
        gp.lineTo(l2.x2, l2.y2);
        return gp;
    }

    @Override
    public Procedure copy(Procedure copy) {
        Procedure p = super.copy(copy);

        if (copy instanceof Function) {
            ((Function) copy).name = name;
            //TODO: copiar argumentos e etc
        } else {
            System.out.println("Erro ao copiar: ");
            print();
        }

        return p;
    }

    public Function copy() {
        return (Function) copy((Procedure) new Function());
    }

    @Override
    public void toString(String ident, StringBuilder sb) {
        sb.append(ident).append("func ").append(name).append("(").append("").append(") {\n");
        Command it = start;
        while (it != null) {
            it.toString(ident + identChar, sb);
            it = it.getNext();
        }
        sb.append(ident).append("}\n");
    }

    public static MutableWidgetContainer createDrawableFunction(final Function f) {

        final JTextField tfName = new JTextField();

        final int TEXTFIELD_WIDTH = 100;
        final int TEXTFIELD_HEIGHT = 25;
        final int BUTTON_WIDTH = 25;
        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Função:", true));
                components.add(new TextLabel("Nome:"));

                tfName.setText(f.name);

                components.add(new Widget(tfName, TEXTFIELD_WIDTH + 20, TEXTFIELD_HEIGHT));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {

                f.name = tfName.getText();
                f.args.clear();

                sb.append(f.name).append(" (");
            }
        };

        //LINES
        final WidgetLine argumentLine = new WidgetLine() {
            private int argN = 0;

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                JTextField txArg = new JTextField();

                argN++;
                components.add(new TextLabel(argN + ":"));

                components.add(new Widget(txArg, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));
            }

            @Override
            public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
//                if (widgets.size() > 0) {
//                    try {
//                        StringBuilder sb = new StringBuilder();
//                        Iterator<Widget> iterator = widgets.iterator();
//                        String str;
//                        Widget tmpWidget;
//                        JComponent jComponent;
//                        //JTextField 1
//                        tmpWidget = iterator.next();
//                        jComponent = tmpWidget.getJComponent();
//                        if (jComponent instanceof JTextField) {
//                            str = ((JTextField) jComponent).getText();
//                            if (!str.isEmpty()) {
//                                if (!f.args.isEmpty()) {
//                                    sb.append(", ");
//                                }
//
//                                sb.append("var ");
//                                sb.append(str);
//                                f.args.add(str);
//                            }
//                        }
//                        return sb.toString();
//                    } catch (NoSuchElementException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return "";
            }
        };

        //END LINE
        final WidgetLine endLine = new WidgetLine(true) {
            private Widget addButton;
            private Widget remButton;

            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                JButton bTmp = new JButton("+");
                bTmp.setEnabled(false); //temporario

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        container.addLine(argumentLine);
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(true);
                        }
                    }
                });

                addButton = new Widget(bTmp, BUTTON_WIDTH, TEXTFIELD_HEIGHT);

                bTmp = new JButton("-");
                bTmp.setEnabled(false);

                bTmp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //desconta headerLine e endLine
                        int size = container.getSize() - 2;
                        if (size > 1) {
                            container.removeLine(size);
                        }
                        if (size - 1 == 1) {
                            JButton btn = (JButton) remButton.getJComponent();
                            btn.setEnabled(false);
                        }
                    }
                });

                remButton = new Widget(bTmp, BUTTON_WIDTH, TEXTFIELD_HEIGHT);
                components.add(addButton);
                components.add(remButton);
            }
        };

        final WidgetLine nullLine = new WidgetLine() {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel("Argumentos:"));
            }
        };

        DrawableProcedureBlock dcb = new DrawableProcedureBlock(f, Color.decode("#E6A82E")) {
            public static final int EXTENDED_HEIGHT = 15;
            public static final int SIMPLE_HEIGHT = 20;
            public static final int SIMPLE_WIDTH = 20;

            {
                updateStructure();
                center = true;
            }

            @Override
            public void updateStructure() {
                //exclui todas as linhas
                clear();

                //adiciona cabeçalho
                addLine(headerLine);

                //adiciona uma nova linha sem dados
                addLine(nullLine);

                for (String arg : f.args) {
                    addLine(argumentLine);
                }

                addLine(endLine);
                boxLabel = getBoxLabel();

                //CUIDADO
//                GUI.getInstance().updateTabNames(); TODO
            }

            public void splitBoxLabel(String original, Collection<String> splitted) {
                splitted.add(original);
            }

            @Override
            public String getBoxLabel() {
                String str = super.getBoxLabel();
                //CUIDADO
//                GUI.getInstance().updateTabNames(); TODO
                return "func " + str.trim() + ") : ";
            }
        };

        return dcb;
    }
    private GraphicObject resource = null;

    @Override
    public GraphicObject getDrawableResource() {
        if (resource == null) {
            //resource = createDrawableFunction(this);
            resource = createSimpleBlock(this, " BEGIN ", Color.black, Color.decode("#631864"), SHAPE_ROUND_RECTANGLE);
        }
        return resource;
    }

    @Override
    public void drawLines(Graphics2D g) {
        GraphicObject resource = getDrawableResource();
        if (resource != null) {
            Command c = super.start;
            if (c instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    Rectangle2D.Double bThis = resource.getObjectBouds();
                    Rectangle2D.Double bStart = d.getObjectBouds();
                    Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bStart.getCenterX(), bStart.getMinY());
                    g.draw(l);
                }
            }
        }
    }
}
