/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.Container;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;

/**
 *
 * @author anderson
 */
public class SubASTParser<I extends Object, O extends Object> {

    public static final int SOMETHING = 1;

    private final Object[] key;
    private final String id;
    private ASTCompositeParser<I, O> parent;

    public SubASTParser(String id, Object... key) {
        this.id = id;
        this.key = key;
    }

    public final String getId() {
        return id;
    }

    public final Object[] getKey() {
        return key;
    }

    public final ASTCompositeParser<I, O> getParent() {
        return parent;
    }

    public final void setParent(ASTCompositeParser<I, O> parent) {
        this.parent = parent;
    }

    public O parse(AstNode subTree, I input) {
        return null;
    }

}
