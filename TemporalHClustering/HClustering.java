package TemporalHClustering;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
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
   //private Map<Integer, List<IsolateSample>> isolateMap = null;
   private ArrayList<Cluster> clusters;
   private Cluster.distType clusterDistType;
   //private IterationResult clusterResults;
   private File dataFile = null;
   private double lowerThreshold, upperThreshold;

   //these variables are floating until I decide what to do with them
   private ArrayList<Dendogram> dendogram;
   private int tupleLength = 0;

   public HClustering() {
      clusters = new ArrayList<Cluster>();
      dendogram = new ArrayList<Dendogram>();
      lowerThreshold = 95;
      upperThreshold = 99.7;
   }

   public static void main(String[] args) {
      HClustering clusterer = new HClustering();

      //handle command line arguments; sets dataFile and threshold
      clusterer.parseArgs(args);

      //each point is a cluster, and we will combine two clusters in each iteration
      clusterer.clusterIsolates(dataFile, clusterDistType);


      BufferedWriter secretary = null, superSecretary = null;

      //outputs xml and cluster data
      for (Dendogram d : clusterer.dendogram) {
         File xmlOutDir = null;
         try {
            xmlOutDir = new File(String.format("HClusterResults/%s_%s_%.2f",
             clusterer.distanceMode.getClass().getName(), clusterer.clusterDistType, clusterer.threshold));

            if(!xmlOutDir.isDirectory()) {
               if (!xmlOutDir.mkdirs())
                  System.out.println("error creating directory");
            }

            superSecretary = new BufferedWriter(new FileWriter(new File(
             String.format("%s/%sClusterStatistics", xmlOutDir,
             clusterer.dataFile.getName().substring(0, clusterer.dataFile.getName().indexOf(".csv"))))));

            secretary = new BufferedWriter(new FileWriter(new File(String.format("%s/%s.xml", xmlOutDir,
             clusterer.dataFile.getName().substring(0, clusterer.dataFile.getName().indexOf(".csv"))))));

            secretary.write(d.getXML());
            secretary.close();
         }
         catch(Exception e1) {
            //System.out.println("Error writing cluster to file");
            e1.printStackTrace();
            System.exit(1);
         }

         if (clusterer.threshold > -1) {
            ArrayList<Dendogram> actualClusters = clusterer.applyThreshold(d);

            for (int dendNdx = 0; dendNdx < actualClusters.size(); dendNdx++) {
               try {
                  File outDir = new File(String.format("%s%s_%s_%s_%.2f", clusterer.relativeDir,
                   clusterer.dataFile.getName(),
                   clusterer.distanceMode.getClass().getName(), clusterer.clusterDistType, clusterer.threshold));

                  if(!outDir.isDirectory()) {
                     if (!outDir.mkdirs())
                        System.out.println("error creating directory");
                  }

                  secretary = new BufferedWriter(new FileWriter(new File(
                   String.format("%s/Cluster-%d", outDir, dendNdx))));
                  secretary.write(actualClusters.get(dendNdx).toCluster().toPlot() + "\n");
                  secretary.close();

                  superSecretary.write("cluster " + dendNdx + ": \n");
                  superSecretary.write(clusterer.clusterResults.clusterStats(dendNdx));
                  superSecretary.write(actualClusters.get(dendNdx).toCluster() + "\n");
               }
               catch(Exception e1) {
                  //System.out.println("Error writing cluster to file");
                  e1.printStackTrace();
                  System.exit(1);
               }
            }
            try {
               superSecretary.close();
            }
            catch(Exception e2) {
               e2.printStackTrace();
               System.exit(1);
            }
         }
      }

   }

   private ArrayList<Dendogram> applyThreshold(Dendogram root) {
      ArrayList<Dendogram> actualClusters = determineClusters(root);
      clusterResults = new IterationResult(tupleLength,
       actualClusters.size());

      clusterResults.centroids = getClusterCentroids(actualClusters);
      clusterResults.clusterCounts = getClusterCounts(actualClusters);
      clusterResults.clusterSumOfSquaredError = getSSEs(actualClusters);
      clusterResults.minDistance = getMinDists(actualClusters);
      clusterResults.maxDistance = getMaxDists(actualClusters);
      clusterResults.averageDistance = getAvgDists(actualClusters);

      return actualClusters;
   }

   /*
   private double[][] getClusterCentroids(ArrayList<Dendogram> clusters) {
      double[][] centroid = new double[clusters.size()][];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         centroid[clustNdx] = clusters.get(clustNdx).toCluster().getCentroid();
      }

      return centroid;
   }
   */

   private int[] getClusterCounts(ArrayList<Dendogram> clusters) {
      int[] counts = new int[clusters.size()];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         counts[clustNdx] = clusters.get(clustNdx).toCluster().size();
      }

      return counts;
   }

   /*
   private double[] getSSEs(ArrayList<Dendogram> clusters) {
      double[] SSEs= new double[clusters.size()];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         SSEs[clustNdx] = clusters.get(clustNdx).toCluster().getSSE();
      }

      return SSEs;
   }
   */

   private double[] getMinDists(ArrayList<Dendogram> clusters) {
      double[] min = new double[clusters.size()];
      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         min[clustNdx] = clusters.get(clustNdx).toCluster().minDist(distanceMode);
      }
      return min;
   }

   private double[] getMaxDists(ArrayList<Dendogram> clusters) {
      double[] max = new double[clusters.size()];
      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         max[clustNdx] = clusters.get(clustNdx).toCluster().maxDist(distanceMode);
      }
      return max;
   }

   private double[] getAvgDists(ArrayList<Dendogram> clusters) {
      double[] avgs = new double[clusters.size()];
      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         avgs[clustNdx] = clusters.get(clustNdx).toCluster().avgDist(distanceMode);
      }
      return avgs;
   }

   /*
   private ArrayList<Dendogram> determineClusters(Dendogram root) {
      ArrayList<Dendogram> clusterSet = new ArrayList<Dendogram>();

      if (root.getHeight() < threshold) {
         clusterSet.add(root);
         return clusterSet;
      }
      
      clusterSet.addAll(determineClusters(root.getLeft()));
      clusterSet.addAll(determineClusters(root.getRight()));

      return clusterSet;
   }
   */

   private List<Cluster> clusterIsolates(File dataFile, Cluster.distType type) {
      Map<Integer, List<IsolateSample>> isolateMap = null;
      List<Cluster> clusters = new ArrayList<Cluster>();

      if (dataFile != null) {
         IsolateFileParser parser = new IsolateFileParser(dataFile);

         isolateMap = parser.extractData();
      }

      for (int sampleDay : isolateMap.keySet()) {
         //Cluster the list of isolates in this day
         List<Cluster> currClusters = clusterIsolateList(isolateMap.get(sampleDay), type);

         //Cluster all previous days with this day
         clusters = clusterToDate(clusters, currClusters);
      }

      return clusters;
   }

   private List<Cluster> clusterIsolateList(List<IsolateSample> isolates, Cluster.distType type) {
      List<Cluster> clusters = new ArrayList<Cluster>();
      for (IsolateSample sample : isolates) {
         clusters.add(new Cluster(sample));
      }

      //variables to prepare for clustering the two closest clusters
      Point closeClusters = new Point(-1, -1);
      double minDist = Double.MAX_VALUE;
      boolean hasChanged;

      do {
         hasChanged = false;

         for (int clustOne = 0; clustOne < clusters.size(); clustOne++) {
            for (int clustTwo = clustOne + 1; clustTwo < clusters.size(); clustTwo++) {
               double clustDist = clusters.get(clustOne).distance(clusters.get(clustTwo));
               if (clustDist < minDist) {
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
         List<Cluster> newClusters = combineClusters(clusters, closeClusters);
         if (newClusters.size() != clusters.size()) {
            hasChanges = true;
            clusters = newClusters;
         }

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
   private List<Cluster> combineClusters(List<Cluster> clusters, Point minNdx) {
      ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
      //ArrayList<Dendogram> newDendogram = new ArrayList<Dendogram>();

      for (int clusterNdx = 0; clusterNdx < clusters.size(); clusterNdx++) {
         if (clusterNdx != (int) minNdx.getX() && clusterNdx != (int) minNdx.getY()) {
            newClusters.add(clusters.get(clusterNdx));
            //newDendogram.add(dendogram.get(clusterNdx));
         }

         else if (clusterNdx == (int) minNdx.getX()) {
            Cluster clusterOne = clusters.get(clusterNdx);
            Cluster clusterTwo = clusters.get(clusters.get((int) minNdx.getY()));

            /*
            Dendogram leftDend = dendogram.get(clusterNdx);
            Dendogram rightDend = dendogram.get((int) minNdx.getY());
            */

            newClusters.add(clusterOne.unionWith(clusterTwo));
            //newDendogram.add(new DendogramNode(minDist, leftDend, rightDend));
         }
      }

      //make the new cluster set into the current cluster set for next iteration
      return newClusters;
      //clusterer.dendogram = newDendogram;
   }

   private List<Cluster> clusterToDate(List<Cluster> clusters, List<Cluster> dailyClusters, Cluster.distType type) {
      //outer for loop loops over clusters in a day
      //inner for loop loops over clusters built up to the current day
      for (Cluster newCluster : dailyClusters) {
         double minDist = Double.MAX_VALUE;
         int closeCluster = -1;

         for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
            double clustDist = newCluster.distance(clusters.get(clustNdx), type);
            if (clustDist < minDist) {
               minDist = clustDist;
               closeCluster = clustNdx;
            }
         }

         //replace the cluster closest to the new Cluster with the
         //oldCluster U newCluster
         clusters.set(closeCluster, clusters.get(closeCluster).unionWith(newCluster));
      }

      return clusters;
   }

   private void parseArgs(String[] args) {
      if (args.length < 1 || args.length > 4) {
         System.out.println("Usage: java hclustering <Filename> [<lowerThreshold>] "+
          "[<upperThreshold>] [single|average|complete]");
         System.exit(1);
      }

      try {
         dataFile = new File(args[0]);
         lowerThreshold = args.length >= 2 ? Double.parseDouble(args[1]) : lowerThreshold;
         upperThreshold = args.length >= 3 ? Double.parseDouble(args[2]) : upperThreshold;

         //use reflection for distance measure
         /*
         distanceMode = args.length >= 3 ? 
          (DistanceMeasure) Class.forName(args[2]).newInstance() :
          new EuclideanDistanceMeasure();
          */

         clusterDistType = args.length >= 4 ?
          Cluster.distType.valueOf(args[3].toUpperCase()) : Cluster.distType.AVERAGE;

      }
      catch (NumberFormatException formatErr) {
         System.out.printf("Invalid threshold values: %d and %d\n", args[1], args[2]);
         System.exit(1);
      }
   }
}
