package kubach.workers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.gui.MainFrame;
import kubach.workers.DownloadFileWorker.SyncTaskState;

/**
 * Worker for file downloading
 *
 * @author Cr0s
 */
public class DownloadFileWorker extends SwingWorker<Void, SyncTaskState> {

    private MainFrame nbf;
    private String path, pathToFile;
    private int totalProgressValue;
    private int tableRowIndex;
    private int numTries = 5;

    public DownloadFileWorker(MainFrame nbf, String pathToFile, String path, int tableRowIndex) {
        this.nbf = nbf;
        this.pathToFile = pathToFile;
        this.path = path;
        this.tableRowIndex = tableRowIndex;
    }

    @Override
    protected Void doInBackground() {
        final int BUFFER_SIZE = 2048;

        BufferedInputStream bis = null;
        ByteArrayOutputStream baos;

        String prefix = ConfigManager.getInstance().getProperties().getProperty("clientprefix");
        String addr = ConfigManager.getInstance().getProperties().getProperty("updateurl");
        addr = addr.replace("%PREFIX%", prefix);
        addr = addr.replace("%FILE%", this.path);

        try {
            URL url = new URL(addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "User-Agent: Kubach v" + ConfigManager.getInstance().getProperties().getProperty("version"));

            this.totalProgressValue = connection.getContentLength();

            int totalDataRead = 0;
            bis = new BufferedInputStream(connection.getInputStream());
            baos = new ByteArrayOutputStream();
            try (BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER_SIZE)) {
                byte[] data = new byte[BUFFER_SIZE];
                int i = 0;

                while ((i = bis.read(data)) != -1) {
                    totalDataRead = totalDataRead + i;
                    baos.write(data, 0, i);

                    publish(new SyncTaskState(addr, "Downloading (" + String.format("%.2f", totalDataRead / (float) this.totalProgressValue * 100) + "%)", totalDataRead));
                }

                // Write file to disk
                File f = new File(this.pathToFile);
                writeBytesToFile(f, baos.toByteArray());

                publish(new SyncTaskState(addr, "Finished", this.totalProgressValue, f));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (--numTries > 0) {
                publish(new SyncTaskState(addr, "Retrying...", 0));

                this.doInBackground();
            } else {
                publish(new SyncTaskState(addr, "FAILED", 0));
            }
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                if (--numTries > 0) {
                    publish(new SyncTaskState(addr, "Retrying...", 0));

                    this.doInBackground();
                } else {
                    publish(new SyncTaskState(addr, "FAILED", 0));
                }
            }
        }

        return null;
    }

    @Override
    protected void process(List<SyncTaskState> chunks) {
        for (SyncTaskState sts : chunks) {
            nbf.setSyncTableRow(this.tableRowIndex, sts.status, sts.progressValue, this.totalProgressValue, sts.file);
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

    public class SyncTaskState {

        public String path;
        public String status;
        public int progressValue;
        public File file;

        public SyncTaskState(String path, String status, int progressValue, File file) {
            this.path = path;
            this.status = status;
            this.progressValue = progressValue;
            this.file = file;
        }
        
        public SyncTaskState(String path, String status, int progressValue) {
            this(path, status, progressValue, null);
        }
    }
}
