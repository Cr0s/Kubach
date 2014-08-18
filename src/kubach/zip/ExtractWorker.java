package kubach.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import kubach.ConfigManager;
import kubach.zip.ExtractWorker.ExtractState;

/**
 * Sends auth request and checks result
 *
 * @author Cr0s
 */
public class ExtractWorker extends SwingWorker<Void, ExtractState> {

    public ExtractingDialog ed;
    private File pack;
    private int totalFiles, filesDone;
    private String currentFile;
    private final int BUFFER_SIZE = 4096;

    public ExtractWorker(ExtractingDialog ed, File pack) {
        this.ed = ed;
        this.pack = pack;
        try {
            try (ZipFile zf = new ZipFile(pack)) {
                this.totalFiles = zf.size();
                System.out.println("[Package] Total files: " + this.totalFiles);
            }
        } catch (ZipException ex) {
            JOptionPane.showMessageDialog(ed, "Failed to extract: " + ex.toString(), "Extracting fail", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ExtractWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        File destDirectory = new File(ConfigManager.getInstance().pathToJar);
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(pack))) {
            ZipEntry entry = zipIn.getNextEntry();
            
            // iterates over entries in the zip file
            while (entry != null) { 
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    this.currentFile = entry.getName();
                    publish(new ExtractState(this.currentFile, this.totalFiles, this.filesDone++));
                    
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {                   
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                    
                    this.currentFile = "creating dir: " + dir.getName();
                    publish(new ExtractState(this.currentFile, this.totalFiles, ++this.filesDone));
                }
                
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        
        publish(new ExtractState(this.currentFile, this.totalFiles, this.totalFiles));
        
        return null;
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    class ExtractState {

        public String currentFile;
        public int totalNumFiles;
        public int filesDone;

        public ExtractState(String currentFile, int totalNumFiles, int filesDone) {
            this.currentFile = currentFile;
            this.totalNumFiles = totalNumFiles;
            this.filesDone = filesDone;
        }
    }

    @Override
    protected void process(List<ExtractState> l) {
        for (ExtractState es : l) {
            this.ed.updateState(es);
        }
    }
}
