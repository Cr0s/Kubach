package kubach.packet;

/**
 * Adds item to WL if user has admin role
 * @author Cr0s
 */
public class PacketAddItemToWL {

    public String item;
    public String username, session;
    public String prefix;
    
    public PacketAddItemToWL() {
    }
}
