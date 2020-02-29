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

public class MQTTIcon implements MQTTWidget {

    Point location;
    Dimension size;
    Rectangle bounds;
    String state;
    HashMap<String, ImageIcon> icons;
    ArrayList<String> states;
    String publish = null;
    CallbackConnection mqtt;

    public MQTTIcon(CallbackConnection m, Element root) throws FileNotFoundException {
        mqtt = m;
        location = new Point(
            Integer.parseInt(root.getAttribute("x")),
            Integer.parseInt(root.getAttribute("y"))
        );

        size = new Dimension(
            Integer.parseInt(root.getAttribute("width")),
            Integer.parseInt(root.getAttribute("height"))
        );

        bounds = new Rectangle(location.x, location.y, size.width, size.height);

        icons = new HashMap<String, ImageIcon>();
        states = new ArrayList<String>();

        publish = root.getAttribute("publish");

        NodeList iconNodes = root.getChildNodes();
        for (int i = 0; i < iconNodes.getLength(); i++) {
            Node n = iconNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;
                String name = e.getNodeName();
                switch (name) {
                    case "state":
                        String message = e.getAttribute("message");
                        states.add(message);
                        String icon = e.getAttribute("image");
                        ImageIcon icn = null;
                        if (icon.startsWith("@")) { // Internal icon image
                            icn = MQTTIcons.get(icon, size);
                            if (icn == null) {
                                icn = MQTTIcons.get("@default", size);
                            }
                        } else { // External PNG file
                        }
            
                        if (icn == null) throw new FileNotFoundException(icon);

                        String color = e.getAttribute("color");
                        if (color != null) {
                            if (color.startsWith("#")) {
                                icn = colorize(icn, color);
                            }
                        }

                        icons.put(message, icn);
                        break;
                }
            }
        }
        state = states.get(0);

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
        ImageIcon i = icons.get(state);
        if (i == null) return;
        i.paintIcon(null, g, location.x, location.y);
    }

    public void setState(String s) {
        if (states.contains(s)) {
            state = s;
        }
    }

    ImageIcon colorize(ImageIcon original, String color) {
        Color c =  new Color(
            Integer.valueOf( color.substring( 1, 3 ), 16 ),
            Integer.valueOf( color.substring( 3, 5 ), 16 ),
            Integer.valueOf( color.substring( 5, 7 ), 16 ),
            Integer.valueOf( color.substring( 7, 9 ), 16 )
        );
        

        Image source = original.getImage();
        BufferedImage colored = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = colored.createGraphics();
        g.drawImage(source, 0,0, null);

        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(c);
        g.fillRect(0, 0, size.width, size.height);
        g.dispose();
        return new ImageIcon(colored);

    }

    public void mouseEntered(MouseEvent evt) { } 
    public void mouseExited(MouseEvent evt) { } 
    public void mousePressed(MouseEvent evt) { } 
    public void mouseReleased(MouseEvent evt) { }
    public void mouseDragged(MouseEvent evt) { }
    public void mouseMoved(MouseEvent evt) { }

    public void mouseClicked(MouseEvent evt) {
        if (publish != null) {
            int i = states.indexOf(state);
            i++;
            if (i >= states.size()) i = 0;
            state = states.get(i);
            mqtt.publish(publish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        }
    }
}
