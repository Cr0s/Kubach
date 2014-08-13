package kubach;

import com.sun.jna.NativeLibrary;
import kubach.gui.MainFrame;

/**
 * @author Cr0s
 */
public class Kubach {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        setLookAndFeel();
        NativeLibrary.addSearchPath("Cr0s", ConfigManager.getInstance().pathToJar);
        
        MainFrame mf = new MainFrame();
        
        mf.setVisible(true);
    }
    
    private static void setLookAndFeel() {
        try {
            OUTER:
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                //System.out.println(info.getName());
                switch (info.getName()) {
                    case "Windows":
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break OUTER;
                    case "GTK+":
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break OUTER;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }        
    }    
}
