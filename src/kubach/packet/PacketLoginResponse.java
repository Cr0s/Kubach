package kubach.packet;

/**
 * A login packet
 * @author Cr0s
 */
public class PacketLoginResponse {
    public boolean isSuccess;
    public String reason;
    
    public String session;
    public String role;
    
    public PacketLoginResponse() {
        
    }
}
