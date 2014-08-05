package kubach.packet;

/**
 * A captcha request packet
 * @author Cr0s
 */
public class PacketRequestCaptcha {
    public String username;
    public String password;

    public PacketRequestCaptcha() {
        
    }
    
    public PacketRequestCaptcha(String login, String password) {
        this.username = login;
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "PacketRequestCaptcha " + username + " : " + password;
    }    
}
