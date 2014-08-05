package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.Constants;
import kubach.gui.MainFrame;
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
import kubach.workers.ChangePasswordWorker.ChangePasswordState;

/**
 * Sends auth request and checks result
 *
 * @author Cr0s
 */
public class ChangePasswordWorker extends SwingWorker<Void, ChangePasswordState> {

    public String username, session, newPassword;
    public MainFrame mf;

    public ChangePasswordWorker(MainFrame f, String username, String session, String newPassword) {
        this.mf = f;
        this.username = username;
        this.session = session;
        this.newPassword = newPassword;
    }

    @Override
    protected Void doInBackground() throws Exception {
        final int BUFFER_SIZE = 10000;
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

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketChangePasswordResponse) {
                    PacketChangePasswordResponse response = (PacketChangePasswordResponse) object;

                    publish(new ChangePasswordState(response.isSuccess));

                    connection.close();
                }
            }
        });

        client.start();

        try {
            client.connect(5000, Constants.SERVER_HOST, Constants.SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        PacketChangePassword pcp = new PacketChangePassword();
        pcp.username = this.username;
        pcp.session = this.session;
        pcp.newPassword = this.newPassword;
        
        client.sendTCP(pcp);
        
        return null;
    }

    class ChangePasswordState {

        public boolean isSuccess;

        public ChangePasswordState(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }

    @Override
    protected void process(List<ChangePasswordState> l) {
        for (ChangePasswordState ls : l) {
            this.mf.parsePasswordChangeResult(ls.isSuccess);
            return;
        }
    }
}
