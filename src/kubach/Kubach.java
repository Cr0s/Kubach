package kubach;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import kubach.gui.FirstLaunch;
import static kubach.gui.FirstLaunch.SUN_JAVA_COMMAND;
import kubach.gui.MainFrame;
import kubach.util.Md5Checksum;
import kubach.workers.DownloadFileWorker;
import static kubach.workers.DownloadFileWorker.writeBytesToFile;

/**
 * Main bootstrap class
 *
 * @author Cr0s
 */
public class Kubach {

    public static void main(String[] args) {
        setLookAndFeel();

        boolean firstLaunch = ConfigManager.getInstance().getProperties().getProperty("virgin").equals("true");
        boolean isLauncherUpdateRequired = launcherUpdateRequired();
        
        System.out.println("First launch: " + firstLaunch + " | launcher update required: " + isLauncherUpdateRequired);
        
        if (!firstLaunch && !isLauncherUpdateRequired) {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        } else { 
            // Prepare to loose our virginity
            FirstLaunch fl = new FirstLaunch();
            fl.setVisible(true);
        }
    }

    private static boolean launcherUpdateRequired() {
        // Obtain path to our jar and get checksum of it 
        String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
        if (mainCommand.length == 0) {
            return false;
        }

        String jarPath = mainCommand[0];
        if (!jarPath.endsWith(".jar")) // It's not a jar (usually it happend when launcher is running from IDE)
        {
            return false;
        }

        try {
            String ourMd5 = Md5Checksum.getMD5Checksum(jarPath);

            // Obtain new launcher checksum from server within 5 tries
            String newMd5 = downloadChecksum(ConfigManager.getInstance().getLauncherChecksumUrl(), 5);

            // Obtaining is failed, abort
            if (newMd5 == null)
                return false;
            
            newMd5 = newMd5.trim();
            
            System.out.println("Launcher md5: " + ourMd5);
            System.out.println("Server md5: " + newMd5);
            System.out.println("Equals: " + ourMd5.equals(newMd5));
            
            // Compare checksums
            return !ourMd5.equals(newMd5);
        } catch (Exception ex) {
            Logger.getLogger(Kubach.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private static String downloadChecksum(String addr, int numTries) {
        final int BUFFER_SIZE = 32;

        BufferedInputStream bis = null;
        ByteArrayOutputStream baos;

        try {
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "User-Agent: Kubach v" + Constants.VERSION);

            int totalDataRead = 0;
            bis = new BufferedInputStream(connection.getInputStream());
            baos = new ByteArrayOutputStream();
            try (BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER_SIZE)) {
                byte[] data = new byte[BUFFER_SIZE];
                int i = 0;

                while ((i = bis.read(data)) != -1) {
                    totalDataRead = totalDataRead + i;
                    baos.write(data, 0, i);
                }

                return new String(baos.toByteArray());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (numTries > 0) {
                return downloadChecksum(addr, numTries - 1);
            } else {
                return null;
            }
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();

                if (numTries > 0) {
                    return downloadChecksum(addr, numTries - 1);
                } else {
                    return null;
                }
            }
        }
    }

    private static void setLookAndFeel() {
        try {
            OUTER:
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                //System.out.println(info.getName());
                switch (info.getName()) {
                    case "Windows":
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break OUTER;
                    case "GTK+":
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break OUTER;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }
    }
}
