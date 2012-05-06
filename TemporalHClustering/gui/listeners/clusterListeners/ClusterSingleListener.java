package TemporalHClustering.gui.listeners.clusterListeners;

import TemporalHClustering.HClustering;
import TemporalHClustering.gui.MainWindow;
import TemporalHClustering.gui.dialogs.InputDialog;

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
import javax.swing.BoxLayout;

public class ClusterSingleListener implements ActionListener {
   private MainWindow mainFrame;
   private InputDialog clusterDialog;

   public ClusterSingleListener(MainWindow parentFrame) {
      super();
      this.mainFrame = parentFrame;
   }
   
   public void actionPerformed(ActionEvent e) {
      clusterDialog = new InputDialog(mainFrame, "Input Cluster Parameters");

      clusterDialog.init();
      clusterDialog.setVisible(true);
   }
}
