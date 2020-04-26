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

public class MQTTBlackBody implements MQTTWidget {

    static final int    NORMAL = 0;
    static final int    KELVIN = 1;

    Point location;
    Dimension size;
    Rectangle bounds;
    String state;
    CallbackConnection mqtt;
    int minimum;
    int maximum;
    int value;
    float brightness;

    String redPublish = null;
    String greenPublish = null;
    String bluePublish = null;
    String whitePublish = null;

    public MQTTBlackBody(CallbackConnection m, Element root) throws FileNotFoundException {
        mqtt = m;
        location = new Point(
            Integer.parseInt(root.getAttribute("x")),
            Integer.parseInt(root.getAttribute("y"))
        );

        size = new Dimension(
            Integer.parseInt(root.getAttribute("width")),
            Integer.parseInt(root.getAttribute("height"))
        );

        minimum = 1900;
        maximum = 10000;
        value = minimum;
        brightness = 0f;

        bounds = new Rectangle(location.x, location.y, size.width, size.height);

        redPublish = root.getAttribute("red");
        greenPublish = root.getAttribute("green");
        bluePublish = root.getAttribute("blue");
        whitePublish = root.getAttribute("white");
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
        int steps = (maximum - minimum) / size.height;

        g.setClip(bounds);

        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++) {
                g.setColor(colorTemperatureToRGB(minimum + (y * steps), (float)x / (float)size.width));
                g.drawLine(location.x + x, location.y + size.height - y, location.x + x, location.y + size.height - y);
            }
        }

        g.setClip(null);
    }

    public void setState(String s) {
        value = Integer.parseInt(s);
        if (value < minimum) value = minimum;
        if (value > maximum) value = maximum;
    }

    void publishColor() {
        Color c = colorTemperatureToRGB(value, 1f);
        String state = "" + c.getRed();
        mqtt.publish(redPublish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        state = "" + c.getGreen();
        mqtt.publish(greenPublish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        state = "" + c.getBlue();
        mqtt.publish(bluePublish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
        int b = (int)(brightness * 255);
        state = "" + b;
        mqtt.publish(whitePublish, state.getBytes(), QoS.AT_LEAST_ONCE, false, null);
    }

    public void mouseEntered(MouseEvent evt) { } 
    public void mouseExited(MouseEvent evt) { } 
    public void mousePressed(MouseEvent evt) { } 
    public void mouseReleased(MouseEvent evt) { }

    public void mouseDragged(MouseEvent evt) { 
            int pos = size.height - evt.getY();
            int range = maximum - minimum;
            int val = pos * range / size.height;
            value = val;
            brightness = (float)evt.getX() / (float)size.width;
            publishColor();
    }
    public void mouseMoved(MouseEvent evt) { }

    public void mouseClicked(MouseEvent evt) {
            int pos = size.height - evt.getY();
            int range = maximum - minimum;
            int val = pos * range / size.height;
            value = val;
            brightness = (float)evt.getX() / (float)size.width;
            publishColor();
    }

    Color colorTemperatureToRGB(int kelvin, float brightness){

        float temp = kelvin / 100f;

        float red, green, blue;

        if( temp <= 66 ){ 
            red = 255; 
            green = temp;
            green = (float)(99.4708025861 * Math.log(green) - 161.1195681661);
            
            if( temp <= 19){
                blue = 0;
            } else {
                blue = temp-10;
                blue = (float)(138.5177312231 * Math.log(blue) - 305.0447927307);
            }
        } else {
            red = temp - 60;
            red = (float)(329.698727446 * Math.pow(red, -0.1332047592));
            
            green = temp - 60;
            green = (float)(288.1221695283 * Math.pow(green, -0.0755148492 ));

            blue = 255;
        }

        int ired = clamp(red, 0, 255);
        int igreen = clamp(green, 0, 255);
        int iblue = clamp(blue, 0, 255);

        float[] hsb = Color.RGBtoHSB(ired, igreen, iblue, null);
        float h = hsb[0];
        float s = hsb[1];
        float b = hsb[2] * brightness;

        return Color.getHSBColor(h, s, b);
    }


    int clamp(float x, int min, int max ) {
        int x1 = (int)x;

        if(x1<min){ return min; }
        if(x1>max){ return max; }

        return x1;

    }
}
