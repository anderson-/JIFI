/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.sidepanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.UIManager;
import robotinterface.drawable.WidgetContainer;
import robotinterface.drawable.GraphicObject;
import robotinterface.drawable.DrawingPanel;
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
    private ArrayList<Item> itens;

    public SidePanel() {
        itens = new ArrayList<>();

        Item.setFont(UIManager.getDefaults().getFont("TabbedPane.font"));

//        itens.add(new Item("Item 1", null, new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5), Color.decode("#C05480")));
//        itens.add(new Item("Item 2", null, SimpleContainer.createDiamond(new Rectangle2D.Double(0, 0, 20, 20)), Color.decode("#98CB59")));

        this.closeBtn = new RoundRectangle2D.Double(-15, 15, 20, 20, 10, 10);
        bounds.x = 0;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setOpen (boolean b){
        open = b;
    }

    @Override
    public int getDrawableLayer() {
        return GraphicObject.TOP_LAYER;
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
            panelItensY -= in.getMouseWheelRotation() * 20;
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
        }

        int x = 10, y = 10 + panelItensY;
        ArrayList<Item> itensTmp = (ArrayList<Item>) itens.clone();
        for (Item i : itensTmp) {
            i.setLocation(x, y);
//                g.setColor(Color.GREEN);
//                g.draw(i.getObjectShape());
            i.draw(g, ga, in);
            y += i.getObjectBouds().height + 10;
            panelWidth = (int) (i.getObjectBouds().width + 2 * 10);
        }

        panelItensHeight = y - panelItensY;

        //scrollbar
        if (ga.getHeight() != 0 && panelItensHeight != 0) {
            //tamanho_da_barra = area_visivel/tamanho_do_conteudo*tamanho_fixo_da_barra
            double barSize = ga.getHeight() / (double) panelItensHeight * ga.getHeight();
            //posição_da_barra = posição_do_conteudo/tamanho_do_conteudo*area_visivel
            double barMidPos = -panelItensY / (double) panelItensHeight * ga.getHeight();
            g.setColor(Color.white);
            g.fillRect(panelWidth - 2, (int) barMidPos, 2, (int) barSize);
        }


        //animação

        double velocity = .2; //em segundos
        velocity = ga.getClock().getDt() * panelWidth / velocity;//converte para px/s dS=dt*(S/t) => dS/dt=v=(S/t) => S=v*t

        if (animOpen) {
            if (bounds.x > ga.getWidth() - panelWidth) {
                bounds.x -= velocity;
            } else {
                bounds.x = ga.getWidth() - panelWidth;
                animOpen = false;
                open = true;
            }
        } else if (animClose) {
            open = false;
            if (bounds.x < ga.getWidth()) {
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

    public void add(Item item) {
        itens.add(item);
        item.setPanel(this);
    }

    public void addAll(Collection<Item> list) {
        itens.addAll(list);
        for (Item i : list) {
            i.setPanel(this);
        }
    }

    public void addAllClasses(Collection<Class> list) {
        for (Class c : list) {
            try {
                if (Classifiable.class.isAssignableFrom(c)) {
                    Classifiable cl = (Classifiable) c.newInstance();
                    Item i = cl.getItem();
                    if (i != null) {
                        itens.add(i);
                        i.setPanel(this);
                        i.setRef(c);
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

    protected void ItemSelected(Item item, Object ref) {
        System.out.println(ref);
    }
}
