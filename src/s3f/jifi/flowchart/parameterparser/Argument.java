/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.parameterparser;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import s3f.magenta.swing.component.Widget;

/**
 *
 * @author antunes2
 */
public class Argument {

    public static final int UNDEFINED = -1;
    public static final int NUMBER_LITERAL = 1;
    public static final int STRING_LITERAL = 2;
    public static final int EXPRESSION = 4;
    public static final int SINGLE_VARIABLE = 8;
    public static final int TEXT = 16;

    private String statement;
    private int type;
    private Object value = null;
    private boolean extended = false;

    public Argument(Object statement, int type) {
        set(statement, type);
    }
    public Argument(Argument arg) {
        set(arg);
    }

    public Argument(Object statement, int type, boolean extended) {
        this.extended = extended;
        set(statement, type);
    }

    public final void set(Object statement, int type) {
        if (statement != null) {
            this.statement = statement.toString();
        } else {
            this.statement = "";
        }
        this.type = type;
    }

    public final void set(Argument argument) {
        this.statement = argument.statement;
        this.type = argument.type;
    }

//    public final void parse(JEP parser) {
//        parser.parseExpression(statement);
//        value = parser.getValueAsObject();
//    }

    public final double getDoubleValue() {
        if (type == NUMBER_LITERAL) {
            return Double.parseDouble(statement);
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return 0.0;
    }

    public final String getStringValue() {
        if (type == STRING_LITERAL || type == TEXT || type == EXPRESSION) {
            return statement;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    public final Object getValue() {
        return value;
    }

    public final String getVariableName() {
        if (type == SINGLE_VARIABLE) {
            return statement;
        }
        return "";
    }

    public final boolean getBooleanValue() {
        if (type == NUMBER_LITERAL) {
            return (getDoubleValue() != 0);
        }
        if (type == STRING_LITERAL) {
            return (!getStringValue().isEmpty());
        }
        return false;
    }

    public final boolean isLiteral() {
        return (type == NUMBER_LITERAL || type == STRING_LITERAL);
    }

    public final boolean isNumber() {
        return (type == NUMBER_LITERAL);
    }

    public final boolean isString() {
        return (type == STRING_LITERAL);
    }

    public final boolean isExpression() {
        return (type == EXPRESSION);
    }

    public final boolean isVariable() {
        return (type == SINGLE_VARIABLE);
    }

    public final int getType() {
        return type;
    }

    public boolean getValueOfExtended(JComponent jc) {
        return false;
    }

    public boolean setValueOfExtended(JComponent jc) {
        return false;
    }

    public final void getValueFrom(Widget w) {
        JComponent jc = w.getJComponent();

        if (extended && getValueOfExtended(jc)) {
            //System.out.println("ex");
        } else if (jc instanceof JSpinner) {
            JSpinner c = (JSpinner) jc;
            set(c.getValue(), NUMBER_LITERAL);
        } else if (jc instanceof JComboBox) {
            JComboBox c = (JComboBox) jc;
            set(c.getSelectedItem(), SINGLE_VARIABLE);
        } else if (jc instanceof JTextField) {
            JTextField c = (JTextField) jc;
            if (w.isDynamic() && !c.getText().contains("\"")) {
                set(c.getText(), ((type == UNDEFINED || type != TEXT) ? EXPRESSION : type));
            } else {
                String str = c.getText();
                if (str.contains(" ")) {
                    set(str, TEXT);
                } else {
                    set(str.replaceAll("\"", ""), STRING_LITERAL);
                }
            }
        } else {
            throw new Error("Invalid JComponent...");
        }
    }

    public final Widget setValueOf(Widget... ws) {

        if (extended) {
            for (Widget w : ws) {
                if (setValueOfExtended(w.getJComponent())) {
                    return w;
                }
            }
        }

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

        throw new Error("JComponent not found : " + type + ", widgets found: " + ws.length + " : " + ((ws.length > 0) ? (ws[0].getJComponent().getClass().getSimpleName()) : "null"));
    }
    
    @Deprecated
    public String getStatement(){
        return statement;
    }

    @Override
    public final String toString() {
        if (statement.contains("\"") || statement.contains("var") || type == TEXT) {
            return statement;
        } else {
            return statement.replaceAll(" ","");
            //return statement.replaceAll("\\s+"," ");
        }
    }
}
