///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package robotinterface.gui.panels.code;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.util.ArrayList;
//import java.util.Collection;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JToolBar;
//import javax.swing.UIManager;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import robotinterface.algorithm.parser.FunctionToken;
//import robotinterface.algorithm.parser.Parser;
//import robotinterface.algorithm.procedure.Function;
//import robotinterface.gui.panels.code.jedit.*;
//import static robotinterface.gui.panels.code.jedit.InputHandler.BACKSPACE;
//import static robotinterface.gui.panels.code.jedit.InputHandler.BACKSPACE_WORD;
//import static robotinterface.gui.panels.code.jedit.InputHandler.DELETE;
//import static robotinterface.gui.panels.code.jedit.InputHandler.DELETE_WORD;
//import static robotinterface.gui.panels.code.jedit.InputHandler.INSERT_BREAK;
//import static robotinterface.gui.panels.code.jedit.InputHandler.INSERT_TAB;
//import static robotinterface.gui.panels.code.jedit.InputHandler.getTextArea;
//import robotinterface.gui.panels.robot.RobotControlPanel;
//import robotinterface.plugin.PluginManager;
//import robotinterface.robot.device.Device;
//import robotinterface.util.cyzx.Undoable;
//import robotinterface.util.cyzx.HistoryManager;
//
///**
// *
// * @author antunes
// */
//class TextAreaState {
//
//    public String string;
//    public int cursor;
//
//    public TextAreaState(String string, int cursor) {
//        this.string = string;
//        this.cursor = cursor;
//    }
//
//    @Override
//    public String toString() {
//        return "{" + "string=" + string.length() + ", cursor=" + cursor + '}';
//    }
//}
//
//public class CodeEditorPanel extends JPanel implements Undoable<TextAreaState> {
//
//    private Function function;
//    private JEditTextArea textArea;
//
//    public CodeEditorPanel(Function function) {
//        super(new BorderLayout());
//
//        this.function = function;
//
//        textArea = new JEditTextArea();
//        textArea.setTokenMarker(new FunctionTokenMarker());
//        textArea.setText(Parser.encode(function));
//        textArea.getPainter().setFont(UIManager.getDefaults().getFont("TextPane.font"));
//        textArea.recalculateVisibleLines();
//        textArea.setFirstLine(0);
//        textArea.setElectricScroll(0);
//        textArea.getPainter().setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
//
//        SyntaxStyle[] styles = SyntaxUtilities.getDefaultSyntaxStyles();
//
//        Color cstring = Color.decode("#f07818");
//        Color cfunction = Color.decode("#6a4a3c");
//        Color cvar = Color.decode("#cc333f");
//        Color cblocks = Color.decode("#00a0b0");
//        Color cfuncandbool = Color.decode("#8fbe00");
//        Color cdevices = Color.decode("#00C12B");
//
//        styles[Token.COMMENT1] = new SyntaxStyle(Color.GRAY, false, false);
//        styles[Token.KEYWORD1] = new SyntaxStyle(cblocks, false, true);
//        styles[Token.KEYWORD2] = new SyntaxStyle(cvar, false, true);
//        styles[Token.KEYWORD3] = new SyntaxStyle(cfunction, true, true);
//
//        styles[Token.LITERAL1] = new SyntaxStyle(cstring, false, false);
//        styles[Token.LITERAL2] = new SyntaxStyle(cfuncandbool, false, true);
//
//        styles[Token.OPERATOR] = new SyntaxStyle(cdevices, false, true);
//
//        textArea.getPainter().setStyles(styles);
//
//        JScrollPane jsp = new JScrollPane(textArea);
//
//        jsp.getVerticalScrollBar().setUnitIncrement(10);
//
//        JToolBar tb = new JToolBar();
//        tb.setFloatable(false);
////
////        JButton commentButton = new JButton("COM");
////        commentButton.addActionListener(new ActionListener() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                commentSelection(textArea);
////            }
////        });
////
////        tb.add(commentButton);
////
////        JButton uncommentButton = new JButton("UNC");
////        uncommentButton.addActionListener(new ActionListener() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                uncommentSelection(textArea);
////            }
////        });
////
////        tb.add(uncommentButton);
////
////        JButton cut = new JButton("Cut");
////        cut.addActionListener(new ActionListener() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                textArea.cut();
////            }
////        });
////
////        tb.add(cut);
////
////        JButton copyButton = new JButton("Copy");
////        copyButton.addActionListener(new ActionListener() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                textArea.copy();
////            }
////        });
////
////        tb.add(copyButton);
//
//        JButton pasteButton = new JButton("Exemplo");
//        pasteButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                textArea.setText(
//                        "func MyFunc(){\n"
//                        + "	var x = 0;\n"
//                        + "	var y;\n"
//                        + "	 while (1 == 1){\n"
//                        + "		read(Bussola,y);\n"
//                        + "		print(\"%v\", y);\n"
//                        + "		var z = -x;\n"
//                        + "		move(x, z);\n"
//                        + "		wait(500);\n"
//                        + "		move(0);\n"
//                        + "		wait(300);\n"
//                        + "		x = x + 10;\n"
//                        + "		if (x >= 120){\n"
//                        + "			x = 0;\n"
//                        + "		}\n"
//                        + "	}\n"
//                        + "}");
//            }
//        });
//
//        tb.add(pasteButton);
//
//        add(tb, BorderLayout.PAGE_START);
////        add(new JButton("converter"));
////        add(new JButton("sei lá..."));
//        add(jsp);
//
//        final HistoryManager<TextAreaState> hm = new HistoryManager(this, 2);
//
//        textArea.getDocument().addDocumentListener(new DocumentListener() {
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                hm.saveState();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                hm.saveState();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                hm.saveState();
//            }
//        });
//
//        textArea.getInputHandler().addKeyBinding("C+z", new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent evt) {
//                hm.undo();
//            }
//        });
//
//        textArea.getInputHandler().addKeyBinding("C+y", new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent evt) {
//                hm.redo();
//            }
//        });
//
//        updateFunctionTokens();
//
//    }
//    private static ArrayList<Class> functionTokenClass = new ArrayList<>();
//    private static ArrayList<FunctionToken> functionTokenInstances = new ArrayList<>();
//
//    public static Collection<FunctionToken> getFunctionTokens() {
//        return functionTokenInstances;
//    }
//
//    public static void updateFunctionTokens() {
//        KeywordMap keywords = FunctionTokenMarker.getKeywords();
//
//        for (Class c : PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt", FunctionToken.class
//        )) {
//            if (!functionTokenClass.contains(c)) {
//                functionTokenClass.add(c);
//                try {
//                    FunctionToken ft = (FunctionToken) c.newInstance();
//                    keywords.add(ft.getToken(), Token.LITERAL2);
//                    functionTokenInstances.add(ft);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//
//        for (Class<? extends Device> c : RobotControlPanel.getAvailableDevices()) {
//            String str = c.getSimpleName();
//            try {
//                str = c.newInstance().getName();
//            } catch (Exception ex) {
//            }
//            keywords.add(str, Token.OPERATOR);
//        }
//    }
//
//    private static void commentSelection(JEditTextArea textArea) {
//        String line;
//        int lineStart;
//        int lineEnd;
//        int i;
//        if (textArea.getSelectedText() != null) {
//            int start = textArea.getSelectionStart();
//            int end = textArea.getSelectionEnd();
//            int startLine = textArea.getSelectionStartLine();
//            int endLine = textArea.getSelectionEndLine();
//            for (i = startLine; i <= endLine; i++) {
//                line = textArea.getLineText(i);
//                lineStart = textArea.getLineStartOffset(i);
//                lineEnd = textArea.getLineEndOffset(i);
//                textArea.select(lineStart, lineEnd - 1);
//                textArea.setSelectedText("//" + line);
//            }
//
//            i -= startLine;
//
//            textArea.select(start + 2, end + i * 2);
//
//        } else {
//            i = textArea.getCaretLine();
//            line = textArea.getLineText(i);
//            lineStart = textArea.getLineStartOffset(i);
//            lineEnd = textArea.getLineEndOffset(i);
//            textArea.select(lineStart, lineEnd - 1);
//            textArea.setSelectedText("//" + line);
//            textArea.selectNone();
//        }
//    }
//
//    private static void uncommentSelection(JEditTextArea textArea) {
//        String line;
//        int lineStart;
//        int lineEnd;
//        int i;
//        if (textArea.getSelectedText() != null) {
//            int start = textArea.getSelectionStart();
//            int end = textArea.getSelectionEnd();
//            int startLine = textArea.getSelectionStartLine();
//            int endLine = textArea.getSelectionEndLine();
//            int j = 0;
//            for (i = startLine; i <= endLine; i++) {
//                line = textArea.getLineText(i);
//                lineStart = textArea.getLineStartOffset(i);
//                lineEnd = textArea.getLineEndOffset(i);
//                textArea.select(lineStart, lineEnd - 1);
//                if (line.startsWith("//")) {
//                    textArea.setSelectedText(line.substring(2));
//                    j++;
//                }
//
//            }
//
//            if (j > 0) {
//                start -= 2;
//            }
//
//            textArea.select(start, end - j * 2);
//        } else {
//            i = textArea.getCaretLine();
//            line = textArea.getLineText(i);
//            lineStart = textArea.getLineStartOffset(i);
//            lineEnd = textArea.getLineEndOffset(i);
//            textArea.select(lineStart, lineEnd - 1);
//            if (line.startsWith("//")) {
//                textArea.setSelectedText(line.substring(2));
//            }
//            textArea.selectNone();
//        }
//    }
//
//    public JEditTextArea getTextArea() {
//        return textArea;
//    }
//
//    public Function getFunction() {
//        return function;
//    }
//
//    @Override
//    public void setState(TextAreaState state) {
//        textArea.setText(state.string);
//        try {
//            textArea.setCaretPosition(state.cursor);
//        } catch (java.lang.IllegalArgumentException e) {
//
//        }
//    }
//
//    @Override
//    public TextAreaState copy() {
//        if (textArea.getText().isEmpty()) {
//            return null;
//        }
//        return new TextAreaState(textArea.getText(), textArea.getCaretPosition() + 1);
//    }
//
//}
package robotinterface.gui.panels.editor;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rtextarea.RTextScrollPane;
import robotinterface.algorithm.parser.FunctionToken;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.procedure.Function;
import robotinterface.gui.panels.editor.syntaxtextarea.FunctionTokenMaker;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.plugin.PluginManager;
import robotinterface.robot.device.Device;

public class EditorPanel extends JPanel {

    private Function function;
    private final RSyntaxTextArea textArea;
    private static DefaultCompletionProvider provider = null;
    private static final ArrayList<Class> functionTokenClass = new ArrayList<>();
    private static final ArrayList<FunctionToken> functionTokenInstances = new ArrayList<>();

    public EditorPanel(Function function) {
        super(new BorderLayout());
        this.function = function;

        textArea = new RSyntaxTextArea(20, 60);
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/Function", "robotinterface.gui.panels.editor.syntaxtextarea.FunctionTokenMaker");
        textArea.setSyntaxEditingStyle("text/Function");
        textArea.setCodeFoldingEnabled(true);
        super.add(new RTextScrollPane(textArea));

        AutoCompletion ac = new AutoCompletion(getCompletionProvider());
        int mask = InputEvent.SHIFT_MASK;
        ac.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, mask));
        ac.setShowDescWindow(true);

//      ac.setAutoCompleteEnabled(true);
//      ac.setAutoActivationDelay(500);
//      ac.setAutoActivationEnabled(true);
//      ac.setAutoCompleteSingleChoices(true);
        ac.install(textArea);
        textArea.setText(Parser.encode(function));
        updateFunctionTokens();
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public static Collection<FunctionToken> getFunctionTokens() {
        return functionTokenInstances;
    }

    public static void updateFunctionTokens() {
        TokenMap tokenMap = FunctionTokenMaker.getTokenMap();

        getCompletionProvider();
        provider.clear();

        for (Class c : PluginManager.getPluginsAlpha("robotinterface/plugin/cmdpack/plugin.txt", FunctionToken.class
        )) {
            int index = functionTokenClass.indexOf(c);
            if (index == -1) {
                functionTokenClass.add(c);
                try {
                    FunctionToken ft = (FunctionToken) c.newInstance();
                    index = functionTokenInstances.size();
                    functionTokenInstances.add(ft);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            FunctionToken ft = functionTokenInstances.get(index);
            Completion completion = ft.getInfo(provider);
            provider.addCompletion(completion);
            tokenMap.put(ft.getToken(), Token.RESERVED_WORD_2);
        }

        for (Class<? extends Device> c : RobotControlPanel.getAvailableDevices()) {
            String str = c.getSimpleName();
            try {
                str = c.newInstance().getName();
            } catch (Exception ex) {
            }
            provider.addCompletion(new BasicCompletion(provider, str));
            tokenMap.put(str, Token.DATA_TYPE);
        }

    }

    /**
     * Create a simple provider that adds some Java-related completions.
     */
    private static CompletionProvider getCompletionProvider() {
        if (provider == null) {
            // A DefaultCompletionProvider is the simplest concrete implementation
            // of CompletionProvider. This provider has no understanding of
            // language semantics. It simply checks the text entered up to the
            // caret position for a match against known completions. This is all
            // that is needed in the majority of cases.
            provider = new DefaultCompletionProvider();

            // Add completions for all Java keywords. A BasicCompletion is just
            // a straightforward word completion.
//            provider.addCompletion(new BasicCompletion(provider, "abstract"));
//            provider.addCompletion(new BasicCompletion(provider, "assert"));
//            provider.addCompletion(new BasicCompletion(provider, "break"));
//            provider.addCompletion(new BasicCompletion(provider, "case"));
//            // ... etc ...
//            provider.addCompletion(new BasicCompletion(provider, "transient"));
//            provider.addCompletion(new BasicCompletion(provider, "try"));
//            provider.addCompletion(new BasicCompletion(provider, "void"));
//            provider.addCompletion(new BasicCompletion(provider, "volatile"));
//            provider.addCompletion(new BasicCompletion(provider, "while"));
//
//            // Add a couple of "shorthand" completions. These completions don't
//            // require the input text to be the same thing as the replacement text.
//            provider.addCompletion(new ShorthandCompletion(provider, "sysout",
//                    "System.out.println(", "System.out.println("));
//            provider.addCompletion(new ShorthandCompletion(provider, "syserr",
//                    "System.err.println(", "System.err.println("));
        }
        return provider;

    }
//
//   public static void main(String[] args) {
//      // Instantiate GUI on the EDT.
//      SwingUtilities.invokeLater(new Runnable() {
//         public void run() {
//            try {
//               String laf = UIManager.getSystemLookAndFeelClassName();
//               UIManager.setLookAndFeel(laf);
//            } catch (Exception e) { /* Never happens */ }
//            new AutoCompleteDemo().setVisible(true);
//         }
//      });
//   }

}
