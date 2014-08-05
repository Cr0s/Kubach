package kubach.packet;

/**
 * A login packet
 * @author Cr0s
 */
public class PacketLogin {
    public String username;
    public String password;

    public PacketLogin() {
        
    }
    
    public PacketLogin(String login, String password) {
        this.username = login;
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "PacketLogin " + username + " : " + password;
    }
}
