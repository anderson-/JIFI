/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;

/**
 *
 * @author anderson
 */
public class SwingASTParser {

    public static final int SOMETHING = 1;

    private final Integer[] key;
    private final String[] names;
    private final String id;

    public SwingASTParser(String id, Integer... key) {
        this(id, null, key);
    }

    public SwingASTParser(String id, String[] names, Integer... key) {
        this.id = id;
        this.names = names;
        this.key = key;
    }

    public Integer[] getKey() {
        return key;
    }

    public String[] getNames() {
        return names;
    }

    public void appendNode(AstNode subTree, Container panel) {

    }

    public String getId() {
        return id;
    }

}
