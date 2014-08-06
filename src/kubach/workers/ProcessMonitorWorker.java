package kubach.workers;

import java.util.List;
import javax.swing.SwingWorker;
import kubach.gui.MainFrame;
import kubach.workers.ProcessMonitorWorker.ProcessState;


/**
 * Worker which waiting for process termination and returns exit code
 *
 * @author Cr0s
 */
public class ProcessMonitorWorker extends SwingWorker<Void, ProcessState> {

    private MainFrame nbf;
    private Process proc;

    public ProcessMonitorWorker(MainFrame nbf, Process proc) {
        this.nbf = nbf;
        this.proc = proc;
    }

    @Override
    protected Void doInBackground() {
        try {
            publish (new ProcessState(proc.waitFor()));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    @Override
    protected void process(List<ProcessState> chunks) {
        for (ProcessState s : chunks) {
            nbf.parseProcessState(s);
        }
    }

    public class ProcessState {

        public int exitCode;

        public ProcessState(int exitCode) {
            this.exitCode = exitCode;
        }
    }
}
