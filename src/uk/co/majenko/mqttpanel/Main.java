package uk.co.majenko.mqttpanel;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.io.File;

public class Main {
    public Main(String source) throws Exception {
        JFrame f = new JFrame("MQTT Panel");

        File file = new File(source);
        MQTTPanel panel = new MQTTPanel(file);
        f.setUndecorated(panel.getUndecorated());
        f.setAlwaysOnTop(panel.getOntop());

        f.setTitle(panel.getTitle());

        f.setSize(panel.getPreferredSize());
        f.setResizable(false);
    
        f.add(panel);
        f.pack();


        f.setLocation(panel.getLocation());

        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Provide an XML file please...");
            return;
        }
        try {
            new Main(args[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
