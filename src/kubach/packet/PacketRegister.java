package kubach.packet;

/**
 * Packet with registration information
 * @author Cr0s
 */
public class PacketRegister {
    public String username;
    public String password;
    
    public String captchaKey;
    public String captchaAnswer;
    
    public PacketRegister() {
        
    }
}
