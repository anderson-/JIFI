/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.procedure;

import robotinterface.algorithm.Command;
import java.util.ArrayList;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Variable;
import robotinterface.robot.Robot;
import robotinterface.interpreter.ExecutionException;
import robotinterface.interpreter.Expression;
import robotinterface.util.trafficsimulator.Clock;

//classe mais 
public class Procedure extends Command implements Expression/*, Observer*/ {

    private static JEP parser;
    private String procedure;

    public Procedure() {
        parser = null;
        procedure = "0";
    }

    public Procedure(String procedure) {
        this();
        this.procedure = procedure;
    }    

    protected final JEP getParser() {
        return parser;
    }

    @Override
    public final void setParser(JEP parser) {
        Procedure.parser = parser;
    }

    public final String getProcedure() {
        return procedure;
    }

    public final void setProcedure(String procedure) {
        this.procedure = procedure;
    }
    
    @Override
    public boolean perform(Robot robot, Clock clock) throws ExecutionException{
        evaluate();
        return true;
    }
    
    //usado pelos descendentes dessa classe para executar expressoes simples
    protected final Object execute(String procedure) throws ExecutionException {
        if (parser == null) throw new ExecutionException("Parser not found!");
        parser.parseExpression(procedure);
        return parser.getValueAsObject();
    }
    
    protected final boolean evaluate(String procedure) throws ExecutionException {
        Object o = execute(procedure);
        if (o instanceof Number){
            Double d = ((Number)o).doubleValue();
            return (d != 0 && !d.isNaN());
        }
        return false;
    }
    
    protected final boolean evaluate() throws ExecutionException {
        return evaluate(procedure);
    }
    
    protected final Variable newVariable (String name, Object value){
        return parser.getSymbolTable().makeVarIfNeeded(name, value);
    }
    
    public ArrayList<String> getDeclaredVariables(){
        ArrayList<String> vars = new ArrayList<>();
        Command it = this;
        while (it != null){
            Command up = it.getPrevious();
            while (up != null){
                if (up instanceof Declaration){
                    vars.add(((Declaration)up).getName());
                }
                up = up.getPrevious();
            }
            it = it.getParent();
        }
        return vars;
    }
    
//    public List<Variable> getVariables(){
//        return variables;
//    }
    
//    public static void getVariableScope (List<Variable> variables){
////        parent.getVariableScope(variables);
////        variables.addAll(this.variables);
//    }

//    @Override
//    public void update(Object o, Object arg) {
//        if (arg instanceof Variable){
//            Variable var = (Variable)arg;
//                
//            
//            
////                for (Variable var : vars){
////                    if (var.getName().equals(temp.getName())){
////                        
////                    }
////                }
////                if(!var.isConstant()){
////                    System.out.println(var.getName());
////                }
//        }
//    }
}
