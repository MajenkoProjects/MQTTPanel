package uk.co.majenko.mqttpanel;

import javax.swing.JPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;

import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;

public class MQTTPanel extends JPanel implements MouseListener, MouseMotionListener {

    File xmlFile;
    Document doc;
    Element root;

    Dimension size;
    Point location;

    MQTT mqtt;
    CallbackConnection connection;
    ArrayList<Topic> topics;
    ArrayList<MQTTWidget> widgets;
    HashMap<String, MQTTWidget> subscriptions;

    File rootDir;

    Element display;

    public MQTTPanel(File xml) throws FileNotFoundException, SAXException, ParserConfigurationException, IOException, URISyntaxException {
        this(xml, new File(System.getProperty("user.dir")));
    }

    public MQTTPanel(File xml, File rd) throws FileNotFoundException, SAXException, ParserConfigurationException, IOException, URISyntaxException {
        super();
        rootDir = rd;
        if (!xml.exists()) {
            throw new FileNotFoundException(xml.getAbsolutePath());
        }
        xmlFile = xml;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        root = doc.getDocumentElement();

        display = getNode(root, "display");
        size = new Dimension(
            Integer.parseInt(display.getAttribute("width")),
            Integer.parseInt(display.getAttribute("height"))
        );

        location = new Point(
            Integer.parseInt(display.getAttribute("x")),
            Integer.parseInt(display.getAttribute("y"))
        );

        Element background = getNode(display, "background");
        setBackground(new Color(
            Integer.parseInt(getTextNode(background, "red")),
            Integer.parseInt(getTextNode(background, "green")),
            Integer.parseInt(getTextNode(background, "blue"))
        ));

        String rootPath = getTextNode(display, "datapath");
        if ((rootPath != null) && !(rootPath.equals(""))) {
            rootDir = new File(rootPath);
        }
    
        setSize(size);

        Element server = getNode(root, "server");

        mqtt = new MQTT();

        mqtt.setHost(getTextNode(server, "address"), Integer.parseInt(getTextNode(server, "port")));
        mqtt.setUserName(getTextNode(server, "user"));
        mqtt.setPassword(getTextNode(server, "password"));

        connection = mqtt.callbackConnection();

        connection.listener(new Listener() {
            public void onDisconnected() { }
            public void onConnected() {}
            public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                processIncoming(topic, payload);
                ack.run();
            }
            public void onFailure(Throwable value) {
                value.printStackTrace();
            }
        });

        Element widgetNodes = getNode(root, "widgets");

        topics = new ArrayList<Topic>();
        subscriptions = new HashMap<String, MQTTWidget>();
        widgets = new ArrayList<MQTTWidget>();
        NodeList widgetList = widgetNodes.getChildNodes();
        for (int i = 0; i < widgetList.getLength(); i++) {
            Node n = widgetList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;

                String topic = e.getAttribute("topic");


                MQTTWidget widget = null;  

                String name = e.getNodeName();
                switch (name) {
                    case "icon": widget = new MQTTIcon(connection, e, rootDir); break;
                    case "text": widget = new MQTTText(connection, e); break;
                    case "vbar": widget = new MQTTVBar(connection, e); break;
                }

                if (widget == null) {
                    throw new ParserConfigurationException("Unknown widget type");
                }
                widgets.add(widget);
                if ((topic != null) && (!topic.equals(""))) {
                    topics.add(new Topic(topic, QoS.AT_LEAST_ONCE));
                    subscriptions.put(topic, widget);
                }
            }
        }

        connection.connect(new Callback<Void>() {
            public void onFailure(Throwable value) {
                value.printStackTrace();
            }

            public void onSuccess(Void v) {
                // Subscribe to a topic
                connection.subscribe(topics.toArray(new Topic[0]), new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        // The result of the subcribe request.
                    }
                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                    }
                });
            }
        });

        addMouseMotionListener(this);
        addMouseListener(this);
    }
    

    @Override
    public Dimension getPreferredSize() { return size; }
    public Dimension getMinimumSize() { return size; }
    public Dimension getMaximumSize() { return size; }

    public String getTitle() {
        Element display = getNode(root, "display");
        return getTextNode(display, "title");
    }

    // Helper functions

    public static Element getNode(Element r, String n) {
        NodeList nl = r.getElementsByTagName(n);
        if (nl == null) return null;
        if (nl.getLength() == 0) return null;
        return (Element)nl.item(0);
    }

    public static String getTextNode(Element r, String n) {
        return getTextNode(r, n, "");
    }

    public static String getTextNode(Element r, String n, String d) {
        Element node = getNode(r, n);
        if (node == null) return d;
        return node.getTextContent();
    }

    void processIncoming(UTF8Buffer topic, Buffer payload) {
        MQTTWidget w = subscriptions.get(topic.toString());
        if (w != null) {
            w.setState(payload.utf8().toString());
            repaint();
        }
    }

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);
        for (MQTTWidget w : widgets) {
            w.paintComponent(g);
        }
    }

    public void mouseEntered(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseEntered(evt);
                repaint();
                return;
            }
        }
    }

    public void mouseExited(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseExited(evt);
                repaint();
                return;
            }
        }
    }

    public void mousePressed(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mousePressed(evt);
                repaint();
                return;
            }
        }
    }

    public void mouseReleased(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseReleased(evt);
                repaint();
                return;
            }
        }
    }

    public void mouseClicked(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseClicked(evt);
                repaint();
                return;
            }
        }
    }

    public void mouseDragged(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseDragged(evt);
                repaint();
                return;
            }
        }
    }

    public void mouseMoved(MouseEvent evt) {
        for (MQTTWidget w : widgets) {
            Rectangle r = w.getBounds();
            if (r.contains(evt.getPoint())) {
                evt.translatePoint(0 - r.x, 0 - r.y);
                w.mouseMoved(evt);
                repaint();
                return;
            }
        }
    }

    public Point getLocation() {
        return location;
    }

    public boolean getUndecorated() {
        String un = display.getAttribute("undecorated");
        if (un == null) return false;
        return un.equals("true");
    }

    public boolean getOntop() {
        String un = display.getAttribute("ontop");
        if (un == null) return false;
        return un.equals("true");
    }

}
