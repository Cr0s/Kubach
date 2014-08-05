package kubach.gui;

import java.awt.Component;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import kubach.ConfigManager;
import kubach.Constants;
import kubach.packet.PacketFilesList;
import kubach.packet.PacketLoginResponse;
import kubach.util.LaunchCommandBuilder;
import kubach.util.Md5Checksum;
import kubach.workers.AuthWorker;
import kubach.workers.ChangePasswordWorker;
import kubach.workers.DownloadFileWorker;
import kubach.workers.LoadCaptchaWorker;
import kubach.workers.RemoveSkinWorker;
import kubach.workers.RequestFilesListWorker;
import kubach.workers.UploadSkinWorker;
import kubach.zip.ExtractingDialog;
import net.minecraft.skintest.ModelPreview;

/**
 * Main frame
 *
 * @author Cr0s
 */
public class MainFrame extends javax.swing.JFrame {

    private String loggedUsername, session;
    // Sync
    private int numFilesToSync, numFilesSynced;
    private PacketFilesList p;
    private SyncState syncState = SyncState.WAITING;

    public void cancelRegistration() {
        txtUsername.setEnabled(true);
        txtPassword.setEnabled(true);

        btnLogin.setEnabled(true);
        btnRegister.setEnabled(true);
        btnRegister.setText("Register");

        this.currentState = LauncherState.WAIT_LOGIN;
    }

    private void startFileSync() {
        this.syncState = SyncState.STARTED;

        RequestFilesListWorker rflw = new RequestFilesListWorker(this, ConfigManager.getInstance().getProperties().getProperty("clientprefix"));
        rflw.execute();
    }

    public void parseFilesList(PacketFilesList p) {
        this.numFilesToSync = 0;
        this.pbSync.setValue(0);

        DefaultTableModel model = (DefaultTableModel) tblSync.getModel();
        int rows = model.getRowCount();
        for (int i = rows - 1; i >= 0; i--) {
            model.removeRow(i);
        }


        this.syncState = SyncState.STARTED;

        boolean needToBeSync = false;

        for (String s : p.files) {
            String[] args = s.split(":");

            if (isFileNeedToSync(args[0], args[1])) {
                needToBeSync = true;
                this.numFilesToSync++;
                this.pbSync.setMaximum(this.numFilesToSync);

                // Make nonexistent dirs first
                String pathToFile = ConfigManager.getInstance().pathToJar + args[0];

                File f = new File(pathToFile);
                File pf = f.getParentFile();

                if (pf != null && !pf.exists()) {
                    pf.mkdirs();
                }

                // Create worker to download
                ((DefaultTableModel) tblSync.getModel()).addRow(new Object[]{args[0], "-", new JProgressBar()});
                DownloadFileWorker wsi = new DownloadFileWorker(this, f.getPath(), args[0], tblSync.getModel().getRowCount() - 1);
                wsi.execute();
            }
        }
        
        if (!needToBeSync) {
            this.syncState = SyncState.COMPLETE;
        }
    }

    private boolean isFileNeedToSync(String path, String md5) {
        File f = new File(ConfigManager.getInstance().pathToJar + path);

        try {
            if (!f.exists() || !md5.equals(Md5Checksum.getMD5Checksum(f.getPath()))) {
                return true;
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }

        return false;
    }

    public void setSyncTableRow(int tableRowIndex, String status, int progressValue, int totalProgressValue, File file) {
        tblSync.getModel().setValueAt(status, tableRowIndex, 1);

        Component c = (Component) tblSync.getValueAt(tableRowIndex, 2);

        if (c instanceof JProgressBar) {
            ((JProgressBar) c).setMaximum(totalProgressValue);
            ((JProgressBar) c).setValue(progressValue);
        }

        // TODO: determine its thread-safe or not
        if (status.equals("Finished")) {
            this.numFilesSynced++;
            
            // Just downloaded package, extract it
            if (file.getPath().endsWith(".package")) {
                ExtractingDialog ed = new ExtractingDialog(this, false, file);
                ed.setVisible(true);
            }
        }

        tblSync.repaint();

        pbSync.setValue(this.numFilesSynced);
        pbSync.setString("Done: " + this.numFilesSynced + "/" + this.numFilesToSync);
        if (this.numFilesToSync != 0) {
            float percentage = this.numFilesSynced / (float) this.numFilesToSync * 100;
            tabs.setTitleAt(1, "File sync (" + String.format("%.2f", percentage) + "%)");
        }

        if (this.numFilesSynced == this.numFilesToSync) {
            this.syncState = SyncState.COMPLETE;

            refreshLogoAndChangelog();
        } else {
            this.syncState = SyncState.SYNCING;
        }
    }

    private void refreshLogoAndChangelog() {
        panBg.removeAll();

        LogoPanel lp = new LogoPanel(panBg);
        panBg.add(lp);
    }

    private String getSkinUrl(String username) {
        return ConfigManager.getInstance().getProperties().getProperty("skinurl").replace("%USERNAME%", username);
    }

    public void updateSkinUploadState(boolean success) {
        removeSkinPanel();
        setSkinPanel();

        btnRemoveSkin.setEnabled(true);
        btnUploadSkin.setEnabled(true);
        btnUploadSkin.setText("Setup new");

        if (!success) {
            JOptionPane.showMessageDialog(this, "An error occured while uploading skin", "Skin upload error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setSkinPanel() {
        ModelPreview mp = new ModelPreview(1, getSkinUrl(this.loggedUsername));
        panSkin.add(mp);

        Insets i = panSkin.getInsets();
        mp.setBounds(i.left, i.top, panSkin.getSize().width - (i.right + i.left), panSkin.getSize().height - (i.bottom + i.top));
        mp.start();
    }

    private void removeSkinPanel() {
        for (int i = 0; i < panSkin.getComponentCount(); i++) {
            Component c = panSkin.getComponent(i);

            if (c instanceof ModelPreview) {
                ((ModelPreview) panSkin.getComponent(i)).stop();
            }
        }

        panSkin.removeAll();        
    }
    
    public void updateSkinRemoveState(boolean success) {
        removeSkinPanel();
        setSkinPanel();

        btnRemoveSkin.setEnabled(true);
        btnUploadSkin.setEnabled(true);
        btnRemoveSkin.setText("Remove");

        if (!success) {
            JOptionPane.showMessageDialog(this, "An error occured while removing skin", "Skin remove error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parsePasswordChangeResult(boolean success) {
        String newPass = new String(txtNewPassword.getPassword());
        
        txtNewPassword.setText("");
        txtNewPassword.setEnabled(true);
        btnChangePassword.setEnabled(true);
        btnChangePassword.setText("Change");
        
        if (success) {
            txtPassword.setText(newPass);
            ConfigManager.getInstance().getProperties().setProperty("password", newPass);
        } else {
            JOptionPane.showMessageDialog(this, "An error occured while changing password!", "Password change error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public enum LauncherState {

        WAIT_LOGIN,
        LOGGED_IN,
        REGISTERING,
        MD5_CHECK,
        READY
    };

    public enum SyncState {

        WAITING,
        STARTED,
        SYNCING,
        COMPLETE
    };
    public LauncherState currentState = LauncherState.WAIT_LOGIN;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();

        LogoPanel lp = new LogoPanel(panBg);
        panBg.add(lp);

        startFileSync();

        fcSkin.addChoosableFileFilter(new FileNameExtensionFilter("PNG files", "png"));
        
        this.redirectSystemStreams();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fcSkin = new javax.swing.JFileChooser();
        grbLogin = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();
        tabs = new javax.swing.JTabbedPane();
        tabMain = new javax.swing.JPanel();
        panBg = new javax.swing.JPanel();
        tabSync = new javax.swing.JPanel();
        panSync = new javax.swing.JPanel();
        pbSync = new javax.swing.JProgressBar();
        spTblSync = new javax.swing.JScrollPane();
        tblSync = new javax.swing.JTable();
        tabLog = new javax.swing.JPanel();
        logTabs = new javax.swing.JTabbedPane();
        tabOutLog = new javax.swing.JPanel();
        spLog = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        tabChatLog = new javax.swing.JPanel();
        scChatDates = new javax.swing.JScrollPane();
        lbChatDates = new javax.swing.JList();
        scChatLog = new javax.swing.JScrollPane();
        txtChatLog = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        grbSkin = new javax.swing.JPanel();
        panSkin = new javax.swing.JPanel();
        btnRemoveSkin = new javax.swing.JButton();
        btnUploadSkin = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnChangePassword = new javax.swing.JButton();
        txtNewPassword = new javax.swing.JPasswordField();
        jPanel3 = new javax.swing.JPanel();
        lblMemory = new javax.swing.JLabel();
        cbMemory = new javax.swing.JComboBox();
        btnLaunch = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        fcSkin.setAcceptAllFileFilterUsed(false);
        fcSkin.setDialogTitle("Select skin png");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        grbLogin.setBorder(javax.swing.BorderFactory.createTitledBorder("Login"));

        lblUsername.setText("Username:");

        txtUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtUsernameKeyPressed(evt);
            }
        });

        lblPassword.setText("Password:");

        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnRegister.setText("Register");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        txtPassword.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtPasswordPropertyChange(evt);
            }
        });
        txtPassword.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtPasswordInputMethodTextChanged(evt);
            }
        });
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtPasswordKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPasswordKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout grbLoginLayout = new javax.swing.GroupLayout(grbLogin);
        grbLogin.setLayout(grbLoginLayout);
        grbLoginLayout.setHorizontalGroup(
            grbLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(grbLoginLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(grbLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPassword)
                    .addComponent(txtUsername)
                    .addGroup(grbLoginLayout.createSequentialGroup()
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(grbLoginLayout.createSequentialGroup()
                        .addGroup(grbLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUsername)
                            .addComponent(lblPassword))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        grbLoginLayout.setVerticalGroup(
            grbLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(grbLoginLayout.createSequentialGroup()
                .addComponent(lblUsername)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPassword)
                .addGap(1, 1, 1)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(grbLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin)
                    .addComponent(btnRegister)))
        );

        javax.swing.GroupLayout panBgLayout = new javax.swing.GroupLayout(panBg);
        panBg.setLayout(panBgLayout);
        panBgLayout.setHorizontalGroup(
            panBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        panBgLayout.setVerticalGroup(
            panBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 477, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout tabMainLayout = new javax.swing.GroupLayout(tabMain);
        tabMain.setLayout(tabMainLayout);
        tabMainLayout.setHorizontalGroup(
            tabMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        tabMainLayout.setVerticalGroup(
            tabMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tabs.addTab("Main", tabMain);

        panSync.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pbSync.setStringPainted(true);

        javax.swing.GroupLayout panSyncLayout = new javax.swing.GroupLayout(panSync);
        panSync.setLayout(panSyncLayout);
        panSyncLayout.setHorizontalGroup(
            panSyncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSyncLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pbSync, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panSyncLayout.setVerticalGroup(
            panSyncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSyncLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pbSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblSync.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "File", "Status", "Progress"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spTblSync.setViewportView(tblSync);
        tblSync.getColumnModel().getColumn(2).setResizable(false);
        tblSync.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                if (table.getValueAt(row, 2) == null) {
                    table.setValueAt(new JProgressBar(), row, 2);  
                } 

                if (table.getValueAt(row, 2) instanceof Component) {
                    return (Component)table.getValueAt(row, 2);
                } else {
                    return null;
                }
            }
        });

        javax.swing.GroupLayout tabSyncLayout = new javax.swing.GroupLayout(tabSync);
        tabSync.setLayout(tabSyncLayout);
        tabSyncLayout.setHorizontalGroup(
            tabSyncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panSync, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(spTblSync, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        tabSyncLayout.setVerticalGroup(
            tabSyncLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabSyncLayout.createSequentialGroup()
                .addComponent(panSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spTblSync, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
        );

        tabs.addTab("File sync", tabSync);

        spLog.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        spLog.setAutoscrolls(true);

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        txtLog.setRows(5);
        spLog.setViewportView(txtLog);

        javax.swing.GroupLayout tabOutLogLayout = new javax.swing.GroupLayout(tabOutLog);
        tabOutLog.setLayout(tabOutLogLayout);
        tabOutLogLayout.setHorizontalGroup(
            tabOutLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spLog, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
        );
        tabOutLogLayout.setVerticalGroup(
            tabOutLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spLog, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
        );

        logTabs.addTab("Output", tabOutLog);

        scChatDates.setViewportView(lbChatDates);

        txtChatLog.setEditable(false);
        txtChatLog.setColumns(20);
        txtChatLog.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        txtChatLog.setRows(5);
        scChatLog.setViewportView(txtChatLog);

        javax.swing.GroupLayout tabChatLogLayout = new javax.swing.GroupLayout(tabChatLog);
        tabChatLog.setLayout(tabChatLogLayout);
        tabChatLogLayout.setHorizontalGroup(
            tabChatLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabChatLogLayout.createSequentialGroup()
                .addComponent(scChatDates, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scChatLog, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE))
        );
        tabChatLogLayout.setVerticalGroup(
            tabChatLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scChatDates, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
            .addComponent(scChatLog)
        );

        logTabs.addTab("Chat log", tabChatLog);

        javax.swing.GroupLayout tabLogLayout = new javax.swing.GroupLayout(tabLog);
        tabLog.setLayout(tabLogLayout);
        tabLogLayout.setHorizontalGroup(
            tabLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logTabs)
        );
        tabLogLayout.setVerticalGroup(
            tabLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logTabs)
        );

        tabs.addTab("Log", tabLog);

        grbSkin.setBorder(javax.swing.BorderFactory.createTitledBorder("Skin setup"));

        panSkin.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panSkinLayout = new javax.swing.GroupLayout(panSkin);
        panSkin.setLayout(panSkinLayout);
        panSkinLayout.setHorizontalGroup(
            panSkinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
        );
        panSkinLayout.setVerticalGroup(
            panSkinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        btnRemoveSkin.setText("Remove");
        btnRemoveSkin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveSkinActionPerformed(evt);
            }
        });

        btnUploadSkin.setText("Setup new");
        btnUploadSkin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadSkinActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout grbSkinLayout = new javax.swing.GroupLayout(grbSkin);
        grbSkin.setLayout(grbSkinLayout);
        grbSkinLayout.setHorizontalGroup(
            grbSkinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(grbSkinLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panSkin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(grbSkinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnUploadSkin, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                    .addComponent(btnRemoveSkin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        grbSkinLayout.setVerticalGroup(
            grbSkinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panSkin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(grbSkinLayout.createSequentialGroup()
                .addComponent(btnUploadSkin)
                .addGap(4, 4, 4)
                .addComponent(btnRemoveSkin))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Change password"));

        jLabel1.setText("New password:");

        btnChangePassword.setText("Change");
        btnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnChangePassword))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 179, Short.MAX_VALUE))
                    .addComponent(txtNewPassword))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChangePassword)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(grbSkin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(grbSkin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(283, Short.MAX_VALUE))
        );

        tabs.addTab("My Account", jPanel1);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Launch", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        lblMemory.setText("Memory:");

        cbMemory.setEditable(true);
        cbMemory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "512", "1024", "2048", "3032", "4096" }));

        btnLaunch.setText("Launch");
        btnLaunch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaunchActionPerformed(evt);
            }
        });

        jButton2.setText("Get launch command");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblMemory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbMemory, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMemory)
                    .addComponent(cbMemory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLaunch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(grbLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(grbLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.setTitle("Kubach Launcher (v" + Constants.VERSION + ")");
        evt.getWindow().setLocationRelativeTo(evt.getOppositeWindow());

        txtUsername.setText(ConfigManager.getInstance().getProperties().getProperty("username"));
        txtPassword.setText(ConfigManager.getInstance().getProperties().getProperty("password"));
    }//GEN-LAST:event_formWindowOpened

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        if (this.currentState == LauncherState.WAIT_LOGIN
                && !txtUsername.getText().trim().isEmpty()
                && !new String(txtPassword.getPassword()).trim().isEmpty()) {
            btnLogin.setEnabled(false);
            AuthWorker aw = new AuthWorker(this, txtUsername.getText(), new String(txtPassword.getPassword()));

            aw.execute();
        } else if (this.currentState == LauncherState.LOGGED_IN) {
            btnLogin.setText("Login");
            this.cancelRegistration(); // this enables back text fields and buttons

            removeSkinPanel();
            
            txtNewPassword.setText("");
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        if (!txtUsername.getText().trim().isEmpty()
                && !new String(txtPassword.getPassword()).trim().isEmpty()) {
            this.cp.username = txtUsername.getText();
            this.cp.password = new String(txtPassword.getPassword());

            txtUsername.setEnabled(false);
            txtPassword.setEnabled(false);

            btnLogin.setEnabled(false);
            btnRegister.setEnabled(false);
            btnRegister.setText("Loading...");

            this.currentState = LauncherState.REGISTERING;

            LoadCaptchaWorker lcw = new LoadCaptchaWorker(cp, txtUsername.getText(), new String(txtPassword.getPassword()));
            lcw.execute();
        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void txtUsernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUsernameKeyPressed
    }//GEN-LAST:event_txtUsernameKeyPressed

    private void txtPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPasswordKeyPressed
    }//GEN-LAST:event_txtPasswordKeyPressed

    private void txtPasswordPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtPasswordPropertyChange
    }//GEN-LAST:event_txtPasswordPropertyChange

    private void txtPasswordInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtPasswordInputMethodTextChanged
    }//GEN-LAST:event_txtPasswordInputMethodTextChanged

    private void txtPasswordKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPasswordKeyTyped
    }//GEN-LAST:event_txtPasswordKeyTyped

    private void btnUploadSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadSkinActionPerformed
        if (fcSkin.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File skinFile = fcSkin.getSelectedFile();

            if (skinFile.exists() && skinFile.canRead()) {
                UploadSkinWorker usw = new UploadSkinWorker(this, skinFile, this.loggedUsername, this.session);
                usw.execute();

                btnRemoveSkin.setEnabled(false);
                btnUploadSkin.setEnabled(false);
                btnUploadSkin.setText("Uploading...");
            }
        }
    }//GEN-LAST:event_btnUploadSkinActionPerformed

    private void btnRemoveSkinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSkinActionPerformed
        RemoveSkinWorker rsw = new RemoveSkinWorker(this, this.loggedUsername, this.session);
        rsw.execute();

        btnRemoveSkin.setEnabled(false);
        btnUploadSkin.setEnabled(false);
        btnRemoveSkin.setText("Removing...");
    }//GEN-LAST:event_btnRemoveSkinActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        if (txtNewPassword.getPassword().length > 0) {
            ChangePasswordWorker cpw = new ChangePasswordWorker(this, this.loggedUsername, this.session, new String(txtNewPassword.getPassword()));
            cpw.execute();
            
            txtPassword.setEnabled(false);
            btnChangePassword.setEnabled(false);
            btnChangePassword.setText("Changing...");
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    private void btnLaunchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaunchActionPerformed
        String memValue = cbMemory.getSelectedItem().toString();
        String launchCommand = LaunchCommandBuilder.getLaunchCommand(this.loggedUsername, this.session, memValue);
        
        if (this.syncState != SyncState.COMPLETE) {
            if (JOptionPane.showConfirmDialog(this, "Synchronization with server isn't done.\nAre you sure to launch unsynchronized client?", "Client not synchronized", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        if (this.currentState != LauncherState.LOGGED_IN) {
            if (JOptionPane.showConfirmDialog(this, "You are not logged in. If you launch game without login you can't join the Kubach servers.\nAre you sure to launch client without login?", "You are not logged in", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }        
        
        System.out.println(launchCommand);
        
        Process process;
        try {
            process = new ProcessBuilder(launchCommand.split(" ")).directory(new File(ConfigManager.getInstance().pathToJar)).start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to start process: " + ex.toString(), "Launch error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
        
        errorGobbler.start();
        outputGobbler.start();        
    }//GEN-LAST:event_btnLaunchActionPerformed

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateLogArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateLogArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }    
    
    private void updateLogArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                txtLog.append(text);
            }
        });
    }    
    
    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        ConfigManager.getInstance().saveProperties();
    }//GEN-LAST:event_formWindowClosed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String memValue = cbMemory.getSelectedItem().toString();
        String launchCommand = LaunchCommandBuilder.getLaunchCommand(this.loggedUsername, this.session, memValue);
        
        LaunchCommandDialog lcd = new LaunchCommandDialog(this, true, launchCommand);
        lcd.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    public void showLoginResult(PacketLoginResponse p) {
        btnLogin.setEnabled(true);

        if (p.isSuccess) {
            System.out.println("Got session: " + p.session);

            this.currentState = LauncherState.LOGGED_IN;
            txtUsername.setEnabled(false);
            txtPassword.setEnabled(false);
            btnRegister.setEnabled(false);

            btnLogin.setText("Log Out");

            this.loggedUsername = txtUsername.getText();
            this.session = p.session;

            setSkinPanel();

            ConfigManager.getInstance().getProperties().setProperty("username", txtUsername.getText());
            ConfigManager.getInstance().getProperties().setProperty("password", new String(txtPassword.getPassword()));

            ConfigManager.getInstance().saveProperties();
        } else {
            System.out.println("Login error: " + p.reason);

            JOptionPane.showMessageDialog(this, p.reason, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    CaptchaPanel cp = new CaptchaPanel(this);
    
    class StreamGobbler extends Thread {
        InputStream is;

        // reads everything from is until empty.
        StreamGobbler(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ( (line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnLaunch;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnRemoveSkin;
    private javax.swing.JButton btnUploadSkin;
    private javax.swing.JComboBox cbMemory;
    private javax.swing.JFileChooser fcSkin;
    private javax.swing.JPanel grbLogin;
    private javax.swing.JPanel grbSkin;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JList lbChatDates;
    private javax.swing.JLabel lblMemory;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTabbedPane logTabs;
    private javax.swing.JPanel panBg;
    private javax.swing.JPanel panSkin;
    private javax.swing.JPanel panSync;
    private javax.swing.JProgressBar pbSync;
    private javax.swing.JScrollPane scChatDates;
    private javax.swing.JScrollPane scChatLog;
    private javax.swing.JScrollPane spLog;
    private javax.swing.JScrollPane spTblSync;
    private javax.swing.JPanel tabChatLog;
    private javax.swing.JPanel tabLog;
    private javax.swing.JPanel tabMain;
    private javax.swing.JPanel tabOutLog;
    private javax.swing.JPanel tabSync;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTable tblSync;
    private javax.swing.JTextArea txtChatLog;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
