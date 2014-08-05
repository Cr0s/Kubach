package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.Constants;
import kubach.gui.CaptchaPanel;
import kubach.packet.PacketCaptchaImage;
import kubach.packet.PacketLogin;
import kubach.packet.PacketLoginResponse;
import kubach.packet.PacketRequestCaptcha;
import kubach.workers.LoadCaptchaWorker.CaptchaImageState;

/**
 * Sends auth request and checks result
 *
 * @author Cr0s
 */
public class LoadCaptchaWorker extends SwingWorker<Void, CaptchaImageState> {

    public String login, password;
    public CaptchaPanel cp;
    
    public LoadCaptchaWorker(CaptchaPanel cp, String login, String password) {
        this.cp = cp;
        
        this.login = login;
        this.password = password;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Client loginClient = new Client(4096 * 2, 4096 * 2);

        loginClient.getKryo().setRegistrationRequired(false);
        
        loginClient.getKryo().register(PacketLogin.class);
        loginClient.getKryo().register(PacketLoginResponse.class);
        loginClient.getKryo().register(PacketRequestCaptcha.class);
        loginClient.getKryo().register(PacketCaptchaImage.class);
        
        loginClient.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketCaptchaImage) {
                    PacketCaptchaImage response = (PacketCaptchaImage) object;
                    System.out.println("Captcha: " + response.toString());
                    
                    publish(new CaptchaImageState(response));
                    
                    connection.close();
                }
            }
        });

        loginClient.start();

        try {
            loginClient.connect(5000, Constants.SERVER_HOST, Constants.SERVER_PORT);   
            
            PacketRequestCaptcha prc = new PacketRequestCaptcha();
            prc.username = login;
            prc.password = password;
            
            loginClient.sendTCP(prc);
        } catch (Exception e) {
            PacketCaptchaImage response = new PacketCaptchaImage();
            response.hasCaptcha = false;
            response.key = e.toString();
            
            publish(new CaptchaImageState(response));
            e.printStackTrace();
            
            return null;
        }
                      
        return null;
    }

    class CaptchaImageState {
        public PacketCaptchaImage p;
        
        public CaptchaImageState(PacketCaptchaImage p) {
            this.p = p;
        }
    }
    
    @Override
    protected void process(List<CaptchaImageState> l) {
        for (CaptchaImageState ls : l) {
            this.cp.loadCaptchaImage(ls.p);
        }
    }    
}
