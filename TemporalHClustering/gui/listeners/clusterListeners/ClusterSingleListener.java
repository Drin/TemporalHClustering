package TemporalHClustering.gui.listeners.clusterListeners;

import TemporalHClustering.HClustering;
import TemporalHClustering.gui.MainWindow;

import java.io.File;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ClusterSingleListener implements ActionListener {
   private MainWindow mainFrame;
   private JDialog clusterDialog;
   private JTextField fileName;
   private JButton okayButton, cancelButton, fileBrowse;
   private String recentlyAccessedDir = "";

   public ClusterSingleListener(MainWindow parentFrame) {
      super();
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(ActionEvent e) {
      System.out.println("action performed!");
      clusterDialog = new JDialog(mainFrame, "Cluster Single File");
      
      clusterDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      clusterDialog.setMinimumSize(new Dimension(475,350));
      clusterDialog.setResizable(false);

      /*
       * Components to be added to Dialog
       */

      //File containing E.coli dissimilarity matrix
      JLabel fileNameLabel = new JLabel("E.coli data file:");
      fileName = new JTextField();
      
      //file browse button
      fileBrowse = new JButton("Browse");
      fileBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //Obtains the file name of the input from the file chooser
            JFileChooser chooser = new JFileChooser();
            //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (recentlyAccessedDir != "") {
               File curDir = new File(recentlyAccessedDir);
               chooser.setCurrentDirectory(curDir);
            }
            
            int returnVal = chooser.showOpenDialog(chooser);
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
               System.out.println("cancelled");
            }
            else if (returnVal == JFileChooser.APPROVE_OPTION) {
               fileName.setText(chooser.getSelectedFile().getAbsolutePath());
               recentlyAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });
      
      //cancel and okay buttons at bottom
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileName.setText("");
            clusterDialog.dispose();
            return;
         }
      });
      
      okayButton = new JButton("Okay");
      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            //basic validation
            System.out.println("calling HClustering...");
            
            HClustering clusterer = new HClustering();

            /*
             * args for clusterer consist of:
             *    String filename - a file name
             *    String lowerThreshold - a double value
             *    String upperThreshold - a double value
             *    one option from:
             *       Single
             *       Average
             *       Complete
             *       Ward
             * argument 1 (filename) is *MANDATORY*
             * argument 2 defaults to 95% similarity
             * argument 3 defaults to 99.7% similarity
             * argument 4 defaults to Average similarity distance
             */

            /*
             * preparing arguments for clusterer
             */
            int numArgsDefined = 1; //change this depending on the try catch below: 1 is the minimum
            String[] args = new String[1];

            try {
               args[0] = fileName.getText();
            }
            catch (NullPointerException emptyValErr) {
               //System.err.println("No file was selected");
               JOptionPane.showMessageDialog(mainFrame,
                "No file was selected",
                "Invalid File", JOptionPane.ERROR_MESSAGE);
            }

            //Should write files somewhere
            if (clusterer.cluster(args)) {
               JOptionPane.showMessageDialog(mainFrame,
                "Clustering complete",
                "Clustering completed", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
               JOptionPane.showMessageDialog(mainFrame,
                "Error occurred while clustering Data",
                "Clustering Error", JOptionPane.ERROR_MESSAGE);
            }
            clusterDialog.dispose();
         }
      });

      /*
       * Adding all initialized components
       */
      clusterDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      clusterDialog.add(fileNameLabel);
      clusterDialog.add(fileName);
      clusterDialog.add(fileBrowse);
      clusterDialog.add(cancelButton);
      clusterDialog.add(okayButton);
   }
}
