/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl.parser;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.WhileLoop;
import s3f.jifi.core.vpl.Function;
import s3f.jifi.core.vpl.Script;
import s3f.jifi.core.vpl.Statement;
import s3f.jifi.core.vpl.While;
import s3f.magenta.DrawingPanel;
import s3f.magenta.util.QuickFrame;

/**
 *
 * @author anderson
 */
public class VPLParser {

    public static Statement parse(AstNode node) {
        switch (node.getType()) {
            case Token.SCRIPT:
                return parse((ScriptNode) node);
            case Token.FUNCTION:
                return parse((FunctionNode) node);
            case Token.BLOCK:
                if (node instanceof Block) {
                    return parse((Block) node);
                } else if (node instanceof Scope){
                    return parse((Scope) node);
                }
            case Token.WHILE:
                return parse((WhileLoop) node);
//            case Token.FOR:
//                return parse((Loop) node);
//            case Token.IF:
//                return parse((IfStatement) node);
            default:
                return new Statement(node);
        }
    }

    private static Script parse(ScriptNode node) {
        Script script = new Script(node);
        for (AstNode f : node.getFunctions()) {
            script.addChild(parse(f));
        }
        for (AstNode s : node.getStatements()) {
            script.addChild(parse(s));
        }
        return script;
    }

    private static Function parse(FunctionNode node) {
        Function function = new Function(node);
        Statement body = parse(node.getBody());
        function.addChild(body);
        return function;
    }

    private static s3f.jifi.core.vpl.Block parse(Block node) {
        s3f.jifi.core.vpl.Block block = new s3f.jifi.core.vpl.Block(node);
        for (Node statement : node) {
            block.addChild(parse((AstNode) statement));
        }
        return block;
    }
    private static s3f.jifi.core.vpl.Block parse(Scope node) {
        s3f.jifi.core.vpl.Block block = new s3f.jifi.core.vpl.Block(node);
        for (Node statement : node) {
            block.addChild(parse((AstNode) statement));
        }
        return block;
    }

    private static While parse(WhileLoop node) {
        While block = new While(node);
        for (Node statement : node) {
            block.addChild(parse((AstNode) statement));
        }
        return block;
    }

    public static void main(String[] args) {
        String script = ""
                + "var x = 2;"
                + "var y = 3;"
                + "func(x,y);"
                + "{print('teste');}"
                + "if (x==2){print('teste2');}"
                + "";
        AstRoot tree = new Parser().parse(script, "", 1);
        Script parse = parse(tree);

        DrawingPanel p = new DrawingPanel();
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
        
        for (s3f.jifi.core.vpl.Node n : parse.getTree()) {
            System.out.println(n);
            p.add(((Statement)n).getGraphicResource());
        }
        
        
    }
}
