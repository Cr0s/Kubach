package kubach.packet;

/**
 * List of files from server to sync with client
 * @author Cr0s
 */
public class PacketFilesList {
    public int numFiles;
    
    public String[] files; // All files presented in format: /path/to/file:md5hash
    
    public PacketFilesList() {
    }
}
