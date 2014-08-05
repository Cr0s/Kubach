package kubach.packet;

/**
 * Packet with data for new password
 * @author Cr0s
 */
public class PacketChangePassword {
    public String username;
    public String session;
    public String newPassword;
    
    public PacketChangePassword() {
    }
}
