package TemporalHClustering.gui.dialogs;

//import TemporalHClustering.HClustering;
//import TemporalHClustering.gui.MainWindow;

import java.io.File;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import java.awt.Component;
import java.awt.Container;
import java.awt.ComponentOrientation;

import javax.swing.JDialog;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

public class InputDialog extends JDialog {
   private final int DIALOG_HEIGHT = 250, DIALOG_WIDTH = 475;
   private String recentlyAccessedDir = "";
   private Container mPane = null, mOwner = null;
   private Map<File, String> dataFileMap;

   private ButtonGroup firstDataRegion, secondDataRegion;
   private JTextField firstDataFile, secondDataFile;

   public InputDialog() {
      super();

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dataFileMap = new HashMap<File, String>();
      firstDataRegion = newRadioSelection();
      secondDataRegion = newRadioSelection();

      firstDataFile = new JTextField(20);
      secondDataFile = new JTextField(20);
   }

   public InputDialog(Frame owner, String title) {
      super(owner, title);

      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setResizable(false);

      mOwner = owner;

      mPane = this.getContentPane();
      mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
      mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dataFileMap = new HashMap<File, String>();
      firstDataRegion = newRadioSelection();
      secondDataRegion = newRadioSelection();

      firstDataFile = new JTextField(20);
      secondDataFile = new JTextField(20);
   }

   public static void main(String[] args) {
      InputDialog dialog = new InputDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      mPane.add(newFileField(firstDataRegion, firstDataFile));
      mPane.add(newFileField(secondDataRegion, secondDataFile));

      mPane.add(controls());

      mPane.validate();
   }

   public JPanel newFileField(ButtonGroup regionSelection, JTextField fileNameField) {
      JPanel radioSelection = new JPanel();
      JPanel fileInput = new JPanel();
      JPanel fileInputPanel = new JPanel();

      Enumeration radioButtons = regionSelection.getElements();

      radioSelection.setLayout(new BoxLayout(radioSelection, BoxLayout.Y_AXIS));

      radioSelection.add(new JLabel("ITS Region:"));
      while (radioButtons.hasMoreElements()) {
         radioSelection.add((JRadioButton) radioButtons.nextElement());
      }
      radioSelection.setAlignmentY(Component.CENTER_ALIGNMENT);

      fileInput.setLayout(new BoxLayout(fileInput, BoxLayout.Y_AXIS));

      fileInput.add(new JLabel("File:"));
      fileInput.add(fileNameField);

      fileInput.setAlignmentY(Component.CENTER_ALIGNMENT);


      fileInputPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      fileInputPanel.add(radioSelection);
      fileInputPanel.add(fileInput);
      fileInputPanel.add(newFileBrowseButton(fileNameField));

      fileInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

      return fileInputPanel;
   }

   public ButtonGroup newRadioSelection() {
      ButtonGroup radioSelection = new ButtonGroup();

      JRadioButton ITS_16_23 = new JRadioButton("16s-23s");
      JRadioButton ITS_23_5 = new JRadioButton("23s-5s");

      radioSelection.add(ITS_16_23);
      radioSelection.add(ITS_23_5);

      return radioSelection;
   }

   public JButton newFileBrowseButton(JTextField fileName) {
      final JTextField tmpFileField = fileName;
      JButton fileBrowse = new JButton("Browse");

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
               tmpFileField.setText(chooser.getSelectedFile().getAbsolutePath());
               recentlyAccessedDir = chooser.getSelectedFile().getPath();
            }
            else {
               System.out.println("Encountered Unknown Error");
               System.exit(0);
            }
         }
      });

      return fileBrowse;
   }

   public JPanel controls() {
      JPanel dialogControls = new JPanel();
      dialogControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

      JButton okayButton = new JButton("Okay");

      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            //basic validation
            System.out.println("calling HClustering...");
            
            //HClustering clusterer = new HClustering();

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
               JRadioButton firstRegion = (JRadioButton) firstDataRegion.getSelection();
               JRadioButton secondRegion = (JRadioButton) secondDataRegion.getSelection();

               args[0] = String.format("%s:%s", firstDataFile.getText(), firstRegion.getText());
               args[1] = String.format("%s:%s", secondDataFile.getText(), secondRegion.getText());
            }
            catch (NullPointerException emptyValErr) {
               //System.err.println("No file was selected");
               JOptionPane.showMessageDialog(mOwner,
                "No file was selected",
                "Invalid File", JOptionPane.ERROR_MESSAGE);
            }

            //Should write files somewhere
            /*
            if (clusterer.cluster(args)) {
               JOptionPane.showMessageDialog(mOwner,
                "Clustering complete",
                "Clustering completed", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
               JOptionPane.showMessageDialog(mOwner,
                "Error occurred while clustering Data",
                "Clustering Error", JOptionPane.ERROR_MESSAGE);
            }
            */

            System.out.println("HClustering completed");
            dispose();
         }
      });

      JButton cancelButton = new JButton("Cancel");

      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
            return;
         }
      });

      dialogControls.add(okayButton);
      dialogControls.add(cancelButton);
      dialogControls.setAlignmentX(Component.CENTER_ALIGNMENT);

      return dialogControls;
   }
}
