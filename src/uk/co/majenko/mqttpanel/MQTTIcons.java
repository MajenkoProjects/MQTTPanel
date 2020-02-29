package uk.co.majenko.mqttpanel;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.Dimension;

import java.net.URL;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.scanners.ResourcesScanner;

public class MQTTIcons {
    static HashMap<String, ImageIcon>icons = null;

    public static ImageIcon get(String name, Dimension size) {
        if (icons == null) loadIcons();

        ImageIcon i = icons.get(name);
        if (i == null) {
            i = icons.get("@default");
        }

        Image scaled = i.getImage().getScaledInstance(size.width, size.height, Image.SCALE_AREA_AVERAGING);
        return new ImageIcon(scaled);
    }

    static void loadIcons() {
        icons = new HashMap<String, ImageIcon>();
        ArrayList<String> files = findIcons("icons/icons.txt");

        for (String file : files) {
            addIcon(file);
        } 
    }

    static void addIcon(String name) {
        URL url = MQTTIcons.class.getResource("icons/" + name + ".png");
        ImageIcon icon = new ImageIcon(url);
        icons.put("@" + name, icon);
    }

    static ArrayList<String> findIcons(String path) {
        ArrayList<String> filenames = new ArrayList<>();

        try {


            ResourcesScanner resourceScanner = new ResourcesScanner();
            Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("uk.co.majenko.mqttpanel.icons"))
                    .setScanners(new ResourcesScanner())
                    .filterInputsBy(new FilterBuilder().includePackage("uk.co.majenko.mqttpanel.icons"))
            );

            Set<String> resources = reflections.getResources(Pattern.compile(".*\\.png"));

            for (String res : resources) {
                res = res.substring(30);
                res = res.substring(0, res.length() - 4);
                filenames.add(res);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return filenames;
    }

}
