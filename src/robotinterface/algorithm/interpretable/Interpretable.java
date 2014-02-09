/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package robotinterface.algorithm.interpretable;

import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.ResourceManager;

/**
 *
 * @author antunes2
 */
public abstract class Interpretable extends Command {
    
    protected Interpretable (boolean varArgs){
        
    }
    
    protected Interpretable (Argument ... args){
        
    }
    
    //inicio da execução do comando
    @Override
    public abstract void begin(ResourceManager rm) throws ExecutionException;
    

    //repete até retornar true ou lançar uma ExecutionException
    @Override
    public abstract boolean perform(ResourceManager rm) throws ExecutionException;
    
    
}
