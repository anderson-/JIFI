/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.FunctionNode;

/**
 *
 * @author anderson
 */
public class Function extends Statement<org.mozilla.javascript.ast.FunctionNode>{

    public Function(FunctionNode subTree) {
        super(subTree);
        super.setName("function");
        super.setGraphicResource(null);
    }
    
}
