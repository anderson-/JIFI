/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.plugin;

/**
 *
 * @author antunes
 */
public interface Pluggable <T> {
    
    public T createInstance ();
    
}
