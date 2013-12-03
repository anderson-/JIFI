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
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.parser.decoder.TokenMgrError;
import robotinterface.algorithm.procedure.Function;
import static robotinterface.drawable.MutableWidgetContainer.autoUpdateValue;
import robotinterface.gui.panels.FlowchartPanel;
import robotinterface.gui.panels.Interpertable;
import robotinterface.gui.panels.SimulationPanel;
import robotinterface.gui.panels.TabController;
import robotinterface.gui.panels.editor.EditorPanel;
import robotinterface.gui.panels.console.MessageConsole;
import robotinterface.gui.panels.robot.RobotControlPanel;
import static robotinterface.gui.panels.robot.RobotControlPanel.VIRTUAL_CONNECTION;
import robotinterface.gui.panels.robot.RobotManager;
import robotinterface.interpreter.Interpreter;
import robotinterface.project.Project;
import robotinterface.robot.Robot;
import robotinterface.robot.connection.Connection;
import robotinterface.util.fommil.jni.JniNamer;
import robotinterface.util.SplashScreen;

/**
 *
 * @author antunes
 */
public class GUI extends javax.swing.JFrame {

    private static Logger logger = null;
    private static GUI INSTANCE = null;
    private Project mainProject = new Project();
    private ArrayList<EditorPanel> mapCE = new ArrayList<>();
    private ArrayList<FlowchartPanel> mapFC = new ArrayList<>();
    private ImageIcon codeIcon;
    private ImageIcon flowchartIcon;
    private final RobotManager robotManager;
    private JFileChooser fileChooser;

    static {
    }
    private final boolean allowMainTabbedPaneStateChanged;

    private GUI() {

        codeIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-generic.png"));
        flowchartIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-script.png"));

        initComponents();
        setLocationRelativeTo(null);
        secondarySplitPane.setDividerLocation(.5);

        //muito importante para fazer o KeyListener funcionar
        //o NetBeans mentiu quando disse que o JFrame era focusable! =(
        setFocusable(true);

        JTextPane console = new JTextPane();
        consolePanel.setLayout(new GridLayout());
        consolePanel.setName("Console");
        consolePanel.add(new JScrollPane(console));
        dynamicTabbedPane.add(consolePanel);
        MessageConsole mc = new MessageConsole(console);
        mc.redirectOut(Color.BLACK, System.out);
        mc.redirectErr(Color.RED, System.err);
        mc.setMessageLines(100);

        jSpinner1.setModel(new SpinnerNumberModel(0, 0, 9999, 50));
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
        autoUpdateValue(jSpinner1);

        //robot manager
        robotManager = new RobotManager(this);
        robotManager.createRobot();
        jScrollPane3.setViewportView(robotManager);
        jScrollPane3.getVerticalScrollBar().setUnitIncrement(10);

        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }

                String extension = null;
                String s = file.getName();
                int i = s.lastIndexOf('.');

                if (i > 0 && i < s.length() - 1) {
                    extension = s.substring(i + 1).toLowerCase();
                }

                if (extension != null) {
                    if (extension.equals(Project.FILE_EXTENSION)) {
                        return true;
                    } else {
                        return false;
                    }
                }

                return false;
            }

            @Override
            public String getDescription() {
                return "Projetos";
            }
        };
        Boolean old = UIManager.getBoolean("FileChooser.readOnly");
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        fileChooser = new JFileChooser();
        UIManager.put("FileChooser.readOnly", old);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(ff);

//        mainProject.importFile("teste.zip");
//
//        for (Function f : mainProject.getFunctions()) {
//            add(new CodeEditorPanel(f), new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
//            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
////            System.out.println(Parser.encode(f));
//        }
        updateRobotList();
        allowMainTabbedPaneStateChanged = true;
        mainTabbedPaneStateChanged(null);
    }

    public SimulationPanel getSimulationPanel() {
        return simulationPanel;
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
                    if (rcp.getConnectionComboBox().getItemCount() >= 2 && VIRTUAL_CONNECTION.equals(rcp.getConnectionComboBox().getSelectedItem())) {
                        int returnVal = JOptionPane.showConfirmDialog(this, "O Robô selecionado ainda não está conectado, \nquer que eu crie uma conexão virtual para você?", "Executar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (returnVal == JOptionPane.NO_OPTION) {
                            return false;
                        }
                    }
                }
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
            int returnVal = JOptionPane.showConfirmDialog(this, "Nenhum robô está selecionado, quer que eu crie um?", "Executar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnVal == JOptionPane.YES_OPTION) {
                robotManager.createRobot();
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

    public void updateTabNames() {
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            Component c = mainTabbedPane.getComponentAt(i);
            if (c instanceof FlowchartPanel) {
                Function f = ((FlowchartPanel) c).getFunction();
                mainTabbedPane.setTitleAt(i, "fx : " + f.getName());
            }
        }
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
        jSeparator1 = new javax.swing.JToolBar.Separator();
        debugButton = new javax.swing.JButton();
        primarySplitPane = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        simulationPanel = new robotinterface.gui.panels.SimulationPanel();
        addNewCodePanel = new javax.swing.JPanel();
        secondarySplitPane = new javax.swing.JSplitPane();
        staticTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        dynamicTabbedPane = new javax.swing.JTabbedPane();
        consolePanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        dynamicToolBar = new javax.swing.JToolBar();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JIFI - Java Interactive Flowchart Interpreter");

        toolBar.setFloatable(false);

        newFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-new.png"))); // NOI18N
        newFileButton.setToolTipText("Novo Arquivo");
        newFileButton.setBorder(null);
        newFileButton.setEnabled(false);
        newFileButton.setFocusable(false);
        newFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(newFileButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-open.png"))); // NOI18N
        openButton.setToolTipText("Abrir");
        openButton.setBorder(null);
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
        saveButton.setBorder(null);
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
        closeProjectButton.setBorder(null);
        closeProjectButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        closeProjectButton.setFocusable(false);
        closeProjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeProjectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProjectButtonActionPerformed(evt);
            }
        });
        toolBar.add(closeProjectButton);
        toolBar.add(jSeparator2);

        robotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        robotComboBox.setMaximumSize(new java.awt.Dimension(32767, 28));
        robotComboBox.setPreferredSize(new java.awt.Dimension(100, 28));
        toolBar.add(robotComboBox);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/media-playback-start.png"))); // NOI18N
        runButton.setToolTipText("Executar");
        runButton.setBorder(null);
        runButton.setFocusable(false);
        runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        toolBar.add(runButton);

        jSpinner1.setMaximumSize(new java.awt.Dimension(32767, 28));
        jSpinner1.setPreferredSize(new java.awt.Dimension(70, 28));
        toolBar.add(jSpinner1);

        stepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/step.png"))); // NOI18N
        stepButton.setToolTipText("Passo-a-passo");
        stepButton.setBorder(null);
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
        pauseButton.setBorder(null);
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
        stopButton.setBorder(null);
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
        abortButton.setBorder(null);
        abortButton.setEnabled(false);
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
        switchCodeButton.setToolTipText("Converter Código");
        switchCodeButton.setBorder(null);
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
        deleteButton.setToolTipText("Fechar Aba");
        deleteButton.setBorder(null);
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);
        toolBar.add(jSeparator1);

        debugButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/apps/utilities-terminal.png"))); // NOI18N
        debugButton.setToolTipText("Debug");
        debugButton.setBorder(null);
        debugButton.setFocusable(false);
        debugButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        debugButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        debugButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugButtonActionPerformed(evt);
            }
        });
        toolBar.add(debugButton);

        primarySplitPane.setBorder(null);
        primarySplitPane.setDividerLocation(180);
        primarySplitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        primarySplitPane.setDoubleBuffered(true);
        primarySplitPane.setEnabled(false);

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
            .addGap(0, 758, Short.MAX_VALUE)
        );
        addNewCodePanelLayout.setVerticalGroup(
            addNewCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 593, Short.MAX_VALUE)
        );

        mainTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel); // NOI18N

        primarySplitPane.setRightComponent(mainTabbedPane);
        mainTabbedPane.getAccessibleContext().setAccessibleName("");
        mainTabbedPane.getAccessibleContext().setAccessibleDescription("");

        secondarySplitPane.setBorder(null);
        secondarySplitPane.setDividerLocation(250);
        secondarySplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        staticTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        staticTabbedPane.setEnabled(false);
        staticTabbedPane.addTab("Robôs", jScrollPane3);

        jScrollPane2.setViewportView(jTree1);

        staticTabbedPane.addTab("Projeto", jScrollPane2);

        secondarySplitPane.setLeftComponent(staticTabbedPane);

        javax.swing.GroupLayout consolePanelLayout = new javax.swing.GroupLayout(consolePanel);
        consolePanel.setLayout(consolePanelLayout);
        consolePanelLayout.setHorizontalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        consolePanelLayout.setVerticalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 338, Short.MAX_VALUE)
        );

        dynamicTabbedPane.addTab("tab1", consolePanel);

        secondarySplitPane.setRightComponent(dynamicTabbedPane);

        primarySplitPane.setLeftComponent(secondarySplitPane);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        dynamicToolBar.setFloatable(false);
        dynamicToolBar.setRollover(true);

        menuFile.setText("Arquivo");

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Carregar");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem3);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Salvar");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem1);

        jMenuItem2.setText("Sair");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem2);

        menuBar.add(menuFile);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dynamicToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(primarySplitPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dynamicToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(primarySplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mainTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabbedPaneStateChanged
        if (!allowMainTabbedPaneStateChanged) {
            return;
        }

        System.gc();
        Component cmp = mainTabbedPane.getSelectedComponent();
        //dynamicTabbedPane.removeAll();

        if (cmp == simulationPanel) {
            simulationPanel.play();
        } else {
            simulationPanel.pause();
        }

        dynamicToolBar.removeAll();

        for (Component cc : dynamicTabbedPane.getComponents()) {
            if (cc != consolePanel) {//jPanel5
                dynamicTabbedPane.remove(cc);
            }
        }

        if (cmp == addNewCodePanel) {
            //adicionando uma nova aba
            FlowchartPanel fp = new FlowchartPanel(new Function());
            mainProject.getFunctions().add(fp.getFunction());
            mapFC.add(fp);
            add(fp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
            return;
        } else {
            if (cmp instanceof TabController) {
                for (JPanel p : ((TabController) cmp).getTabs()) {
                    dynamicTabbedPane.addTab(p.getName(), p);
                }

                for (JComponent jc : ((TabController) cmp).getToolBarComponents()) {
                    dynamicToolBar.add(jc);
                }
            }
        }

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

        if (cmp instanceof FlowchartPanel || cmp instanceof EditorPanel) {
            switchCodeButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            switchCodeButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        if (cmp instanceof KeyListener) {
            boolean n = true;
            for (KeyListener l : getKeyListeners()) {
                if (l.equals(cmp)) {
                    n = false;
                }
//                removeKeyListener(l);
            }
//            System.out.println("addKeyListener:" + cmp);
            if (n) {
                addKeyListener((KeyListener) cmp);
            }
        }

        if (cmp instanceof ComponentListener) {
            for (ComponentListener l : getComponentListeners()) {
                removeComponentListener(l);
            }
            addComponentListener((ComponentListener) cmp);
            ((ComponentListener) cmp).componentResized(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
        }

        if (cmp instanceof FlowchartPanel) {
            switchCodeButton.setIcon(codeIcon);
        } else if (cmp instanceof EditorPanel) {
            switchCodeButton.setIcon(flowchartIcon);
        }

        updateTabNames();
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
        closeProjectButtonActionPerformed(null);
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            simulationPanel.resetSimulation();
            File file = fileChooser.getSelectedFile();
            mainProject.importFile(file.getAbsolutePath());
        }

        loadLoop:
        for (Function f : mainProject.getFunctions()) {
            for (Component c : mainTabbedPane.getComponents()) {
                if (c instanceof FlowchartPanel) {
                    if (((FlowchartPanel) c).getFunction() == f) {
                        continue loadLoop;
                    }
                } else if (c instanceof EditorPanel) {
                    int i = mapCE.indexOf(c);
                    if (i != -1 && i < mapFC.size()) {
                        if (mapFC.get(i).getFunction() == f) {
                            continue loadLoop;
                        }
                    }
                }
            }
            FlowchartPanel fcp = new FlowchartPanel(f);
            add(fcp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mapFC.add(fcp);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
        }

    }//GEN-LAST:event_openButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filename = file.toString();
            if (!filename.endsWith("." + Project.FILE_EXTENSION)) {
                filename += "." + Project.FILE_EXTENSION;
            }
            file = new File(filename);

            if (file.exists()) {
                returnVal = JOptionPane.showConfirmDialog(this, "Deseja sobreescrever o arquivo?", "Salvar", JOptionPane.YES_NO_OPTION);
                if (returnVal != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            ArrayList<Function> functions = mainProject.getFunctions();
            boolean showNameRepeatDialog = true;
            int k;
            for (int i = 0; i < functions.size() - 1; i++) {
                k = 2;
                for (int j = i + 1; j < functions.size(); j++) {
                    if (functions.get(i).getName().equals(functions.get(j).getName())) {
                        if (showNameRepeatDialog) {
                            returnVal = JOptionPane.showConfirmDialog(this, "Existem funções com o mesmo nome,\n"
                                    + "deseja renomea-las automaticamente?", "Salvar", JOptionPane.YES_NO_OPTION);
                            if (returnVal != JOptionPane.YES_OPTION) {
                                return;
                            }
                            showNameRepeatDialog = false;
                        }
                        functions.get(j).setName(functions.get(j).getName() + k);
                        k++;
                    }
                }
            }

            mainProject.save(file.getAbsolutePath());
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void switchCodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchCodeButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();

        if (cmp instanceof FlowchartPanel) {
            FlowchartPanel fcp = (FlowchartPanel) cmp;
            EditorPanel cep;

            int i = mapFC.indexOf(cmp);
            if (i != -1 && i < mapCE.size()) {
                cep = mapCE.get(i);
            } else {
                cep = new EditorPanel(fcp.getFunction());
                mapCE.add(cep);
            }

            cep.getTextArea().setText(Parser.encode(fcp.getFunction()));
            cep.getTextArea().setCaretPosition(0);

            add(cep, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 2);
            mainTabbedPane.remove(fcp);

            fcp.getInterpreter().setInterpreterState(Interpreter.STOP);

//            switchCodeButton.setIcon(codeIcon);
        } else if (cmp instanceof EditorPanel) {
            EditorPanel cep = (EditorPanel) cmp;
            //int returnVal = JOptionPane.showConfirmDialog(this, "Durante a conversão erros podem ocorrer, deseja prosseguir?", "Converter Código", JOptionPane.YES_NO_OPTION);

            if (true /*returnVal == JOptionPane.YES_OPTION*/) {
                FlowchartPanel fcp;
                Function f = null;
                int i = mapCE.indexOf(cmp);
                int errorOnLine = -1;
                int errorColumn = 0;

                try {
                    f = Parser.decode(cep.getTextArea().getText());
                } catch (ParseException ex) {
                    errorOnLine = ex.currentToken.next.endLine - 1;
                    errorColumn = ex.currentToken.next.beginColumn;
//                    ex.printStackTrace();
                } catch (TokenMgrError ex) {
                    String msg = ex.getMessage();
                    msg = msg.substring(0, msg.indexOf(','));
                    msg = msg.substring(msg.lastIndexOf(" ") + 1);
                    try {
                        errorOnLine = Integer.parseInt(msg);
                    } catch (Exception ex1) {
                        errorOnLine = -2;
                    }
                    msg = ex.getMessage();
                    msg = msg.substring(0, msg.indexOf("."));
                    msg = msg.substring(msg.lastIndexOf(' ') + 1);
                    errorColumn = Integer.parseInt(msg);
//                    ex.printStackTrace();
                }

                switch (errorOnLine) {
                    case -1:
                        break;
                    case -2:
                        System.err.println("Erro desconhecido.");
                        return;
                    default:
                        System.err.println("Erro na linha " + errorOnLine + ".");
                        try {
                            RSyntaxTextArea textArea = cep.getTextArea();
                            textArea.removeAllLineHighlights();
//                            textArea.addLineHighlight(errorOnLine - 1, Color.red.brighter().brighter());

                            RSyntaxTextAreaHighlighter highlighter = (RSyntaxTextAreaHighlighter) textArea.getHighlighter();

                            SquiggleUnderlineHighlightPainter parserErrorHighlightPainter = new SquiggleUnderlineHighlightPainter(Color.RED);
                            System.out.println(errorColumn);
                            int p0 = textArea.getLineStartOffset(errorOnLine - 1);
                            int p1 = textArea.getLineEndOffset(errorOnLine - 1);
                            String line = textArea.getText(p0, p1);
                            //todo tirar tabs e espaços
                            highlighter.addHighlight(p0, p1, parserErrorHighlightPainter);
                        } catch (BadLocationException ex) {

                        }
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
                fcp.getInterpreter().setInterpreterState(Interpreter.STOP);
            }
        }
    }//GEN-LAST:event_switchCodeButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

        Component cmp = mainTabbedPane.getSelectedComponent();

        if (!(cmp instanceof FlowchartPanel) && !(cmp instanceof EditorPanel)) {
            return;
        }

        int returnVal = JOptionPane.showConfirmDialog(this, "Deseja excluir esse programa?", "Excluir", JOptionPane.YES_NO_OPTION);

        if (returnVal == JOptionPane.YES_OPTION) {

            Function f = null;

            if (cmp instanceof FlowchartPanel) {
                f = ((FlowchartPanel) cmp).getFunction();
            } else if (cmp instanceof EditorPanel) {
                int i = mapCE.indexOf(cmp);
                if (i != -1 && i < mapFC.size()) {
                    f = mapFC.get(i).getFunction();
                }
            }

            if (f != null && mainProject.getFunctions().remove(f)) {
                mainTabbedPane.setSelectedIndex(0);
                mainTabbedPane.remove(cmp);
            } else {
                System.out.println("fail ><");
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        saveButtonActionPerformed(null);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        openButtonActionPerformed(null);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

  private void closeProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeProjectButtonActionPerformed

      int returnVal = JOptionPane.YES_OPTION;

      if (evt != null) {
          returnVal = JOptionPane.showConfirmDialog(this, "Deseja fechar esse projeto e resetar a simulação?", "Fechar", JOptionPane.YES_NO_OPTION);
      }

      if (returnVal == JOptionPane.YES_OPTION) {

          for (FlowchartPanel fp : mapFC) {
              Interpreter i = ((FlowchartPanel) fp).getInterpreter();
              if (i != null) {
                  i.setInterpreterState(Interpreter.STOP);
              }
          }

          simulationPanel.resetSimulation();
          simulationPanel.repaint();

          mainTabbedPane.setSelectedIndex(0);
          mainProject.getFunctions().clear();
          for (Component cmp : mainTabbedPane.getComponents()) {
              if ((cmp instanceof FlowchartPanel) || (cmp instanceof EditorPanel)) {
                  mainTabbedPane.remove(cmp);
              }
          }
          mapFC.clear();
          mapCE.clear();
      }
  }//GEN-LAST:event_closeProjectButtonActionPerformed

    private static <T> void compare(List<T> a, List<T> b, Logger lg) {
        Level level = Level.OFF;
        lg.log(level, "\t{0}:", new Object[]{a.get(0)});
        boolean change = false;
        for (T t : a) {
            if (!b.contains(t)) {
                lg.log(level, "\t\t{0} [{1}] *removido*", new Object[]{t.getClass().getSimpleName(), t.hashCode()});
                change = true;
            }
        }
        for (T t : b) {
            if (!a.contains(t)) {
                lg.log(level, "\t\t{0} [{1}] *adicionado*", new Object[]{t.getClass().getSimpleName(), t.hashCode()});
                change = true;
            }
        }

        if (!change) {
            lg.log(level, "\t\t*sem alterações*");
        }
    }

    private static HashMap<Component, ArrayList<ArrayList<Object>>> STATE = new HashMap<>();

    private static void saveStateAndCompare(Component c, Logger lg) {
        Level level = Level.OFF;
        lg.log(level, ">> {0} [{1}]", new Object[]{c.getClass().getSimpleName(), c.hashCode()});
        ArrayList<ArrayList<Object>> componentState = new ArrayList<>();
        ArrayList<Object> listeners;

        Class listenerTypes[] = new Class[]{
            MouseListener.class,
            KeyListener.class,
            MouseWheelListener.class,
            MouseWheelListener.class,
            ActionListener.class,
            ComponentListener.class,
            MouseMotionListener.class
        };

        for (Class type : listenerTypes) {
            listeners = new ArrayList<>();
            listeners.add(type.getSimpleName());
            listeners.addAll(Arrays.asList(c.getListeners(type)));
            componentState.add(listeners);
        }

//        listeners = new ArrayList<>();
//        listeners.add("MouseListener");
//        listeners.addAll(Arrays.asList(c.getListeners(MouseListener.class)));
//        componentState.add(listeners);
//        listeners = new ArrayList<>();
//        listeners.add("KeyListener");
//        listeners.addAll(Arrays.asList(c.getListeners(KeyListener.class)));
//        componentState.add(listeners);
//        listeners = new ArrayList<>();
//        listeners.add("MouseWheelListener");
//        listeners.addAll(Arrays.asList(c.getListeners(MouseWheelListener.class)));
//        componentState.add(listeners);
//        listeners = new ArrayList<>();
//        listeners.add("ActionListener");
//        listeners.addAll(Arrays.asList(c.getListeners(ActionListener.class)));
//        componentState.add(listeners);
//        listeners = new ArrayList<>();
//        listeners.add("ComponentListener");
//        listeners.addAll(Arrays.asList(c.getListeners(ComponentListener.class)));
//        componentState.add(listeners);
//        listeners = new ArrayList<>();
//        listeners.add("MouseMotionListener");
//        listeners.addAll(Arrays.asList(c.getListeners(MouseMotionListener.class)));
//        componentState.add(listeners);
        //compare
        if (STATE.containsKey(c)) {
            ArrayList<ArrayList<Object>> componentOldState = STATE.get(c);
            for (int i = 0; i < componentOldState.size(); i++) {
                compare(componentOldState.get(i), componentState.get(i), lg);
            }
            STATE.remove(c);
        } else {
            lg.log(level, "\t\t{0} [{1}] *novo componente swing*", new Object[]{c.getClass().getSimpleName(), c.hashCode()});
        }
        STATE.put(c, componentState);
    }

    private static void logListeners(Component c, Logger lg) {
        Level level = Level.OFF;
        lg.log(level, ">> {0} [{1}]", new Object[]{c.getClass().getSimpleName(), c.hashCode()});
        lg.log(level, "\tMouseListeners:");
        for (MouseListener l : c.getListeners(MouseListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
        lg.log(level, "\tKeyListeners:");
        for (KeyListener l : c.getListeners(KeyListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
        lg.log(level, "\tMouseWheelListeners:");
        for (MouseWheelListener l : c.getListeners(MouseWheelListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
        lg.log(level, "\tActionListeners:");
        for (ActionListener l : c.getListeners(ActionListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
        lg.log(level, "\tComponentListeners:");
        for (ComponentListener l : c.getListeners(ComponentListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
        lg.log(level, "\tMouseMotionListeners:");
        for (MouseMotionListener l : c.getListeners(MouseMotionListener.class)) {
            lg.log(level, "\t\t{0} [{1}]", new Object[]{l.getClass().getSimpleName(), l.hashCode()});
        }
    }

    private void debugButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugButtonActionPerformed
        Date date = new Date();
        getLogger().log(Level.OFF, "< Inicio {0} >", date);
        saveStateAndCompare(this, logger);
        saveStateAndCompare(mainTabbedPane, logger);
        for (Component c : mainTabbedPane.getComponents()) {
            saveStateAndCompare(c, logger);
        }
        logger.log(Level.OFF, "< Fim {0} >\n", date);
        System.out.println("Salvo no Log");
    }//GEN-LAST:event_debugButtonActionPerformed

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
        } else {
            Command currentCommand = interpreter.getCurrentCommand();
            Function function = interpreter.getMainFunction();
            if (currentCommand != function) {
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
        }
        jSpinner1.setEnabled(true);
    }

    public static Logger getLogger() {

        Formatter myFormatter = new Formatter() {
            @Override
            public String format(final LogRecord r) {
                StringBuilder sb = new StringBuilder();
                sb.append(formatMessage(r)).append(System.getProperty("line.separator"));
                if (null != r.getThrown()) {
                    sb.append("Throwable occurred: "); //$NON-NLS-1$
                    Throwable t = r.getThrown();
                    PrintWriter pw = null;
                    try {
                        StringWriter sw = new StringWriter();
                        pw = new PrintWriter(sw);
                        t.printStackTrace(pw);
                        sb.append(sw.toString());
                    } finally {
                        if (pw != null) {
                            try {
                                pw.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    }
                }
                return sb.toString();
            }
        };

        if (logger == null) {
            logger = Logger.getLogger(GUI.class
                    .getName());
            FileHandler fh;

            try {

                // This block configure the logger with handler and formatter 
                Date date = new Date();
                fh = new FileHandler("log" + date + ".txt");
                fh.setFormatter(myFormatter);
                logger.addHandler(fh);
                logger.setUseParentHandlers(false);

                // the following statement is used to log any messages  
//                logger.info("My first log");
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }

    /**
     * Sets the java library path to the specified path
     *
     * @param path the new library path
     * @throws Exception
     */
    public static void setLibraryPath(String path) throws Exception {
        System.setProperty("java.library.path", path);

        //set sys_paths to null
        final Field sysPathsField = ClassLoader.class
                .getDeclaredField("sys_paths");
        sysPathsField.setAccessible(
                true);
        sysPathsField.set(
                null, null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final SplashScreen splashScreen = new SplashScreen("/resources/jifi5.png");
        splashScreen.splash();

        String path = GUI.class
                .getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf('/') + 1);
        path += "natives/" + JniNamer.os() + "/" + JniNamer.arch();

        try {
            String newPath = path;
            /*
             * 
             * Make sure the library is on the java lib path
             * Make sure you're using System.loadLibrary() correctly. 
             * If your library is called "libSample.so", 
             * the call should be System.loadLibrary("Sample").
             * Consider that there may be an issue with the library under OpenJDK, 
             * and that's the Java VM you're using. 
             * Run the command java -version and if part of the response is 
             * something like OpenJDK Runtime Environment (build 1.6.0_0-b11), 
             * try installing the official Sun JDK and see if that works.
             */
            setLibraryPath(newPath);
//            System.loadLibrary("rxtxSerial");
        } catch (Error | Exception e) {
//            e.printStackTrace();
            System.exit(0);
        }

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            if (!systemLookAndFeelClassName.equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
                UIManager.setLookAndFeel(systemLookAndFeelClassName);
            } else {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                GUI RobofIDE = GUI.getInstance();
                RobofIDE.setVisible(true);
                splashScreen.dispose();
            }
        }
        );
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JPanel addNewCodePanel;
    private javax.swing.JButton closeProjectButton;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JButton debugButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTabbedPane dynamicTabbedPane;
    private javax.swing.JToolBar dynamicToolBar;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
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
