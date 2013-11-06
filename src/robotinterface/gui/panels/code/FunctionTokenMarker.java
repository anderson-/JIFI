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
        if (keywords == null) {
            keywords = new KeywordMap(false);
            
            keywords.add("do", Token.KEYWORD1);
            keywords.add("else", Token.KEYWORD1);
            keywords.add("for", Token.KEYWORD1);
            keywords.add("if", Token.KEYWORD1);
            keywords.add("return", Token.KEYWORD1);
            keywords.add("while", Token.KEYWORD1);
            
            keywords.add("break", Token.KEYWORD1);
            keywords.add("continue", Token.KEYWORD1);
            
            keywords.add("var", Token.KEYWORD2);
            keywords.add("func", Token.KEYWORD3);
            
            keywords.add("&", Token.OPERATOR);
        }
        return keywords;
    }
    // private members
    private static KeywordMap keywords;
}
