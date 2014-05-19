/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.sidepanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.UIManager;
import robotinterface.drawable.swing.WidgetContainer;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
import robotinterface.drawable.graphicresource.SimpleContainer;
import robotinterface.drawable.swing.MutableWidgetContainer;
import robotinterface.plugin.Pluggable;

/**
 *
 * @author antunes
 */
public class SidePanel extends WidgetContainer {

    private int panelWidth = 100;
    private int panelItensHeight = 0;
    private int panelItensY = 0;
    private boolean open = true;
    private boolean animOpen = false;
    private boolean animClose = false;
    private boolean zoomDisabled = false;
    private boolean dragDisabled = false;
    private RoundRectangle2D.Double closeBtn;
    private Color color = Color.gray;
    private ArrayList<GraphicObject> itens;
    private ArrayList<GraphicObject> tmpItens;
    private DrawingPanel drawingPanel;

    public SidePanel(DrawingPanel drawingPanel) {
        itens = new ArrayList<>();
        tmpItens = new ArrayList<>();

        this.drawingPanel = drawingPanel;

        Item.setFont(UIManager.getDefaults().getFont("TabbedPane.font"));

//        addTmp(new GraphicObject("GraphicObject 1", new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#C05480")));
//        addTmp(new GraphicObject("GraphicObject 2", SimpleContainer.createDiamond(new Rectangle2D.Double(0, 0, 20, 20)), Color.decode("#98CB59")));
        this.closeBtn = new RoundRectangle2D.Double(-15, 15, 20, 20, 10, 10);
        bounds.x = 0;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setOpen(boolean b) {
        open = b;
    }

    @Override
    public int getDrawableLayer() {
        return GraphicObject.TOP_LAYER;
    }

    public void clearTempPanel() {
        tmpItens.clear();
    }
    
    public void clearPanel() {
        itens.clear();
    }

    boolean switchAnim = false;
    boolean animLeft = true;
    double animPos = 0;

    public void switchAnimLeft() {
        if (!tmpItens.isEmpty()) {
            switchAnim = true;
            animLeft = true;
        }
    }

    public void switchAnimRight() {
        if (!tmpItens.isEmpty()) {
            switchAnim = true;
            animLeft = false;
        }
    }

    public void hideSwing(boolean hide) {
        if (hide) {
            for (GraphicObject o : tmpItens) {
                if (o instanceof MutableWidgetContainer) {
                    MutableWidgetContainer c = (MutableWidgetContainer) o;
                    drawingPanel.remove(c);
                }
            }
        } else {
            for (GraphicObject o : tmpItens) {
                if (o instanceof MutableWidgetContainer) {
                    MutableWidgetContainer c = (MutableWidgetContainer) o;
                    drawingPanel.add(c);
                }
            }
        }
    }

    private void drawItens(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in, ArrayList<GraphicObject> itens, boolean tmp) {
        int x;
        if (tmp) {
            x = (int) (10 + animPos);
        } else {
            x = (int) (10 - panelWidth + animPos);
        }
        int y = 10 + panelItensY;
        int wtmp = panelWidth;
        panelWidth = 0;
        ArrayList<GraphicObject> itensTmp = (ArrayList<GraphicObject>) itens.clone();
        for (GraphicObject i : itensTmp) {

//                g.setColor(Color.GREEN);
//                g.draw(i.getObjectShape());
            if (i instanceof Item) {
                i.setLocation(x, y);
                i.draw(g, ga, in);
            } else {
                i.setLocation(x + drawingPanel.getWidth() - panelWidth, y);
                AffineTransform t = ga.getT(g.getTransform());
                g.translate(x, y);
                i.draw(g, ga, in);
                g.setTransform(t);
                i.getObjectBouds();
            }

            y += i.getObjectBouds().height + 10;
            int w = (int) (i.getObjectBouds().width + 25);
            if (w > panelWidth) {
                panelWidth = w;
            }
        }

        if (wtmp > panelWidth) {
            panelWidth = wtmp;
        }

        panelItensHeight = y - panelItensY + 30;
    }

    @Override
    public void drawTopLayer(Graphics2D g, DrawingPanel.GraphicAttributes ga, DrawingPanel.InputState in) {
        g.setStroke(new BasicStroke());

        g.setColor(Color.white);
        g.fill(closeBtn);
        g.setColor(color);

        if (in.mouseGeneralClick() && closeBtn.contains(in.getRelativeMouse())) {
            if (!(animOpen || animClose)) {
                if (open) {
                    animClose = true;
                } else {
                    animOpen = true;
                }
            }
        }

        if (in.isMouseOver()) {
            zoomDisabled = zoomDisabled | ga.isZoomEnabled();
            dragDisabled = dragDisabled | ga.isDragEnabled();
            ga.setZoomEnabled(false);
            ga.setDragEnabled(false);
//            panelItensY -= in.getMouseDrag().y;
            panelItensY += in.getMouseWheelRotation() * 20;
            panelItensY = (panelItensY < (ga.getHeight() - panelItensHeight)) ? (ga.getHeight() - panelItensHeight) : panelItensY;
            panelItensY = (panelItensY > 0) ? 0 : panelItensY;
        } else {
            if (zoomDisabled) {
                ga.setZoomEnabled(true);
                zoomDisabled = false;
            }

            if (dragDisabled) {
                ga.setDragEnabled(true);
                zoomDisabled = false;
            }
        }

        g.setFont(Item.getFont());

        g.draw(closeBtn);

        if (open) {
            g.translate(-8, 20);
            g.fillPolygon(new int[]{0, 0, 6, 0},
                    new int[]{0, 12, 6, 0}, 4);
            g.translate(8, -20);
        } else {
            g.translate(-9, 20);
            g.fillPolygon(new int[]{0, 6, 6, 0},
                    new int[]{6, 12, 0, 6}, 4);
            g.translate(9, -20);
        }

        g.fillRect(0, 0, panelWidth, ga.getHeight());
        //super.setLocation(0, x);
        if (in.isKeyPressed(KeyEvent.VK_1)) {
            animOpen = true;
        } else if (in.isKeyPressed(KeyEvent.VK_2)) {
            animClose = true;
        } else if (in.isKeyPressed(KeyEvent.VK_3)) {
            switchAnimLeft();
        } else if (in.isKeyPressed(KeyEvent.VK_4)) {
            switchAnimRight();
        }

        //animação
        double velocity = .2; //em segundos
        velocity = ga.getClock().getDt() * panelWidth / velocity;//converte para px/s dS=dt*(S/t) => dS/dt=v=(S/t) => S=v*t

        //set drawing bounds?
        g.setClip(0, 0, panelWidth, ga.getHeight());
        if (switchAnim) {
            hideSwing(true);
            if (animLeft) {
                if (animPos + velocity < panelWidth) {
                    animPos += velocity;
                } else {
                    switchAnim = false;
                }
            } else {
                if (animPos - velocity > 0) {
                    animPos -= velocity;
                } else {
                    switchAnim = false;
                }
            }
        } else {
            if (animLeft) {
                animPos = panelWidth;
            } else {
                animPos = 0;
                hideSwing(false);
            }
//            animPos = panelWidth;
        }

        drawItens(g, ga, in, itens, false);
        drawItens(g, ga, in, tmpItens, true);

        g.setClip(null);

        //scrollbar
        if (ga.getHeight() != 0 && panelItensHeight != 0) {
            //tamanho_da_barra = area_visivel/tamanho_do_conteudo*tamanho_fixo_da_barra
            double barSize = ga.getHeight() / (double) panelItensHeight * ga.getHeight();
            //posição_da_barra = posição_do_conteudo/tamanho_do_conteudo*area_visivel
            double barMidPos = -panelItensY / (double) panelItensHeight * ga.getHeight();
            g.setColor(Color.white);
            g.fillRect(panelWidth - 2, (int) barMidPos, 2, (int) barSize);
        }

        if (animOpen) {
            if (bounds.x - velocity > ga.getWidth() - panelWidth) {
                bounds.x -= velocity;
            } else {
                bounds.x = ga.getWidth() - panelWidth;
                animOpen = false;
                open = true;
            }
        } else if (animClose) {
            open = false;
            if (bounds.x + velocity < ga.getWidth()) {
                bounds.x += velocity;
            } else {
                bounds.x = ga.getWidth();
                animClose = false;
            }
        } else if (open) {
            bounds.width = panelWidth;
            bounds.x = ga.getWidth() - panelWidth;
            bounds.y = 0;
            bounds.height = ga.getHeight();
        } else {
            bounds.width = panelWidth;
            bounds.x = ga.getWidth();
            bounds.y = 0;
        }
    }

    public void add(GraphicObject item) {
        add(item, itens);
    }

    public void addAll(Collection<GraphicObject> list) {
        addAll(list, itens);
    }

    public void addAllClasses(Collection<Class> list) {
        addAllClasses(list, itens);
    }

    public void addTmp(GraphicObject item) {
        add(item, tmpItens);
    }

    public void addTmpAll(Collection<GraphicObject> list) {
        addAll(list, tmpItens);
    }

    public void addTmpAllClasses(Collection<Class> list) {
        addAllClasses(list, tmpItens);
    }

    private void add(GraphicObject item, ArrayList<GraphicObject> itens) {
        itens.add(item);
        if (item instanceof Item) {
            ((Item) item).setPanel(this);
        }
        if (item instanceof WidgetContainer) {
            drawingPanel.add(item);
            if (item instanceof MutableWidgetContainer) {
                ((MutableWidgetContainer) item).setAbsolute(true);
                ((MutableWidgetContainer) item).setWidgetVisible(true);
                ((MutableWidgetContainer) item).setMouseBlocked(true);
                ((MutableWidgetContainer) item).setSimpleDraw(true);
            }
        }
    }

    private void addAll(Collection<GraphicObject> list, ArrayList<GraphicObject> itens) {
        itens.addAll(list);
        for (GraphicObject i : list) {
            if (i instanceof Item) {
                ((Item) i).setPanel(this);
            }
            if (i instanceof WidgetContainer) {
                drawingPanel.add(i);
                if (i instanceof MutableWidgetContainer) {
                    ((MutableWidgetContainer) i).setAbsolute(true);
                }
            }
        }
    }

    private void addAllClasses(Collection<Class> list, ArrayList<GraphicObject> itens) {
        for (Class c : list) {
            try {
                if (Classifiable.class.isAssignableFrom(c)) {
                    Classifiable cl = (Classifiable) c.newInstance();
                    GraphicObject i = cl.getItem();
                    if (i != null) {
                        itens.add(i);
                        if (i instanceof Item) {
                            ((Item) i).setPanel(this);
                            ((Item) i).setRef(c);
                        }
                        if (i instanceof WidgetContainer) {
                            drawingPanel.add(i);
                            if (i instanceof MutableWidgetContainer) {
                                ((MutableWidgetContainer) i).setAbsolute(true);
                            }
                        }
                    }
                } else {
                    throw new Exception("ClassTypeError");
                }
//                    tmp = SidePanel.newInstance(ref);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected static final <T> T newInstance(Object ref) {
        if (ref instanceof Class) {
            Class c = (Class) ref;
            if (Pluggable.class.isAssignableFrom(c)) {
                try {
//                    return (T) c.newInstance();
                    Pluggable p = (Pluggable) c.newInstance();
                    return (T) p.createInstance();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public void itemSelected(Item item, Object ref) {
        System.out.println(ref);
    }
}
