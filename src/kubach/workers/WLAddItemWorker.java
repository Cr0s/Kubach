package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.Constants;
import kubach.gui.WhiteListChecker;
import kubach.packet.PacketAddItemToWL;
import kubach.packet.PacketBadWLItems;
import kubach.packet.PacketCaptchaImage;
import kubach.packet.PacketChangePassword;
import kubach.packet.PacketChangePasswordResponse;
import kubach.packet.PacketFilesList;
import kubach.packet.PacketFilesListRequest;
import kubach.packet.PacketLogin;
import kubach.packet.PacketLoginResponse;
import kubach.packet.PacketRegister;
import kubach.packet.PacketRegistered;
import kubach.packet.PacketRequestCaptcha;
import kubach.packet.PacketWLData;
import kubach.packet.PacketWLRequestRealm;

/**
 * Doing check in whitelist
 *
 * @author Cr0s
 */
public class WLAddItemWorker extends SwingWorker<Void, Void> {
    public WhiteListChecker wc;
    public String username, session;
    public String item;
    
    public WLAddItemWorker(WhiteListChecker wc, String username, String session, String item) {
        //System.setSecurityManager(new NullSecurityManager());
        this.wc = wc;
        
        this.username = username;
        this.session = session;
        this.item = item;
        
        System.setSecurityManager(null);
    }

    @Override
    protected Void doInBackground() throws Exception {
        final int BUFFER_SIZE = 64000;
        Client client = new Client(BUFFER_SIZE, BUFFER_SIZE);

        client.getKryo().register(PacketLogin.class);
        client.getKryo().register(PacketLoginResponse.class);
        client.getKryo().register(PacketRequestCaptcha.class);
        client.getKryo().register(PacketCaptchaImage.class);
        client.getKryo().register(byte[].class);
        client.getKryo().register(PacketRegister.class);
        client.getKryo().register(PacketRegistered.class);
        client.getKryo().register(PacketFilesListRequest.class);
        client.getKryo().register(PacketFilesList.class);
        client.getKryo().register(String[].class);
        client.getKryo().register(PacketChangePassword.class);
        client.getKryo().register(PacketChangePasswordResponse.class);
        client.getKryo().register(PacketWLRequestRealm.class);
        client.getKryo().register(PacketWLData.class);
        client.getKryo().register(PacketAddItemToWL.class);
        client.getKryo().register(PacketBadWLItems.class);

        client.start();

        client.addListener(new Listener() { 
            @Override
            public void received(Connection connection, Object object) {
                connection.close();
            }            
        });
        
        try {
            client.connect(5000, Constants.SERVER_HOST, Constants.SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        PacketAddItemToWL packet = new PacketAddItemToWL();
        packet.item = this.item;
        packet.session = this.session;
        packet.username = this.username;
        packet.prefix = ConfigManager.getInstance().getClientPrefix();
        
        client.sendTCP(packet);

        return null;
    }
    
    public class WLCheckState {

        public PacketBadWLItems p;

        public WLCheckState(PacketBadWLItems p) {
            this.p = p;
        }
    }
}
