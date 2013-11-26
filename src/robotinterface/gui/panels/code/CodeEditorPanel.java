/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.procedure.Function;
import robotinterface.gui.panels.code.jedit.*;
import static robotinterface.gui.panels.code.jedit.InputHandler.BACKSPACE;
import static robotinterface.gui.panels.code.jedit.InputHandler.BACKSPACE_WORD;
import static robotinterface.gui.panels.code.jedit.InputHandler.DELETE;
import static robotinterface.gui.panels.code.jedit.InputHandler.DELETE_WORD;
import static robotinterface.gui.panels.code.jedit.InputHandler.INSERT_BREAK;
import static robotinterface.gui.panels.code.jedit.InputHandler.INSERT_TAB;
import static robotinterface.gui.panels.code.jedit.InputHandler.getTextArea;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.plugin.PluginManager;
import robotinterface.robot.device.Device;
import robotinterface.util.cyzx.Undoable;
import robotinterface.util.cyzx.HistoryManager;

/**
 *
 * @author antunes
 */
class TextAreaState {

    public String string;
    public int cursor;

    public TextAreaState(String string, int cursor) {
        this.string = string;
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "{" + "string=" + string.length() + ", cursor=" + cursor + '}';
    }
}

public class CodeEditorPanel extends JPanel implements Undoable<TextAreaState> {

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
//
//        JButton commentButton = new JButton("COM");
//        commentButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                commentSelection(textArea);
//            }
//        });
//
//        tb.add(commentButton);
//
//        JButton uncommentButton = new JButton("UNC");
//        uncommentButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                uncommentSelection(textArea);
//            }
//        });
//
//        tb.add(uncommentButton);
//
//        JButton cut = new JButton("Cut");
//        cut.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                textArea.cut();
//            }
//        });
//
//        tb.add(cut);
//
//        JButton copyButton = new JButton("Copy");
//        copyButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                textArea.copy();
//            }
//        });
//
//        tb.add(copyButton);

        JButton pasteButton = new JButton("Exemplo");
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
                        + "		var z = -x;\n"
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

        final HistoryManager<TextAreaState> hm = new HistoryManager(this, 2);

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                hm.saveState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hm.saveState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hm.saveState();
            }
        });

        textArea.getInputHandler().addKeyBinding("C+z", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                hm.undo();
            }
        });

        textArea.getInputHandler().addKeyBinding("C+y", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                hm.redo();
            }
        });

        updateFunctionTokens();

    }
    private static ArrayList<Class> functionTokenClass = new ArrayList<>();
    private static ArrayList<FunctionToken> functionTokenInstances = new ArrayList<>();

    public static Collection<FunctionToken> getFunctionTokens() {
        return functionTokenInstances;
    }

    public static void updateFunctionTokens() {
        KeywordMap keywords = FunctionTokenMarker.getKeywords();

        for (Class c : PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt", FunctionToken.class
        )) {
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

    @Override
    public void setState(TextAreaState state) {
        textArea.setText(state.string);
        try {
            textArea.setCaretPosition(state.cursor);
        } catch (java.lang.IllegalArgumentException e) {

        }
    }

    @Override
    public TextAreaState copy() {
        if (textArea.getText().isEmpty()) {
            return null;
        }
        return new TextAreaState(textArea.getText(), textArea.getCaretPosition() + 1);
    }

}
