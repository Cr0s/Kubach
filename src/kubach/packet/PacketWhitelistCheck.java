package kubach.packet;

/**
 * List of files from client to server to check in white list
 * @author Cr0s
 */
public class PacketWhitelistCheck {
    public int numFiles;
    
    public String[] files; // All files presented in format: /path/to/file:md5hash
    
    public PacketWhitelistCheck() {
    }
}
