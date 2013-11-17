/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.procedure.Function;
import robotinterface.gui.GUI;
import robotinterface.gui.panels.FlowchartPanel;
import robotinterface.gui.panels.code.jedit.*;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.plugin.PluginManager;
import robotinterface.robot.device.Device;

/**
 *
 * @author antunes
 */
public class CodeEditorPanel extends JPanel implements KeyListener {

    private Function function;
    private JEditTextArea textArea;

    public CodeEditorPanel(Function function) {
        super(new BorderLayout());

        this.function = function;

        textArea = new JEditTextArea();
        textArea.setTokenMarker(new FunctionTokenMarker());
        textArea.setText(Parser.encode(function));
        textArea.getPainter().setFont(UIManager.getDefaults().getFont("TextPane.font"));
        textArea.recalculateVisibleLines();
        textArea.setFirstLine(0);
        textArea.setElectricScroll(0);
        textArea.getPainter().setSelectionColor(
                UIManager.getColor("TextArea.selectionBackground"));

        SyntaxStyle[] styles = SyntaxUtilities.getDefaultSyntaxStyles();

        Color cstring = Color.decode("#f07818");
        Color cfunction = Color.decode("#6a4a3c");
        Color cvar = Color.decode("#cc333f");
        Color cblocks = Color.decode("#00a0b0");
        Color cfuncandbool = Color.decode("#8fbe00");
        Color cdevices = Color.decode("#00C12B");

        styles[Token.COMMENT1] = new SyntaxStyle(Color.GRAY, false, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(cblocks, false, true);
        styles[Token.KEYWORD2] = new SyntaxStyle(cvar, false, true);
        styles[Token.KEYWORD3] = new SyntaxStyle(cfunction, true, true);

        styles[Token.LITERAL1] = new SyntaxStyle(cstring, false, false);
        styles[Token.LITERAL2] = new SyntaxStyle(cfuncandbool, false, true);

        styles[Token.OPERATOR] = new SyntaxStyle(cdevices, false, true);

        textArea.getPainter().setStyles(styles);

        JScrollPane jsp = new JScrollPane(textArea);

        jsp.getVerticalScrollBar().setUnitIncrement(10);

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JButton commentButton = new JButton("COM");
        commentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commentSelection(textArea);
            }
        });

        tb.add(commentButton);

        JButton uncommentButton = new JButton("UNC");
        uncommentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uncommentSelection(textArea);
            }
        });

        tb.add(uncommentButton);

        JButton cut = new JButton("Cut");
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.cut();
            }
        });

        tb.add(cut);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.copy();
            }
        });

        tb.add(copyButton);

        JButton pasteButton = new JButton("Paste ex.");
        pasteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(
                        "func MyFunc(){\n"
                        + "	var x = 0;\n"
                        + "	var y;\n"
                        + "	 while (1 == 1){\n"
                        + "		read(Bussola,y);\n"
                        + "		print(\"%v\", y);\n"
                        + "             var z = -x;\n"
                        + "		move(x, z);\n"
                        + "		wait(500);\n"
                        + "		move(0);\n"
                        + "		wait(300);\n"
                        + "		x = x + 10;\n"
                        + "		if (x >= 120){\n"
                        + "			x = 0;\n"
                        + "		}\n"
                        + "	}\n"
                        + "}");
            }
        });

        tb.add(pasteButton);

        add(tb, BorderLayout.PAGE_START);
//        add(new JButton("converter"));
//        add(new JButton("sei lá..."));
        add(jsp);

        updateFunctionTokens();

    }
    private static ArrayList<Class> functionTokenClass = new ArrayList<>();
    private static ArrayList<FunctionToken> functionTokenInstances = new ArrayList<>();

    public static Collection<FunctionToken> getFunctionTokens() {
        return functionTokenInstances;
    }

    public static void updateFunctionTokens() {
        KeywordMap keywords = FunctionTokenMarker.getKeywords();

        for (Class c : PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt", FunctionToken.class)) {
            if (!functionTokenClass.contains(c)) {
                functionTokenClass.add(c);
                try {
                    FunctionToken ft = (FunctionToken) c.newInstance();
                    keywords.add(ft.getToken(), Token.LITERAL2);
                    functionTokenInstances.add(ft);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        for (Class<? extends Device> c : RobotControlPanel.getAvailableDevices()) {
            String str = c.getSimpleName();
            try {
                str = c.newInstance().getName();
            } catch (Exception ex) {
            }
            keywords.add(str, Token.OPERATOR);
        }
    }

    private static void commentSelection(JEditTextArea textArea) {
        String line;
        int lineStart;
        int lineEnd;
        int i;
        if (textArea.getSelectedText() != null) {
            int start = textArea.getSelectionStart();
            int end = textArea.getSelectionEnd();
            int startLine = textArea.getSelectionStartLine();
            int endLine = textArea.getSelectionEndLine();
            for (i = startLine; i <= endLine; i++) {
                line = textArea.getLineText(i);
                lineStart = textArea.getLineStartOffset(i);
                lineEnd = textArea.getLineEndOffset(i);
                textArea.select(lineStart, lineEnd - 1);
                textArea.setSelectedText("//" + line);
            }

            i -= startLine;

            textArea.select(start + 2, end + i * 2);

        } else {
            i = textArea.getCaretLine();
            line = textArea.getLineText(i);
            lineStart = textArea.getLineStartOffset(i);
            lineEnd = textArea.getLineEndOffset(i);
            textArea.select(lineStart, lineEnd - 1);
            textArea.setSelectedText("//" + line);
            textArea.selectNone();
        }
    }

    private static void uncommentSelection(JEditTextArea textArea) {
        String line;
        int lineStart;
        int lineEnd;
        int i;
        if (textArea.getSelectedText() != null) {
            int start = textArea.getSelectionStart();
            int end = textArea.getSelectionEnd();
            int startLine = textArea.getSelectionStartLine();
            int endLine = textArea.getSelectionEndLine();
            int j = 0;
            for (i = startLine; i <= endLine; i++) {
                line = textArea.getLineText(i);
                lineStart = textArea.getLineStartOffset(i);
                lineEnd = textArea.getLineEndOffset(i);
                textArea.select(lineStart, lineEnd - 1);
                if (line.startsWith("//")) {
                    textArea.setSelectedText(line.substring(2));
                    j++;
                }

            }

            if (j > 0) {
                start -= 2;
            }

            textArea.select(start, end - j * 2);
        } else {
            i = textArea.getCaretLine();
            line = textArea.getLineText(i);
            lineStart = textArea.getLineStartOffset(i);
            lineEnd = textArea.getLineEndOffset(i);
            textArea.select(lineStart, lineEnd - 1);
            if (line.startsWith("//")) {
                textArea.setSelectedText(line.substring(2));
            }
            textArea.selectNone();
        }
    }

    public JEditTextArea getTextArea() {
        return textArea;
    }

    public Function getFunction() {
        return function;
    }

//    public void setFunction(Function function) {
//        this.function = function;
//    }
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            textArea.copy();
            System.out.println("c");
        } else if ((e.getKeyCode() == KeyEvent.VK_X) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            textArea.cut();
            System.out.println("x");
        } else if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            textArea.paste();
            System.out.println("v");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
