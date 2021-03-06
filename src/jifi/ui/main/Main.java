/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.ui.main;

import java.awt.Component;
import java.awt.Dimension;
import java.io.InputStream;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import jifi.project.Project;
import jifi.project.ProjectTreeView;
import jifi.project.Element;
import jifi.project.FileCreator;
import jifi.ui.view.View;
import jifi.ui.view.tabbedpaneview.TabbedPaneView;

/**
 *
 * @author Anderson
 */
public class Main {

    private static class TmpView implements View {

        private final ViewData data;

        public TmpView(String title, Icon icon, String tooltip, Component component) {
            data = new ViewData(title,icon,tooltip,component);
        }

        @Override
        public void update() {

        }

        @Override
        public void selected() {

        }

        @Override
        public ViewData getData() {
            return data;
        }

    };

    static class TMPElement implements Element {

        private final String name;
        private final Icon icon;
        private final String cname = "asd" + (int) (Math.random() * 10);

        public TMPElement(String name, Icon icon) {
            this.name = name;
            this.icon = icon;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public void save(FileCreator fileCreator) {

        }

        @Override
        public CategoryData getCategoryData() {
            //return new CategoryData(cname, ".x", UIManager.getIcon("FileView.computerIcon"), this);
            return new CategoryData(cname, ".x", null, this);
        }

        @Override
        public Element load(InputStream stream) {
            return null;
        }

    }

    public static void main(String[] args) {

        if (true) {

            //UIManager.put("nimbusBase", Color.GREEN.darker());
            //UIManager.put("nimbusBlueGrey", Color.red.brighter());
            //UIManager.put("control", Color.YELLOW.brighter());
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {

            }
        }

        JFrame frame = new JFrame("Isso é um teste!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setPreferredSize(new Dimension(400, 400));
        TabbedPaneView mainView = new TabbedPaneView();

//        final JToolBar toolBar = new JToolBar();
//        //toolBar.add(new ToolBarButton().getJComponent());
//        toolBar.add(new JButton("asd"));
//        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(mainView.getJPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        mainView.split(TabbedPaneView.VERTICAL, true);
        Project project = new Project("Projeto X");

        for (int i = 0; i < 10; i++) {
            project.addElement(new TMPElement("asdl", UIManager.getIcon("FileView.fileIcon")));
        }

        mainView.add(new ProjectTreeView(project));
        mainView.add(new jifi.ui.view.MessageTab());
        addTabs(mainView);
        addTabs(mainView.get(TabbedPaneView.SECOND));
        

        //addTabs(mainView.get(TabbedPaneView.SECOND));
    }

    public static void addTabs(TabbedPaneView view) {
        view.add(new TmpView("Teste", null, "sem dica", new ToolBarButton().getJComponent()));
        view.add(new TmpView("JTree 00", UIManager.getIcon("FileView.fileIcon"), "sem dica", new JScrollPane(new JTree())));
        view.add(new TmpView("JLabel 01", null, "sem dica", new JLabel("Test")));
        view.add(new TmpView("JTextArea 03", null, "sem dica", new JScrollPane(new JTextArea("asfasdfasfasdfas\nafasfasdfaf\n"))));
        view.add(new TmpView("JLabel 04", null, "sem dica", new JLabel("<html>asfasfdasdfasdfsa<br>asfdd13412341234123446745fgh")));
        view.add(new TmpView("Title " + Math.random(), null, "sem dica", new JScrollPane(new JTree())));
        
    }

}
