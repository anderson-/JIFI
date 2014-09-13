/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import java.awt.Color;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;
import s3f.jifi.core.vpl.parser.LabelParser;
import s3f.magenta.core.CompositeGraphicObject;
import s3f.magenta.core.ShapeCreator;
import s3f.magenta.core.ShapeRender;

/**
 *
 * @author anderson
 */
public class Statement<T extends AstNode> extends Node {

    private final T subTree;
    protected CompositeGraphicObject gr;
    private boolean USE_SYNTAX_TEXT_AREA = true;

    public Statement(T subTree) {
        this.subTree = subTree;
        super.setName("statement");
    }

    public T getSubTree() {
        return subTree;
    }

    public void indent(int hGap, int vGap) {

    }

    protected final void setGraphicResource(CompositeGraphicObject cgo) {
        gr = cgo;
    }

    public final CompositeGraphicObject getGraphicResource() {
        if (gr == null) {
            CompositeGraphicObject cgo = new CompositeGraphicObject();
            if (USE_SYNTAX_TEXT_AREA) {
                JPanel panel = cgo.getContainer();
                panel.setLayout(new MigLayout("insets 25 10 0 10"));
                RSyntaxTextArea rTextArea = new RSyntaxTextArea();
                rTextArea.setSyntaxEditingStyle("text/javascript");
                rTextArea.setText(subTree.toSource());
                rTextArea.setHighlightCurrentLine(false);
                rTextArea.setBracketMatchingEnabled(false);
                rTextArea.setEditable(false);
                panel.add(rTextArea);
            } else {
                LabelParser.parse(subTree, cgo);
            }
            cgo.setWidgetVisible(true);
            cgo.setShapeCreator(ShapeCreator.DIAMOND);
            cgo.addRender(new ShapeRender(Color.GREEN));
            gr = cgo;
        }
        return gr;
    }
}
