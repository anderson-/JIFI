/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser.parameterparser;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.nfunk.jep.JEP;
import robotinterface.drawable.swing.component.Widget;

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
        if (statement != null) {
            this.statement = statement.toString();
        } else {
            this.statement = "";
        }
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
        if (type == NUMBER_LITERAL) {
            return Double.parseDouble(statement);
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return 0.0;
    }

    public String getStringValue() {
        if (type == STRING_LITERAL) {
            return statement;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    public Object getValue() {
        return value;
    }

    public String getVariableName() {
        if (type == SINGLE_VARIABLE) {
            return statement;
        }
        return "";
    }

    public boolean getBooleanValue() {
        if (type == NUMBER_LITERAL) {
            return (getDoubleValue() != 0);
        }
        if (type == STRING_LITERAL) {
            return (!getStringValue().isEmpty());
        }
        return false;
    }

    public boolean isLiteral() {
        return (type == NUMBER_LITERAL || type == STRING_LITERAL);
    }

    public boolean isNumber() {
        return (type == NUMBER_LITERAL);
    }

    public boolean isString() {
        return (type == STRING_LITERAL);
    }

    public boolean isExpression() {
        return (type == EXPRESSION);
    }

    public boolean isVariable() {
        return (type == SINGLE_VARIABLE);
    }

    public void getValueFrom(Widget w) {
        JComponent jc = w.getJComponent();
        if (jc instanceof JSpinner) {
            JSpinner c = (JSpinner) jc;
            set(c.getValue(), NUMBER_LITERAL);
        } else if (jc instanceof JComboBox) {
            JComboBox c = (JComboBox) jc;
            set(c.getSelectedItem(), SINGLE_VARIABLE);
        } else if (jc instanceof JTextField) {
            JTextField c = (JTextField) jc;
            if (w.isDynamic() && !c.getText().contains("\"")) {
                set(c.getText(), EXPRESSION);
            } else {
                String str = c.getText();
                set(str.replaceAll("\"", ""), STRING_LITERAL);
            }
        } else {
            throw new Error("Invalid JComponent...");
        }
    }

    public Widget setValueOf(Widget... ws) {
        if (type == NUMBER_LITERAL) {
            //JSpinner
            for (Widget w : ws) {
                if (w.getJComponent() instanceof JSpinner) {
                    JSpinner c = (JSpinner) w.getJComponent();
                    c.setValue((int) getDoubleValue());
                    return w;
                }
            }
        }
        
        if (type == SINGLE_VARIABLE) {
            //JComboBox
            for (Widget w : ws) {
                if (w.getJComponent() instanceof JComboBox) {
                    JComboBox c = (JComboBox) w.getJComponent();
                    c.setSelectedItem(statement);
                    return w;
                }
            }
        }
        
        //JTextField
        for (Widget w : ws) {
            if (w.getJComponent() instanceof JTextField) {
                JTextField c = (JTextField) w.getJComponent();
                c.setText(statement);
                return w;
            }
        }

        throw new Error("JComponent not found : " + type);
    }

    @Override
    public String toString() {
        if (statement.contains("\"") || statement.contains("var")) {
            return statement;
        } else {
            return statement.replace(" ", "");
        }
    }
}
