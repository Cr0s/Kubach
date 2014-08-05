package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.Constants;
import kubach.gui.MainFrame;
import kubach.packet.PacketCaptchaImage;
import kubach.packet.PacketFilesList;
import kubach.packet.PacketFilesListRequest;
import kubach.packet.PacketLogin;
import kubach.packet.PacketLoginResponse;
import kubach.packet.PacketRegister;
import kubach.packet.PacketRegistered;
import kubach.packet.PacketRequestCaptcha;
import kubach.workers.AuthWorker.LoginState;

/**
 * Sends auth request and checks result
 *
 * @author Cr0s
 */
public class AuthWorker extends SwingWorker<Void, LoginState> {

    public String login, password;
    public MainFrame mf;

    public AuthWorker(MainFrame f, String login, String password) {
        this.mf = f;
        this.login = login;
        this.password = password;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Client client = new Client();

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


        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketLoginResponse) {
                    PacketLoginResponse response = (PacketLoginResponse) object;
                    System.out.println(response.isSuccess + " : " + response.reason + " : " + response.session);

                    publish(new LoginState(response));

                    connection.close();
                }
            }
        });

        client.start();

        try {
            client.connect(5000, Constants.SERVER_HOST, Constants.SERVER_PORT);
            client.sendTCP(new PacketLogin(login, password));
        } catch (Exception e) {
            PacketLoginResponse response = new PacketLoginResponse();
            response.isSuccess = false;
            response.reason = e.toString();
            response.session = "";
            
            publish(new LoginState(response));
            
            e.printStackTrace();
            return null;
        }

        return null;
    }

    class LoginState {

        public PacketLoginResponse p;

        public LoginState(PacketLoginResponse p) {
            this.p = p;
        }
    }

    @Override
    protected void process(List<LoginState> l) {
        for (LoginState ls : l) {
            this.mf.showLoginResult(ls.p);
        }
    }
}
