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
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.FileNotFoundException;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;

public class MQTTVBar implements MQTTWidget {

    Point location;
    Dimension size;
    Rectangle bounds;
    String state;
    String publish = null;
    CallbackConnection mqtt;
    int minimum;
    int maximum;
    int value;
    Color color;

    public MQTTVBar(CallbackConnection m, Element root) throws FileNotFoundException {
        mqtt = m;
        location = new Point(
            Integer.parseInt(root.getAttribute("x")),
            Integer.parseInt(root.getAttribute("y"))
        );

        size = new Dimension(
            Integer.parseInt(root.getAttribute("width")),
            Integer.parseInt(root.getAttribute("height"))
        );

        minimum = Integer.parseInt(root.getAttribute("minimum"));
        maximum = Integer.parseInt(root.getAttribute("maximum"));
        value = minimum;

        bounds = new Rectangle(location.x, location.y, size.width, size.height);

        String colStr = root.getAttribute("color");

        color =  new Color(
            Integer.valueOf( colStr.substring( 1, 3 ), 16 ),
            Integer.valueOf( colStr.substring( 3, 5 ), 16 ),
            Integer.valueOf( colStr.substring( 5, 7 ), 16 ),
            Integer.valueOf( colStr.substring( 7, 9 ), 16 )
        );
        publish = root.getAttribute("publish");

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
        g.setClip(bounds);
        g.setColor(color);
        int range = maximum - minimum;
        int offset = value - minimum;
        int scaled = value * size.height / range;

        g.fillRect(location.x, location.y + size.height - scaled, size.width, scaled);
        g.setClip(null);
    }

    public void setState(String s) {
        value = Integer.parseInt(s);
        if (value < minimum) value = minimum;
        if (value > maximum) value = maximum;
    }

    public void mouseEntered(MouseEvent evt) { } 
    public void mouseExited(MouseEvent evt) { } 
    public void mousePressed(MouseEvent evt) { } 
    public void mouseReleased(MouseEvent evt) { }

    public void mouseDragged(MouseEvent evt) { 
        if (publish != null) {
            int pos = size.height - evt.getY();
            int range = maximum - minimum;
            int val = pos * range / size.height;
            value = val;
            String state = "" + val;
            mqtt.publish(publish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        }
    }
    public void mouseMoved(MouseEvent evt) { }

    public void mouseClicked(MouseEvent evt) {
        if (publish != null) {
            int pos = size.height - evt.getY();
            int range = maximum - minimum;
            int val = pos * range / size.height;
            value = val;
            String state = "" + val;
            mqtt.publish(publish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        }
    }
}
