/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import java.awt.geom.Rectangle2D;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Scope;
import s3f.magenta.GraphicObject;
import s3f.magenta.graphicresource.GraphicResource;

/**
 *
 * @author anderson
 */
public class Block extends Statement<AstNode> {

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

    @Override
    public void ident(double x, double y, double j, double k) {
        
        System.out.println(getSubTree().toSource() + "#");

        double cw = 0;
        double ch = 0;

        Rectangle2D.Double t = null;
        if (this instanceof GraphicResource) {
            GraphicObject d = ((GraphicResource) this).getDrawableResource();

            if (d != null) {
                t = (Rectangle2D.Double) d.getObjectBouds();
            }
        }

        if (t != null) {
            cw = t.width;
            ch = t.height;

            double px = x - (cw / 2);
            double py = y;

            if (this instanceof GraphicResource) {
                GraphicObject d = ((GraphicResource) this).getDrawableResource();

                if (d != null) {
                    d.setLocation(px, py);
                }
            }

            y += ch + j;
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
    
}
