/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser.parameterparser;

import org.nfunk.jep.JEP;
import robotinterface.interpreter.Expression;

/**
 *
 * @author antunes2
 */
public class Argument {

    public static final int NUMBER = 1;
    public static final int STRING = 2;
    public static final int VARARG = 4;
    
    private final String statement;
    private Object value = null;

    public Argument(String statement) {
        this.statement = statement;
    }

    public void parse(JEP parser) {
        parser.parseExpression(statement);
        value = parser.getValueAsObject();
    }

    public double getDoubleValue() {
        if (value instanceof Double){
            return (Double) value;
        }
        return 0.0;
    }

    public String getStringValue() {
        if (value instanceof String){
            return (String) value;
        }
        return "";
    }

    public boolean getBooleanValue() {
        if (value instanceof Boolean){
            return (Boolean) value;
        }
        return false;
    }

}
