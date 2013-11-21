/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser;

import robotinterface.algorithm.Command;

/**
 *
 * @author antunes
 */
public interface FunctionToken <T extends Command> {

    public String getToken();
    
    public T createInstance(String args);
    
}
