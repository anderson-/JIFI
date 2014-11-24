/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Scope;
import s3f.magenta.GraphicObject;

/**
 *
 * @author anderson
 */
public class Block extends Statement {

    public Block(Scope subTree) {
        super(subTree);
        super.setName("block");
        super.setGraphicResource(null);
    }

    public Block(org.mozilla.javascript.ast.Block subTree) {
        super(subTree);
        super.setName("block");
        super.setGraphicResource(null);
    }

    protected Block(AstNode subTree) {
        super(subTree);
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
        GraphicObject go = getGraphicResource();
        if (go != null) {
            go.setLocation(x - super.getBounds(x, y, i, l)[2] / 2, y - super.getBounds(x, y, i, l)[3]);
        }

        Node it = getFirstChild();
        Statement s;
//        x += (this.getClass() == Block.class) ? 50 : 0;
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
