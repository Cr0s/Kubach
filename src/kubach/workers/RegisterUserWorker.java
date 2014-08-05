package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import kubach.Constants;
import kubach.gui.CaptchaPanel;
import kubach.packet.PacketCaptchaImage;
import kubach.packet.PacketLogin;
import kubach.packet.PacketLoginResponse;
import kubach.packet.PacketRegister;
import kubach.packet.PacketRegistered;
import kubach.packet.PacketRequestCaptcha;
import kubach.workers.RegisterUserWorker.RegistrationState;

/**
 * Sends registration request and checks server answer
 *
 * @author Cr0s
 */
public class RegisterUserWorker extends SwingWorker<Void, RegistrationState> {

    public String login, password, key, ans;
    public CaptchaPanel cp;


    public RegisterUserWorker(CaptchaPanel cp, String username, String password, String key, String text) {
        this.cp = cp;
        
        this.login = username;
        this.password = password;
        this.key = key;
        this.ans = text;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Client loginClient = new Client(4096 * 2, 4096 * 2);

        loginClient.getKryo().register(PacketLogin.class);
        loginClient.getKryo().register(PacketLoginResponse.class);
        loginClient.getKryo().register(PacketRequestCaptcha.class);
        loginClient.getKryo().register(PacketCaptchaImage.class);
        loginClient.getKryo().register(byte[].class);
        loginClient.getKryo().register(PacketRegister.class);
        loginClient.getKryo().register(PacketRegistered.class);
        
        loginClient.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketCaptchaImage) {
                    PacketCaptchaImage response = (PacketCaptchaImage) object;
                    System.out.println("Invalid captcha, new: " + response.toString());
                    
                    publish(new RegistrationState(response));
                    
                    connection.close();
                } else if (object instanceof PacketRegistered) {
                    PacketRegistered response = (PacketRegistered) object;
                    System.out.println("Registered: " + response.toString());
                    
                    publish(new RegistrationState(true));
                    
                    connection.close();                    
                }
            }
        });

        loginClient.start();

        try {
            loginClient.connect(5000, Constants.SERVER_HOST, Constants.SERVER_PORT);   
            
            PacketRegister pr = new PacketRegister();
            pr.username = login;
            pr.password = password;
            pr.captchaKey = key;
            pr.captchaAnswer = ans;
            
            loginClient.sendTCP(pr);
        } catch (Exception e) {
            e.printStackTrace();
            
            return null;
        }
                      
        return null;
    }

    class RegistrationState {
        public PacketCaptchaImage p;
        public boolean isSuccess;
        
        public RegistrationState(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
        
        public RegistrationState(PacketCaptchaImage p) {
            this.p = p;
        }
    }
    
    @Override
    protected void process(List<RegistrationState> l) {
        for (RegistrationState ls : l) {
            if (ls.isSuccess) {
                this.cp.setVisible(false);
                
                JOptionPane.showMessageDialog(cp, "Username successfully registered", "Success", JOptionPane.INFORMATION_MESSAGE, null);
                
                this.cp.mf.cancelRegistration();
                return;
            }
            
            this.cp.loadCaptchaImage(ls.p);
        }
    }    
}
