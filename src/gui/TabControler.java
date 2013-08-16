/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author antunes
 */
public interface TabControler {
    
    public class Tab{

        public Tab(JComponent comp, String name) {
            this.comp = comp;
            this.name = name;
        }
        
        public final JComponent comp;
        public final String name;
    }
    
    public List<Tab> getTabs();
    
}
