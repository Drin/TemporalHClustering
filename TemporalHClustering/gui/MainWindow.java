package TemporalHClustering.gui;

import TemporalHClustering.gui.listeners.clusterListeners.ClusterSingleListener;
import TemporalHClustering.gui.listeners.dendogramListeners.DendogramListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MainWindow extends JFrame {
   private JMenuBar mainMenuBar;
   
   public MainWindow() {
      super("E.coli Clustering");

      mainMenuBar = new JMenuBar();
      
      //Menus that are on the Menu Bar
      JMenu fileMenu = new JMenu("File");
      JMenu clusterMenu = new JMenu("Cluster");
      JMenu dendogramMenu = new JMenu("Dendogram");
      //JMenu pyroMenu = new JMenu("PyroSequencing");

      //width is loosely calculated as number of characters in JMenu * 10
      setSize(new Dimension(250, 300));
       
      //Items that will go into the File menu
      JMenuItem importFile = new JMenuItem("View dendogram file");
      JMenuItem saveFile = new JMenuItem("Save As...");
      JMenuItem exitProgram = new JMenuItem("Exit");
       
      //Items that will go into the cluster menu
      JMenuItem clusterSingle = new JMenuItem("E.coli data file");

      //Items that will go into the dendogram menu
      JMenuItem thresholdDendogram = new JMenuItem("Apply dendogram threshold");
       
      //Listener for exit menu item
      exitProgram.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            System.exit(0);
         }
      });
       
      //Listener for view dendogram menu item
      /*
      ComparePyrogramsListener libCompareListener = new ComparePyrogramsListener();
      libCompareListener.setOwner(this);
      libCompare.addActionListener(libCompareListener);
      */

      //Listener for save as menu item
      /*
      CompareXMLPyrogramsListener pyroCompareListener = new CompareXMLPyrogramsListener();
      pyroCompareListener.setOwner(this);
      pyroCompare.addActionListener(pyroCompareListener);
      */
       
      //Listener for clusterSingle menu item
      ClusterSingleListener clustSingleListener = new ClusterSingleListener(this);
      clusterSingle.addActionListener(clustSingleListener);

      //Listener for thresholdDendogram menu item
      DendogramListener dendThresholdListener = new DendogramListener(this);
      thresholdDendogram.addActionListener(dendThresholdListener);
       
      //Listener for clusterMany menu item
      /*
      DisplayXMLPyrogramListener pyroDisplayListener = new DisplayXMLPyrogramListener();
      pyroDisplayListener.setOwner(this);
      pyroDisplay.addActionListener(pyroDisplayListener);
      */
       
      //adding Items to their Menus
      fileMenu.add(importFile);
      fileMenu.add(saveFile);
      fileMenu.add(exitProgram);
       
      clusterMenu.add(clusterSingle);

      dendogramMenu.add(thresholdDendogram);

      //adding Menus to the MenuBar
      mainMenuBar.add(fileMenu);
      mainMenuBar.add(clusterMenu);
      mainMenuBar.add(dendogramMenu);
       
      setJMenuBar(mainMenuBar);
      //setSize(1000, 760);
      validate();
   }
   
   public void showWindow() {
      setVisible(true);
      repaint();
   }
}
