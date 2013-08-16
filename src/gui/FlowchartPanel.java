/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * TODO copy paste undo redo drag-drop move.
 *
 * @author antunes
 */
public class FlowchartPanel extends JPanel implements ListSelectionListener {

    private JLabel picture;
    private JList list;
    private JSplitPane splitPane;
    private String[] imageNames = {"Bird", "Cat", "Dog", "Rabbit", "Pig", "dukeWaveRed",
        "kathyCosmo", "lainesTongue", "left", "middle", "right", "stickerface"};

    public FlowchartPanel() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        //Create the list of images and put it in a scroll pane.

        list = new JList(imageNames);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);


        JScrollPane listScrollPane = new JScrollPane(list);
        picture = new JLabel();
        picture.setFont(picture.getFont().deriveFont(Font.ITALIC));
        picture.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane pictureScrollPane = new JScrollPane(picture);

        //Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listScrollPane, pictureScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        listScrollPane.setMinimumSize(minimumSize);
        pictureScrollPane.setMinimumSize(minimumSize);

        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(400, 200));
        updateLabel(imageNames[list.getSelectedIndex()]);
    }

    //Listens to the list
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        updateLabel(imageNames[list.getSelectedIndex()]);
    }

    //Renders the selected image
    protected void updateLabel(String name) {
        ImageIcon icon = createImageIcon("images/" + name + ".gif");
        picture.setIcon(icon);
        if (icon != null) {
            picture.setText(null);
        } else {
            picture.setText("Image not found");
        }
    }

    //Used by SplitPaneDemo2
    public JList getImageList() {
        return list;
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FlowchartPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void testeJtabbedPane() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        //frame.setUndecorated(true);

        frame.setLayout(new LayoutManager() {
            private ArrayList<Component> special = new ArrayList<>();

            public void addLayoutComponent(String name, Component comp) {
                if (name != null) {
                    special.add(comp);
                }
            }

            public void removeLayoutComponent(Component comp) {
                special.remove(comp);
            }

            public Dimension preferredLayoutSize(Container parent) {
                Dimension ps = new Dimension();
                for (Component component : parent.getComponents()) {
                    if (!special.contains(component)) {
                        Dimension cps = component.getPreferredSize();
                        ps.width = Math.max(ps.width, cps.width);
                        ps.height = Math.max(ps.height, cps.height);
                    }
                }
                return ps;
            }

            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            public void layoutContainer(Container parent) {
                Insets insets = parent.getInsets();
                for (Component component : parent.getComponents()) {
                    if (!special.contains(component)) {
                        component.setBounds(insets.left, insets.top,
                                parent.getWidth() - insets.left - insets.right,
                                parent.getHeight() - insets.top - insets.bottom);
                    } else {
                        Dimension ps = component.getPreferredSize();
                        component.setBounds(parent.getWidth() - insets.right - 2 - ps.width,
                                insets.top + 2, ps.width, ps.height);
                    }
                }
            }
        });

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tab1", new JLabel());
        tabbedPane.addTab("Tab2", new JLabel());
        tabbedPane.addTab("Tab3", new JLabel());
        frame.add(tabbedPane);
        
        final JLabel label = new JLabel("Close X");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                System.exit(0);
            }
        });
        frame.add(label, "special", 0);
        
        //anderson
        final JButton button = new JButton("Close X");
        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                System.exit(0);
            }
        });
        frame.add(button, "special", 1);
        //fim
        frame.setSize(200, 150);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //QuickFrame.create(new FlowchartPanel().getSplitPane(), "hello world");
        //QuickFrame.schedule(new FlowchartPanel().getSplitPane(), "hello world");
        testeJtabbedPane();
    }
}