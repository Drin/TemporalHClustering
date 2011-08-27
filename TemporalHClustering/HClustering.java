package TemporalHClustering;

import TemporalHClustering.dataParser.IsolateFileParser;
import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.ClusterDendogram;
import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.distanceMeasures.IsolateDistance;
import TemporalHClustering.dendogram.Dendogram;
import TemporalHClustering.dendogram.DendogramNode;
import TemporalHClustering.dendogram.DendogramLeaf;
import TemporalHClustering.IsolateOutputWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

      String outputFileName = String.format("%s/%s.xml", outputFileDir,
       clusterer.mDataFile.getName().substring(0,
       clusterer.mDataFile.getName().indexOf(".csv")));

      IsolateOutputWriter.outputClusters(clustDends, outputFileDir, outputFileName);
      IsolateOutputWriter.outputCytoscapeFormat(clustDends);
      IsolateOutputWriter.outputTemporalClusters(clustDends);
   }

   private List<ClusterDendogram> clusterIsolates(File dataFile, double lowerThreshold, double upperThreshold, Cluster.distType type) {
      //mappings represent days to isolates
      Map<Integer, List<IsolateSample>> isolateMap = null;
      //list of all constructed clusters
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      if (dataFile != null) {
         IsolateFileParser parser = new IsolateFileParser(dataFile, lowerThreshold, upperThreshold);

         isolateMap = parser.extractData();
      }

      for (int sampleDay : isolateMap.keySet()) {
         //Cluster the list of isolates in this day
         List<ClusterDendogram> currClusters = clusterIsolateList(isolateMap.get(sampleDay), type);

         IsolateOutputWriter.outputClustersByDay(sampleDay, currClusters);
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
      //represent fecal samples
      List<ClusterDendogram> clusterF = new ArrayList<ClusterDendogram>();
      //represent immediate (after) samples
      List<ClusterDendogram> clusterI = new ArrayList<ClusterDendogram>();
      //represent later samples
      List<ClusterDendogram> clusterL = new ArrayList<ClusterDendogram>();

      //clusters resulting from clustering the above clusters will be placed in
      //clusters and this will prevent me from having to refactor the rest of this
      //method.
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      for (IsolateSample sample : isolates) {
         Cluster newCluster = new Cluster(sample);
         Dendogram newDendogram = new DendogramLeaf(sample);

         switch(sample.getSampleMethod()) {
            case FECAL:
               clusterF.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case IMM:
               clusterI.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            case LATER:
               clusterL.add(new ClusterDendogram(newCluster, newDendogram));
               break;
            default:
               System.err.println("serious error here");
               break;
         }
      }
      //System.out.printf("clusterList size: %d\n", clusters.size());

      //cluster within each group
      clusterF = clusterGroup(clusterF, type);
      clusterI = clusterGroup(clusterI, type);
      clusterL = clusterGroup(clusterL, type);


      //cluster each group together:
      //F and I together first since they are the closest in time
      //F_I and L next since they are the next closest in time

      //was going to use "clusterAcrossGroup" but there seemed to be a lot of
      //logical traps such as where to put clusters that are combined and all of
      //the problems that followed from that
      clusters.addAll(clusterF);
      clusters.addAll(clusterI);
      clusters = clusterGroup(clusters, type);

      clusters.addAll(clusterL);
      clusters = clusterGroup(clusters, type);

      //clusters within all the day's clusters
      //based on the above clusterGroup call this would likely be repetitive
      //clusters = clusterGroup(clusters, type);

      return clusters;
   }

   private List<ClusterDendogram> clusterGroup(List<ClusterDendogram> clusters, Cluster.distType type) {
      Point closeClusters = new Point(-1, -1);
      //double minDist = Double.MAX_VALUE;
      double minDist = 0;
      boolean hasChanged;

      do {
         hasChanged = false;

         for (int clustOne = 0; clustOne < clusters.size(); clustOne++) {
            for (int clustTwo = clustOne + 1; clustTwo < clusters.size(); clustTwo++) {
               Cluster cluster_A = clusters.get(clustOne).getCluster();
               Cluster cluster_B = clusters.get(clustTwo).getCluster();
               //double clustDist = cluster_A.distance(cluster_B, type);

               //this will ensure that i'm only comparing based on correlations
               double clustDist = cluster_A.corrDistance(cluster_B, type);
               /*
               if (clustDist > 1) {
                  System.err.println("cluster group clustDist: " + clustDist + " between " + cluster_A + " and " + cluster_B);
               }
               */

               //System.out.println("ward's distance: " + clustDist);
               //if (clustDist < minDist && clustDist > 99.7 ) {
               //if (clustDist < minDist && clustDist < .03 ) {
               //if (clustDist < minDist && clustDist > 99.7 ) { this
               //corresponds to results used in paper
               //TODO  investigate the results for when you use '> minDist'
               if (/*clustDist > minDist &&*/ clustDist > 99.7 ) {
                  //this is equivalent to previous line unless '> minDist' is uncommented.
                  minDist = clustDist;
                  closeClusters = new Point(clustOne, clustTwo);
               }
            }
         }

         /*
          * if newCluster list is a different sized then clearly two clusters were
          * combined. In this case set hasChanges to true and set the cluster list to
          * the new cluster list
          */
         List<ClusterDendogram> newClusters = combineClusters(clusters, closeClusters, minDist, type);

         if (newClusters.size() != clusters.size()) {
            hasChanged = true;
            clusters = newClusters;
         }

         //reset various variables
         closeClusters = new Point(-1, -1);
         minDist = Double.MAX_VALUE;

         //continue clustering until clusters do not change
      } while (hasChanged);

      return clusters;
   }

   /*
    * I think this adds too many logical errors.
   private List<ClusterDendogram> clusterAcrossGroups(List<ClusterDendogram> groupOne,
    List<ClusterDendogram> groupTwo) {
      List<ClusterDendogram> clusters = new ArrayList<ClusterDendogram>();

      Point closeClusters = new Point(-1, -1);
      double minDist = Double.MAX_VALUE;
      boolean hasChanged;

      while (!groupOne.isEmpty() && !groupTwo.isEmpty()) {
         if (groupOne.isEmpty()) {
            for (ClusterDendogram clustDend : groupTwo) {
               clusters.add(clustDend);
            }
            break;
         }

         else if (groupTwo.isEmpty()) {
            for (ClusterDendogram clustDend : groupOne) {
               clusters.add(clustDend);
            }
            break;
         }

         for (int clustOne = 0; clustOne < groupOne.size(); clustOne++) {
            for (int clustTwo = 0; clustTwo < groupTwo.size(); clustTwo++) {
               Cluster cluster_A = groupOne.get(clustOne).getCluster();
               Cluster cluster_B = groupTwo.get(clustTwo).getCluster();
               double clustDist = cluster_A.distance(cluster_B, type);
               System.err.println("cross group clustDist: " + clustDist);

               if (clustDist < minDist && clustDist < .03 ) {
                  minDist = clustDist;
                  closeClusters = new Point(clustOne, clustTwo);
               }
            }
         }

         if (((int) closeClusters.getX()) != -1 && ((int) closeClusters.getY()) != -1) {
            //using closeClusters for cluster one for consistency and readability
            //System.out.printf("closeClusters X: %d closeClusters Y: %d clustersLength: %d", (int) closeClusters.getX(), (int) closeClusters.getY(), clusters.size());
            Cluster clusterOne = groupOne.get((int) closeClusters.getX()).getCluster();
            Cluster clusterTwo = groupTwo.get((int) closeClusters.getY()).getCluster();
            Cluster combinedCluster = new Cluster(clusterOne.unionWith(clusterTwo));

            //using closeClusters for dendogram one for consistency and readability
            Dendogram leftDend = groupOne.get((int) closeClusters.getX()).getDendogram();
            Dendogram rightDend = groupTwo.get((int) closeClusters.getY()).getDendogram();
            Dendogram newDendogram = new DendogramNode(clusterOne.actualDistance(clusterTwo, type), leftDend, rightDend);

            clusters.add(new ClusterDendogram(combinedCluster, newDendogram));
            groupOne.set((int) closeClusters.getX(), 

            //possible that multiple clusters in groupTwo will cluster with a cluster in groupOne
            //groupOne.remove((int) closeClusters.getX());
            groupTwo.remove((int) closeClusters.getY());
         }

         //reset various variables
         closeClusters = new Point(-1, -1);
         minDist = Double.MAX_VALUE;

         //continue clustering until clusters do not change
      }

      return clusters;
   }
   */

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
            //Dendogram newDendogram = new DendogramNode(clusterOne.actualDistance(clusterTwo, type), leftDend, rightDend);
            Dendogram newDendogram = new DendogramNode(clusterOne.corrDistance(clusterTwo, type), leftDend, rightDend);

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

            //if (clustDist < minDist && clustDist < mLowerThreshold) {
            //System.out.println("cluster to date ward's distance: " + clustDist);
            if (clustDist < minDist && clustDist >= mUpperThreshold) {
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
          "[<upperThreshold>] [single|average|complete|ward]");
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

}
