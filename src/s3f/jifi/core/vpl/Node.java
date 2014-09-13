/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.vpl;

import java.util.Iterator;
import java.util.Stack;

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

    public static boolean DEBUG = true;

    private static int classCounter = 0;
    private final int id = classCounter++;
    private int iteratorType = TREE_PRE_ORDER_MODE;
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

    public void setNext(Node next) {
        this.next = next;
    }

    public final Node getPrevious() {
        return prev;
    }

    public void setPrevious(Node previous) {
        this.prev = previous;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getFirstChild() {
        return child;
    }

    private void setFirstChild(Node child) {
        this.child = child;
        child.parent = this;
    }

    public Node getLastChild() {
        return lchild;
    }

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
        if (child == null){
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
        int size = 1;
        Node it = this.child;
        while (it.next != null) {
            size += it.getTreeSize();
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
        iteratorType = TREE_PRE_ORDER_MODE;
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
        private Stack<Node> stack = new Stack<Node>();
        private final int mode;

        private TreeIterator(Node node, int mode) {
            stack.push(node);
            this.mode = mode;
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Node next() {
            if (mode == TREE_PRE_ORDER_MODE) {
                next = stack.pop();
                if (next != null && next.child != null) {
                    Node it = next.child.getLevelEnd();
                    while (it != null) {
                        stack.push(it);
                        it = it.prev;
                    }
                }
                return next;
            } else {
                throw new RuntimeException("Not implemented yet.");
            }
        }

        @Override
        public void remove() {
            next.consume();
        }
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
        {
            Node a = new Node("a");
            Node b = new Node("b");
            Node c = new Node("c");
            Node d = new Node("d");
            Node e = new Node("e");
            d.addBefore(c);
            b.addAfter(e);
            a.setChildren(b);
            b.setChildren(c);
            for (Node n : e.getLevelInverse()) {
                System.out.println(n);
            }
            System.out.println();
            for (Node n : e.getLevel()) {
                System.out.println(n);
            }
            System.out.println();
            for (Node n : d.getLevelInverse()) {
                System.out.println(n);
            }
            System.out.println();
            for (Node n : d.getLevel()) {
                System.out.println(n);
            }
        }
    }
}
