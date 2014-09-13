/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Scope;

/**
 *
 * @author anderson
 */
public class Block extends Statement<AstNode> {

    public Block(Scope subTree) {
        super(subTree);
        super.setName("block");
    }
    
    public Block(org.mozilla.javascript.ast.Block subTree) {
        super(subTree);
        super.setName("block");
    }

}
