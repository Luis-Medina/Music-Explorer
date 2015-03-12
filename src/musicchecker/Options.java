/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Options.java
 *
 * Created on Dec 30, 2009, 11:29:36 PM
 */
package musicchecker;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Luiso
 */
public class Options extends javax.swing.JFrame {

    JFileChooser chooser = new JFileChooser();

    /** Creates new form Options */
    public Options() {
        initComponents();
        ItemListener comboListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //System.out.println(e.getItemSelectable().getSelectedObjects()[0]);
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem().equals("Usenet")) {
                        jSpinner1.setEnabled(false);
                    } else {
                        jSpinner1.setEnabled(true);
                    }
                }
            }
        };
        modeCombo.addItemListener(comboListener);
        jTextField1.setText(Main.prefs.get("downpath").toString());
        jSpinner1.getModel().setValue(Main.prefs.get("maxdownloads"));
        modeCombo.setSelectedIndex((Integer)Main.prefs.get("downloadmode"));
        if(modeCombo.getSelectedIndex() == 0){
            jSpinner1.setEnabled(false);
        }
        notifyWeekCheckBox.setSelected((Boolean)Main.prefs.get("autocheck"));
        closeToTrayCheckBox.setSelected((Boolean)Main.prefs.get("closetotray"));
        jRadioButton1.setSelected((Boolean)Main.prefs.get("shutdownafterfinish"));
        jRadioButton2.setSelected((Boolean)Main.prefs.get("closeafterfinish"));
        hot100Checkbox.setSelected((Boolean)Main.prefs.get("hot100")); 
        latinCheckbox.setSelected((Boolean)Main.prefs.get("latin")); 
        hiphopCheckbox.setSelected((Boolean)Main.prefs.get("hiphop")); 
        countryCheckbox.setSelected((Boolean)Main.prefs.get("country")); 
        rockCheckbox.setSelected((Boolean)Main.prefs.get("rock")); 
        popCheckbox.setSelected((Boolean)Main.prefs.get("pop")); 
        danceclubCheckbox.setSelected((Boolean)Main.prefs.get("danceclub")); 
        laMegaCheckbox.setSelected((Boolean)Main.prefs.get("lamega"));
        tocaDeToCheckbox.setSelected((Boolean)Main.prefs.get("tocadeto"));
        kq105Checkbox.setSelected((Boolean)Main.prefs.get("kq105"));
        reggaeton94Checkbox.setSelected((Boolean)Main.prefs.get("reggaeton94"));
        fidelityCheckbox.setSelected((Boolean)Main.prefs.get("fidelity"));
        estereoTempoCheckbox.setSelected((Boolean)Main.prefs.get("estereotempo"));
        deleteNzbCheckbox.setSelected((Boolean)Main.prefs.get("deletenzb"));
        par2Checkbox.setSelected((Boolean) Main.prefs.get("par2check"));
        unrarCheckbox.setSelected((Boolean) Main.prefs.get("unrar"));
        ignoreErrorsCheckbox.setSelected((Boolean) Main.prefs.get("ignoreerrors"));
        
        setLocationRelativeTo(null); 
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        modeCombo = new javax.swing.JComboBox();
        notifyWeekCheckBox = new javax.swing.JCheckBox();
        closeToTrayCheckBox = new javax.swing.JCheckBox();
        deleteNzbCheckbox = new javax.swing.JCheckBox();
        par2Checkbox = new javax.swing.JCheckBox();
        unrarCheckbox = new javax.swing.JCheckBox();
        ignoreErrorsCheckbox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        hot100Checkbox = new javax.swing.JCheckBox();
        latinCheckbox = new javax.swing.JCheckBox();
        hiphopCheckbox = new javax.swing.JCheckBox();
        popCheckbox = new javax.swing.JCheckBox();
        rockCheckbox = new javax.swing.JCheckBox();
        countryCheckbox = new javax.swing.JCheckBox();
        danceclubCheckbox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        laMegaCheckbox = new javax.swing.JCheckBox();
        reggaeton94Checkbox = new javax.swing.JCheckBox();
        estereoTempoCheckbox = new javax.swing.JCheckBox();
        kq105Checkbox = new javax.swing.JCheckBox();
        fidelityCheckbox = new javax.swing.JCheckBox();
        tocaDeToCheckbox = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Options");

        jLabel1.setText("Current download directory is:");

        jTextField1.setEditable(false);

        jButton1.setText("Change");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Max simultaneous downloads: ");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1, 1, 20, 1));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Shutdown after all downloads are finished.");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Close program after all downloads are finished.");

        jLabel3.setText("Download Mode: ");

        modeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Usenet", "IRC" }));

        notifyWeekCheckBox.setText("Notify when a week has passed");

        closeToTrayCheckBox.setText("Close to tray");

        deleteNzbCheckbox.setText("Delete NZB after downloading");

        par2Checkbox.setText("Par 2 Check");

        unrarCheckbox.setText("Rar Extract");

        ignoreErrorsCheckbox.setText("Ignore errors on media update");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(closeToTrayCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(unrarCheckbox)
                            .addComponent(deleteNzbCheckbox)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(notifyWeekCheckBox)
                                .addGap(111, 111, 111)
                                .addComponent(ignoreErrorsCheckbox))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(par2Checkbox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(47, 47, 47)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(modeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(notifyWeekCheckBox)
                    .addComponent(ignoreErrorsCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeToTrayCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteNzbCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(par2Checkbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unrarCheckbox)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("General", jPanel1);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Billboard Charts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        hot100Checkbox.setText("Hot 100");

        latinCheckbox.setText("Latin");

        hiphopCheckbox.setText("R&B/Hip-Hop");

        popCheckbox.setText("Pop");

        rockCheckbox.setText("Rock");

        countryCheckbox.setText("Country");

        danceclubCheckbox.setText("Dance/Club Play");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hot100Checkbox)
                    .addComponent(latinCheckbox))
                .addGap(55, 55, 55)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(countryCheckbox)
                    .addComponent(hiphopCheckbox))
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(rockCheckbox)
                        .addGap(42, 42, 42)
                        .addComponent(danceclubCheckbox))
                    .addComponent(popCheckbox))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rockCheckbox)
                            .addComponent(hiphopCheckbox)
                            .addComponent(danceclubCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(popCheckbox)
                            .addComponent(countryCheckbox)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(hot100Checkbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(latinCheckbox)))
                .addGap(26, 26, 26))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Other", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        laMegaCheckbox.setText("La Mega");

        reggaeton94Checkbox.setText("Reggaeton 94");

        estereoTempoCheckbox.setText("EstereoTempo");

        kq105Checkbox.setText("KQ-105");

        fidelityCheckbox.setText("Fidelity");

        tocaDeToCheckbox.setText("Toca de To'");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(laMegaCheckbox)
                    .addComponent(reggaeton94Checkbox))
                .addGap(55, 55, 55)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(estereoTempoCheckbox)
                    .addComponent(tocaDeToCheckbox))
                .addGap(39, 39, 39)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fidelityCheckbox)
                    .addComponent(kq105Checkbox))
                .addContainerGap(179, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fidelityCheckbox)
                            .addComponent(estereoTempoCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kq105Checkbox)
                            .addComponent(tocaDeToCheckbox)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(laMegaCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(reggaeton94Checkbox)))
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Song Sources", jPanel2);

        jButton3.setText("OK");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int choice = chooser.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            jTextField1.setText(folder.getAbsolutePath() + "\\");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setEnabled(false);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Main.prefs.put("downpath", jTextField1.getText());
        Main.prefs.put("maxdownloads", ((Integer) jSpinner1.getModel().getValue()).intValue());
        Main.prefs.put("shutdownafterfinish", jRadioButton1.isSelected());
        Main.prefs.put("closeafterfinish", jRadioButton2.isSelected());
        Main.prefs.put("downloadmode", modeCombo.getSelectedIndex());
        Main.prefs.put("autocheck", notifyWeekCheckBox.isSelected());
        Main.prefs.put("closetotray", closeToTrayCheckBox.isSelected());
        Main.prefs.put("hot100", hot100Checkbox.isSelected());
        Main.prefs.put("latin", latinCheckbox.isSelected());
        Main.prefs.put("hiphop", hiphopCheckbox.isSelected());
        Main.prefs.put("country", countryCheckbox.isSelected());
        Main.prefs.put("rock", rockCheckbox.isSelected());
        Main.prefs.put("pop", popCheckbox.isSelected());
        Main.prefs.put("danceclub", danceclubCheckbox.isSelected());
        Main.prefs.put("lamega", laMegaCheckbox.isSelected());
        Main.prefs.put("tocadeto", tocaDeToCheckbox.isSelected());
        Main.prefs.put("kq105", kq105Checkbox.isSelected());
        Main.prefs.put("reggaeton94", reggaeton94Checkbox.isSelected());
        Main.prefs.put("fidelity", fidelityCheckbox.isSelected());
        Main.prefs.put("estereotempo", estereoTempoCheckbox.isSelected());
        Main.prefs.put("deletenzb", deleteNzbCheckbox.isSelected());
        Main.prefs.put("par2check", par2Checkbox.isSelected());
        Main.prefs.put("unrar", unrarCheckbox.isSelected());
        Main.prefs.put("ignoreerrors", ignoreErrorsCheckbox.isSelected());

        JDBC.setProgramOptions();
        System.out.println(Main.prefs.entrySet());
        this.setEnabled(false);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Options().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox closeToTrayCheckBox;
    private javax.swing.JCheckBox countryCheckbox;
    private javax.swing.JCheckBox danceclubCheckbox;
    private javax.swing.JCheckBox deleteNzbCheckbox;
    private javax.swing.JCheckBox estereoTempoCheckbox;
    private javax.swing.JCheckBox fidelityCheckbox;
    private javax.swing.JCheckBox hiphopCheckbox;
    private javax.swing.JCheckBox hot100Checkbox;
    private javax.swing.JCheckBox ignoreErrorsCheckbox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox kq105Checkbox;
    private javax.swing.JCheckBox laMegaCheckbox;
    private javax.swing.JCheckBox latinCheckbox;
    private javax.swing.JComboBox modeCombo;
    private javax.swing.JCheckBox notifyWeekCheckBox;
    private javax.swing.JCheckBox par2Checkbox;
    private javax.swing.JCheckBox popCheckbox;
    private javax.swing.JCheckBox reggaeton94Checkbox;
    private javax.swing.JCheckBox rockCheckbox;
    private javax.swing.JCheckBox tocaDeToCheckbox;
    private javax.swing.JCheckBox unrarCheckbox;
    // End of variables declaration//GEN-END:variables
}
