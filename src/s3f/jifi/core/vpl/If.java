/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.IfStatement;

/**
 *
 * @author anderson
 */
public class If extends Statement<org.mozilla.javascript.ast.IfStatement> {

    public If(IfStatement subTree) {
        super(subTree);
        super.setName("if");
    }

}
