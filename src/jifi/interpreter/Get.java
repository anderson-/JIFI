/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.interpreter;

import java.util.Stack;
import java.util.Vector;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 *
 * @author antunes
 */
public class Get extends PostfixMathCommand {

    public Get() {
        numberOfParameters = 2;
    }

    @Override
    public void run(Stack s) throws ParseException {
        checkStack(s);// check the stack
        Object rhs = s.pop();
        Object lhs = s.pop();
        if (!(lhs instanceof Vector || lhs instanceof String)) {
            throw new ParseException("Get: lhs must be an instance of Vector or String");
        }

        if (rhs instanceof Number) {
            int index = ((Number) rhs).intValue();
            Object val = null;
            if (lhs instanceof Vector) {
                val = ((Vector) lhs).elementAt(index - 1);
            } else if (lhs instanceof String) {
                val = "" + (((String) lhs).toCharArray()[index - 1]);
            }

            s.push(val);
            return;
        }
        if (rhs instanceof Vector) {
            Vector vec = (Vector) rhs;
            if (vec.size() != 1) {
                throw new ParseException("Get: only single dimension arrays supported in JEP");
            }
            Object val = null;
            int index = ((Number) vec.firstElement()).intValue();
            if (lhs instanceof Vector) {
                val = ((Vector) lhs).elementAt(index - 1);
            } else if (lhs instanceof String) {
                val = "" + (((String) lhs).toCharArray()[index - 1]);
            }
            s.push(val);
            return;

        }
        throw new ParseException("Get: only single dimension arrays supported in JEP");
    }
}
