package uk.co.majenko.mqttpanel;

//import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.w3c.dom.Attr;
//import org.xml.sax.SAXException;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.FileNotFoundException;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;

public class MQTTText implements MQTTWidget {

    Point location;
    Dimension size;
    Rectangle bounds;
    String text = "";
    CallbackConnection mqtt;
    Font font;
    Color color;

    public MQTTText(CallbackConnection m, Element root) throws FileNotFoundException {
        mqtt = m;
        location = new Point(
            Integer.parseInt(root.getAttribute("x")),
            Integer.parseInt(root.getAttribute("y"))
        );

        size = new Dimension(
            Integer.parseInt(root.getAttribute("width")),
            Integer.parseInt(root.getAttribute("height"))
        );

        String colStr = root.getAttribute("color");
        color =  new Color(
            Integer.valueOf( colStr.substring( 1, 3 ), 16 ),
            Integer.valueOf( colStr.substring( 3, 5 ), 16 ),
            Integer.valueOf( colStr.substring( 5, 7 ), 16 ),
            Integer.valueOf( colStr.substring( 7, 9 ), 16 )
        );

        int style = Font.PLAIN;

        if (root.getAttribute("bold") != null) {
            if (root.getAttribute("bold").equals("true")) {
                style = Font.BOLD;
            }
        }

        int fontsize = Integer.parseInt(root.getAttribute("size"));
    
        font = new Font(root.getAttribute("font"), style, fontsize);

        bounds = new Rectangle(location.x, location.y, size.width, size.height);

        if (root.getAttribute("default") != null) {
            text = root.getAttribute("default");
        }
    }

    public Dimension getSize() {
        return size;
    }

    public Point getLocation() {
        return location;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setClip(bounds);
        g2d.setFont(font);

        g2d.setColor(color);
        drawCenteredString(g2d, text, bounds, font);
        g2d.setClip(null);
    }

    public void drawCenteredString(Graphics g, String txt, Rectangle rect, Font fnt) {
        FontMetrics metrics = g.getFontMetrics(fnt);
        int x = rect.x + (rect.width - metrics.stringWidth(txt)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(txt, x, y);
    }

    public void setState(String s) {
        text = s;
    }

    public void mouseEntered(MouseEvent evt) { } 
    public void mouseExited(MouseEvent evt) { } 
    public void mousePressed(MouseEvent evt) { } 
    public void mouseReleased(MouseEvent evt) { } 
    public void mouseClicked(MouseEvent evt) { } 
    public void mouseDragged(MouseEvent evt) { }
    public void mouseMoved(MouseEvent evt) { }
}
