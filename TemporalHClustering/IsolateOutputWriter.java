package TemporalHClustering;

import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.ClusterDendogram;
import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.distanceMeasures.IsolateDistance;
import TemporalHClustering.dendogram.Dendogram;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

public class IsolateOutputWriter {

   public static void outputTemporalClusters(List<ClusterDendogram> clustDends, String filePrefix) {
      String outFileName = filePrefix + "_temporalDiagram.csv";
      String cytoFormat = "";

      int clusterNum = -1;
      for (ClusterDendogram clustDend : clustDends) {
         clusterNum++;
         //the new lines are to obviate the separation between clusters
         cytoFormat += clustDend.getCluster().toTemporalFormat(clusterNum);
      }

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(cytoFormat);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputCytoscapeFormat(List<ClusterDendogram> clustDends, String filePrefix) {
      String outFileName = filePrefix + "_cytoscapeNetwork.txt";
      String cytoFormat = Cluster.cytoscapeFormatHeader();

      int clusterNum = -1;
      for (ClusterDendogram clustDend : clustDends) {
         clusterNum++;
         //the new lines are to obviate the separation between clusters
         cytoFormat += "\n\n\n";
         cytoFormat += clustDend.getCluster().toCytoscapeCluster("Cluster_" + clusterNum);
         //cytoFormat += clustDend.getCluster().toCytoscapeFormat();
         cytoFormat += "\n\n\n";
      }

      try {
         File outputFile = new File(outFileName);
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(cytoFormat);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputClustersByDay(int sampleDay, List<ClusterDendogram> clustDends) {
      String outFileName = "ClusterDays/clustersByDay" + sampleDay + ".xml";
      String separator = "\n====================\n";
      String clusterOutput = "";

      for (ClusterDendogram clustDend : clustDends) {
         clusterOutput += clustDend.getDendogram().getXML() + separator;
      }

      clusterOutput += "\n\n\n\n";
      List<IsolateSample> isolateList = new ArrayList<IsolateSample>();

      for (ClusterDendogram clustDend : clustDends) {
         isolateList.addAll(clustDend.getCluster().getIsolates());
      }

      clusterOutput += "\t";
      for (int ndx = 0; ndx < isolateList.size(); ndx++) {
         clusterOutput += "\t" + isolateList.get(ndx);
      }
      clusterOutput += "\n";

      for (int ndxOne = 0; ndxOne < isolateList.size(); ndxOne++) {
         clusterOutput += isolateList.get(ndxOne);
         for (int ndxTwo = 0; ndxTwo < isolateList.size(); ndxTwo++) {
            clusterOutput += "\t" + IsolateDistance.findCorrelation(
             isolateList.get(ndxOne), isolateList.get(ndxTwo));
         }
         clusterOutput += "\n";
      }

      try {
         File outputFile = new File(outFileName);
         if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
         }
         if (!outputFile.exists()) {
            outputFile.createNewFile();
         }
         BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));

         fileWriter.write(clusterOutput);
         fileWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   public static void outputClusters(List<ClusterDendogram> clustDends,
    String outFileDir, String outputFileName) {
      BufferedWriter xmlWriter = null;
      String xmlOutput = "";

      List<Dendogram> dendogramList = new ArrayList<Dendogram>();
      for (ClusterDendogram clusterDend : clustDends) {
         dendogramList.add(clusterDend.getDendogram());
         //System.out.println(clusterDend.getCluster());
      }

      //System.out.println(DendogramNode.toUAGDot(dendogramList));

      for (ClusterDendogram clustDend : clustDends) {
         xmlOutput += clustDend.getDendogram().getXML() + "\n";
         //System.out.println("dendogram: " + clustDend.getDendogram().getXML());
      }

      //outputs xml and cluster data
      File xmlOutDir = null;

      try {
         xmlOutDir = new File(outFileDir);

         if(!xmlOutDir.isDirectory()) {
            if (!xmlOutDir.mkdirs()) {
               System.out.println("error creating directory");
               System.exit(1);
            }
         }

         xmlWriter = new BufferedWriter(new FileWriter(
          new File(outputFileName)));

         xmlWriter.write(xmlOutput);
         xmlWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }
}
