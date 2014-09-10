/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.js;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.AstRoot;
import s3f.magenta.core.SwingASTParser;
import s3f.magenta.core.SwingASTParserManager;

//-- This is the class whose instance method will be made available in a JavaScript scope as a global function.
//-- It extends from ScriptableObject because instance methods of only scriptable objects can be directly exposed
//-- in a js scope as a global function.
public class MyScriptable extends ScriptableObject {

    public HashMap<String, Method> functions = new HashMap<>();

    public MyScriptable() {

    }

    private void parse(String script, final String name) {
        AstRoot tree = new Parser().parse(script, name, 1);

//        System.out.println(tree.debugPrint());
//        tree.visitAll(new NodeVisitor() {
//            @Override
//            public boolean visit(AstNode n) {
////                switch (n.getType()) {
//////                    case Token.SCRIPT:
//////                    case Token.FUNCTION:
//////                    case Token.BLOCK:
//////                        return true;
////                    default:
////                        System.out.println(n.toSource());
////                        return true;
////                }
//                if (n.getType() == Token.NAME && n.getParent().getType() != Token.GETPROP && n.getParent().getType() != Token.FUNCTION && n.getParent().getType() != Token.CALL) {
////                    ((org.mozilla.javascript.ast.Name) n).setIdentifier(((org.mozilla.javascript.ast.Name) n).getIdentifier().toUpperCase());
//                } else if (n.getType() == Token.IF) {
//                    org.mozilla.javascript.ast.IfStatement i = (org.mozilla.javascript.ast.IfStatement) n;
//                    System.out.println(i.getCondition().toSource());
//                    AstNode parse = ((org.mozilla.javascript.ast.ExpressionStatement) new Parser().parse("boolFunc(3)", name, 1).getFirstChild()).getExpression();
//
//                    i.setCondition(parse);
//                }
//                return true;
//            }
//        });
//
        System.out.println(tree.debugPrint());
//        System.out.println(tree.toSource());

        SwingASTParserManager pm = new SwingASTParserManager();
        pm.register(new SwingASTParser("2", Token.SCRIPT, Token.FUNCTION));
        pm.register(new SwingASTParser("1", Token.SCRIPT));
        pm.register(new SwingASTParser("3", Token.SCRIPT, Token.FUNCTION, Token.NAME, Token.NAME, Token.BLOCK, Token.VAR, Token.VAR, Token.NAME, Token.MUL, Token.NAME, Token.NUMBER));
        pm.register(new SwingASTParser("6", new String[]{"fun"}, Token.CALL, -1));
        pm.register(new SwingASTParser("ret", Token.RETURN, Token.NUMBER));
        //implementar OR(int,int,int,int) alocando 4 numeros < 255 em um int
        //vai dar problema com os numeros negativos, então arrueme...
        pm.build(tree, null);

    }

    static class C {

        public static Double tests(Double number, String s) {
            System.out.println(s);
            return number * 3.0d;
        }
    }

    public static void main(String args[]) throws Exception {
        MyScriptable myScriptable = new MyScriptable();

        myScriptable.register("fun", C.class, "tests", Double.class, String.class);

        String testScript = ""
                + "    function f(n){\n"
                + "var x = n*3;\n"
                + "//teste\n"
                + "var result = fun(12.32 * n);\n"
                + "    x++;\n"
                + "    java.lang.System.out.println(result);\n"
                + "    java.lang.System.out.println(x);\n"
                + "    if (x == 10){\n"
                + "     java.lang.System.out.println(13123);\n"
                + "    } else {\n"
                + "     java.lang.System.out.println(234234);\n"
                + "    }\n"
                + "    return n;\n"
                + "    }\n"
                + "    f(0);\n"
                + "var w = 3;\n"
                + "    f(w);\n"
                + "";

        myScriptable.parse(testScript, "My Script");
//        myScriptable.compileAndRun(testScript, "My Script");
    }

    public void register(String javascriptFunctionName, Class anClass, String methodName, Class... args) throws NoSuchMethodException {
        Method method = anClass.getMethod(methodName, args);
        if (Modifier.isStatic(method.getModifiers())) {
            functions.put(javascriptFunctionName, method);
        } else {
            throw new IllegalArgumentException(method.getName() + "(...) is not static.");
        }
    }

    public void compileAndRun(final String script, String name) {
        try {
            Context.enter();
            Context context = Context.getCurrentContext();
            context.setOptimizationLevel(-1);
            context.setGeneratingDebug(true);
//            context.setDebugger(new JSDebugger(), "My DEb");

            Scriptable scriptExecutionScope = new ImporterTopLevel(context);

            for (Map.Entry<String, Method> e : functions.entrySet()) {
                FunctionObject scriptableInstanceMethodBoundJavascriptFunction = new MyFunctionObject(e.getKey(), e.getValue(), this);
                scriptExecutionScope.put(e.getKey(), scriptExecutionScope, scriptableInstanceMethodBoundJavascriptFunction);
            }
            context.initStandardObjects();
            org.mozilla.javascript.Script compiledScript = context.compileString(script, name, 0, null);
            Object o = compiledScript.exec(context, scriptExecutionScope);
            System.out.println("Ret:" + o);

        } catch (Exception e) {
            throw e;
        } finally {
            Context.exit();
        }
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    private static class MyFunctionObject extends FunctionObject {

        private MyFunctionObject(String name, Member methodOrConstructor, Scriptable parentScope) {
            super(name, methodOrConstructor, parentScope);
        }

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            Object[] argsEx = new Object[args.length + 1];
            System.arraycopy(args, 0, argsEx, 0, args.length);
            argsEx[args.length] = "asd"; //resourceManager
            return super.call(cx, scope, getParentScope(), argsEx);
        }
    }
}
