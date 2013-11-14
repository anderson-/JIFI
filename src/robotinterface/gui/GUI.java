/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.procedure.Function;
import robotinterface.gui.panels.FlowchartPanel;
import robotinterface.gui.panels.Interpertable;
import robotinterface.gui.panels.SimulationPanel;
import robotinterface.gui.panels.TabController;
import robotinterface.gui.panels.code.CodeEditorPanel;
import robotinterface.gui.panels.console.MessageConsole;
import robotinterface.gui.panels.robot.RobotControlPanel;
import robotinterface.gui.panels.robot.RobotManager;
import robotinterface.interpreter.Interpreter;
import robotinterface.project.Project;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.Connection;

/**
 *
 * @author antunes
 */
public class GUI extends javax.swing.JFrame {

    private static GUI INSTANCE = null;
    private Project mainProject = new Project();
    private ArrayList<CodeEditorPanel> mapCE = new ArrayList<>();
    private ArrayList<FlowchartPanel> mapFC = new ArrayList<>();
    private ImageIcon codeIcon;
    private ImageIcon flowchartIcon;
    private final RobotManager robotManager;

    {
        codeIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-generic.png"));
        flowchartIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-script.png"));
    }

    private GUI() {
        initComponents();
        setLocationRelativeTo(null);
        secondarySplitPane.setDividerLocation(.5);

        //muito importante para fazer o KeyListener funcionar
        //o NetBeans mentiu quando disse que o JFrame era focusable! =(
        setFocusable(true);

//        simulationPanel.addRobot(new Robot());
//        simulationPanel.addRobot(new Robot());

        mainTabbedPaneStateChanged(null);

        JTextPane console = new JTextPane();
        consolePanel.setLayout(new GridLayout());
        consolePanel.setName("Console");
        consolePanel.add(new JScrollPane(console));
        dynamicTabbedPane.add(consolePanel);
        MessageConsole mc = new MessageConsole(console);
        mc.redirectOut(Color.BLACK, System.out);
        mc.redirectErr(Color.RED, System.err);
        mc.setMessageLines(100);

        jSpinner1.setModel(new SpinnerNumberModel(100, 0, 9999, 10));
        jSpinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = (int) jSpinner1.getValue();
                Component cmp = mainTabbedPane.getSelectedComponent();
                if (cmp instanceof Interpertable) {
                    Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
                    interpreter.setTimestep(i);
                }
            }
        });

        //robot manager

        robotManager = new RobotManager(this);
        jScrollPane3.setViewportView(robotManager);
        jScrollPane3.getVerticalScrollBar().setUnitIncrement(10);

//        mainProject.importFile("teste.zip");
//
//        for (Function f : mainProject.getFunctions()) {
//            add(new CodeEditorPanel(f), new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
//            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
////            System.out.println(Parser.encode(f));
//        }
        updateRobotList();
    }

    public void updateRobotList() {
        //combobox
        robotComboBox.removeAllItems();
        ArrayList<Robot> simualtionRobotList = simulationPanel.getRobots();
        for (RobotControlPanel panel : robotManager) {
            robotComboBox.addItem(panel);
            //simulation
            if (!simualtionRobotList.contains(panel.getRobot())) {
                simulationPanel.addRobot(panel.getRobot());
                panel.getRobot().setEnvironment(simulationPanel.getEnv());
            }
        }
    }

    public boolean setDefaultRobot(Interpreter interpreter, boolean ask) {
        Object o = robotComboBox.getSelectedItem();
        if (o instanceof RobotControlPanel) {
            RobotControlPanel rcp = (RobotControlPanel) o;
            Robot r = rcp.getRobot();
            Connection c = r.getMainConnection();
            if (c == null || !c.isConnected()) {
                if (ask) {
                    int returnVal = JOptionPane.showConfirmDialog(this, "O Robô selecionado ainda não está conectado, \nquer que eu crie uma conexão virtual para você?", "Executar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (returnVal == JOptionPane.NO_OPTION) {
                        return false;
                    }
                }
                rcp.refresh();
                rcp.setConnection(0);
                boolean connected = rcp.tryConnect();
                if (connected) {
                    interpreter.setRobot(r);
                }
                return connected;
            } else {
                //FAZER DIREITO
                interpreter.setRobot(r);
            }
        } else if (o == null) {
            int returnVal = JOptionPane.showConfirmDialog(this, "Nenhum robô está seleionado, quer que eu crie um?", "Executar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnVal == JOptionPane.YES_OPTION) {
                robotManager.createRobot(this);
                setDefaultRobot(interpreter, false);
            }
        }
        return true;
    }

    public static GUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GUI();
        }
        return INSTANCE;
    }

    public Collection<Function> getFunctions() {
        ArrayList<Function> funcs = new ArrayList<>();

        for (Component cc : mainTabbedPane.getComponents()) {
            if (cc instanceof FlowchartPanel) {
                funcs.add(((FlowchartPanel) cc).getFunction());
            }
        }

        return funcs;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        newFileButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        closeProjectButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        closeProjectButton1 = new javax.swing.JButton();
        closeProjectButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        robotComboBox = new javax.swing.JComboBox();
        runButton = new javax.swing.JButton();
        jSpinner1 = new javax.swing.JSpinner();
        stepButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        switchCodeButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        primarySplitPane = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        simulationPanel = new robotinterface.gui.panels.SimulationPanel();
        addNewCodePanel = new javax.swing.JPanel();
        secondarySplitPane = new javax.swing.JSplitPane();
        staticTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        dynamicTabbedPane = new javax.swing.JTabbedPane();
        consolePanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        dynamicToolBar = new javax.swing.JToolBar();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuEdit = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        toolBar.setFloatable(false);

        newFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-new.png"))); // NOI18N
        newFileButton.setToolTipText("Novo Arquivo");
        newFileButton.setFocusable(false);
        newFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(newFileButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-open.png"))); // NOI18N
        openButton.setToolTipText("Abrir");
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        toolBar.add(openButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/devices/media-floppy.png"))); // NOI18N
        saveButton.setToolTipText("Salvar");
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        toolBar.add(saveButton);

        closeProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/edit-clear.png"))); // NOI18N
        closeProjectButton.setToolTipText("Fechar Projeto");
        closeProjectButton.setFocusable(false);
        closeProjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeProjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeProjectButton);
        toolBar.add(jSeparator2);

        closeProjectButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/edit-undo.png"))); // NOI18N
        closeProjectButton1.setToolTipText("Fechar Projeto");
        closeProjectButton1.setFocusable(false);
        closeProjectButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeProjectButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeProjectButton1);

        closeProjectButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/edit-redo.png"))); // NOI18N
        closeProjectButton2.setToolTipText("Fechar Projeto");
        closeProjectButton2.setFocusable(false);
        closeProjectButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeProjectButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeProjectButton2);
        toolBar.add(jSeparator1);

        robotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        robotComboBox.setPreferredSize(new java.awt.Dimension(100, 28));
        toolBar.add(robotComboBox);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/media-playback-start.png"))); // NOI18N
        runButton.setToolTipText("Executar");
        runButton.setFocusable(false);
        runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        toolBar.add(runButton);

        jSpinner1.setPreferredSize(new java.awt.Dimension(70, 28));
        toolBar.add(jSpinner1);

        stepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/step.png"))); // NOI18N
        stepButton.setToolTipText("Passo-a-passo");
        stepButton.setFocusable(false);
        stepButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stepButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepButtonActionPerformed(evt);
            }
        });
        toolBar.add(stepButton);

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/media-playback-pause.png"))); // NOI18N
        pauseButton.setToolTipText("Pausar");
        pauseButton.setFocusable(false);
        pauseButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pauseButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        toolBar.add(pauseButton);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/media-playback-stop.png"))); // NOI18N
        stopButton.setToolTipText("Parar");
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        toolBar.add(stopButton);

        abortButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/process-stop.png"))); // NOI18N
        abortButton.setToolTipText("Abortar");
        abortButton.setFocusable(false);
        abortButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        abortButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });
        toolBar.add(abortButton);
        toolBar.add(jSeparator3);

        switchCodeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-generic.png"))); // NOI18N
        switchCodeButton.setToolTipText("Fechar Projeto");
        switchCodeButton.setFocusable(false);
        switchCodeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        switchCodeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        switchCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchCodeButtonActionPerformed(evt);
            }
        });
        toolBar.add(switchCodeButton);

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/tab-remove.png"))); // NOI18N
        deleteButton.setToolTipText("Fechar Projeto");
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);

        primarySplitPane.setDividerLocation(180);
        primarySplitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        primarySplitPane.setDoubleBuffered(true);
        primarySplitPane.setOneTouchExpandable(true);

        mainTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabbedPaneStateChanged(evt);
            }
        });
        mainTabbedPane.addTab("Simulação", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/devices/input-gaming.png")), simulationPanel); // NOI18N

        javax.swing.GroupLayout addNewCodePanelLayout = new javax.swing.GroupLayout(addNewCodePanel);
        addNewCodePanel.setLayout(addNewCodePanelLayout);
        addNewCodePanelLayout.setHorizontalGroup(
            addNewCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 742, Short.MAX_VALUE)
        );
        addNewCodePanelLayout.setVerticalGroup(
            addNewCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 551, Short.MAX_VALUE)
        );

        mainTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel); // NOI18N

        primarySplitPane.setRightComponent(mainTabbedPane);
        mainTabbedPane.getAccessibleContext().setAccessibleName("");
        mainTabbedPane.getAccessibleContext().setAccessibleDescription("");

        secondarySplitPane.setDividerLocation(250);
        secondarySplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        secondarySplitPane.setOneTouchExpandable(true);

        staticTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jScrollPane2.setViewportView(jTree1);

        staticTabbedPane.addTab("Projeto", jScrollPane2);
        staticTabbedPane.addTab("Robôs", jScrollPane3);

        secondarySplitPane.setLeftComponent(staticTabbedPane);

        javax.swing.GroupLayout consolePanelLayout = new javax.swing.GroupLayout(consolePanel);
        consolePanel.setLayout(consolePanelLayout);
        consolePanelLayout.setHorizontalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );
        consolePanelLayout.setVerticalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 295, Short.MAX_VALUE)
        );

        dynamicTabbedPane.addTab("tab1", consolePanel);

        secondarySplitPane.setRightComponent(dynamicTabbedPane);

        primarySplitPane.setLeftComponent(secondarySplitPane);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        dynamicToolBar.setFloatable(false);
        dynamicToolBar.setRollover(true);

        menuFile.setText("Arquivo");
        menuBar.add(menuFile);

        menuEdit.setText("Editar");
        menuBar.add(menuEdit);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(primarySplitPane)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dynamicToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dynamicToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(primarySplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mainTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabbedPaneStateChanged
        System.gc();

        Component c = mainTabbedPane.getSelectedComponent();
        //dynamicTabbedPane.removeAll();

        dynamicToolBar.removeAll();

        for (Component cc : dynamicTabbedPane.getComponents()) {
            if (cc != consolePanel) {//jPanel5
                dynamicTabbedPane.remove(cc);
            }
        }

        if (c == addNewCodePanel) {
            //adicionando uma nova aba
            FlowchartPanel fp = new FlowchartPanel(new Function());
            mainProject.getFunctions().add(fp.getFunction());
            mapFC.add(fp);
            add(fp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));

            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
        } else {
            if (c instanceof TabController) {
                for (JPanel p : ((TabController) c).getTabs()) {
                    dynamicTabbedPane.addTab(p.getName(), p);
                }

                for (JComponent jc : ((TabController) c).getToolBarComponents()) {
                    dynamicToolBar.add(jc);
                }
            }
        }

        Component cmp = mainTabbedPane.getSelectedComponent();
        if (cmp instanceof Interpertable) {
            Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
            updateControlBar(interpreter);
        } else {
            runButton.setEnabled(false);
            jSpinner1.setEnabled(false);
            stepButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }

        if (cmp instanceof KeyListener) {
            for (KeyListener l : getKeyListeners()) {
                removeKeyListener(l);
            }
            addKeyListener((KeyListener) cmp);
        }

        if (cmp instanceof ComponentListener) {
            for (ComponentListener l : getComponentListeners()) {
                removeComponentListener(l);
            }
            addComponentListener((ComponentListener) cmp);
        }

        if (cmp instanceof FlowchartPanel) {
            switchCodeButton.setIcon(codeIcon);
        } else if (cmp instanceof CodeEditorPanel) {
            switchCodeButton.setIcon(flowchartIcon);
        }

        dynamicToolBar.updateUI();
    }//GEN-LAST:event_mainTabbedPaneStateChanged

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();
        if (cmp instanceof Interpertable) {
            Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
            if (setDefaultRobot(interpreter, true)) {
                interpreter.setInterpreterState(Interpreter.PLAY);
            } else {
                interpreter.setInterpreterState(Interpreter.STOP);
            }
            updateControlBar(interpreter);
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();
        if (cmp instanceof Interpertable) {
            Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
            if (setDefaultRobot(interpreter, true)) {
                interpreter.step();
            }
            updateControlBar(interpreter);
        }
    }//GEN-LAST:event_stepButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();
        if (cmp instanceof Interpertable) {
            Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
            interpreter.setInterpreterState(Interpreter.WAITING);
            updateControlBar(interpreter);
        }
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();
        if (cmp instanceof Interpertable) {
            Interpreter interpreter = ((Interpertable) cmp).getInterpreter();
            interpreter.setInterpreterState(Interpreter.STOP);
            updateControlBar(interpreter);
        }
    }//GEN-LAST:event_stopButtonActionPerformed

    private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_abortButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            mainProject.importFile(file.getAbsolutePath());
        }

        loadLoop:
        for (Function f : mainProject.getFunctions()) {
            for (Component c : mainTabbedPane.getComponents()) {
                if (c instanceof FlowchartPanel) {
                    if (((FlowchartPanel) c).getFunction() == f) {
                        continue loadLoop;
                    }
                } else if (c instanceof CodeEditorPanel) {
                    int i = mapCE.indexOf(c);
                    if (i != -1 && i < mapFC.size()) {
                        if (mapFC.get(i).getFunction() == f) {
                            continue loadLoop;
                        }
                    }
                }
            }
            add(new FlowchartPanel(f), new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
        }

    }//GEN-LAST:event_openButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (file.exists()) {
                returnVal = JOptionPane.showConfirmDialog(this, "Deseja sobreescrever o arquivo?", "Salvar", JOptionPane.YES_NO_OPTION);
                if (returnVal != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            mainProject.save(file.getAbsolutePath());
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void switchCodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchCodeButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();

        if (cmp instanceof FlowchartPanel) {
            FlowchartPanel fcp = (FlowchartPanel) cmp;
            CodeEditorPanel cep;

            int i = mapFC.indexOf(cmp);
            if (i != -1 && i < mapCE.size()) {
                cep = mapCE.get(i);
            } else {
                cep = new CodeEditorPanel(fcp.getFunction());
                mapCE.add(cep);
            }

            add(cep, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
            mainTabbedPane.remove(fcp);
//            switchCodeButton.setIcon(codeIcon);

        } else if (cmp instanceof CodeEditorPanel) {
            CodeEditorPanel cep = (CodeEditorPanel) cmp;
            //int returnVal = JOptionPane.showConfirmDialog(this, "Durante a conversão erros podem ocorrer, deseja prosseguir?", "Converter Código", JOptionPane.YES_NO_OPTION);

            if (true /*returnVal == JOptionPane.YES_OPTION*/) {
                FlowchartPanel fcp;
                Function f = null;
                int i = mapCE.indexOf(cmp);

                try {
                    f = Parser.decode(cep.getTextArea().getText());
                } catch (Exception ex) {
                    System.out.println("ERRO!!! D:");
                    ex.printStackTrace();
                    return;
                }

                if (i != -1 && i < mapFC.size()) {
                    fcp = mapFC.get(i);
                    mainProject.getFunctions().remove(fcp.getFunction());
                    fcp.setFunction(f);
                } else {
                    fcp = new FlowchartPanel(f);
                    mapFC.add(fcp);
                }

                mainProject.getFunctions().add(f);

                add(fcp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
                mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
                mainTabbedPane.remove(cep);
//                switchCodeButton.setIcon(flowchartIcon);
            }
        }
    }//GEN-LAST:event_switchCodeButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

        int returnVal = JOptionPane.showConfirmDialog(this, "Deseja excluir?", "Excluir", JOptionPane.YES_NO_OPTION);

        if (returnVal == JOptionPane.YES_OPTION) {
            Component cmp = mainTabbedPane.getSelectedComponent();

            if (cmp instanceof FlowchartPanel) {
            } else if (cmp instanceof CodeEditorPanel) {
            }

        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    public void add(JComponent panel, ImageIcon icon) {
        mainTabbedPane.remove(addNewCodePanel);
        mainTabbedPane.addTab(panel.getName(), icon, panel);
        if (panel instanceof ComponentListener) {
            addComponentListener((ComponentListener) panel);
        }
        mainTabbedPane.addTab(addNewCodePanel.getName(), new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel);
    }

    public void updateControlBar(Interpreter interpreter) {

        if (interpreter.getInterpreterState() == Interpreter.PLAY) {
            //play
            runButton.setEnabled(false);
            stepButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
        } else if (!interpreter.getCurrentCommand().equals(interpreter.getMainFunction())) {
            //pause
            runButton.setEnabled(true);
            stepButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            //stop
            runButton.setEnabled(true);
            stepButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
        jSpinner1.setEnabled(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GUI.getInstance().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JPanel addNewCodePanel;
    private javax.swing.JButton closeProjectButton;
    private javax.swing.JButton closeProjectButton1;
    private javax.swing.JButton closeProjectButton2;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTabbedPane dynamicTabbedPane;
    private javax.swing.JToolBar dynamicToolBar;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuEdit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JButton newFileButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JSplitPane primarySplitPane;
    private javax.swing.JComboBox robotComboBox;
    private javax.swing.JButton runButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JSplitPane secondarySplitPane;
    private robotinterface.gui.panels.SimulationPanel simulationPanel;
    private javax.swing.JTabbedPane staticTabbedPane;
    private javax.swing.JButton stepButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JButton switchCodeButton;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
