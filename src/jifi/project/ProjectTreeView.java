/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.project;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import jifi.ui.view.SecondaryView;

/**
 *
 * @author antunes2
 */
public class ProjectTreeView implements SecondaryView {

    private final JTree tree;
    private final JScrollPane treeView;
    private Project project;
    private ViewData data;

    public ProjectTreeView(Project project) {
        //create a tree that allows one selection at a time
        tree = new JTree();
        tree.setCellRenderer(new FeeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //create the scroll pane and add the tree to it
        treeView = new JScrollPane(tree);
        //update content
        setProject(project);

        data = new ViewData("Projeto", null, "Informações sobre o projeto atual", treeView);
    }

    public final void setProject(Project project) {
        this.project = project;
        update();
    }

    @Override
    public final void update() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(project);
        tree.setModel(new DefaultTreeModel(top));
        for (Element.CategoryData category : project.getElementsCategories()) {
            DefaultMutableTreeNode elementCategory = new DefaultMutableTreeNode(category);

            top.add(elementCategory);
            for (Element se : project.getElements(category.getName())) {
                DefaultMutableTreeNode element = new DefaultMutableTreeNode(se);
                elementCategory.add(element);
            }
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    @Override
    public void selected() {

    }

    @Override
    public ViewData getData() {
        return data;
    }

    class FeeRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean bSelected, boolean bExpanded, boolean bLeaf, int nRow, boolean bFocus) {
            super.getTreeCellRendererComponent(tree, value, bSelected, bExpanded, bLeaf, nRow, bFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();
            Icon icon = null;
            if (obj instanceof Element) {
                icon = ((Element) obj).getIcon();
            } else if (obj instanceof Project) {
                setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
                return this;
            } else if (obj instanceof Element.CategoryData) {
                icon = ((Element.CategoryData) obj).getIcon();
                if (icon != null) {
                    setIcon(icon);
                    return this;
                }
            }

            if (bLeaf) {
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setIcon(UIManager.getIcon("Tree.leafIcon"));
                }
            } else {
                if (bExpanded) {
                    setIcon(UIManager.getIcon("Tree.openIcon"));
                } else {
                    setIcon(UIManager.getIcon("Tree.closedIcon"));
                }
            }
            return this;
        }
    }

}
