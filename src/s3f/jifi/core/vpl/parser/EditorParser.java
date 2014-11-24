/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl.parser;

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import s3f.jifi.core.vpl.Statement;
import s3f.magenta.DrawingPanel;
import s3f.magenta.core.ASTCompositeParser;
import s3f.magenta.core.FlowchartSymbol;
import s3f.magenta.core.ShapeCreator;
import s3f.magenta.core.ShapeRender;
import s3f.magenta.core.SubASTParser;
import s3f.magenta.util.QuickFrame;

/**
 *
 * @author anderson
 */
public class EditorParser {

    private static int[] getInfixTokens() {
        return new int[]{
            Token.OR,
            Token.AND,
            Token.BITOR,
            Token.BITXOR,
            Token.BITAND,
            Token.EQ,
            Token.NE,
            Token.LT,
            Token.LE,
            Token.GT,
            Token.GE,
            Token.LSH,
            Token.RSH,
            Token.URSH,
            Token.ADD,
            Token.SUB,
            Token.MUL,
            Token.DIV,
            Token.MOD
        };
    }

    private static String getOperatorSymbol(int op) {
        switch (op) {
            case Token.OR:
                return "||";
            case Token.AND:
                return "&&";
            case Token.BITOR:
                return "|";
            case Token.BITXOR:
                return "^";
            case Token.BITAND:
                return "&";
            case Token.EQ:
                return "==";
            case Token.NE:
                return "!=";
            case Token.LT:
                return "<";
            case Token.LE:
                return "<=";
            case Token.GT:
                return ">";
            case Token.GE:
                return ">=";
            case Token.LSH:
                return "<<";
            case Token.RSH:
                return ">>";
            case Token.URSH:
                return ">>>";
            case Token.ADD:
                return "+";
            case Token.SUB:
                return "-";
            case Token.MUL:
                return "*";
            case Token.DIV:
                return "/";
            case Token.MOD:
                return "%";
            default:
                return "?";
        }
    }

    private EditorParser() {

    }

    public static JPanel parse(AstNode tree, FlowchartSymbol cgo) {
        JPanel panel = cgo.getPanel();
        panel.setLayout(new MigLayout("insets 25 10 0 10"));
        ASTCompositeParser<JPanel, JPanel> parser;
        parser = new ASTCompositeParser<>();
        parser.register(new SubASTParser<JPanel, JPanel>("fcall", Token.CALL) {
            @Override
            public JPanel parse(AstNode subTree, JPanel input) {
                FunctionCall fcall = (FunctionCall) subTree;
                Name name = (Name) fcall.getTarget();
                JLabel label = new JLabel(name.getIdentifier());
                label.setForeground(Color.BLUE);
                input.add(label);
                input.add(new JLabel("("));
                boolean ok = false;
                for (AstNode node : fcall.getArguments()) {
                    if (ok) {
                        input.add(new JLabel(","));
                    }
                    getParent().parse(node, input);
                    ok = true;
                }
                input.add(new JLabel(")"));
                return null;
            }
        });
        parser.register(new SubASTParser<JPanel, JPanel>("parentesis", Token.LP) {
            @Override
            public JPanel parse(AstNode subTree, JPanel input) {
                ParenthesizedExpression ex = (ParenthesizedExpression) subTree;
                input.add(new JLabel("("));
                getParent().parse(ex.getExpression(), input);
                input.add(new JLabel(")"));
                return null;
            }
        });
        parser.register(new SubASTParser<JPanel, JPanel>("binary op.", getInfixTokens()) {
            @Override
            public JPanel parse(AstNode subTree, JPanel input) {
                InfixExpression op = (InfixExpression) subTree;
                getParent().parse(op.getLeft(), input);
                input.add(new JLabel(getOperatorSymbol(op.getOperator())));
                getParent().parse(op.getRight(), input);
                return null;
            }
        });
        parser.register(new SubASTParser<JPanel, JPanel>("name", Token.NAME) {
            @Override
            public JPanel parse(AstNode subTree, JPanel input) {
                Name name = (Name) subTree;
                JLabel label = new JLabel(name.getIdentifier());
                label.setForeground(Color.GREEN);
                input.add(label);
                return null;
            }
        });
        parser.register(new SubASTParser<JPanel, JPanel>("number", Token.NUMBER) {
            @Override
            public JPanel parse(AstNode subTree, JPanel input) {
                NumberLiteral number = (NumberLiteral) subTree;
//                JLabel label = new JLabel(number.getValue());
//                label.setForeground(Color.MAGENTA);
                JSpinner jSpinner = new JSpinner();
                jSpinner.setValue(Double.parseDouble(number.getValue()));
                input.add(jSpinner);
                return null;
            }
        });
//        parser.register(new SubASTParser<JPanel, JPanel>("something", SubASTParser.SOMETHING) {
//            @Override
//            public JPanel parse(AstNode subTree, JPanel input) {
//                RSyntaxTextArea rTextArea = new RSyntaxTextArea();
//                rTextArea.setSyntaxEditingStyle("text/javascript");
//                rTextArea.setText(subTree.toSource());
//                input.add(rTextArea);
//                return null;
//            }
//        });
        return parser.parse(tree, panel);
    }

    static long t() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        DrawingPanel p = new DrawingPanel();
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
        FlowchartSymbol cgo = new FlowchartSymbol();
        String ex = "(23/7)*func(12 + x)+(2*(3||2-x(1,2,3*3.1415+f((5%22)))))";
        long t = t();
        AstNode expression = ((org.mozilla.javascript.ast.ExpressionStatement) new Parser().parse(ex, "etc", 1).getFirstChild()).getExpression();
        System.out.println("t:" + (t() - t));
        t = t();
        System.out.println(expression.debugPrint());
        System.out.println("t:" + (t() - t));
        t = t();
        EditorParser.parse(expression, cgo);
        System.out.println("t:" + (t() - t));
//        cgo.setWidgetVisible(true);
        cgo.setShapeCreator(ShapeCreator.DIAMOND);
        cgo.addRender(new ShapeRender(Color.GREEN));
        p.add(cgo);
    }

}
