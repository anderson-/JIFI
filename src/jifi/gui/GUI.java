/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import jifi.algorithm.Command;
import jifi.algorithm.parser.Parser;
import jifi.algorithm.parser.decoder.ParseException;
import jifi.algorithm.parser.decoder.TokenMgrError;
import jifi.algorithm.procedure.Function;
import jifi.drawable.DrawingPanel;
import static jifi.drawable.swing.MutableWidgetContainer.autoUpdateValue;
import jifi.gui.panels.FlowchartPanel;
import jifi.gui.panels.Interpertable;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.SimulationPanel;
import jifi.gui.panels.TabController;
import jifi.gui.panels.editor.EditorPanel;
import jifi.gui.panels.console.MessageConsole;
import jifi.gui.panels.robot.RobotControlPanel;
import static jifi.gui.panels.robot.RobotControlPanel.VIRTUAL_CONNECTION;
import jifi.gui.panels.robot.RobotManager;
import jifi.interpreter.Interpreter;
import jifi.project.Project;
import jifi.robot.Robot;
import jifi.robot.connection.Connection;
import jifi.util.fommil.jni.JniNamer;
import jifi.util.SplashScreen;

/**
 *
 * @author antunes
 */
public class GUI extends JFrame implements ComponentListener {

    private static Logger logger = null;
    private static GUI INSTANCE = null;
    private Project mainProject = new Project();
    private ArrayList<EditorPanel> mapCE = new ArrayList<>();
    private ArrayList<FlowchartPanel> mapFC = new ArrayList<>();
    private ImageIcon codeIcon;
    private ImageIcon flowchartIcon;
    private final ImageIcon splitIcon;
    private final ImageIcon unsplitIcon;
    private final RobotManager robotManager;
    private final JFileChooser fileChooser;
    private final boolean allowMainTabbedPaneStateChanged;
    private final JSplitPane simulationSplitPanel;
    private boolean splitView = false;
    private static JTextArea console;
    public boolean LOG = false;
    private static ConsoleManagerThread cmt = null;
    private Interpreter mainInterpreter = new Interpreter();
    private Interpreter interpreter;
    private ShortcutsWindow shortcutsWindow;
    private int lastIdx;
    private final JToolBar helpPanel;
    private String helpTip = "";
    private AboutWindow aboutWindow;
    private RobotControlPanel createRobot ;

    private JToolBar aushd() {
        return helpPanel;
    }

    @Deprecated
    public FlowchartPanel getFlowcharPanel() {
        return mapFC.get(0);
    }

    private static class ConsoleManagerThread extends Thread {

        public ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            int size;
            while (true) {
                size = queue.size();
                if (size != 0) {
                    console.setText(console.getText() + "\n" + queue.poll());
                    console.selectAll();
                    if (size > 20) {
                        System.err.println("Console Queue Overflow :/");
                        console.setText("Console Queue Overflow :/\n" + queue.poll());
                        queue.clear();
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void enqueue(String str) {
            queue.offer(str);
        }

    }

    public static void print(String str) {
        if (cmt == null || !cmt.isAlive()) {
            cmt = new ConsoleManagerThread();
            cmt.start();
        }

        cmt.enqueue(str);
    }

    private GUI() {

        codeIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-generic.png"));
        flowchartIcon = new ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-script.png"));
        splitIcon = new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/window-split-vertical.png"));
        unsplitIcon = new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/window-unsplit.png"));

        helpPanel = new JToolBar() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawString(helpTip, 25, g.getFontMetrics().getAscent() + 2);
            }
        };

        initComponents();
        setLocationRelativeTo(null);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
//        secondarySplitPane.setDividerLocation(.5);
        stepButton.setVisible(false);
        deleteButton.setVisible(false);
        jSeparator5.setVisible(false);

        //muito importante para fazer o KeyListener funcionar
        //o NetBeans mentiu quando disse que o JFrame era focusable! =(
        setFocusable(true);

        console = new JTextArea();
        consolePanel.setLayout(new GridLayout());
        consolePanel.setName("Console");
        consolePanel.add(new JScrollPane(console));
//        dynamicTabbedPane.add(consolePanel);
//        MessageConsole mc = new MessageConsole(console);
//        mc.redirectOut(Color.BLACK, System.out);
//        mc.redirectErr(Color.RED, System.err);
//        mc.setMessageLines(100);
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Limpar");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.setText("");
            }
        });
        popupMenu.add(menuItem);
        console.add(popupMenu);
        console.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == e.BUTTON3) {
                    popupMenu.show(console, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

//        jSpinner1.setModel(new SpinnerNumberModel(0, 0, 9999, 10));
//        jSpinner1.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                int i = (int) jSpinner1.getValue();
//                if (interpreter != null) {
//                    interpreter.setTimestep(i);
//                }
//            }
//        });
//        autoUpdateValue(jSpinner1);
        jSpinner1.setVisible(false);
        robotComboBox.setVisible(false);
        timestepTButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (interpreter != null) {
                    if (timestepTButton.getModel().isSelected()) {
                        interpreter.setTimestep(200);
                    } else {
                        interpreter.setTimestep(0);
                    }
                } else if (timestepTButton.getModel().isSelected()) {
                    timestepTButton.getModel().setPressed(false);
                }
            }
        });

        //robot manager
        robotManager = new RobotManager(this);
        createRobot = robotManager.createRobot();
        createRobot.setConnectButton(conectButton);
        
        dynamicTabbedPane.add(new JScrollPane(createRobot),"Conexões");
        
//        jScrollPane3.setViewportView(robotManager);
//        jScrollPane3.getVerticalScrollBar().setUnitIncrement(10);

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

        simulationSplitPanel = new JSplitPane();

//        dynamicToolBar.setVisible(false);
        updateRobotList();

        if (LOG) {
            saveSatateAndCompare();
        }
        allowMainTabbedPaneStateChanged = true;
        mainTabbedPaneStateChanged(null);
        super.addComponentListener(this);

        console.setText("");
        addDebugMenu();
        super.setIconImage(new ImageIcon(getClass().getResource("/resources/jifi_icon.png")).getImage());

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                String ObjButtons[] = {"Fechar", "Cancelar"};
                int PromptResult = JOptionPane.showOptionDialog(null, "Tem certeza que deseja fechar o programa?\nTodas as alterações não salvas serão perdidas.", "JIFI", JOptionPane.NO_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]);
                if (PromptResult == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        //simplificando....
        addNewCodePanel.setVisible(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                {
                    //adicionando um novo fluxograma
                    FlowchartPanel fp = new FlowchartPanel(new Function(), mainInterpreter);
                    mainProject.getFunctions().add(fp.getFunction());
                    mapFC.add(fp);
                    add(fp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
                    mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2

                    //adicionando editor do robo
                    for (RobotControlPanel rp : robotManager) {
                        RobotEditorPanel ep = new RobotEditorPanel(rp.getRobot());
                        add(ep, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/preferences-system.png")));
                        mainTabbedPane.setTitleAt(mainTabbedPane.getTabCount() - 1, "Editor");
                    }
                }
            }
        });

    }

    @Deprecated
    public final void addDebugMenu() {
        menuDev.add(newItem("Print lib dir", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String str = "natives/" + JniNamer.os() + "/" + JniNamer.arch();
                JOptionPane.showMessageDialog(null, str, "Print lib dir", JOptionPane.INFORMATION_MESSAGE);
            }
        }));
    }

    @Deprecated
    public final JMenuItem newItem(String name, ActionListener action) {
        JMenuItem item = new JMenuItem();
        item.setText(name);
        item.addActionListener(action);
        return item;
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
                mainTabbedPane.setTitleAt(i, "Fluxograma");
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

        dynamicToolBar = new javax.swing.JToolBar();
        secondarySplitPane = new javax.swing.JSplitPane();
        staticTabbedPane = new javax.swing.JTabbedPane();
        toolBar = new javax.swing.JToolBar();
        newFileButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        clearSimulationButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        timestepTButton = new javax.swing.JToggleButton();
        robotComboBox = new javax.swing.JComboBox();
        runButton = new javax.swing.JButton();
        jSpinner1 = new javax.swing.JSpinner();
        stepButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        switchCodeButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        deleteButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        splitViewButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        conectButton = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        keyboardShortcutsButton = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        keyboardShortcutsButton1 = new javax.swing.JButton();
        primarySplitPane = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        simulationPanel = new jifi.gui.panels.SimulationPanel();
        addNewCodePanel = new javax.swing.JPanel();
        dynamicTabbedPane = new javax.swing.JTabbedPane();
        consolePanel = new javax.swing.JPanel();
        jToolBar1 = aushd();
        helpCheckBox = new javax.swing.JCheckBox();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        menuDev = new javax.swing.JMenu();

        dynamicToolBar.setFloatable(false);
        dynamicToolBar.setRollover(true);

        secondarySplitPane.setBorder(null);
        secondarySplitPane.setDividerLocation(220);
        secondarySplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        secondarySplitPane.setEnabled(false);

        staticTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        staticTabbedPane.setEnabled(false);
        secondarySplitPane.setLeftComponent(staticTabbedPane);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JIFI - Java Interactive Flowchart Interpreter");

        toolBar.setFloatable(false);

        newFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-new.png"))); // NOI18N
        newFileButton.setToolTipText("Novo Arquivo");
        newFileButton.setBorder(null);
        newFileButton.setFocusable(false);
        newFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                newFileButtonMouseEntered(evt);
            }
        });
        newFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFileButtonActionPerformed(evt);
            }
        });
        toolBar.add(newFileButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/document-open.png"))); // NOI18N
        openButton.setToolTipText("Abrir");
        openButton.setBorder(null);
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openButtonMouseEntered(evt);
            }
        });
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
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveButtonMouseEntered(evt);
            }
        });
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        toolBar.add(saveButton);
        toolBar.add(jSeparator4);

        clearSimulationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/edit-clear.png"))); // NOI18N
        clearSimulationButton.setToolTipText("Limpar Simulação");
        clearSimulationButton.setBorder(null);
        clearSimulationButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        clearSimulationButton.setFocusable(false);
        clearSimulationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearSimulationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearSimulationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearSimulationButtonMouseEntered(evt);
            }
        });
        clearSimulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSimulationButtonActionPerformed(evt);
            }
        });
        toolBar.add(clearSimulationButton);
        toolBar.add(jSeparator2);

        timestepTButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/appointment-new.png"))); // NOI18N
        timestepTButton.setToolTipText("Diminuir Tempo");
        timestepTButton.setFocusable(false);
        timestepTButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        timestepTButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        timestepTButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                timestepTButtonMouseEntered(evt);
            }
        });
        timestepTButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timestepTButtonActionPerformed(evt);
            }
        });
        toolBar.add(timestepTButton);

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
        runButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                runButtonMouseEntered(evt);
            }
        });
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
        pauseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pauseButtonMouseEntered(evt);
            }
        });
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
        stopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                stopButtonMouseEntered(evt);
            }
        });
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        toolBar.add(stopButton);
        toolBar.add(jSeparator3);

        switchCodeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/mimetypes/text-x-generic.png"))); // NOI18N
        switchCodeButton.setToolTipText("Converter");
        switchCodeButton.setBorder(null);
        switchCodeButton.setFocusable(false);
        switchCodeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        switchCodeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        switchCodeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                switchCodeButtonMouseEntered(evt);
            }
        });
        switchCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchCodeButtonActionPerformed(evt);
            }
        });
        toolBar.add(switchCodeButton);
        toolBar.add(jSeparator5);

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/tab-remove.png"))); // NOI18N
        deleteButton.setToolTipText("Fechar Aba");
        deleteButton.setBorder(null);
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButtonMouseEntered(evt);
            }
        });
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);
        toolBar.add(jSeparator1);

        splitViewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/actions/window-split-vertical.png"))); // NOI18N
        splitViewButton.setToolTipText("Visão Lado a Lado");
        splitViewButton.setBorder(null);
        splitViewButton.setFocusable(false);
        splitViewButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        splitViewButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        splitViewButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                splitViewButtonMouseEntered(evt);
            }
        });
        splitViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitViewButtonActionPerformed(evt);
            }
        });
        toolBar.add(splitViewButton);
        splitViewButton.getAccessibleContext().setAccessibleDescription("Dividir Janela");

        toolBar.add(filler1);

        conectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/status/network-offline.png"))); // NOI18N
        conectButton.setToolTipText("Conectar");
        conectButton.setBorder(null);
        conectButton.setFocusable(false);
        conectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        conectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        conectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                conectButtonMouseEntered(evt);
            }
        });
        conectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conectButtonActionPerformed(evt);
            }
        });
        toolBar.add(conectButton);
        toolBar.add(jSeparator8);

        keyboardShortcutsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/apps/preferences-desktop-keyboard-shortcuts.png"))); // NOI18N
        keyboardShortcutsButton.setToolTipText("Atalhos do Programa");
        keyboardShortcutsButton.setBorder(null);
        keyboardShortcutsButton.setFocusable(false);
        keyboardShortcutsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        keyboardShortcutsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        keyboardShortcutsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                keyboardShortcutsButtonMouseEntered(evt);
            }
        });
        keyboardShortcutsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardShortcutsButtonActionPerformed(evt);
            }
        });
        toolBar.add(keyboardShortcutsButton);
        toolBar.add(jSeparator6);

        keyboardShortcutsButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tango/32x32/apps/help-browser.png"))); // NOI18N
        keyboardShortcutsButton1.setToolTipText("Sobre o JIFI");
        keyboardShortcutsButton1.setBorder(null);
        keyboardShortcutsButton1.setFocusable(false);
        keyboardShortcutsButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        keyboardShortcutsButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        keyboardShortcutsButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                keyboardShortcutsButton1MouseEntered(evt);
            }
        });
        keyboardShortcutsButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardShortcutsButton1ActionPerformed(evt);
            }
        });
        toolBar.add(keyboardShortcutsButton1);

        primarySplitPane.setBorder(null);
        primarySplitPane.setDividerLocation(200);
        primarySplitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        primarySplitPane.setDoubleBuffered(true);

        mainTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabbedPaneStateChanged(evt);
            }
        });
        mainTabbedPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mainTabbedPaneMouseMoved(evt);
            }
        });
        mainTabbedPane.addTab("Simulação", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/devices/input-gaming.png")), simulationPanel); // NOI18N

        javax.swing.GroupLayout addNewCodePanelLayout = new javax.swing.GroupLayout(addNewCodePanel);
        addNewCodePanel.setLayout(addNewCodePanelLayout);
        addNewCodePanelLayout.setHorizontalGroup(
            addNewCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 726, Short.MAX_VALUE)
        );
        addNewCodePanelLayout.setVerticalGroup(
            addNewCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );

        mainTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel); // NOI18N

        primarySplitPane.setRightComponent(mainTabbedPane);
        mainTabbedPane.getAccessibleContext().setAccessibleName("");
        mainTabbedPane.getAccessibleContext().setAccessibleDescription("");

        dynamicTabbedPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                dynamicTabbedPaneMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout consolePanelLayout = new javax.swing.GroupLayout(consolePanel);
        consolePanel.setLayout(consolePanelLayout);
        consolePanelLayout.setHorizontalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        consolePanelLayout.setVerticalGroup(
            consolePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        dynamicTabbedPane.addTab("Terminal", consolePanel);

        primarySplitPane.setLeftComponent(dynamicTabbedPane);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        helpCheckBox.setSelected(true);
        helpCheckBox.setToolTipText("Selecione para dicas");
        helpCheckBox.setFocusable(false);
        helpCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        helpCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        helpCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpCheckBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(helpCheckBox);

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

        jMenu1.setText("Ajuda");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Atalhos do Programa");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);
        jMenu1.add(jSeparator7);

        jMenuItem5.setText("Sobre");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        menuBar.add(jMenu1);

        menuDev.setText("    ");
        menuBar.add(menuDev);
        menuDev.getAccessibleContext().setAccessibleName(".");

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(primarySplitPane)
            .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(primarySplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void setSimulationMode() {
        mainTabbedPane.setSelectedComponent(simulationPanel);
        System.out.println("set");
    }

//    Interpreter interpreter = null;

    private void mainTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabbedPaneStateChanged
        if (!allowMainTabbedPaneStateChanged) {
            return;
        }

        System.gc();
        Component cmp = mainTabbedPane.getSelectedComponent();
        //dynamicTabbedPane.removeAll();

        if (cmp instanceof EditorPanel){
            EditorPanel.updateFunctionTokens();
        }
        
        if (cmp == simulationPanel || (cmp instanceof JSplitPane)) {
            simulationPanel.play();
        } else {
            simulationPanel.pause();
        }

        dynamicToolBar.removeAll();

//        for (Component cc : dynamicTabbedPane.getComponents()) {
//            if (cc != consolePanel) {//jPanel5
//                dynamicTabbedPane.remove(cc);
//            }
//        }
        if (cmp == addNewCodePanel) {
            //adicionando uma nova aba
            FlowchartPanel fp = new FlowchartPanel(new Function(), mainInterpreter);
            mainProject.getFunctions().add(fp.getFunction());
            mapFC.add(fp);
            add(fp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2
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
            interpreter = ((Interpertable) cmp).getInterpreter();
        } else if (cmp == simulationPanel) {
            for (Component c : mainTabbedPane.getComponents()) {
                if (c instanceof Interpertable) {
                    Interpreter i = ((Interpertable) c).getInterpreter();
                    if (i.getInterpreterState() == Interpreter.PLAY) {
                        interpreter = i;
                        break;
                    }
                }
            }
        }
        updateControlBar(interpreter);

        if (cmp instanceof FlowchartPanel || cmp instanceof EditorPanel) {
            switchCodeButton.setEnabled(true);
            deleteButton.setEnabled(true);
            splitViewButton.setEnabled(true);
        } else {
            switchCodeButton.setEnabled(false);
            deleteButton.setEnabled(false);
            if (!(cmp instanceof JSplitPane)) {
                splitViewButton.setEnabled(false);
            } else {
                splitViewButton.setEnabled(true);
            }
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
            for (ComponentListener l : mainTabbedPane.getComponentListeners()) {
                mainTabbedPane.removeComponentListener(l);
            }
            mainTabbedPane.addComponentListener((ComponentListener) cmp);
            ((ComponentListener) cmp).componentResized(new ComponentEvent(mainTabbedPane, ComponentEvent.COMPONENT_RESIZED));
        }

        if (cmp instanceof FlowchartPanel) {
            switchCodeButton.setIcon(codeIcon);
        } else if (cmp instanceof EditorPanel) {
            switchCodeButton.setIcon(flowchartIcon);
            interpreter = null;
            updateControlBar(null);
        }

        updateTabNames();
        dynamicToolBar.updateUI();
        if (LOG) {
            saveSatateAndCompare();
        }
    }//GEN-LAST:event_mainTabbedPaneStateChanged

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        if (interpreter != null) {
            if (setDefaultRobot(interpreter, true)) {
                interpreter.setInterpreterState(Interpreter.PLAY);
                if (console != null) {
                    console.setText("");
                }
            } else {
                interpreter.setInterpreterState(Interpreter.STOP);
            }
        }
        updateControlBar(interpreter);
    }//GEN-LAST:event_runButtonActionPerformed

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepButtonActionPerformed
        if (interpreter != null) {
            interpreter.setInterpreterState(Interpreter.WAITING);
            if (setDefaultRobot(interpreter, true)) {
                while (true) {
                    interpreter.step();
                    if (interpreter.getCurrentCommand() != null && interpreter.getCurrentCommand().getDrawableResource() != null) {
                        break;
                    }
                }
            }
        }
        updateControlBar(interpreter);
    }//GEN-LAST:event_stepButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        if (interpreter != null) {
            interpreter.setInterpreterState(Interpreter.WAITING);
        }
        updateControlBar(interpreter);
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if (interpreter != null) {
            interpreter.setInterpreterState(Interpreter.STOP);
        }
        updateControlBar(interpreter);
    }//GEN-LAST:event_stopButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            returnVal = JOptionPane.showConfirmDialog(this, "O projeto atual será fechado, deseja prosseguir?", "Abrir", JOptionPane.YES_NO_OPTION);

            if (returnVal != JOptionPane.YES_OPTION) {
                return;
            }
            newFileButtonActionPerformed(null);
            simulationPanel.resetSimulation();
            File file = fileChooser.getSelectedFile();
            mainProject.importFile(file.getAbsolutePath());
        } else {
            return;
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
            FlowchartPanel fcp = new FlowchartPanel(f, mainInterpreter);
            add(fcp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mapFC.add(fcp);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2
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

            long t = System.currentTimeMillis();
            cep.getTextArea().setText(Parser.encode(fcp.getFunction()));
            long t2 = System.currentTimeMillis();
//            System.out.println("conversão: " + ((t2 - t) / 1000.0) + "ms");

            cep.getTextArea().setCaretPosition(0);

            add(cep, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);// - 2
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
                String errorDesc = "";

                try {
                    long t = System.currentTimeMillis();
                    f = Parser.decode(cep.getTextArea().getText());
                    long t2 = System.currentTimeMillis();
//                    System.out.println("conversão: " + ((t2 - t) / 1000.0) + "ms");

                } catch (ParseException ex) {
                    errorOnLine = ex.currentToken.next.endLine;
                    errorColumn = ex.currentToken.next.beginColumn;
                    if (ex.tokenImage.length == 1) {
                        errorDesc = ex.tokenImage[0];
                    } else {
                        errorDesc = "Error de sintaxe";
                    }
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
                    errorDesc = "Erro lexico";
                } catch (Throwable e) {
                    errorDesc = e.getMessage();
                    errorOnLine = -2;
                    e.printStackTrace();
                }

                switch (errorOnLine) {
                    case -1:
                        break;
                    case -2:
                        System.err.println("Erro desconhecido: " + errorDesc);
                        return;
                    default:
                        System.err.println(errorDesc + " na linha " + errorOnLine + ".");
                        try {
                            RSyntaxTextArea textArea = cep.getTextArea();
//                            textArea.removeAllLineHighlights();
//                            textArea.addLineHighlight(errorOnLine - 1, Color.red.brighter().brighter());

                            RSyntaxTextAreaHighlighter highlighter = (RSyntaxTextAreaHighlighter) textArea.getHighlighter();

                            SquiggleUnderlineHighlightPainter parserErrorHighlightPainter = new SquiggleUnderlineHighlightPainter(Color.RED);
//                            System.out.println(errorColumn);
                            int p0 = textArea.getLineStartOffset(errorOnLine - 1);
//                            System.out.println(p0);
                            int p1 = textArea.getLineEndOffset(errorOnLine - 1);
                            String line = textArea.getText(p0, p1 - p0);
//                            System.out.println("'" + line + "'");
                            for (char c : line.toCharArray()) {
                                if (c == ' ' || c == '\t') {
                                    p0++;
                                } else {
                                    break;
                                }
                            }
                            //todo tirar tabs e espaços
                            highlighter.removeAllHighlights();
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
                    fcp = new FlowchartPanel(f, mainInterpreter);
                    mapFC.add(fcp);
                }

                mainProject.getFunctions().add(f);

                add(fcp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
                mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2
                mainTabbedPane.remove(cep);
//                switchCodeButton.setIcon(flowchartIcon);
                fcp.getInterpreter().setInterpreterState(Interpreter.STOP);
            }
        }
        mainTabbedPaneStateChanged(null);
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

  private void clearSimulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSimulationButtonActionPerformed
      int returnVal = JOptionPane.showConfirmDialog(this, "Deseja limpar a simulação?", "Limpar", JOptionPane.YES_NO_OPTION);

      if (returnVal == JOptionPane.YES_OPTION) {
          simulationPanel.resetSimulation();
          simulationPanel.repaint();
      }
  }//GEN-LAST:event_clearSimulationButtonActionPerformed

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

    private static void saveComponentStateAndCompare(Component c, Logger lg) {
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

    private void saveSatateAndCompare() {
        Date date = new Date();
        getLogger().log(Level.OFF, "< Inicio {0} >", date);
        saveComponentStateAndCompare(this, logger);
        saveComponentStateAndCompare(mainTabbedPane, logger);
        for (Component c : mainTabbedPane.getComponents()) {
            saveComponentStateAndCompare(c, logger);
        }
        logger.log(Level.OFF, "< Fim {0} >\n", date);
    }

    private boolean askLog = true;

    private void newFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFileButtonActionPerformed
        if (splitView) {
            splitViewButtonActionPerformed(null);
        }
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

            if (evt != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        {
                            //adicionando um novo fluxograma
                            FlowchartPanel fp = new FlowchartPanel(new Function(), mainInterpreter);
                            mainProject.getFunctions().add(fp.getFunction());
                            mapFC.add(fp);
                            add(fp, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
                            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2
                        }
                    }
                });
            }
        }
    }//GEN-LAST:event_newFileButtonActionPerformed

    private void splitViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitViewButtonActionPerformed
        Component cmp = mainTabbedPane.getSelectedComponent();

        if (!splitView && cmp instanceof FlowchartPanel) {
            final FlowchartPanel fcp = (FlowchartPanel) cmp;
            mainTabbedPane.remove(addNewCodePanel);
            mainTabbedPane.remove(fcp);
            mainTabbedPane.remove(simulationPanel);

            simulationSplitPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            simulationSplitPanel.setBottomComponent(fcp);
            simulationSplitPanel.setTopComponent(simulationPanel);
            simulationSplitPanel.setOneTouchExpandable(false);
            Dimension minimumSize = new Dimension(100, 50);
            fcp.setMinimumSize(minimumSize);
            simulationPanel.setMinimumSize(minimumSize);
            interpreter = ((Interpertable) cmp).getInterpreter();
            updateControlBar(interpreter);
            fcp.hideSidePanel(true);
            simulationPanel.hideSidePanel(true);
            simulationSplitPanel.setDividerLocation(mainTabbedPane.getWidth() / 2);
            PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fcp.createBuffers();
                            simulationPanel.createBuffers();
                            fcp.resetGlobalPosition();
                            simulationPanel.resetGlobalPosition();
                        }
                    });
                }
            };
            simulationSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, propertyChangeListener);
            //mainTabbedPane.add(simulationSplitPanel, new ImageIcon(getClass().getResource("/resources/tango/16x16/apps/preferences-system-windows.png")));
            mainTabbedPane.addTab("Simulação + Fluxograma", new ImageIcon(getClass().getResource("/resources/tango/16x16/apps/preferences-system-windows.png")), simulationSplitPanel);
//            mainTabbedPane.addTab(addNewCodePanel.getName(), new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    simulationSplitPanel.setDividerLocation(simulationSplitPanel.getDividerLocation() + 1);
                }
            });
            splitView = true;
            splitViewButton.setIcon(unsplitIcon);
            splitViewButton.setToolTipText("Visão em Abas");
        } else if (splitView) {
            mainTabbedPane.remove(addNewCodePanel);
            mainTabbedPane.remove(simulationSplitPanel);
            mainTabbedPane.addTab("Simulação", new javax.swing.ImageIcon(getClass().getResource("/resources/tango/16x16/devices/input-gaming.png")), simulationPanel);
            Component bottomComponent = simulationSplitPanel.getBottomComponent();
            simulationPanel.hideSidePanel(false);
            if (bottomComponent instanceof FlowchartPanel) {
                ((FlowchartPanel) bottomComponent).hideSidePanel(false);
            }
            mainTabbedPane.add(bottomComponent, new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
//            mainTabbedPane.addTab(addNewCodePanel.getName(), new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);//-2
            splitView = false;
            if (evt != null && cmp != simulationSplitPanel && simulationSplitPanel.getBottomComponent() != cmp) {
                mainTabbedPane.setSelectedComponent(cmp);
                splitViewButtonActionPerformed(null);
            }
            splitViewButton.setIcon(splitIcon);
            splitViewButton.setToolTipText("Visão Lado a Lado");
        }

    }//GEN-LAST:event_splitViewButtonActionPerformed

    private void timestepTButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timestepTButtonActionPerformed

    }//GEN-LAST:event_timestepTButtonActionPerformed

    private void keyboardShortcutsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardShortcutsButtonActionPerformed
        if (shortcutsWindow == null) {
            shortcutsWindow = new ShortcutsWindow();
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                shortcutsWindow.setVisible(true);
            }
        });
    }//GEN-LAST:event_keyboardShortcutsButtonActionPerformed

    private void newFileButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newFileButtonMouseEntered
        printHelp("Fecha o projeto atual, limpa a simulação e cria um programa vazio");
    }//GEN-LAST:event_newFileButtonMouseEntered

    private void openButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openButtonMouseEntered
        printHelp("Abre um projeto salvo");
    }//GEN-LAST:event_openButtonMouseEntered

    private void saveButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseEntered
        printHelp("Salva o projeto atual (Ambiente e Código)");
    }//GEN-LAST:event_saveButtonMouseEntered

    private void clearSimulationButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearSimulationButtonMouseEntered
        printHelp("Remove todas as linhas colocadas na simulação (esta operação não pode ser desfeita)");
    }//GEN-LAST:event_clearSimulationButtonMouseEntered

    private void timestepTButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timestepTButtonMouseEntered
        printHelp("Coloca uma espera a cada comando executado, permitindo visualizar a execução do programa lentamente");
    }//GEN-LAST:event_timestepTButtonMouseEntered

    private void runButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runButtonMouseEntered
        printHelp("Executa o programa criado");
    }//GEN-LAST:event_runButtonMouseEntered

    private void pauseButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseButtonMouseEntered
        printHelp("Pausa a execução do programa");
    }//GEN-LAST:event_pauseButtonMouseEntered

    private void stopButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopButtonMouseEntered
        printHelp("Interrompe a execução do programa e retorna para o primeiro comando");
    }//GEN-LAST:event_stopButtonMouseEntered

    private void switchCodeButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_switchCodeButtonMouseEntered
        printHelp("Transforma o fluxograma em código fonte e vice-versa");
    }//GEN-LAST:event_switchCodeButtonMouseEntered

    private void deleteButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteButtonMouseEntered
        printHelp("????");
    }//GEN-LAST:event_deleteButtonMouseEntered

    private void splitViewButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_splitViewButtonMouseEntered
        printHelp("Permite visualizar a simulação e o programa lado a lado, clique outra vez para separa-los");
    }//GEN-LAST:event_splitViewButtonMouseEntered

    private void keyboardShortcutsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_keyboardShortcutsButtonMouseEntered
        printHelp("Exibe uma lista dos principais atalhos do programa");
    }//GEN-LAST:event_keyboardShortcutsButtonMouseEntered

    private void helpCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpCheckBoxActionPerformed
        helpTip = "";
        helpPanel.repaint();
    }//GEN-LAST:event_helpCheckBoxActionPerformed

    private void mainTabbedPaneMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainTabbedPaneMouseMoved
        JTabbedPane tp = (JTabbedPane) evt.getSource();
        int idx = tp.indexAtLocation(evt.getX(), evt.getY());
        if (idx != lastIdx) {
            lastIdx = idx;
            if (idx == 0) {
                printHelp("Área de simulação, permite visualizar o robô se movendo e interagindo com obstáculos conforme o programado");
            } else if (idx == 1) {
                printHelp("O editor de código permite definir o comportamento do robô em sua interação com o ambiente");
            }
        }
    }//GEN-LAST:event_mainTabbedPaneMouseMoved

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        keyboardShortcutsButtonActionPerformed(null);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        if (aboutWindow == null) {
            aboutWindow = new AboutWindow();
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                aboutWindow.setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void keyboardShortcutsButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_keyboardShortcutsButton1MouseEntered
        printHelp("Exibe informações sobre o progama e o projeto");
    }//GEN-LAST:event_keyboardShortcutsButton1MouseEntered

    private void keyboardShortcutsButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardShortcutsButton1ActionPerformed
        jMenuItem5ActionPerformed(null);
    }//GEN-LAST:event_keyboardShortcutsButton1ActionPerformed

    private void conectButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conectButtonMouseEntered
        printHelp("Conecta ao robô");
    }//GEN-LAST:event_conectButtonMouseEntered

    private void conectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conectButtonActionPerformed
        createRobot.simpleConectButtonActionPerformed();
    }//GEN-LAST:event_conectButtonActionPerformed

    private void dynamicTabbedPaneMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dynamicTabbedPaneMouseMoved
        JTabbedPane tp = (JTabbedPane) evt.getSource();
        int idx = tp.indexAtLocation(evt.getX(), evt.getY());
        if (idx != lastIdx) {
            lastIdx = idx;
            if (idx == 0) {
                printHelp("O Terminal permite visualizar as mensagens enviadas pelo robô (comando 'Exibir')");
            } else if (idx == 1) {
                printHelp("A aba Conexões permite se comunicar com o robô real e acompanhar os pacotes enviados, perdidos e recebidos");
            }
        }
    }//GEN-LAST:event_dynamicTabbedPaneMouseMoved

    public void printHelp(String str) {
        if (helpCheckBox.isSelected()) {
            helpTip = str;
            helpPanel.repaint();
        }
    }

    public void add(JComponent panel, ImageIcon icon) {
        mainTabbedPane.remove(addNewCodePanel);
        mainTabbedPane.addTab(panel.getName(), icon, panel);
        if (panel instanceof ComponentListener) {
            mainTabbedPane.addComponentListener((ComponentListener) panel);
        }
//        mainTabbedPane.addTab(addNewCodePanel.getName(), new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/list-add.png")), addNewCodePanel);
    }

    public void updateControlBar(Interpreter interpreter) {
        if (interpreter == null) {
            runButton.setEnabled(false);
            jSpinner1.setEnabled(false);
            stepButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            return;
        }

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
    private javax.swing.JPanel addNewCodePanel;
    private javax.swing.JButton clearSimulationButton;
    private javax.swing.JButton conectButton;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTabbedPane dynamicTabbedPane;
    private javax.swing.JToolBar dynamicToolBar;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JCheckBox helpCheckBox;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton keyboardShortcutsButton;
    private javax.swing.JButton keyboardShortcutsButton1;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuDev;
    private javax.swing.JMenu menuFile;
    private javax.swing.JButton newFileButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JSplitPane primarySplitPane;
    private javax.swing.JComboBox robotComboBox;
    private javax.swing.JButton runButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JSplitPane secondarySplitPane;
    private jifi.gui.panels.SimulationPanel simulationPanel;
    private javax.swing.JButton splitViewButton;
    private javax.swing.JTabbedPane staticTabbedPane;
    private javax.swing.JButton stepButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JButton switchCodeButton;
    private javax.swing.JToggleButton timestepTButton;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    public Interpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        if (splitView) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //simulationSplitPanel.setDividerLocation(simulationSplitPanel.getDividerLocation() + 1);
                    simulationSplitPanel.setDividerLocation(mainTabbedPane.getWidth() / 2);

                    int width = e.getComponent().getWidth() - 50;
                    helpPanel.setPreferredSize(new Dimension(width, 25));

                    helpPanel.repaint();
                }
            });
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

}
