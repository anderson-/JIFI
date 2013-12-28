/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.util.ArrayList;
import java.util.Collection;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.drawable.swing.MutableWidgetContainer;

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

    @Deprecated
    public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data){
        
    }
    
    @Deprecated
    public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {
        return "";
    }
    
    //abstract
    public void createRow(Collection<Component> components, MutableWidgetContainer container, Object data){
        
    }

    @Deprecated
    public String getString(Collection<Component> components, MutableWidgetContainer container) {
        return "";
    }
    
    public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {
        
    }
}
