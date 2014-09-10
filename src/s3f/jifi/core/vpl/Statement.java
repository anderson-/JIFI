/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;

/**
 *
 * @author anderson
 */
public abstract class Statement extends Node<Statement> {

    public Statement() {

    }

//    public Statement spawn(AstNode subTree) {
//        subTree.visit(new NodeVisitor() {
//            @Override
//            public boolean visit(AstNode tree) {
//                return true
//            }
//        });
//    }

}
