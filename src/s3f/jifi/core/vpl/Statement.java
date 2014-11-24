/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.mozilla.javascript.ast.AstNode;
import s3f.jifi.core.vpl.parser.LabelParser;
import s3f.magenta.GraphicObject;
import s3f.magenta.core.FlowchartSymbol;
import s3f.magenta.core.ShapeCreator;
import s3f.magenta.core.ShapeRender;

/**
 *
 * @author anderson
 */
public class Statement extends Node {

    protected final AstNode subTree;
    protected AstNode statementSource;
    protected GraphicObject go;
    private boolean USE_SYNTAX_TEXT_AREA = true;
    private boolean resourceIsSetted = false;

    public Statement(AstNode subTree) {
        this.subTree = subTree;
        super.setName("statement");
    }

    public AstNode getSubTree() {
        return subTree;
    }

    public void setStatementSource(AstNode statementSource) {
        this.statementSource = statementSource;
    }

    public String getStatementSource() {
        if (statementSource == null) {
            return subTree.toSource();
        }
        return statementSource.toSource();
    }

    public double[] getBounds(double x, double y, double i, double l) {
        GraphicObject go = getGraphicResource();
        if (go != null) {
            Rectangle2D.Double bounds = go.getObjectBouds();
            return new double[]{bounds.x, bounds.y, bounds.width, bounds.height + l};
        }
        return new double[]{x, y, 0, 0};
    }

    public void ident(double x, double y, double i, double l) {
        GraphicObject go = getGraphicResource();
        if (go != null) {
            go.setLocation(x - getBounds(x, y, i, l)[2] / 2, y);
        }
    }

//    @Override
//    public void drawLines(Graphics2D g) {
//        GraphicObject resource = getGraphicResource();
//        if (resource != null) {
//            Statement c = (Statement) getNext();
//            if (c instanceof GraphicResource) {
//                GraphicObject d = ((GraphicResource) c).getDrawableResource();
//                if (d != null) {
//                    Rectangle2D.Double bThis = resource.getObjectBouds();
//                    Rectangle2D.Double bNext = d.getObjectBouds();
//                    Line2D.Double l = new Line2D.Double(bThis.getCenterX(), bThis.getMaxY(), bNext.getCenterX(), bNext.getMinY());
//                    g.draw(l);
////                    drawArrow(g, bNext.getCenterX() + .2, bNext.getMinY(), 0);
//                }
//            }
//        }
//    }
    protected final void setGraphicResource(FlowchartSymbol cgo) {
        go = cgo;
        resourceIsSetted = true;
    }

    protected final void resetGraphicResource() {
        resourceIsSetted = false;
    }

    public final GraphicObject getGraphicResource() {
        if (!resourceIsSetted) {
            FlowchartSymbol cgo = new FlowchartSymbol();
            if (USE_SYNTAX_TEXT_AREA) {
                JPanel panel = cgo.getPanel();
                panel.setLayout(new MigLayout("insets 5 5 5 5"));
                RSyntaxTextArea rTextArea = new RSyntaxTextArea();
                rTextArea.setSyntaxEditingStyle("text/javascript");
                String source = getStatementSource();
                rTextArea.setText(source.replaceFirst("\n", ""));
                rTextArea.setHighlightCurrentLine(false);
                rTextArea.setBracketMatchingEnabled(false);
                rTextArea.setEditable(false);
                panel.add(rTextArea);
            } else {
                LabelParser.parse(subTree, cgo);
            }
            cgo.setShapeCreator(ShapeCreator.DEFAULT);
            cgo.addRender(new ShapeRender(s3f.util.RandomColor.generate()));
            go = cgo;
            resourceIsSetted = true;
        }
        return go;
    }
}
