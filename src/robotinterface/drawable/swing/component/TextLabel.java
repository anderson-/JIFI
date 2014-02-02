/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes
 */
public class TextLabel extends Component {

    protected static Font defaultFont;

    static {
        defaultFont = new Font("Dialog", Font.BOLD, 12);
    }

    private Font font = defaultFont;
    private Color color = Color.BLACK;
    private String text = "";
    private boolean center = false;
    private double forceWidth = 0; //TODO

    public TextLabel() {
    }

    public TextLabel(String text) {
        this.text = text;
    }

    public TextLabel(String text, boolean center) {
        this.text = text;
        this.center = center;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean center() {
        return center;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }
    
    @Override
    public Rectangle2D.Double getBounds(Rectangle2D.Double tmp, Graphics2D g) {
        tmp.setRect(g.getFontMetrics(font).getStringBounds(text, g));
        tmp.x = 0;
        tmp.y = 0;
        return tmp;
    }
    
    @Override
    public Rectangle2D.Double getInsets (Rectangle2D.Double tmp){
        tmp.setRect(DEFAULT_INSETS);
        return tmp;
    }
    
}
