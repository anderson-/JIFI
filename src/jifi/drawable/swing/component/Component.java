/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jifi.drawable.swing.component;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes2
 */
public class Component {
    
    public static final Rectangle2D.Double DEFAULT_INSETS = new Rectangle2D.Double(6,5,0,0);
    
    protected int x, y;
    
    public Rectangle2D.Double getBounds (Rectangle2D.Double tmp, Graphics2D g){
        return tmp;
    }
    
    public Rectangle2D.Double getInsets (Rectangle2D.Double tmp){
        tmp.setRect(DEFAULT_INSETS);
        return tmp;
    }
    
    public void setTempLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
