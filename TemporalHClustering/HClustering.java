package TemporalHClustering;

import TemporalHClustering.dataParser.IsolateFileParser;
import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.distanceMeasures.IsolateDistance;
import TemporalHClustering.dendogram.Dendogram;
import TemporalHClustering.dendogram.DendogramNode;
import TemporalHClustering.dendogram.DendogramLeaf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.awt.Point;

/*
 * Use LinkedHashMap<Integer, HashMap<String, double[]>> to represent data
 * Use HashMap<String, Integer> to represent each isolates index in the correlation matrix
 *
 * Foreach day in LinkedHashMap
 *    Cluster the isolates in the map for that day
 *    use HashMap to ensure correct correlation is used between pyrograms
 *
 *    after each isolate has been clustered for this day then
 *    cluster all clusters for this day with clusters from the previous day
 *
 * the day for a cluster will primarily determine the order in which it gets clustered overall
 *
 * the group of an isolate (f - fecal, i - immediate, l - later) determines a temporal
 *  distance between isolates (proximity is important)
 * the correlation between two isolates (may mirror, may not?) determine actual similarity
 *
 * when calculating distance between two clusters use euclidean distance between
 * (1, x) where 1 is compared to the correlation for the two clusters and x is
 * the distance between the two clusters' groups
 *
 */
public class HClustering {
   private Cluster.distType mClusterDistType;
   private double mLowerThreshold, mUpperThreshold;
   private File mDataFile = null;

   public HClustering() {
      mLowerThreshold = 95;
      mUpperThreshold = 99.7;
   }

   public static void main(String[] args) {
      HClustering clusterer = new HClustering();

      //handle command line arguments; sets dataFile and threshold
      clusterer.parseArgs(args);

      //each point is a cluster, and we will combine two clusters in each iteration
      List<ClusterDendogram> clustDends = clusterer.clusterIsolates(clusterer.mDataFile,
       clusterer.mLowerThreshold, clusterer.mUpperThreshold, clusterer.mClusterDistType);
      //System.err.println("clustDends length: " + clustDends.size());

      String outputFileDir = String.format("ClusterResults/%s_%.02f_%.02f",
       clusterer.mClusterDistType, clusterer.mLowerThreshold, clusterer.mUpperThreshold);

      clusterer.outputClusters(clustDends, outputFileDir);
   }

   private void outputClusters(List<ClusterDendogram> clustDends, String outFileDir) {
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
          new File(String.format("%s/%s.xml", xmlOutDir,
          mDataFile.getName().substring(0, mDataFile.getName().indexOf(".csv"))))));

         xmlWriter.write(xmlOutput);
         xmlWriter.close();
      }
      catch(Exception e1) {
         //System.out.println("Error writing cluster to file");
         e1.printStackTrace();
         System.exit(1);
      }
   }

   private List<ClusterDendogram> clusterIsolates(File dataFile, double lowerThreshold, double upperThreshold, Cluster.distType type) {
      Map<Integer, List<IsolateSample>> isolateMap = null;
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      if (dataFile != null) {
         IsolateFileParser parser = new IsolateFileParser(dataFile, lowerThreshold, upperThreshold);

         isolateMap = parser.extractData();
      }

      for (int sampleDay : isolateMap.keySet()) {
         //Cluster the list of isolates in this day
         List<ClusterDendogram> currClusters = clusterIsolateList(isolateMap.get(sampleDay), type);

         /*
         System.err.println("currClusters length: " + currClusters.size());
         /*
         for (ClusterDendogram clustDend : currClusters) {
            System.out.println(clustDend.getDendogram().getXML());
         }
         */

         //System.err.printf("on day %d there are a total of %d clusters", sampleDay, clusters.size());
         //Cluster all previous days with this day
         clusters = clusterToDate(clusters, currClusters, type);

      }

      return clusters;
   }

   private List<ClusterDendogram> clusterIsolateList(List<IsolateSample> isolates, Cluster.distType type) {
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      for (IsolateSample sample : isolates) {
         Cluster newCluster = new Cluster(sample);
         Dendogram newDendogram = new DendogramLeaf(sample);

         clusters.add(new ClusterDendogram(newCluster, newDendogram));
      }
      //System.out.printf("clusterList size: %d\n", clusters.size());

      //variables to prepare for clustering the two closest clusters
      Point closeClusters = new Point(-1, -1);
      double minDist = Double.MAX_VALUE;
      boolean hasChanged;

      /*
       * clustering within a day uses correlations and sampling methods for distance
       */
      do {
         hasChanged = false;

         //System.out.printf("IsolateList size: %d\n", isolates.size());
         for (int clustOne = 0; clustOne < clusters.size(); clustOne++) {
            for (int clustTwo = clustOne + 1; clustTwo < clusters.size(); clustTwo++) {
               Cluster cluster_A = clusters.get(clustOne).getCluster();
               Cluster cluster_B = clusters.get(clustTwo).getCluster();
               double clustDist = cluster_A.distance(cluster_B, type);

               //apply a threshold here
               //if (clustDist < minDist ) {
               if (clustDist < minDist && clustDist < 3.03 ) {
                  //System.out.printf("a minimum has been found at index <%d, %d>\n", clustOne, clustTwo);
                  //System.out.printf("clustOne:\n\t\t%s\nclustTwo:\n\t\t%s\n", cluster_A, cluster_B);
                  minDist = clustDist;
                  closeClusters = new Point(clustOne, clustTwo);
               }
            }
         }
         //System.out.printf("closeClusters: <%f, %f> dist: %.02f\n", closeClusters.getX(), closeClusters.getY(), minDist);

         /*
          * if newCluster list is a different sized then clearly two clusters were
          * combined. In this case set hasChanges to true and set the cluster list to
          * the new cluster list
          */
         List<ClusterDendogram> newClusters = combineClusters(clusters, closeClusters, minDist, type);
         //System.err.printf("newClusterList size: %d\n", newClusters.size());
         if (newClusters.size() != clusters.size()) {
            hasChanged = true;
            clusters = newClusters;
         }

         //reset various variables
         closeClusters = new Point(-1, -1);
         minDist = Double.MAX_VALUE;

         //for each sample cluster with other samples based on sample distance
         //sample distance should handle group and correlation distance

         //continue clustering until clusters do not change
      } while (hasChanged);

      return clusters;
   }

   /*
    * move clusters to new cluster list, when one of the clusters that will be merged
    * is found, merge it with the other cluster, then add to new cluster list
    * only do this for one cluster to be merged to avoid duplicates
    */
   private List<ClusterDendogram> combineClusters(List<ClusterDendogram> clusters, Point minNdx, double correlation, Cluster.distType type) {
      //System.out.printf("minNdx for cluster combining is <%f, %f>\n", minNdx.getX(), minNdx.getY());
      ArrayList<ClusterDendogram> newClusters = new ArrayList<ClusterDendogram>();
      //ArrayList<Dendogram> newDendogram = new ArrayList<Dendogram>();

      for (int clusterNdx = 0; clusterNdx < clusters.size(); clusterNdx++) {
         if (clusterNdx != (int) minNdx.getX() && clusterNdx != (int) minNdx.getY()) {
            newClusters.add(clusters.get(clusterNdx));
            //newDendogram.add(dendogram.get(clusterNdx));
         }

         else if (clusterNdx == (int) minNdx.getX()) {
            //using minNdx for cluster one for consistency and readability
            //System.out.printf("minNdx X: %d minNdx Y: %d clustersLength: %d", (int) minNdx.getX(), (int) minNdx.getY(), clusters.size());
            Cluster clusterOne = clusters.get((int) minNdx.getX()).getCluster();
            Cluster clusterTwo = clusters.get((int) minNdx.getY()).getCluster();
            Cluster combinedCluster = new Cluster(clusterOne.unionWith(clusterTwo));

            //using minNdx for dendogram one for consistency and readability
            Dendogram leftDend = clusters.get((int) minNdx.getX()).getDendogram();
            Dendogram rightDend = clusters.get((int) minNdx.getY()).getDendogram();
            Dendogram newDendogram = new DendogramNode(clusterOne.actualDistance(clusterTwo, type), leftDend, rightDend);

            newClusters.add(new ClusterDendogram(combinedCluster, newDendogram));
         }
      }

      //make the new cluster set into the current cluster set for next iteration
      return newClusters;
      //clusterer.dendogram = newDendogram;
   }

   //TODO check clusters vs dailyClusters, there's a problem in this method
   private List<ClusterDendogram> clusterToDate(List<ClusterDendogram> clusters,
    List<ClusterDendogram> dailyClusters, Cluster.distType type) {
      //System.out.printf("Clustering clusters (%d) with new day's clusters (%d)\n", clusters.size(), dailyClusters.size());

      //outer for loop loops over clusters in a day
      //inner for loop loops over clusters built up to the current day
      /*
       * clustering between days uses just correlations
       */
      for (ClusterDendogram newClusterDend : dailyClusters) {
         Cluster newCluster = newClusterDend.getCluster();

         double minDist = Double.MAX_VALUE;
         int closeClusterNdx = -1;

         for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
            Cluster currClust = clusters.get(clustNdx).getCluster();
            double clustDist = newCluster.corrDistance(currClust, type);

            if (clustDist < minDist && clustDist < mLowerThreshold) {
               minDist = clustDist;
               closeClusterNdx = clustNdx;
            }
         }

         //replace the cluster closest to the new Cluster with the
         //oldCluster U newCluster
         if (closeClusterNdx != -1) {
            Cluster closeCluster = clusters.get(closeClusterNdx).getCluster();
            Dendogram newDendogram = new DendogramNode(minDist, newClusterDend.getDendogram(),
             clusters.get(closeClusterNdx).getDendogram());
            ClusterDendogram newClustDend = new ClusterDendogram(
             closeCluster.unionWith(newCluster), newDendogram);

            clusters.set(closeClusterNdx, newClustDend);
         }
         else {
            clusters.add(newClusterDend);
         }
      }
      //System.out.printf("clusters combined into size %d\n", clusters.size());

      return clusters;
   }

   private void parseArgs(String[] args) {
      if (args.length < 1 || args.length > 4) {
         System.out.println("Usage: java hclustering <Filename> [<lowerThreshold>] "+
          "[<upperThreshold>] [single|average|complete]");
         System.exit(1);
      }

      try {
         mDataFile = new File(args[0]);
         mLowerThreshold = args.length >= 2 ? Double.parseDouble(args[1]) : mLowerThreshold;
         mUpperThreshold = args.length >= 3 ? Double.parseDouble(args[2]) : mUpperThreshold;

         //use reflection for distance measure
         /*
         distanceMode = args.length >= 3 ? 
          (DistanceMeasure) Class.forName(args[2]).newInstance() :
          new EuclideanDistanceMeasure();
          */

         mClusterDistType = args.length >= 4 ?
          Cluster.distType.valueOf(args[3].toUpperCase()) : Cluster.distType.AVERAGE;

      }
      catch (NumberFormatException formatErr) {
         System.out.printf("Invalid threshold values: %d and %d\n", args[1], args[2]);
         System.exit(1);
      }
   }

private class ClusterDendogram {
   private Cluster mCluster;
   private Dendogram mDendogram;

   public ClusterDendogram(Cluster cluster, Dendogram dendogram) {
      mCluster = cluster;
      mDendogram = dendogram;
   }

   public Cluster getCluster() {
      return mCluster;
   }

   public Dendogram getDendogram() {
      return mDendogram;
   }
}
}
