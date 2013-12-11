/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.util.Collection;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.drawable.swing.Widget;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.swing.WidgetContainer;

/**
 *
 * @author antunes2
 */
public abstract class WidgetLine extends Component {

    private boolean onPageEnd = false;

    public WidgetLine() {
    }

    @Deprecated
    public WidgetLine(int height) {
//            this.height = height;
    }

    @Deprecated
    public WidgetLine(int width, int height) {
//            this.width = width;
//            this.height = height;
    }

    public WidgetLine(boolean onPageEnd) {
        this.onPageEnd = onPageEnd;
    }

    public boolean isOnPageEnd() {
        return onPageEnd;
    }

    public abstract void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data);

    public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {
        return "";
    }
}
