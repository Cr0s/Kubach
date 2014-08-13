package kubach.workers;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.Constants;
import kubach.ac.CCLoader;
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
import kubach.workers.WLCheckWorker.WLCheckState;

/**
 * Doing check in whitelist
 *
 * @author Cr0s
 */
public class WLCheckWorker extends SwingWorker<Void, WLCheckState> {
    public WhiteListChecker wc;
    public String username, session;
    
    public WLCheckWorker(WhiteListChecker wc, String username, String session) {
        //System.setSecurityManager(new NullSecurityManager());
        this.wc = wc;
        
        this.username = username;
        this.session = session;
        
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


        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketWLRequestRealm) {
                    PacketWLRequestRealm p = (PacketWLRequestRealm) object;
                    
                    doCheckStep(connection, p.realm);
                } else if (object instanceof PacketBadWLItems) {
                    publish(new WLCheckState((PacketBadWLItems) object));
                    
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

        PacketWLRequestRealm pwlrr = new PacketWLRequestRealm();
        pwlrr.username = this.username;
        pwlrr.session = this.session;
        
        client.sendTCP(pwlrr);

        return null;
    }

    private void doCheckStep(Connection cnn, String realm) {
        CCLoader loader = new CCLoader();
        final int BUFFER_SIZE = 4096;
        BufferedInputStream bis = null;
        
        try {
            String addr = ConfigManager.getInstance().getProperties().getProperty("nativecheckurl");
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "User-Agent: Kubach v" + Constants.VERSION);

            int totalDataRead = 0;
            bis = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER_SIZE)) {
                byte[] data = new byte[BUFFER_SIZE];
                int i = 0;

                while ((i = bis.read(data)) != -1) {
                    totalDataRead = totalDataRead + i;
                    baos.write(data, 0, i);
                }
                
                loader.setClassContent(baos.toByteArray());
                try {
                    Class clazz = loader.findClass("kubach.ac.NativeCheck");
                    
                    
                    try {
                        try {
                            Object obj = clazz.newInstance();

                            PacketWLData response = new PacketWLData();
                            response.data = (String) clazz.getDeclaredMethod("doCheck", String.class).invoke(obj, realm);
                            response.prefix = ConfigManager.getInstance().getClientPrefix();
                            response.session = this.session;
                            response.username = this.username;
                            
                            cnn.sendTCP(response);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    } catch (NoSuchMethodException | SecurityException ex) {
                        ex.printStackTrace();
                    }
                    
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    
    public class WLCheckState {

        public PacketBadWLItems p;

        public WLCheckState(PacketBadWLItems p) {
            this.p = p;
        }
    }

    @Override
    protected void process(List<WLCheckState> l) {
        for (WLCheckState state : l) {
            this.wc.parseCheckResult(state);
        }
    }
}
