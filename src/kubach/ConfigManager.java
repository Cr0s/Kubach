package kubach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Some configs
 *
 * @author Cr0s
 */
public class ConfigManager {

    private static ConfigManager instance;
    public String pathToJar;
    private Properties properties;
    private static final String CONFIG_NAME = "config.properties";

    public String chatlogsDir;

    private ConfigManager() {
        try {
            System.out.println(ClassLoader.getSystemClassLoader().getResource(""));
            this.pathToJar = URLDecoder.decode(new File(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(), "UTF-8");
            this.chatlogsDir = this.pathToJar + File.separatorChar + "chatlogs";

            Logger.getLogger(ConfigManager.class.getName()).log(Level.INFO, "Path to JAR: " + this.pathToJar);
        } catch (UnsupportedEncodingException | URISyntaxException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        readProperties();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    private void readProperties() {
        try {
            this.properties = new Properties();
            File propertiesFile = new File(this.pathToJar + File.separatorChar + CONFIG_NAME);

            if (!propertiesFile.exists()) {
                try (FileOutputStream propertiesOut = new FileOutputStream(propertiesFile)) {
                    propertiesFile.createNewFile();

                    initDefaultProperties(properties, propertiesOut);
                }
            }
            try (FileInputStream propertiesIn = new FileInputStream(propertiesFile)) {
                properties.load(propertiesIn);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void initDefaultProperties(Properties properties, FileOutputStream propertiesOut) throws IOException {
        properties.setProperty("memory", "1024");
        properties.setProperty("username", "");
        properties.setProperty("password", "");
        properties.setProperty("clientprefix", "ic2");

        properties.setProperty("serverhost", "kubach.tk");
        properties.setProperty("serverport", "1488");

        properties.setProperty("updateurl", "http://kubach.tk/update/%PREFIX%/%FILE%");
        properties.setProperty("skinurl", "http://kubach.tk/getskin.php?user=%USERNAME%");
        properties.setProperty("skinuploadurl", "http://kubach.tk/uploadskin.php");
        properties.setProperty("skinremoveurl", "http://kubach.tk/removeskin.php");
        properties.setProperty("nativecheckurl", "http://kubach.tk/NativeCheck.class");

        properties.setProperty("forgeversion", "1.6.4-Forge9.11.1.947");

        properties.setProperty("virgin", "true");
        properties.setProperty("virgincondoms", "http://kubach.tk/update/condoms.package");

        properties.store(propertiesOut, null);
    }

    public void saveProperties() {
        File propertiesFile = new File(this.pathToJar + File.separatorChar + CONFIG_NAME);
        try {
            try (FileOutputStream propertiesOut = new FileOutputStream(propertiesFile)) {
                properties.store(propertiesOut, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getClientPrefix() {
        String prefix = this.properties.getProperty("clientprefix");

        // Get OS name
        String osName = System.getProperty("os.name").toLowerCase();
        
        // Probe for keywords in OS name
        boolean isLinux = osName.contains("ux") || osName.contains("nix") || osName.contains("aix");
        boolean isMac = osName.contains("mac");
        boolean isWin = osName.contains("win");

        // Make a corresponding prefix
        prefix += (isLinux)
                ? "_linux"
                : (isMac)   // Check for macintosh
                        ? "_mac"
                        : (isWin) // Check for windows
                            ? "_win" 
                            : "_unknown";

        return prefix;
    }
}
