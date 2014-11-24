/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.Dimension;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 *
 * @author anderson
 */
public class Node implements Iterable<Node> {

    public static final int LIST_LEVEL_MODE = 1;
    public static final int LIST_LEVEL_INVERSE_MODE = 2;
    public static final int TREE_PRE_ORDER_MODE = 3;
    public static final int TREE_IN_ORDER_MODE = 4;
    public static final int TREE_POS_ORDER_MODE = 5;
    public static final int TREE_LEVEL_MODE = 6;
    public static final int DEFAULT_MODE = TREE_PRE_ORDER_MODE;
    private static int iteratorType = TREE_PRE_ORDER_MODE;

    public static boolean DEBUG = true;

    private static int classCounter = 0;
    private final int id = classCounter++;
    private String name;
    private Node prev = null;
    private Node next = null;
    private Node parent = null;
    private Node child = null;
    private Node lchild = null;

    public Node() {
        name = "?";
    }

    public Node(String name) {
        this.name = name;
    }

    public final int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getNext() {
        return next;
    }

    @Deprecated
    public void setNext(Node next) {
        this.next = next;
    }

    public final Node getPrevious() {
        return prev;
    }

    @Deprecated
    public void setPrevious(Node previous) {
        this.prev = previous;
    }

    public Node getParent() {
        return parent;
    }

    @Deprecated
    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getFirstChild() {
        return child;
    }

    @Deprecated
    private void setFirstChild(Node child) {
        this.child = child;
        child.parent = this;
    }

    public Node getLastChild() {
        return lchild;
    }

    @Deprecated
    private void setLastChild(Node child) {
        this.lchild = child;
        child.parent = this;
    }

    public void setChildren(Node children) {
        this.child = children;
        this.lchild = null;
        Node it = children;
        while (it != null) {
            it.parent = this;
            if (it.next == null) {
                this.lchild = it;
            }
            it = it.next;
        }
    }

    public final int getDepth() {
        int depth = 0;
        Node it = parent;
        while (it != null) {
            depth++;
            it = it.parent;
        }
        return depth;
    }

    public void addBefore(Node node) {
        if (prev != node) {
            if (prev == null) {
                if (parent != null) {
                    parent.setFirstChild(node);
                }
            } else {
                prev.next = node;
            }
            node.prev = prev;
            node.next = this;
            this.prev = node;
            node.parent = parent;
        }
    }

    public void addAfter(Node node) {
        if (next != node) {
            if (next != null) {
                next.prev = node;
            } else if (parent != null) {
                parent.setLastChild(node);
            }
            node.next = next;
            node.prev = this;
            this.next = node;
            node.parent = parent;
        }
    }

    public void addChild(Node node) {
        if (child == null) {
            this.setChildren(node);
        } else {
            this.lchild.addAfter(node);
        }
    }

    public void consume() {
        if (parent != null && parent.child == this) {
            parent.child = next;
        }
        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        prev = null;
        next = null;
        parent = null;
    }

    public Node getLevelStart() {
        Node it = this;
        while (it.prev != null) {
            it = it.prev;
        }
        return it;
    }

    public Node getLevelEnd() {
        Node it = this;
        while (it.next != null) {
            it = it.next;
        }
        return it;
    }

    public int getLevelSize() {
        int size = 1;
        Node it = this;
        while (it.prev != null) {
            it = it.prev;
            size++;
        }
        it = this;
        while (it.next != null) {
            it = it.next;
            size++;
        }
        return size;
    }

    public Node getTreeRoot() {
        Node it = this;
        while (it.parent != null) {
            it = it.parent;
        }
        return it;
    }

    public int getTreeSize() {
        int size = 0;
        Node it = this;
        while (it != null) {
            if (it.child != null) {
                size += it.child.getTreeSize() + 1;
            }
            it = it.next;
        }
        return size;
    }

    public Node getLevel() {
        iteratorType = LIST_LEVEL_MODE;
        return this.getLevelStart();
    }

    public Node getLevelInverse() {
        iteratorType = LIST_LEVEL_INVERSE_MODE;
        return this.getLevelEnd();
    }

    public Node getTree() {
        iteratorType = TREE_PRE_ORDER_MODE;
        return this;
    }

    public Node getMode(int mode) {
        iteratorType = mode;
        return this;
    }

    @Override
    public Iterator<Node> iterator() {
        int mode = iteratorType;
        iteratorType = DEFAULT_MODE;
        if (mode == LIST_LEVEL_MODE || mode == LIST_LEVEL_INVERSE_MODE) {
            return new ListIterator(this, mode);
        } else {
            return new TreeIterator(this, mode);
        }
    }

    @Override
    public String toString() {
        return name + "[" + id + "]";
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        print(out, new String[]{
            "> ",
            "  ",
            " '-- ",
            " +-- ",
            "     ",
            " :   "
        });
    }

    public void print(PrintStream out, String[] symbols) {
        out.println(symbols[0] + toString());
        Node it = child;
        while (it != null) {
            it.print(symbols[1], (it.next == null), out, symbols);
            it = it.next;
        }
    }

    private void print(String prefix, boolean isTail, PrintStream out, String[] symbols) {
        out.println(prefix + symbols[isTail ? 2 : 3] + toString());
        Node it = child;
        while (it != null) {
            it.print(prefix + symbols[isTail ? 4 : 5], (it.next == null), out, symbols);
            it = it.next;
        }
    }

    public static class ListIterator implements Iterator<Node> {

        private Node it;
        private final int mode;

        private ListIterator(Node node, int mode) {
            it = node;
            this.mode = mode;
        }

        @Override
        public boolean hasNext() {
            return it != null;
        }

        @Override
        public Node next() {
            Node tmp = it;
            if (mode == LIST_LEVEL_MODE) {
                it = it.next;
            } else {
                it = it.prev;
            }
            return tmp;
        }

        @Override
        public void remove() {
            it.consume();
        }
    }

    public static class TreeIterator implements Iterator<Node> {

        private Node next = null;
        private final Deque<Node> deque = new ArrayDeque<>();
        private final int mode;

        private TreeIterator(Node node, int mode) {
            this.mode = mode;
            if (mode == TREE_PRE_ORDER_MODE) {
                buildPreOrder(node);
            } else if (mode == TREE_IN_ORDER_MODE) {
                buildInOrder(node);
            } else if (mode == TREE_POS_ORDER_MODE) {
                buildPosOrder(node);
                deque.addLast(node);
            } else if (mode == TREE_LEVEL_MODE) {
                if (node != null) {
                    Deque<Node> q = new ArrayDeque<>();
                    q.addLast(node);
                    while (!q.isEmpty()) {
                        node = q.removeFirst();
                        deque.addLast(node);
                        Node it = node.child;
                        while (it != null) {
                            q.addLast(it);
                            it = it.next;
                        }
                    }
                }
            }
        }

        private void buildPreOrder(Node node) {
            deque.addLast(node);
            if (node != null) {
                Node it = node.child;
                while (it != null) {
                    buildPreOrder(it);
                    it = it.next;
                }
            }
        }

        private void buildInOrder(Node node) {
            if (node != null) {
                buildInOrder(node.child);
                deque.addLast(node);
                buildInOrder(node.lchild);
            }
        }

        private void buildPosOrder(Node node) {
            if (node != null) {
                Node it = node.child;
                while (it != null) {
                    buildPosOrder(it);
                    deque.addLast(it);
                    it = it.next;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !deque.isEmpty();
        }

        @Override
        public Node next() {
            next = deque.removeFirst();
            return next;
        }

        @Override
        public void remove() {
            next.consume();
        }
    }

    private static void show(String title, Node... nodes) {
        int w, h;
        w = h = 500;

        final SparseMultigraph<Node, String> graph = new SparseMultigraph<>();

        //adiciona vertices
        for (Node n : nodes) {
            graph.addVertex(n);
        }

        //adciona arestas
        for (Node n : nodes) {
            if (n.next != null) {
                graph.addEdge(n.getName() + ".next", n, n.next);
            }
            if (n.prev != null) {
                graph.addEdge(n.getName() + ".prev", n, n.prev);
            }
            if (n.parent != null) {
                graph.addEdge(n.getName() + ".parent", n, n.parent);
            }
            if (n.child != null) {
                graph.addEdge(n.getName() + ".child", n, n.child);
            }
            if (n.lchild != null) {
                graph.addEdge(n.getName() + ".lchild", n, n.lchild);
            }
        }

        KKLayout<String, String> kkLayout = new KKLayout(graph);//new FRLayout(graph);
        kkLayout.setSize(new Dimension(w, h));

        VisualizationViewer<String, String> vv = new VisualizationViewer<>(kkLayout);
        vv.setPreferredSize(new Dimension(w, h));

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>());

        vv.setEdgeToolTipTransformer(new ToStringLabeller<String>());

        vv.setVertexToolTipTransformer(new ToStringLabeller<String>());

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        final JFrame frame = new JFrame(title);
        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(gm.getModeMenu());
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        {
//            Node a = new Node("a");
//            Node b = new Node("b");
//            Node c = new Node("c");
//            Node d = new Node("d");
//            Node e = new Node("e");
//            a.setFirstChild(b);
//            b.addAfter(e);
//            b.setFirstChild(c);
//            c.addAfter(d);
//            for (Node n : a) {
//                System.out.println(n);
//            }
//        }
//        {
//            Node a = new Node("a");
//            Node b = new Node("b");
//            Node c = new Node("c");
//            Node d = new Node("d");
//            Node e = new Node("e");
//            a.setFirstChild(e);
//            e.addBefore(b);
//            b.setFirstChild(d);
//            d.addBefore(c);
//            for (Node n : a) {
//                System.out.println(n);
//            }
//        }
//        {
//            Node a = new Node("a");
//            Node b = new Node("b");
//            Node c = new Node("c");
//            Node d = new Node("d");
//            Node e = new Node("e");
//            d.addBefore(c);
//            b.addAfter(e);
//            a.setChildren(b);
//            b.setChildren(c);
////            for (Node n : e.getLevelInverse()) {
////                System.out.println(n);
////            }
////            System.out.println();
////            for (Node n : e.getLevel()) {
////                System.out.println(n);
////            }
////            System.out.println();
////            for (Node n : d.getLevelInverse()) {
////                System.out.println(n);
////            }
////            System.out.println();
////            for (Node n : d.getLevel()) {
////                System.out.println(n);
////            }
////            System.out.println("IN:");
////            for (Node n : a.getMode(Node.TREE_IN_ORDER_MODE)) {
////                System.out.println(n);
////            }
//        }
        {
            Node a = new Node("a");
            Node b = new Node("b");
            Node c = new Node("c");
            Node d = new Node("d");
            Node e = new Node("e");
            Node f = new Node("f");
            Node g = new Node("g");
            Node h = new Node("h");
            Node i = new Node("i");
            Node j = new Node("j");
            Node k = new Node("k");
            Node l = new Node("l");
            Node m = new Node("m");
            a.setChildren(b);
            b.addAfter(c);
            c.addAfter(g);
            c.setChildren(d);
            d.addAfter(e);
            e.setChildren(f);
            g.setChildren(h);
            h.addAfter(l);
            l.addAfter(m);
            h.setChildren(i);
            i.setChildren(j);
            j.addAfter(k);
//            show("", a, b, c, d, e, f, g, h, i, j, k, l, m);
            System.out.println("PRE:");
            for (Node n : a.getMode(Node.TREE_PRE_ORDER_MODE)) {
                System.out.println(n);
            }
            System.out.println("POS:");
            for (Node n : a.getMode(Node.TREE_POS_ORDER_MODE)) {
                System.out.println(n);
            }
            System.out.println("LEVEL:");
            for (Node n : a.getMode(Node.TREE_LEVEL_MODE)) {
                System.out.println(n);
            }
            System.out.println(h.getTreeSize());
            a.print(System.out);
        }
    }
}
