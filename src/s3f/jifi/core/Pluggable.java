/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core;

/**
 *
 * @author antunes
 */
public interface Pluggable <T> {//TODO: remover
    
    public T createInstance ();
    
}
