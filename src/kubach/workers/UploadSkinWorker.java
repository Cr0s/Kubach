package kubach.workers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.gui.MainFrame;
import kubach.workers.UploadSkinWorker.SkinUploadState;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Worker for file downloading
 *
 * @author Cr0s
 */
public class UploadSkinWorker extends SwingWorker<Void, SkinUploadState> {

    private MainFrame nbf;
    private File skinFile;
    private String username, session;

    public UploadSkinWorker(MainFrame nbf, File skinFile, String username, String session) {
        this.nbf = nbf;
        this.skinFile = skinFile;
        this.username = username;
        this.session = session;
    }

    @Override
    protected Void doInBackground() {
        String url = ConfigManager.getInstance().getProperties().getProperty("skinuploadurl");

        try {
            uploadSkinFile(url);
        } catch (Exception e) {
            publish(new SkinUploadState(false));
            e.printStackTrace();
        }

        return null;
    }

    private void uploadSkinFile(String url) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

            FileBody skin = new FileBody(skinFile);
            StringBody user = new StringBody(this.username, ContentType.TEXT_PLAIN);
            StringBody session = new StringBody(this.session, ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("skinfile", skin)
                    .addPart("user", user)
                    .addPart("sessionId", session)
                    .build();


            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String resString = EntityUtils.toString(resEntity);
                    if (resString.equals("OK")) {
                        publish(new SkinUploadState(true));
                    } else {
                        publish(new SkinUploadState(false));
                    }
                }
                
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Override
    protected void process(List<SkinUploadState> chunks) {
        for (SkinUploadState sts : chunks) {
            nbf.updateSkinUploadState(sts.isSuccess);
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

    public class SkinUploadState {

        public boolean isSuccess;

        public SkinUploadState(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }
}
