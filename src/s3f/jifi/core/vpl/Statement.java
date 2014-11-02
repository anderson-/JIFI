/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;
import s3f.jifi.core.Command;
import static s3f.jifi.core.Command.ARROW_DOWN;
import static s3f.jifi.core.Command.drawArrow;
import s3f.jifi.core.GraphicFlowchart;
import s3f.jifi.core.parser.parameterparser.Argument;
import s3f.jifi.core.vpl.parser.LabelParser;
import s3f.jifi.flowchart.Procedure;
import s3f.magenta.FlowchartBlock;
import s3f.magenta.GraphicObject;
import s3f.magenta.core.FlowchartSymbol;
import s3f.magenta.core.ShapeCreator;
import s3f.magenta.core.ShapeRender;
import s3f.magenta.graphicresource.GraphicResource;
import s3f.magenta.swing.DrawableProcedureBlock;
import s3f.magenta.swing.MutableWidgetContainer;
import s3f.magenta.swing.component.Component;
import s3f.magenta.swing.component.SubLineBreak;
import s3f.magenta.swing.component.TextLabel;
import s3f.magenta.swing.component.Widget;
import s3f.magenta.swing.component.WidgetLine;
import static s3f.magenta.swing.component.WidgetLine.ARG_TEXTFIELD;

/**
 *
 * @author anderson
 * @param <T>
 */
public class Statement<T extends AstNode> extends Node implements GraphicFlowchart, FlowchartBlock {

    private final T subTree;
    protected GraphicObject go;
    private boolean USE_SYNTAX_TEXT_AREA = true;
    private boolean resourceIsSetted = false;

    public Statement(T subTree) {
        this.subTree = subTree;
        super.setName("statement");
    }

    public T getSubTree() {
        return subTree;
    }

    @Override
    public void ident(double x, double y, double j, double k) {

        System.out.println(getSubTree().toSource() + "#");
        
        double cw = 0;
        double ch = 0;

        Rectangle2D.Double t;
        GraphicObject d = getGraphicResource();

        if (d != null) {
            t = (Rectangle2D.Double) d.getObjectBouds();
            if (t != null) {
                cw = t.width;
                ch = t.height;

                double px = x - (cw / 2);
                double py = y;

                d.setLocation(px, py);

                y += ch + j;
            }
        }

        if (getFirstChild() != null) {
            ((Statement) getFirstChild()).ident(x, y, j, k);
        }

        if (getNext() != null) {
            ((Statement) getNext()).ident(x, y, j, k);
        }
    }

    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, double j, double k) {
        return getBounds(this, tmp, j, k);
    }

    protected static Rectangle2D.Double getBounds(Statement c, Rectangle2D.Double tmp, double j, double k) {
        Rectangle2D.Double t = null;
        if (c instanceof GraphicResource) {
            GraphicObject d = ((GraphicResource) c).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (tmp == null) {
            tmp = new Rectangle2D.Double();
        }

        tmp.setRect(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);

        if (t != null) {
            tmp.setRect(t);
//            tmp.x = 0;
//            tmp.y = 0;
//            tmp.x = (t.x < tmp.x) ? t.x : tmp.x;
//            tmp.y = (t.y < tmp.y) ? t.y : tmp.y;
//
//            tmp.width += t.width;
//            tmp.height += t.height;

            tmp.height += j;
        }
        return tmp;
    }

    @Override
    public void drawLines(Graphics2D g) {
        GraphicObject resource = getGraphicResource();
        if (resource != null) {
            Statement c = (Statement) getNext();
            if (c instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) c).getDrawableResource();
                if (d != null) {
                    Rectangle2D.Double bThis = resource.getObjectBouds();
                    Rectangle2D.Double bNext = d.getObjectBouds();
                    Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bNext.getCenterX(), bNext.getMinY());
                    g.draw(l);
                    drawArrow(g, bNext.getCenterX() + .2, bNext.getMinY(), ARROW_DOWN);
                }
            }
        }
    }

    protected final void setGraphicResource(FlowchartSymbol cgo) {
        go = cgo;
        resourceIsSetted = true;
    }

    public static MutableWidgetContainer createSimpleIf(final Procedure p, final Statement c, final String def) {
        //HEADER LINE
        final WidgetLine headerLine = new WidgetLine(20) {
            @Override
            public void createRow(Collection<Component> components, final MutableWidgetContainer container, int index) {
                components.add(new TextLabel(c.getName() + ":", true));
                components.add(new SubLineBreak());
                JPanel panel = new JPanel();
                panel.setLayout(new MigLayout("insets 25 10 0 10"));
                RSyntaxTextArea rTextArea = new RSyntaxTextArea();
                rTextArea.setSyntaxEditingStyle("text/javascript");
                //rTextArea.setText("var x = 2.323 * sin(2)");
                rTextArea.setText(c.subTree.toSource());
                rTextArea.setHighlightCurrentLine(false);
                rTextArea.setBracketMatchingEnabled(false);
//                rTextArea.setEditable(false);
                panel.add(rTextArea);
                components.add(new Widget(rTextArea));
                //createGenericField(p, p.getArg(0), "Condição:", 120, 25, components, container, ARG_TEXTFIELD);
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
//                p.setProcedure(p.getArg(0).toString());
//                boxLabel = p.getProcedure();
//                if (boxLabel.equals(def) || boxLabel.trim().isEmpty()) {
//                    boxLabel = getName();
//                    p.setProcedure(def);
//                }
//                p.getArg(0).set(p.getProcedure(), Argument.EXPRESSION);
//                return boxLabel;
                return c.subTree.toSource();
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

    public final GraphicObject getGraphicResource() {
        if (!resourceIsSetted) {
//            FlowchartSymbol cgo = new FlowchartSymbol();
//            if (USE_SYNTAX_TEXT_AREA) {
//                JPanel panel = cgo.getContainer();
//                panel.setLayout(new MigLayout("insets 25 10 0 10"));
//                RSyntaxTextArea rTextArea = new RSyntaxTextArea();
//                rTextArea.setSyntaxEditingStyle("text/javascript");
//                rTextArea.setText(subTree.toSource());
//                rTextArea.setHighlightCurrentLine(false);
//                rTextArea.setBracketMatchingEnabled(false);
//                rTextArea.setEditable(false);
//                panel.add(rTextArea);
//            } else {
//                LabelParser.parse(subTree, cgo);
//            }
//            cgo.setShapeCreator(ShapeCreator.DEFAULT);
//            cgo.addRender(new ShapeRender(Color.GREEN));
//            go = cgo;
            go = createSimpleIf(new Procedure(), this, "sad");
            resourceIsSetted = true;
        }
        return go;
    }
}
