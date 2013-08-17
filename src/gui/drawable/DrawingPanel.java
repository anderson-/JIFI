/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.drawable;

import algorithm.Command;
import gui.QuickFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import gui.drawable.Drawable;
import gui.drawable.DrawableTest.Circle;
import gui.drawable.SwingContainer.DynamicJComponent;
import java.awt.Shape;
import util.trafficsimulator.Clock;

/**
 *
 * @author antunes
 */
public class DrawingPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ComponentListener, Drawable {

    public final double MIN_ZOOM = 0.1;
    public final double MAX_ZOOM = 10.0;
    protected final long PAINT_DELAY = 2;
    protected final long NO_PAINT_DELAY = 100;
    protected final Clock clock;
    private final ArrayList<Drawable> objects;
    private final ArrayList<Integer> keys;
    private final Point mouse;
    private Thread repaintThread;
    private BufferedImage buffer;
    private boolean repaint = false;
    private int width;
    private int height;
    private int globalX = 0, globalY = 0;
    private double zoom = 1.0;
    private boolean autoFullSize = false;
    //********** componente atual
    private AffineTransform currentTransform;
    private Rectangle currentBounds;
    private Drawable currentObject;
    private int objectX = 0;
    private int objectY = 0;
    private GraphicAttributes currentGraphicAtributes;
    private InputState currentInputState;

    public DrawingPanel(Clock c) {
        super(true);
        //tamanho preferencial
        this.setPreferredSize(new Dimension(640, 480));
        //ATENÇÂO! setFocusable é sempre ignorado em algum lugar 
        //(netbeans, implementação, etc), e isso faz com que 
        //KeyListener não funcione!
        this.setFocusable(true);
        //componentes Swing em toda parte
        this.setLayout(null);
        
        mouse = new Point();
        clock = c;
        objects = new ArrayList<>();
        keys = new ArrayList<>();
        repaintThread = null;
        currentGraphicAtributes = new GraphicAttributes();
        currentInputState = new InputState();
        currentTransform = new AffineTransform();
        currentBounds = new Rectangle();
        
        //teste
//        add(new DrawableTest().appendTo(this));
//        Circle cw = new Circle();
//        cw.setBounds(10, 10, 30, 30);
//        add(cw);
        
        //adiciona listeners
        addListeners();
        //inicia a thread de desenho
        play();
    }

    private void addListeners() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
        this.add((Drawable)this);
    }

    public DrawingPanel(Clock c, boolean autoFullSize) {
        this(c);
        this.autoFullSize = autoFullSize;
    }

    protected final void createBuffers() {
        width = this.getWidth();
        height = this.getHeight();
        //cria buffers no padrão do sistema (teoricamente mais eficiente)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        buffer = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
    }

    public DrawingPanel() {
        this(new Clock());
    }

    public final void play() {
        clock.setPaused(false);
        //cria thread para pintar a tela
        if (repaintThread == null) {
            repaintThread = new Thread("Repaint Thread " + Thread.activeCount()) {
                @Override
                public void run() {
                    try {
                        while (true) {
                            if (repaint) {
                                repaint();
                                Thread.sleep(PAINT_DELAY);
                            } else {
                                Thread.sleep(NO_PAINT_DELAY);
                            }
                        }
                    } catch (InterruptedException ex) {
                        repaintThread = null;
                    }
                }
            };
            repaintThread.start();
        }

        repaint = true;
    }

    public final void pause() {
        repaint = false;
    }

    public final void add(Drawable d) {
        objects.add(d);
    }

    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g); //não usar

        //ignora chamadas antes do buffer ser construido
        if (buffer == null) {
            return;
        }

        //desenha o fundo dentro do buffer
        Graphics g1 = buffer.getGraphics();
        g1.setColor(Color.WHITE);
        g1.fillRect(0, 0, width, height);

        //desenha o buffer no painel
        g.drawImage(buffer, 0, 0, null);

        //relogio global
        clock.increase();

        //converte Graphics para Graphics2D
        Graphics2D g2 = (Graphics2D) g;

        //deixa tudo lindo (antialiasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //desenha fundo
        synchronized (objects) {
            for (Drawable d : objects) {
                currentObject = d;
                if (d.isVisible()) {
                    currentTransform.setToIdentity();
                    g2.setTransform(currentTransform);
                    currentTransform.translate(globalX, globalY);
                    currentTransform.scale(zoom, zoom);
                    currentBounds.setBounds(currentTransform.createTransformedShape(d.getBounds()).getBounds());
//                    g2.setClip(currentBounds);
                    currentTransform.translate(d.getX(), d.getY());
                    g2.setTransform(currentTransform);
                    d.drawBackground(g2, currentGraphicAtributes, currentInputState);
                }
                currentObject = null;
            }
        }

        //desenha coisas
        synchronized (objects) {
            for (Drawable d : objects) {
                currentObject = d;
                if (d.isVisible()) {
                    currentTransform.setToIdentity();
                    g2.setTransform(currentTransform);
                    currentTransform.translate(globalX, globalY);
                    currentTransform.scale(zoom, zoom);
                    currentBounds.setBounds(currentTransform.createTransformedShape(d.getBounds()).getBounds());
//                    g2.setClip(currentBounds);
                    currentTransform.translate(d.getX(), d.getY());
                    g2.setTransform(currentTransform);
                    d.draw(g2, currentGraphicAtributes, currentInputState);
                }
                currentObject = null;
            }
        }

        //desenha primeiro plano (sem posição e zoom)
        synchronized (objects) {
            for (Drawable d : objects) {
                currentObject = d;
                if (d.isVisible()) {
                    currentTransform.setToIdentity();
                    g2.setTransform(currentTransform);
                    //currentTransform.translate(globalX, globalY);
                    //currentTransform.scale(zoom, zoom);
                    currentBounds.setBounds(currentTransform.createTransformedShape(d.getBounds()).getBounds());
//                    g2.setClip(currentBounds);
                    currentTransform.translate(d.getX(), d.getY());
                    g2.setTransform(currentTransform);
                    d.drawTopLayer(g2, currentGraphicAtributes, currentInputState);
                }
                currentObject = null;
            }
        }

        //reseta o zoom e posição para desenhar os componentes swing
        currentTransform.setToIdentity();
        g2.setTransform(currentTransform);
        g2.setClip(0, 0, width, height);

        //redefine o tamanho e a posição dos componentes swing
        synchronized (objects) {
            for (Drawable d : objects) {
                if (d instanceof SwingContainer) {
                    SwingContainer s = (SwingContainer) d;
                    for (DynamicJComponent c : s) {
                        currentTransform.setToIdentity();
                        currentTransform.translate(globalX, globalY);
                        currentTransform.scale(zoom, zoom);
                        currentTransform.translate(d.getX(), d.getY());

                        //define o tamanho do componente swing atual como
                        //o tamanho original do mesmo transformado pelo
                        //contexto atual
                        c.getComponent().setBounds(currentTransform.createTransformedShape(c.getBounds()).getBounds());
                    }
                }
            }
        }
    }

    public static void drawBallOrbitingCenter(Graphics2D g, int width, int height) {
        double time = 2 * Math.PI * (System.currentTimeMillis() % 10000) / 10000.;
        g.setColor(Color.RED);
        g.fillOval((int) (Math.sin(time) * width / 3 + width / 2 - 20), (int) (Math.cos(time) * height / 3 + height / 2) - 20, 40, 40);
    }

    @Override
    public final void keyTyped(KeyEvent e) {
        synchronized (keys) {
            if (e.getKeyCode() != 0 && !keys.contains(e.getKeyCode())) {
                keys.add(e.getKeyCode());
            }
        }
    }

    @Override
    public final void keyPressed(KeyEvent e) {
        synchronized (keys) {
            if (e.getKeyCode() != 0 && !keys.contains(e.getKeyCode())) {
                keys.add(e.getKeyCode());
            }
        }
    }

    @Override
    public final void keyReleased(KeyEvent e) {
        synchronized (keys) {
            while (keys.contains(e.getKeyCode())) {
                keys.remove(keys.indexOf(e.getKeyCode()));
            }
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        new Pew(e.getX(), e.getY()).start();
    }

    @Override
    public void mousePressed(MouseEvent e) {
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

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getComponent() != this) {
            return;
        }
        synchronized (mouse) {
            //define a posição relativa com base no deslocamento do mouse
            setPosition((int) (mouse.getX() - e.getPoint().getX()), (int) (mouse.getY() - e.getPoint().getY()));
            mouse.setLocation(e.getPoint());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getComponent() != this) {
            Rectangle r = e.getComponent().getBounds();
            Point tmp = e.getPoint();
            tmp.translate(r.x, r.y);

            mouse.setLocation(tmp);
            return;
        }
        synchronized (mouse) {
            mouse.setLocation(e.getPoint());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        setZoom(e.getWheelRotation() * 0.1, e.getPoint());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (autoFullSize) {
            width = e.getComponent().getWidth();
            height = e.getComponent().getHeight();
            this.setSize(width, height);
        }
        createBuffers();
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

    public synchronized void setZoom(double z, Point pos) {
        if ((zoom + z) >= MIN_ZOOM && (zoom + z) <= MAX_ZOOM) {

            Point posInside = getMouse(pos);

            if (new Rectangle(0, 0, width, height).contains(posInside)) {
                globalX -= (int) (((pos.getX() - globalX) / zoom) * z);
                globalY -= (int) (((pos.getY() - globalY) / zoom) * z);
            } else {
                globalX -= (int) (width * z / 2.0);
                globalY -= (int) (height * z / 2.0);
            }

            zoom = zoom + z;
        }
    }

    public synchronized double getZoom() {
        return zoom;
    }

    public synchronized Point getPosition() {
        return new Point(globalX, globalY);
    }

    public synchronized void setPosition(int x, int y) {
        globalX -= x;
        globalY -= y;
    }

    public synchronized void center(Rectangle clip) {
        globalX = (int) (clip.width * zoom / 2.0 - width * zoom / 2.0);
        globalY = (int) (clip.height * zoom / 2.0 - height * zoom / 2.0);
    }

    public synchronized Point getMouse(Point mouse) {
        return new Point((int) ((mouse.getX() - globalX) / zoom), (int) ((mouse.getY() - globalY) / zoom));
    }

    public static void drawGrade(Graphics2D g, int grid, float p, Rectangle bounds) {
        if (grid > 0) {

            int prop = (int) p / grid;

            for (int x = -(bounds.width / prop) / 2; x <= (bounds.width / prop) / 2; x++) {
                g.drawLine(x * prop, -bounds.height / 2, x * prop, bounds.height / 2);
            }

            for (int y = -(bounds.height / prop) / 2; y <= (bounds.height / prop) / 2; y++) {
                g.drawLine(-bounds.width / 2, y * prop, bounds.width / 2, y * prop);
            }

        } else if (grid < 0) {
            int prop = (int) p / -grid;

            for (int x = -(bounds.width / prop) / 2; x <= (bounds.width / prop) / 2; x++) {
                for (int y = -(bounds.height / prop) / 2; y <= (bounds.height / prop) / 2; y++) {
                    g.fillRect(x * prop - 1, y * prop - 1, 2, 2);
                }
            }

        } else {
            return;
        }
        String str = "grade: " + Math.abs(1.0f / grid) * 100 + " cm";
        int sx = g.getFontMetrics().stringWidth(str);
        int sy = g.getFontMetrics().getHeight();
        int px = bounds.width / 2 - 10 - sx;
        int py = bounds.height / 2 - 20;
        g.setColor(Color.lightGray);
        g.fillRect(px, py - 11, sx, sy);
        g.setColor(Color.white);
        g.drawString(str, px, py);

    }

    @Override
    public void setX(int x) {
        
    }

    @Override
    public void setY(int y) {
        
    }

    @Override
    public Shape getShape() {
        return new Rectangle(0,0,width,height);
    }

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
        
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
        
    }

    public class GraphicAttributes {

        private GraphicAttributes() {
        }

        public Clock getClock() {
            return clock;
        }

        public void applyZoom(AffineTransform t) {
            t.scale(zoom, zoom);
        }

        public void removeZoom(AffineTransform t) {
            t.scale(1 / zoom, 1 / zoom);
        }

        public void applyGlobalPosition(AffineTransform t) {
            t.translate(globalX, globalY);
        }

        public void removeGlobalPosition(AffineTransform t) {
            t.translate(-globalX, -globalY);
        }

        public void reset(AffineTransform t) {
            t.setToIdentity();
            t.translate(objectX, objectY);
        }
    }

    public class InputState {

        private InputState() {
        }

        public boolean isKeyPressed(int key) {
            synchronized (keys) {
                return keys.contains(key);
            }
        }

        public int getSingleKey() {
            synchronized (keys) {
                if (!keys.isEmpty()) {
                    return keys.get(0);
                } else {
                    return -1;
                }
            }
        }

        public boolean isMouseOver() {
            synchronized (mouse) {
                return currentBounds.contains(mouse);
            }
        }

        public Point getMouse() {
            synchronized (mouse) {
                return new Point(mouse);//mudar para posição relativa?
            }
        }
    }

    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        final DrawingPanel p = new DrawingPanel();
        final JFrame f = QuickFrame.create(p, "Teste");
        f.addComponentListener(p);

//        final JPanel panel = new JPanel();
        final JPanel panel = new JPanel() {
            @Override
            public void repaint() {
                JRootPane rootPane = SwingUtilities.getRootPane(this);
                if (rootPane != null) {
                    JPanel contentPane = (JPanel) rootPane.getContentPane();
                    contentPane.repaint();
                }
            }
        };
        panel.setBackground(Color.magenta);
        JButton jb = new JButton("test");
//        jb.setEnabled(false);
        //jb.setBounds(300, 300, 20, 20);


//        p.addSwing(jb, new Rectangle(300, 300, 80, 40));
        panel.setPreferredSize(new Dimension(100, 100));



        DrawingPanel pInner = new DrawingPanel();
        pInner.setBounds(0, 100, 200, 200);
        pInner.setSize(200, 200);
//        pInner.test = true;
//        f.getContentPane().addKeyListener(p);
//        f.addKeyListener(p);
        f.addComponentListener(pInner);
//        pInner.createBuffers();
//        p.add(pInner);

        panel.setBounds(20, 150, 200, 200);
        panel.setSize(200, 200);
//        p.add(panel);

//        p.createBuffers();
    }

    public class Pew extends Thread {

        JTextField field = new JTextField("pew!");
        double x, y;

        public Pew(int x, int y) {
            this.x = x;
            this.y = y;
            add(field);
        }

        public void run() {
            while (true) {
                field.setBounds(new Rectangle((int) x, (int) y, 100, 20));
                x += Math.random() - 0.5;
                y += Math.random() - 0.5;
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
        }
    }
}