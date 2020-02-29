package uk.co.majenko.mqttpanel;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

public interface MQTTWidget {
    public Dimension getSize();
    public Point getLocation();
    public Rectangle getBounds();
    public void paintComponent(Graphics g);
    public void setState(String message);
    public void mouseEntered(MouseEvent evt);
    public void mouseExited(MouseEvent evt);
    public void mousePressed(MouseEvent evt);
    public void mouseReleased(MouseEvent evt);
    public void mouseClicked(MouseEvent evt);
    public void mouseDragged(MouseEvent evt);
    public void mouseMoved(MouseEvent evt);
}
