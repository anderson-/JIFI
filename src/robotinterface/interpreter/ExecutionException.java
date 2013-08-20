/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.interpreter;

/**
 *
 * @author antunes
 */
public class ExecutionException extends Exception{
    
    public ExecutionException(){
        super("ExecutionException");
    }
    
    public ExecutionException(String e){
        super(e);
    }
    
}
