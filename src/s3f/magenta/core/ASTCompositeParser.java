/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.magenta.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import static s3f.magenta.core.SubASTParser.SOMETHING;

/**
 *
 * @author anderson
 */
public class ASTCompositeParser<I extends Object, O extends Object> {

    public static boolean DEBUG_VISITOR = false;

    private HashMap<Object, ArrayList<Integer>> map;
    private ArrayList<SubASTParser<I, O>> parsers;
    private boolean multiParser = false;
    private boolean stopOnMatch = true;

    public ASTCompositeParser() {
        map = new HashMap<>();
        parsers = new ArrayList<>();
    }

    public void register(SubASTParser<I, O> parser) {
        int i = parsers.size();
        Object[] key = parser.getKey();
        parsers.add(parser);

        if (key[0] instanceof Integer) {
            ArrayList<Integer> parsersIndex = map.get(key[0]);
            if (parsersIndex == null) {
                parsersIndex = new ArrayList<>();
            }
            parsersIndex.add(i);
            map.put(key[0], parsersIndex);
        } else if (key[0] instanceof int[]) {
            for (int k : ((int[]) key[0])) {
                ArrayList<Integer> parsersIndex = map.get(k);
                if (parsersIndex == null) {
                    parsersIndex = new ArrayList<>();
                }
                parsersIndex.add(i);
                map.put(k, parsersIndex);
            }
        }
    }

    private static String getTokenName(int t) {
        Field[] declaredFields = Token.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                try {
                    if (field.getInt(null) == t) {
                        return field.getName();
                    }
                } catch (Exception ex) {
                }
            }
        }
        return "?";
    }

    protected boolean handleSubParserCall(SubASTParser<I, O> parser, AstNode tree, I input) {
        return true;
    }

    protected I postSubParserCall(SubASTParser<I, O> parser, AstNode tree, I input, O output) {
        return input;
    }

    protected O getOutput() {
        return null;
    }

    public final O parse(AstNode tree, I startInput) {
        class FinalObject<T> {

            public FinalObject() {

            }

            public FinalObject(T value) {
                this.value = value;
            }

            private T value;

            public T get() {
                return value;
            }

            public void set(T value) {
                this.value = value;
            }
        }
        final FinalObject<I> input = new FinalObject<>();
        input.set(startInput);
        tree.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode tree) {
                final FinalObject<Boolean> bool = new FinalObject<>(false);
                ArrayList<Integer> parsersIndex = map.get(tree.getType());
                if (parsersIndex == null) {
                    parsersIndex = map.get(SOMETHING);
                }
                if (parsersIndex != null) {
                    availableParsers:
                    for (int p : parsersIndex) {
                        final SubASTParser<I, O> parser = parsers.get(p);
                        parser.setParent(ASTCompositeParser.this);
                        final Iterator<Object> iterator;
                        iterator = Arrays.asList(parser.getKey()).iterator();
                        bool.set(true);
                        tree.visit(new NodeVisitor() {
                            int c = -1;

                            @Override
                            public boolean visit(AstNode it) {
                                c++;
                                if (iterator.hasNext()) {
                                    Object o = iterator.next();
                                    if (o instanceof Integer) {
                                        int intKey = (Integer) o;
                                        if (intKey != SOMETHING && intKey != it.getType()) {
                                            bool.set(false);
                                            if (DEBUG_VISITOR) {
                                                System.out.println(
                                                        parser.getId()
                                                        + " was desqualified on token "
                                                        + "#" + c + " "
                                                        //Token.typeToName
                                                        + getTokenName(intKey)
                                                        + " != "
                                                        + getTokenName(it.getType())
                                                );
                                            }
                                        }
                                    } else if (o instanceof int[]) {
                                        int[] arrayKey = (int[]) o;
                                        boolean ok = false;
                                        for (int key : arrayKey) {
                                            if (key == SOMETHING || key == it.getType()) {
                                                ok = true;
                                                break;
                                            }
                                        }
                                        if (!ok) {
                                            bool.set(false);
                                            if (DEBUG_VISITOR) {
                                                System.out.println(
                                                        parser.getId()
                                                        + " was desqualified on all tokens in "
                                                        + "#" + c + " "
                                                        + Arrays.toString(arrayKey)
                                                        + " != "
                                                        + getTokenName(it.getType())
                                                );
                                            }
                                        }
                                    } else if (o instanceof String && it instanceof Name) {
                                        Name name = (Name) it;
                                        String stringKey = (String) o;
                                        if (!stringKey.equals(name.getIdentifier())) {
                                            bool.set(false);
                                            if (DEBUG_VISITOR) {
                                                System.out.println(
                                                        parser.getId()
                                                        + " was desqualified on token "
                                                        + "#" + c + " "
                                                        + stringKey
                                                        + " != "
                                                        + name.getIdentifier()
                                                );
                                            }
                                        }
                                    } else {
                                        bool.set(false);
                                    }
                                    if (!bool.get()) {
                                        while (iterator.hasNext()) {
                                            iterator.next();
                                        }
                                    }
                                    return bool.get();
                                }
                                return false;
                            }
                        });
                        if (bool.get() && handleSubParserCall(parser, tree, input.get())) {
                            if (DEBUG_VISITOR) {
                                System.out.println("Callig " + parser.getId());
                            }
                            O output = parser.parse(tree, input.get());
                            input.set(postSubParserCall(parser, tree, input.get(), output));
                            if (!multiParser) {
                                break availableParsers;
                            }
                        }
                    }
                }
                return !(stopOnMatch && bool.get());
            }
        });
        return getOutput();
    }
}
