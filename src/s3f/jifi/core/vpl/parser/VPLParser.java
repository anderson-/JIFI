/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.WhileLoop;
import s3f.jifi.core.vpl.Function;
import s3f.jifi.core.vpl.If;
import s3f.jifi.core.vpl.Script;
import s3f.jifi.core.vpl.Statement;
import s3f.jifi.core.vpl.While;
import s3f.magenta.DrawingPanel;
import s3f.magenta.GraphicObject;
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
                } else if (node instanceof Scope) {
                    return parse((Scope) node);
                }
            case Token.WHILE:
                return parse((WhileLoop) node);
//            case Token.FOR:
//                return parse((Loop) node);
            case Token.IF:
                return parse((IfStatement) node);
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

    private static If parse(IfStatement node) {
        If ifStatement = new If(node);
        return ifStatement;
    }
    
    static String readFile(String path, Charset encoding) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found: " + path);
        }
    }

    public static void main(String[] args) {

        String script = readFile("/home/gnome3/testscript.js", Charset.defaultCharset());
        AstRoot tree = new Parser().parse(script, "", 1);
        final Script parse = parse(tree);

        //verifique a ordem 
//        parse.
        DrawingPanel p = new DrawingPanel();
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
        int x = 0, y = 0;
        for (s3f.jifi.core.vpl.Node n : parse.getTree()) {
            System.out.println("> " + n);
            GraphicObject graphicResource = ((Statement) n).getGraphicResource();
            if (graphicResource != null) {
                p.add(graphicResource);
                graphicResource.setLocation(x += 20, y += 20);
            }
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        parse.ident(0, 0, 50, 15);
                        Thread.sleep(100);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();

        parse.print();

    }
}
