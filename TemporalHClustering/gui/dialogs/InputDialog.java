package TemporalHClustering.gui.dialogs;

import TemporalHClustering.HClustering;
import TemporalHClustering.gui.MainWindow;

import java.io.File;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
   private final int DIALOG_HEIGHT = 325, DIALOG_WIDTH = 475;
   private String recentlyAccessedDir = "", mArgDelim = null;
   private Container mPane = null, mOwner = null;
   private Map<File, String> dataFileMap;

   private ButtonGroup firstDataRegion, secondDataRegion;
   private JTextField firstDataFile, secondDataFile;
   private JTextField firstDataThreshold, secondDataThreshold, globalThreshold;

   public InputDialog() {
      super();
      mArgDelim = HClustering.getArgSeparator();

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

      firstDataThreshold = new JTextField("99.7", 20);
      secondDataThreshold = new JTextField("99.7", 20);
      globalThreshold = new JTextField("99.7", 20);
   }

   public InputDialog(Frame owner, String title) {
      super(owner, title);
      mArgDelim = HClustering.getArgSeparator();

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

      firstDataThreshold = new JTextField("99.7", 20);
      secondDataThreshold = new JTextField("99.7", 20);
      globalThreshold = new JTextField("99.7", 20);
   }

   public static void main(String[] args) {
      InputDialog dialog = new InputDialog();

      dialog.init();
      dialog.setVisible(true);
   }

   public void init() {
      mPane.add(newGlobalThreshold(globalThreshold));

      mPane.add(new JLabel("Input Dataset"));
      mPane.add(newFileField(firstDataRegion, firstDataFile, firstDataThreshold));

      mPane.add(new JLabel("Input Dataset"));
      mPane.add(newFileField(secondDataRegion, secondDataFile, secondDataThreshold));

      mPane.add(controls());

      mPane.validate();
   }

   public JPanel newGlobalThreshold(JTextField thresholdText) {
      JPanel thresholdField = new JPanel();

      thresholdField.setLayout(new BoxLayout(thresholdField, BoxLayout.X_AXIS));

      thresholdField.add(new JLabel("Clustering Threshold"));
      thresholdField.add(thresholdText);
      thresholdField.setAlignmentX(Component.CENTER_ALIGNMENT);

      return thresholdField;
   }

   public JPanel newFileField(ButtonGroup regionSelection, JTextField fileNameField, JTextField thresholdField) {
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
      fileInput.add(new JLabel("Threshold:"));
      fileInput.add(thresholdField);

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

            /*
             * args for clusterer consist of:
             *    String filename - a file name
             *    String regionname - name of the ITS region represented
             *    String lowerThreshold - a double value
             *    String upperThreshold - a double value
             *    one option from:
             *       Single
             *       Average
             *       Complete
             *       Ward
             * argument 1 (filename) is *MANDATORY*
             * argument 2 (region name) is *MANDATORY*
             * argument 2 defaults to 95% similarity
             * argument 3 defaults to 99.7% similarity
             * argument 4 defaults to Average similarity distance
             */

            /*
             * preparing arguments for clusterer
             */
            ArrayList<String> args = new ArrayList<String>();

            try {
               HClustering.setDistanceThreshold(Double.parseDouble(globalThreshold.getText()));
               double firstThreshold = Double.parseDouble(firstDataThreshold.getText());
               double secondThreshold = Double.parseDouble(secondDataThreshold.getText());
               
               JRadioButton firstRegion = null, secondRegion = null;
               Enumeration regionSelection = firstDataRegion.getElements();

               while (regionSelection.hasMoreElements()) {
                  JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

                  if (tmpRadio.isSelected()) {
                     firstRegion = tmpRadio;
                  }
               }

               regionSelection = secondDataRegion.getElements();

               while (regionSelection.hasMoreElements()) {
                  JRadioButton tmpRadio = (JRadioButton) regionSelection.nextElement();

                  if (tmpRadio.isSelected()) {
                     secondRegion = tmpRadio;
                  }
               }

               if (firstRegion != null && secondRegion != null &&
                firstRegion.getText().equals(secondRegion.getText())) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid regions selected: Please select different regions for each input data",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);
                  return;
               }
               if (firstDataFile.getText().equals(secondDataFile.getText())) {
                  int selectedOption = JOptionPane.showConfirmDialog(mOwner,
                   "Same file input for both regions. Are you sure?",
                   "Duplicate input confirmation", JOptionPane.YES_NO_OPTION);

                  if (selectedOption == JOptionPane.NO_OPTION) {
                     JOptionPane.showMessageDialog(mOwner,
                      "Clustering cancelled",
                      "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                     return;
                  }
               }
               
               if (firstThreshold < 1 || secondThreshold < 1) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid threshold values",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);
                  return;
               }

               if (!firstDataFile.getText().equals("") && firstRegion != null) {
                  args.add(String.format("%s%s%s%s%.03f", firstDataFile.getText(),
                   mArgDelim, firstRegion.getText(), mArgDelim, firstThreshold));
               }
               else if (!firstDataFile.getText().equals("") || firstRegion != null) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid parameters for first data input",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);

                  return;
               }

               if (!secondDataFile.getText().equals("") && secondRegion != null) {
                  args.add(String.format("%s%s%s%s%.03f", secondDataFile.getText(),
                   mArgDelim, secondRegion.getText(), mArgDelim, secondThreshold));
               }
               else if (!secondDataFile.getText().equals("") || secondRegion != null) {
                  JOptionPane.showMessageDialog(mOwner,
                   "Invalid parameters for second data input",
                   "Invalid Options", JOptionPane.ERROR_MESSAGE);

                  return;
               }
            }
            catch (NullPointerException emptyValErr) {
               //System.err.println("No file was selected");
               JOptionPane.showMessageDialog(mOwner,
                "No file was selected",
                "Invalid File", JOptionPane.ERROR_MESSAGE);
            }

            //Should write files somewhere
            String[] argsArr = new String[args.size()];
            args.toArray(argsArr);

            System.out.println("calling HClustering...");
            
            HClustering clusterer = new HClustering(args.size());

            if (clusterer.cluster(argsArr)) {
               JOptionPane.showMessageDialog(mOwner,
                "Clustering complete",
                "Clustering completed", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
               JOptionPane.showMessageDialog(mOwner,
                "Error occurred while clustering Data",
                "Clustering Error", JOptionPane.ERROR_MESSAGE);

               return;
            }

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
