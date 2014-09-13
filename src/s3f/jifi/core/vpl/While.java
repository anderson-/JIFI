/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.WhileLoop;

/**
 *
 * @author anderson
 */
public class While extends Statement<org.mozilla.javascript.ast.WhileLoop> {

    public While(WhileLoop subTree) {
        super(subTree);
        super.setName("while");
    }

}
