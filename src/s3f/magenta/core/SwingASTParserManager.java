/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import static javax.swing.SwingConstants.NEXT;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import static s3f.magenta.core.SwingASTParser.SOMETHING;

/**
 *
 * @author anderson
 */
public class SwingASTParserManager {

    private HashMap<Integer, ArrayList<Integer>> map;
    private ArrayList<SwingASTParser> parsers;

    public SwingASTParserManager() {
        map = new HashMap<>();
        parsers = new ArrayList<>();
    }

    public void register(SwingASTParser parser) {
        int i = parsers.size();
        Integer[] key = parser.getKey();
        parsers.add(parser);
        ArrayList<Integer> parsersIndex = map.get(key[0]);
        if (parsersIndex == null) {
            parsersIndex = new ArrayList<>();
        }
        parsersIndex.add(i);
        map.put(key[0], parsersIndex);
    }

    public final void build(AstNode tree, final Container panel) {
        class Bool {

            private boolean value;

            public boolean get() {
                return value;
            }

            public void set(boolean value) {
                this.value = value;
            }
        }

        tree.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode tree) {
                ArrayList<Integer> parsersIndex = map.get(tree.getType());
                if (parsersIndex != null) {
                    final Bool bool = new Bool();
//                    currentParserVerify:
                    for (int p : parsersIndex) {
                        final SwingASTParser parser = parsers.get(p);
                        final Iterator<Integer> iterator = Arrays.asList(parser.getKey()).iterator();
                        bool.set(true);
                        tree.visit(new NodeVisitor() {
                            @Override
                            public boolean visit(AstNode it) {
                                if (iterator.hasNext()) {
                                    int k = iterator.next();
                                    if (k > 0) {
                                        if (k != SOMETHING && k != it.getType()) {
                                            System.out.println(parser.getId() + " was desqualified on token " + Token.keywordToName(k) + " != " + Token.keywordToName(it.getType()));
                                            bool.set(false);
                                        }
                                    } else if (it instanceof Name) {
                                        k = -(k + 1);
                                        Name name = (Name) it;
                                        if (!parser.getNames()[k].equals(name.getIdentifier())) {
                                            System.out.println(parser.getId() + " was desqualified on token " + Token.keywordToName(k) + " != " + Token.keywordToName(it.getType()));
                                            bool.set(false);
                                        }
                                    } else {
                                        bool.set(false);
                                    }
                                }
                                return bool.get();
                            }
                        });
                        if (bool.get()) {
                            System.out.println("Callig " + parser.getId());
                            parser.appendNode(tree, panel);
//                            break currentParserVerify;
                        }
                    }
                }
                return true;
            }
        });
    }
}
