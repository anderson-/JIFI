/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.Loop;

/**
 *
 * @author anderson
 */
public class ForLoop extends Statement{

    public ForLoop(Loop subTree) {
        super(subTree);
        super.setName("for");
    }
    
}
