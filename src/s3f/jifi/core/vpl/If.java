/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.IfStatement;
import static s3f.jifi.core.vpl.parser.VPLParser.parse;
import s3f.magenta.GraphicObject;
import s3f.magenta.core.FlowchartSymbol;
import s3f.magenta.core.ShapeCreator;

/**
 *
 * @author anderson
 */
public class If extends Statement {

    public If(IfStatement subTree) {
        super(subTree);
        super.setName("if");
        addChild(parse(subTree.getThenPart()));
        addChild(parse(subTree.getElsePart()));
        FlowchartSymbol symbol = (FlowchartSymbol) getGraphicResource();
        symbol.setShapeCreator(ShapeCreator.DIAMOND);
    }

    @Override
    public String getStatementSource() {
        return "if (" + ((IfStatement) subTree).getCondition().toSource() + ")";
    }

    @Override
    public double[] getBounds(double x, double y, double i, double l) {
        double[] bounds = super.getBounds(x, y, i, l);
        x = bounds[0];
        y = bounds[1];
        double width = bounds[2];
        double height = (bounds[3] == 0) ? 0 : bounds[3] + l;
        Node it = getFirstChild();
        while (it != null) {
            bounds = ((Statement) it).getBounds(x, y, i, l);
            if (bounds[0] < x) {
                x = bounds[0];
            }
            if (bounds[2] > width) {
                width = bounds[2];
            }
            height += bounds[3] + l;
            it = it.getNext();
        }
        return new double[]{x, y, width, height};
    }

    @Override
    public void ident(double x, double y, double i, double l) {
        Statement trueBlock = (Statement) getFirstChild();
        Statement falseBlock = (Statement) getFirstChild();
        
        getGraphicResource().setLocation(x - super.getBounds(x, y, i, l)[2] / 2, y);
        y += super.getBounds(x, y, i, l)[3];
        
        Node it = getFirstChild();
        GraphicObject go;
        Statement s;
        while (it != null) {
            s = (Statement) it;
            go = s.getGraphicResource();
            if (go != null) {
                go.setLocation(x, y);
                y += l;
            }
            s.ident(x, y, i, l);
            y += s.getBounds(x, y, i, l)[3];
            it = it.getNext();
        }
    }

}
