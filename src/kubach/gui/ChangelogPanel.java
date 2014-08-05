/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kubach.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import kubach.ConfigManager;

/**
 * A transparent text area with changelog info Should be rendered inside
 * logo/screenshot area
 *
 * @author Cr0s
 */
public class ChangelogPanel extends JTextPane {

    public String changelogContent;
    
    public ChangelogPanel(JPanel parent) {
        setEditable(false);
        setOpaque(false);
        setBorder(new CompoundBorder(new EmptyBorder(20, 20, 20, 20), new LineBorder(Color.DARK_GRAY)));

        try {
            changelogContent = new String(Files.readAllBytes(Paths.get(ConfigManager.getInstance().pathToJar + File.separatorChar + "changelog.txt")), "UTF-8");

            this.setContentType("text/html");
            this.setText("<font face='courier new' size=3><center><strong>Changelog</strong></center><br>" + changelogContent + "</font>");
        } catch (IOException ex) {
            System.out.println("[Changelog] Unable to load changelog: " + ex.toString());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(255, 255, 255, 128));
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - (insets.left + insets.right);
        int height = getHeight() - (insets.top + insets.bottom);
        g.fillRect(x, y, width, height);
        super.paintComponent(g);
    }
}
