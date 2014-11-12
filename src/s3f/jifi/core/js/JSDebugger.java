/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.js;

import java.awt.Color;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;
import s3f.core.code.CodeEditorTab;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Editor;
import s3f.core.project.Project;
import s3f.core.script.Script;

/**
 *
 * @author anderson
 */
public class JSDebugger implements Debugger, DebugFrame {

    Stack<ScriptableObject> scopeStack = new Stack<>();
    ScriptableObject currentScope = null;
    private final String source;
    private RSyntaxTextArea textArea;

    public JSDebugger(String source) {
        this.source = source;
        EntityManager em = PluginManager.getInstance().createFactoryManager(null);

        Project project = (Project) em.getProperty("s3f.core.project.tmp", "project");
        for (s3f.core.project.Element e : project.getElements()) {
            if (e instanceof Script) {
                Script script = (Script) e;
                if (script.getText().equals(source)) {
                    Editor currentEditor = script.getCurrentEditor();
                    if (currentEditor instanceof CodeEditorTab) {
                        CodeEditorTab codeEditorTab = (CodeEditorTab) currentEditor;
                        textArea = codeEditorTab.getTextArea();
                        textArea.setEnabled(false);
                        textArea.setHighlightCurrentLine(false);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void handleCompilationDone(Context cntxt, DebuggableScript ds, String string) {

    }

    @Override
    public DebugFrame getFrame(Context cntxt, DebuggableScript ds) {
        return this;
    }

    @Override
    public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
        if (activation instanceof ScriptableObject) {
            currentScope = (ScriptableObject) activation;
            scopeStack.push(currentScope);
        }
    }

    @Override
    public void onLineChange(Context cx, int lineNumber) {

//        System.out.println("onLineChange:" + source.split("\\n")[lineNumber - 1]);
//
//        for (Object id : currentScope.getIds()) {
//            Object value = currentScope.get(id);
//            System.out.println(": " + id + " " + value + " " + ((value != null) ? value.getClass().getSimpleName() : ""));
//        }
        try {
            textArea.removeAllLineHighlights();
//            textArea.addLineHighlight(lineNumber, Color.decode("#9AFF86"));
            textArea.addLineHighlight(lineNumber, Color.getHSBColor(lineNumber/20f, 0.35f, 0.95f));
        } catch (BadLocationException ex) {

        }
        
        try {
            Thread.sleep(5);
        } catch (Exception e){
            
        }
        
    }

    @Override
    public void onExceptionThrown(Context cx, Throwable ex) {
        textArea.setEnabled(true);
        textArea.removeAllLineHighlights();
        textArea.setHighlightCurrentLine(true);
    }

    @Override
    public void onExit(Context cx, boolean byThrow, Object resultOrException) {
        scopeStack.pop();
        if (!scopeStack.empty()) {
            currentScope = scopeStack.peek();
        } else {
            currentScope = null;
            done();
        }
    }

    @Override
    public void onDebuggerStatement(Context cntxt) {

    }

    public void done() {
        textArea.setEnabled(true);
        textArea.removeAllLineHighlights();
        textArea.setHighlightCurrentLine(true);
    }

}
