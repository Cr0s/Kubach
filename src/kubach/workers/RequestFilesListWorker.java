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
import kubach.workers.RequestFilesListWorker.FilesListState;

/**
 * Sends auth request and checks result
 *
 * @author Cr0s
 */
public class RequestFilesListWorker extends SwingWorker<Void, FilesListState> {

    public String prefix;
    public MainFrame mf;

    public RequestFilesListWorker(MainFrame f, String prefix) {
        this.mf = f;
        this.prefix = prefix;
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


        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketFilesList) {
                    PacketFilesList response = (PacketFilesList) object;
                    System.out.println("[Sync] Got files: " + response.numFiles + " | " + response.files);

                    publish(new FilesListState(response));

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

        Thread.sleep(1000);
        PacketFilesListRequest req = new PacketFilesListRequest();
        req.clientPrefix = this.prefix;
        
        client.sendTCP(req);


        return null;
    }

    class FilesListState {

        public PacketFilesList p;

        public FilesListState(PacketFilesList p) {
            this.p = p;
        }
    }

    @Override
    protected void process(List<FilesListState> l) {
        for (FilesListState ls : l) {
            this.mf.parseFilesList(ls.p);
        }
    }
}
