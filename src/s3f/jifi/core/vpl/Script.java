/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.ScriptNode;

/**
 *
 * @author anderson
 */
public class Script extends Statement<org.mozilla.javascript.ast.ScriptNode>{

    public Script(ScriptNode subTree) {
        super(subTree);
        super.setName("script");
        super.setGraphicResource(null);
    }
    
    
    
}
