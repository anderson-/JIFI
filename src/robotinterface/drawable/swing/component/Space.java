/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.awt.geom.Rectangle2D;
import static robotinterface.drawable.swing.component.Component.DEFAULT_INSETS;

/**
 *
 * @author antunes2
 */
public class Space extends Component {

    private int x, y;

    public Space() {
    }

    public Space(int x) {
        this.x = x;
    }

    public Space(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Rectangle2D.Double getInsets(Rectangle2D.Double tmp) {
        tmp.setRect(DEFAULT_INSETS.x + x, DEFAULT_INSETS.y + y, DEFAULT_INSETS.width, DEFAULT_INSETS.width);
        return tmp;
    }

}
