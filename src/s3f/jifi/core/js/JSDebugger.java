/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.js;

import java.util.Stack;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

/**
 *
 * @author anderson
 */
public class JSDebugger implements Debugger, DebugFrame {

    Stack<ScriptableObject> scopeStack = new Stack<>();
    ScriptableObject currentScope = null;

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
//        System.out.println("onLineChange:" + js.split("\\n")[lineNumber - 1]);

        for (Object id : currentScope.getIds()) {
            Object value = currentScope.get(id);
            System.out.println(": " + id + " " + value + " " + ((value != null) ? value.getClass().getSimpleName() : ""));
        }
    }

    @Override
    public void onExceptionThrown(Context cx, Throwable ex) {

    }

    @Override
    public void onExit(Context cx, boolean byThrow, Object resultOrException) {
        scopeStack.pop();
        if (!scopeStack.empty()) {
            currentScope = scopeStack.peek();
        } else {
            currentScope = null;
        }
    }

    @Override
    public void onDebuggerStatement(Context cntxt) {

    }

}
