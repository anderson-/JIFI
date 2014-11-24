/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import org.mozilla.javascript.ast.WhileLoop;
import static s3f.jifi.core.vpl.parser.VPLParser.parse;
import s3f.magenta.core.FlowchartSymbol;
import s3f.magenta.core.ShapeCreator;

/**
 *
 * @author anderson
 */
public class While extends Block {

    public While(WhileLoop subTree) {
        super(subTree);
        super.setName("while");
        addChild(parse(subTree.getBody()));
        resetGraphicResource();
        FlowchartSymbol symbol = (FlowchartSymbol) getGraphicResource();
        symbol.setShapeCreator(ShapeCreator.DIAMOND);
    }

    @Override
    public String getStatementSource() {
        return "while (" + ((WhileLoop) subTree).getCondition().toSource() + ")";
    }

}
