/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser.parameterparser;

import org.nfunk.jep.JEP;

/**
 *
 * @author antunes2
 */
public final class Argument {

    public static final int NUMBER_LITERAL = 1;
    public static final int STRING_LITERAL = 2;
    public static final int EXPRESSION = 4;
    public static final int SINGLE_VARIABLE = 8;
    
    private String statement;
    private int type;
    private Object value = null;

    public Argument(Object statement, int type) {
        set(statement, type);
    }
    
    public void set(Object statement, int type) {
        this.statement = statement.toString();
        this.type = type;
    }

    public void set(Argument argument) {
        this.statement = argument.statement;
        this.type = argument.type;
    }

    public void parse(JEP parser) {
        parser.parseExpression(statement);
        value = parser.getValueAsObject();
    }

    public double getDoubleValue() {
        if (type == NUMBER_LITERAL){
            return Double.parseDouble(statement);
        }
        if (value instanceof Double){
            return (Double) value;
        }
        return 0.0;
    }

    public String getStringValue() {
        if (type == STRING_LITERAL){
            return statement;
        }
        if (value instanceof String){
            return (String) value;
        }
        return "";
    }

    public boolean getBooleanValue() {
        if (type == NUMBER_LITERAL){
            return (getDoubleValue() != 0);
        }
        if (type == STRING_LITERAL){
            return (!getStringValue().isEmpty());
        }
        return false;
    }
    
    public boolean isLiteral(){
        return (type == NUMBER_LITERAL || type == STRING_LITERAL);
    }
    
    public boolean isNumber(){
        return (type == NUMBER_LITERAL);
    }
    
    public boolean isString(){
        return (type == STRING_LITERAL);
    }
    
    public boolean isExpression(){
        return (type == EXPRESSION);
    }
    
    public boolean isVariable (){
        return (type == SINGLE_VARIABLE);
    }

    @Override
    public String toString() {
        if (statement.contains("\"")){
            return statement;
        } else {
            return statement.replace(" ", "");
        }
    }
}
