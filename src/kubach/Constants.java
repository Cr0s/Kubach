package kubach;

/**
 * Some constants
 * @author Cr0s
 */
public class Constants {
    public static final String SERVER_HOST = ConfigManager.getInstance().getProperties().getProperty("serverhost");
    public static final int SERVER_PORT = Integer.parseInt(ConfigManager.getInstance().getProperties().getProperty("serverport"));
    
    public static final String VERSION = "1.0.1a";
}
