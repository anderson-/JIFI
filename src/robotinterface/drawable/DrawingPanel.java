/**
 * @file .java
 * @author Anderson Antunes <anderson.utf@gmail.com>
 *         *seu nome* <*seu email*>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Copyright (C) 2013 by Anderson Antunes <anderson.utf@gmail.com>
 *                       *seu nome* <*seu email*>
 *
 * RobotInterface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RobotInterface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RobotInterface. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package robotinterface.drawable;

import java.awt.BasicStroke;
import robotinterface.drawable.util.QuickFrame;
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
import javax.swing.JPanel;
import robotinterface.drawable.Drawable;
import robotinterface.drawable.WidgetContainer.Widget;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import robotinterface.util.trafficsimulator.Clock;

/**
 * Painel para desenho de componentes desenháveis.
 */
public class DrawingPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ComponentListener, GraphicObject {

    //outras constantes
    public final double MIN_ZOOM = 0.5;
    public final double MAX_ZOOM = 4.0;
    protected long PAINT_DELAY = 2;
    protected long NO_PAINT_DELAY = 100;
    protected final Clock clock;
    private static int tempTransformsSize = 10;
    private static AffineTransform[] tempTransforms;
    private final ArrayList<Drawable> objects;
    private final ArrayList<Drawable> objectsTmp;
    private final ArrayList<Integer> keys;
    private final Point mouse;
    private boolean dragEnabled = true;
    private int mouseDragX = 0;
    private int mouseDragY = 0;
    private Thread repaintThread;
    private BufferedImage buffer;
    private boolean repaint = false;
    protected int width;
    protected int height;
    private int globalX = 0, globalY = 0;
    private boolean zoomEnabled = true;
    private double zoom = 1.0;
    private boolean autoFullSize = false;
    private Rectangle2D.Double bounds;
    //********** componente atual
    private AffineTransform originalTransform;
    private AffineTransform currentTransform;
    private Shape currentBounds;
    private boolean beginDrawing = false;
    private boolean mouseClick = false;
    private int mouseWheelRotation = 0;
    private Drawable currentObject;
    private int objectX = 0;
    private int objectY = 0;
    private GraphicAttributes currentGraphicAtributes;
    private InputState currentInputState;
    private int mouseButton;
    private int mouseClickCount;

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
        objectsTmp = new ArrayList<>();
        objects = new ArrayList<>();
        keys = new ArrayList<>();
        tempTransforms = new AffineTransform[10];
        for (int i = 0; i < tempTransformsSize; i++){
            tempTransforms[i] = new AffineTransform();
        }
        repaintThread = null;
        bounds = new Rectangle2D.Double();
        currentGraphicAtributes = new GraphicAttributes();
        currentInputState = new InputState();
        currentTransform = new AffineTransform();
        currentBounds = new Rectangle();

        globalX = this.getPreferredSize().width / 2;
        globalY = this.getPreferredSize().height / 4;

        //adiciona listeners
        addListeners();
        //inicia a thread de desenho
        play();
    }

    public DrawingPanel(Clock c, boolean autoFullSize) {
        this(c);
        this.autoFullSize = autoFullSize;
    }

    @Deprecated
    public DrawingPanel() {
        this(new Clock());
    }

    private void addListeners() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
        this.add((Drawable) this);
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

    public final void play() {
        clock.setPaused(false);
        //cria thread para pintar a tela
        if (repaintThread == null) {
            repaintThread = new Thread("Repaint Thread- " + Thread.activeCount()) {
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

    public final boolean contains(Drawable d) {
        synchronized (objects) {
            return objects.contains(d);
        }
    }

    public final void add(Drawable d) {
        if (d != null) {
            synchronized (objects) {
                if (!objects.contains(d)) {
                    objects.add(0, d);
                }
            }
            if (d instanceof WidgetContainer) {
                ((WidgetContainer) d).appendTo(this);
            }
        }
    }

    public final void clear() {
        synchronized (objects) {
            for (Drawable d : objects) {
                if (d instanceof WidgetContainer) {
                    for (Widget w : ((WidgetContainer) d)) {
                        super.remove(w.getJComponent());
                    }
                }
            }
            objects.clear();
        }
    }

    public final void remove(Drawable d) {
        if (d instanceof WidgetContainer) {
            for (Widget w : ((WidgetContainer) d)) {
                super.remove(w.getJComponent());
            }
        }
        synchronized (objects) {
            objects.remove(d);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g); //não usar

        //ignora chamadas antes do buffer ser construido
        if (buffer == null) {
            createBuffers();
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
        originalTransform = g2.getTransform();

        //deixa tudo lindo (antialiasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        beginDrawing = true;

        objectsTmp.clear();
        synchronized (objects) {
            objectsTmp.addAll(objects);
        }

        //desenha fundo
        for (Drawable d : objectsTmp) {
            currentObject = d;
            if ((d.getDrawableLayer() & BACKGROUND_LAYER) != 0) {
                currentTransform.setTransform(originalTransform);
                g2.setTransform(currentTransform);
                currentTransform.translate(globalX, globalY);
                currentTransform.scale(zoom, zoom);
                currentTransform.translate(d.getPosX(), d.getPosY());
                g2.setTransform(currentTransform);
                d.drawBackground(g2, currentGraphicAtributes, currentInputState);
            }
            currentObject = null;
        }

        //desenha coisas
        for (Drawable d : objectsTmp) {
            currentObject = d;
            if ((d.getDrawableLayer() & DEFAULT_LAYER) != 0) {
                currentTransform.setTransform(originalTransform);
                g2.setTransform(currentTransform);
                currentTransform.translate(globalX, globalY);
                currentTransform.scale(zoom, zoom);
                if (d instanceof GraphicObject) {
                    currentBounds = currentTransform.createTransformedShape(((GraphicObject) d).getObjectShape());
                }
                currentTransform.translate(d.getPosX(), d.getPosY());
                //g2.setClip(currentBounds); usar limite de pintura
                g2.setTransform(currentTransform);
                d.draw(g2, currentGraphicAtributes, currentInputState);
            }
            currentObject = null;
        }

        //desenha primeiro plano (sem posição global e zoom)
        for (Drawable d : objectsTmp) {
            currentObject = d;
            if ((d.getDrawableLayer() & TOP_LAYER) != 0) {
                currentTransform.setTransform(originalTransform);
                if (d instanceof GraphicObject) {
                    currentBounds = ((GraphicObject) d).getObjectBouds();
                }
                currentTransform.translate(d.getPosX(), d.getPosY());
                g2.setTransform(currentTransform);
                d.drawTopLayer(g2, currentGraphicAtributes, currentInputState);
            }
            currentObject = null;
        }

        //reseta o zoom e posição para desenhar os componentes swing
        g2.setTransform(originalTransform);
        g2.setClip(0, 0, width, height);

        //redefine o tamanho e a posição dos componentes swing
        for (Drawable d : objectsTmp) {
            if (d instanceof WidgetContainer) {
                WidgetContainer dwc = (WidgetContainer) d;
                for (Widget c : dwc) {
                    if (!dwc.isWidgetVisible() && !c.isStatic()) {
                        c.getJComponent().setVisible(false);
                        continue;
                    }

                    //ativando double buffer e fundo transparente
                    c.getJComponent().setVisible(true);
                    c.getJComponent().setDoubleBuffered(true);
//                    c.getJComponent().setOpaque(false);
                    c.getJComponent().revalidate();

                    //tamanho do componente swing
                    currentTransform.setToIdentity();
                    currentTransform.translate(globalX, globalY);
                    currentTransform.scale(zoom, zoom);
                    currentTransform.translate(d.getPosX(), d.getPosY());
                    c.getJComponent().setBounds(currentTransform.createTransformedShape(c.getBounds()).getBounds());
                }
            }
        }

//        synchronized (mouse) {
////            currentTransform.setTransform(originalTransform);
////            currentTransform.translate(globalX, globalY);
////            currentTransform.scale(zoom, zoom);
////            Point2D p = currentTransform.transform(mouse, null);
//            int x = (int) ((mouse.x - globalX) / zoom);
//            int y = (int) ((mouse.y - globalY) / zoom);
//
//            if (mouseClick) {
//                g.setColor(Color.red);
//            } else {
//                g.setColor(Color.black);
//            }
//            g.drawString("[" + x + "," + y + "]", mouse.x, mouse.y);
//        }
        if (beginDrawing) {
            beginDrawing = false;
            mouseClick = false;
            mouseWheelRotation = 0;
        }

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
        if (e.getButton() != MouseEvent.BUTTON2) {
            mouseClick = true;
            mouseButton = e.getButton();
            mouseClickCount = e.getClickCount();
        } else {
            zoom = 1;
            globalX = 0;
            globalY = 0;
        }
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

        mouseDragX = 0;
        mouseDragY = 0;

        synchronized (mouse) {
            //define a posição relativa com base no deslocamento do mouse
            mouseDragX = (int) (mouse.getX() - e.getPoint().getX());
            mouseDragY = (int) (mouse.getY() - e.getPoint().getY());

            if (dragEnabled) {
                setPosition(mouseDragX, mouseDragY);
            }
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
        mouseWheelRotation = e.getWheelRotation();
        if (zoomEnabled) {
            setZoom(e.getWheelRotation() * 0.1, e.getPoint());
        }
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

    public void setZoom(double z, Point pos) {
        if ((zoom + z) >= MIN_ZOOM && (zoom + z) <= MAX_ZOOM) {
            globalX -= (int) (((pos.getX() - globalX) / zoom) * z);
            globalY -= (int) (((pos.getY() - globalY) / zoom) * z);
            zoom = zoom + z;
        }
    }

    public double getZoom() {
        return zoom;
    }

    public Point getPosition() {
        return new Point(globalX, globalY);
    }

    public void setPosition(int x, int y) {
        globalX -= x;
        globalY -= y;
    }

    public void center(Rectangle clip) {
        globalX = (int) (clip.width * zoom / 2.0 - width * zoom / 2.0);
        globalY = (int) (clip.height * zoom / 2.0 - height * zoom / 2.0);
    }

    public Point getMouse(Point mouse) {
        return new Point((int) ((mouse.getX() - globalX) / zoom), (int) ((mouse.getY() - globalY) / zoom));
    }
//
//    public static void drawGrade(Graphics2D g, int grid, float p, Rectangle bounds) {
//        if (grid > 0) {
//
//            int prop = (int) p / grid;
//
//            for (int x = -(bounds.width / prop) / 2; x <= (bounds.width / prop) / 2; x++) {
//                g.drawLine(x * prop, -bounds.height / 2, x * prop, bounds.height / 2);
//            }
//
//            for (int y = -(bounds.height / prop) / 2; y <= (bounds.height / prop) / 2; y++) {
//                g.drawLine(-bounds.width / 2, y * prop, bounds.width / 2, y * prop);
//            }
//
//        } else if (grid < 0) {
//            int prop = (int) p / -grid;
//
//            for (int x = -(bounds.width / prop) / 2; x <= (bounds.width / prop) / 2; x++) {
//                for (int y = -(bounds.height / prop) / 2; y <= (bounds.height / prop) / 2; y++) {
//                    g.fillRect(x * prop - 1, y * prop - 1, 2, 2);
//                }
//            }
//
//        } else {
//            return;
//        }
//        String str = "grade: " + Math.abs(1.0f / grid) * 100 + " cm";
//        int sx = g.getFontMetrics().stringWidth(str);
//        int sy = g.getFontMetrics().getHeight();
//        int px = bounds.width / 2 - 10 - sx;
//        int py = bounds.height / 2 - 20;
//        g.setColor(Color.lightGray);
//        g.fillRect(px, py - 11, sx, sy);
//        g.setColor(Color.white);
//        g.drawString(str, px, py);
//
//    }

    @Override
    public int getDrawableLayer() {
        return Drawable.BACKGROUND_LAYER | Drawable.DEFAULT_LAYER | Drawable.TOP_LAYER;
    }

    public static void drawGrid(Graphics2D g, int s, double x, double y, double w, double h, boolean dots) {
        Line2D.Double l = new Line2D.Double();

        if (dots) {
            for (double i = -y + y % s; i <= h - y; i += s) {
                for (double j = -x + x % s; j <= w - x; j += s) {
                    g.fillRect((int) j - 1, (int) i - 1, 2, 2);
                }
            }
        } else {
            for (double i = -y + y % s; i <= h - y; i += s) {
                l.setLine(-x, i, w - x, i);
                g.draw(l);
            }

            for (double j = -x + x % s; j <= w - x; j += s) {
                l.setLine(j, -y, j, h - y);
                g.draw(l);
            }
        }

        String str = "grade: " + Math.abs(s) + " cm";
        int sx = g.getFontMetrics().stringWidth(str);
        int sy = g.getFontMetrics().getHeight();
        int px = (int) -x + (int) w - 10 - sx;
        int py = (int) -y + (int) h - 20;
        g.setColor(Color.lightGray);
        g.fillRect(px, py - 11, sx, sy);
        g.setColor(Color.white);
        g.drawString(str, px, py);
    }

    @Override
    public void drawBackground(Graphics2D g, GraphicAttributes ga, InputState in) {
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(DEFAULT_STROKE);
        drawGrid(g, 60, globalX / zoom, globalY / zoom, width / zoom, height / zoom, false);
    }

    @Override
    public void draw(Graphics2D g, GraphicAttributes ga, InputState in) {
//        g.setColor(Color.MAGENTA);
//        g.draw(currentBounds);
//        synchronized (objects) {
//            for (Drawable d : objects) {
//                if (d != this) {
//                    if ((d.getDrawableLayer() & Drawable.DEFAULT_LAYER) != 0) {
//                        if (d.getObjectShape().contains(getMouse(mouse))) {
//                            System.out.println(d);
//                            g.draw(d.getObjectBouds());
//                        }
//                    } else {
//                        if (d.getObjectShape().contains(mouse)) {
//                            System.out.println(d);
//                            g.draw(d.getObjectBouds());
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public void drawTopLayer(Graphics2D g, GraphicAttributes ga, InputState in) {
    }

    @Override
    public Shape getObjectShape() {
        return getObjectBouds();
    }

    @Override
    public Rectangle2D.Double getObjectBouds() {
        bounds.width = width;
        bounds.height = height;
        return bounds;
    }

    @Override
    public void setLocation(double x, double y) {
        bounds.x = x;
        bounds.y = y;
    }

    @Override
    public void setObjectBounds(double x, double y, double width, double height) {
        bounds.x = x;
        bounds.y = y;
        bounds.width = width;
        bounds.height = height;
    }

    @Override
    public double getPosX() {
        return bounds.x;
    }

    @Override
    public double getPosY() {
        return bounds.y;
    }

    public class GraphicAttributes {

        private GraphicAttributes() {
        }

        public Clock getClock() {
            return clock;
        }

        public boolean isZoomEnabled() {
            return zoomEnabled;
        }

        public void setZoomEnabled(boolean zoomEnabled) {
            DrawingPanel.this.zoomEnabled = zoomEnabled;
        }

        public boolean isDragEnabled() {
            return dragEnabled;
        }

        public void setDragEnabled(boolean dragEnabled) {
            DrawingPanel.this.dragEnabled = dragEnabled;
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

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void removeRelativePosition(AffineTransform t) {
            Rectangle2D b = currentBounds.getBounds2D();
            t.translate(-b.getX(), -b.getY());
        }

        public AffineTransform getT(int i) {
            i %= (tempTransformsSize+1);
            tempTransforms[i].setToIdentity();
            return tempTransforms[i];
        }
        
        public AffineTransform getT(int i, AffineTransform toCopy) {
            i %= (tempTransformsSize+1);
            tempTransforms[i].setTransform(toCopy);
            return tempTransforms[i];
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

        public int keysPressed() {
            synchronized (keys) {
                return keys.size();
            }
        }

        public int getSingleKey() {
            synchronized (keys) {
                if (!keys.isEmpty()) {
                    return keys.get(0);
                } else {
                    return 0;
                }
            }
        }

        public boolean isMouseOver() {
            synchronized (mouse) {
                return currentBounds.contains(mouse);
            }
        }

        public Drawable getObjectOverMouse() {
            ArrayList<Drawable> tmp;
            synchronized (objects) {
                tmp = (ArrayList<Drawable>) objects.clone();
            }
            for (Drawable d : tmp) {
                if (d != this && d instanceof GraphicObject) {
                    GraphicObject o = (GraphicObject) d;
                    if ((o.getDrawableLayer() & Drawable.DEFAULT_LAYER) != 0) {
                        if (o.getObjectShape().contains(DrawingPanel.this.getMouse(mouse))) {
                            return o;
                        }
                    } else {
                        if (o.getObjectShape().contains(mouse)) {
                            return o;
                        }
                    }
                }
            }
            return null;
        }

        public boolean mouseClicked() {
            synchronized (mouse) {
                if (mouseClick && beginDrawing && currentBounds.contains(mouse)) {
//                    System.out.println(currentBounds.getBounds2D());
                    return true;
                }
                return false;
            }
        }

        public int getMouseButton() {
            return mouseButton;
        }

        public int getMouseClickCount() {
            return mouseClickCount;
        }

        public boolean mouseGeneralClick() {
            return mouseClick;
        }

        public Point getRelativeMouse() {
            synchronized (mouse) {
                Point p = new Point(mouse);
//                System.out.println(currentObject.getObjectBouds());
                p.x -= (int) currentObject.getPosX();
                p.y -= (int) currentObject.getPosY();
                return p;
            }
        }

        public Point getTransformedMouse() {
            return new Point((int) ((mouse.getX() - globalX) / zoom), (int) ((mouse.getY() - globalY) / zoom));
        }

        public Point getMouse() {
            synchronized (mouse) {
                return new Point(mouse);//mudar para posição relativa?
            }
        }

        public Point getMouseDrag() {
            return new Point(mouseDragX, mouseDragY);
        }

        public int getMouseWheelRotation() {
            return mouseWheelRotation;
        }
    }

    public static void main(String[] args) {
        DrawingPanel p = new DrawingPanel();
        QuickFrame.create(p, "Teste do painel de desenho").addComponentListener(p);
        System.err.println("Não se esqueça de alterar getDrawableLayer() se for desenhar algo nesse painel!!");
    }
}
