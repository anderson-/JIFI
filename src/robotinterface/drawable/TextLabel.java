/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author antunes
 */
public class TextLabel {

    protected static Font defaultFont;

    static {
        defaultFont = new Font("Dialog", Font.BOLD, 12);
    }

    private Font font = defaultFont;
    private Color color = Color.BLACK;
    private String text;
    private boolean center;
    private double x = 0;
    private double y = 0;

    public TextLabel() {
        text = "";
        center = false;
    }

    public TextLabel(String text, double y, boolean center) {
        this.text = text;
        this.x = 0;
        this.y = y;
        this.center = center;
    }

    public TextLabel(String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.center = false;
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

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getPosX() {
        return x;
    }

    public double getPosY() {
        return y;
    }
}
