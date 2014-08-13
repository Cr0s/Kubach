package kubach.packet;

/**
 * List of files that illegal by current WL
 * @author Cr0s
 */
public class PacketBadWLItems {
    public int numFiles;
    
    public String[] files; // All files presented in format: filename*md5hash
    
    public PacketBadWLItems() {
    }
}
