package kubach.workers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.gui.MainFrame;
import kubach.workers.RemoveSkinWorker.SkinRemoveState;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 * Worker for file downloading
 *
 * @author Cr0s
 */
public class RemoveSkinWorker extends SwingWorker<Void, SkinRemoveState> {

    private MainFrame nbf;
    private String username, session;

    public RemoveSkinWorker(MainFrame nbf, String username, String session) {
        this.nbf = nbf;
        this.username = username;
        this.session = session;
    }

    @Override
    protected Void doInBackground() {
        String url = ConfigManager.getInstance().getProperties().getProperty("skinremoveurl");

        try {
            uploadSkinFile(url);
        } catch (Exception e) {
            publish(new SkinRemoveState(false));
            e.printStackTrace();
        }

        return null;
    }

    private void uploadSkinFile(String url) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost(url);

            StringBody user = new StringBody(this.username, ContentType.TEXT_PLAIN);
            StringBody sess = new StringBody(this.session, ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("user", user)
                    .addPart("sessionId", sess)
                    .build();


            httppost.setEntity(reqEntity);

            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String resString = EntityUtils.toString(resEntity);
                    if (resString.equals("OK")) {
                        publish(new SkinRemoveState(true));
                    } else {
                        publish(new SkinRemoveState(false));
                    }
                }
                
                EntityUtils.consume(resEntity);
            }
        }
    }

    @Override
    protected void process(List<SkinRemoveState> chunks) {
        for (SkinRemoveState sts : chunks) {
            nbf.updateSkinRemoveState(sts.isSuccess);
        }
    }

    /**
     * Writes the specified byte[] to the specified File.
     *
     * @param theFile File Object representing the path to write to.
     * @param bytes The byte[] of data to write to the File.
     * @throws IOException Thrown if there is problem creating or writing the
     * File.
     */
    public static void writeBytesToFile(File theFile, byte[] bytes) throws IOException {
        BufferedOutputStream bos = null;

        try {
            FileOutputStream fos = new FileOutputStream(theFile);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } finally {
            if (bos != null) {
                try {
                    //flush and close the BufferedOutputStream
                    bos.flush();
                    bos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public class SkinRemoveState {

        public boolean isSuccess;

        public SkinRemoveState(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }
}
