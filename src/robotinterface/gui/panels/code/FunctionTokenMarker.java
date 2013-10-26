/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.code;

import robotinterface.gui.panels.code.jedit.CTokenMarker;
import static robotinterface.gui.panels.code.jedit.JavaTokenMarker.getKeywords;
import robotinterface.gui.panels.code.jedit.KeywordMap;
import robotinterface.gui.panels.code.jedit.Token;

/**
 *
 * @author antunes
 */
public class FunctionTokenMarker extends CTokenMarker {

    public FunctionTokenMarker() {
        super(false, getKeywords());
    }

    public static KeywordMap getKeywords() {
        if (javaKeywords == null) {
            javaKeywords = new KeywordMap(false);
            javaKeywords.add("var", Token.KEYWORD1);
            javaKeywords.add("func", Token.KEYWORD3);
            
            javaKeywords.add("do", Token.KEYWORD1);
            javaKeywords.add("else", Token.KEYWORD1);
            javaKeywords.add("for", Token.KEYWORD1);
            javaKeywords.add("if", Token.KEYWORD1);
            javaKeywords.add("return", Token.KEYWORD1);
            javaKeywords.add("while", Token.KEYWORD1);
            
            javaKeywords.add("break", Token.KEYWORD1);
            javaKeywords.add("continue", Token.KEYWORD1);
            
            javaKeywords.add("print", Token.LITERAL2);
            javaKeywords.add("false", Token.LITERAL2);
        }
        return javaKeywords;
    }
    // private members
    private static KeywordMap javaKeywords;
}
