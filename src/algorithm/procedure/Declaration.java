/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.procedure;

import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import robot.Robot;
import simulation.ExecutionException;
import util.Clock;

/**
 *
 * @author antunes
 */
public class Declaration extends Procedure {

    private String name;
    private Object value;

    public Declaration(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public void begin(Robot robot, Clock clock) throws ExecutionException {
        SymbolTable st = getParser().getSymbolTable();
        if (st.getVar(name) != null && st.getVar(name).hasValidValue()){
            throw new ExecutionException("Variable already exists!");
        } else {
            st.makeVarIfNeeded(name, value);
        }
    }
    
    
    
}
