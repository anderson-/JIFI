/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.drawable.swing.component;

import java.awt.geom.Rectangle2D;
import static jifi.drawable.swing.component.Component.DEFAULT_INSETS;

/**
 *
 * @author antunes2
 */
public class SubLineBreak extends Component {

    private boolean endLine = false;

    public SubLineBreak() {
    }

    public SubLineBreak(boolean endLine) {
        this.endLine = endLine;
    }

    public boolean isEndLine() {
        return endLine;
    }
    
    @Override
    public Rectangle2D.Double getInsets(Rectangle2D.Double tmp) {
        if (endLine) {
            tmp.setRect(DEFAULT_INSETS);
        }
        return tmp;
    }

}
