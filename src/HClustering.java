import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.File;
import java.awt.Point;

public class HClustering {
   private ArrayList<ArrayList<Double>> distancesMatrix;
   private ArrayList<Cluster> clusters;
   private ArrayList<Dendogram> dendogram;
   private Cluster.distType clusterDistType;
   private DistanceMeasure distanceMode = null;
   private IterationResult clusterResults;
   private File dataFile = null;
   private final String relativeDir = "plotdata/HClustering/";
   private int tupleLength = 0;
   private double threshold;

   public HClustering() {
      distancesMatrix = new ArrayList<ArrayList<Double>>();
      clusters = new ArrayList<Cluster>();
      dendogram = new ArrayList<Dendogram>();
      threshold = -1;
   }

   public static void main(String[] args) {
      HClustering clusterer = new HClustering();

      //handle command line arguments; sets dataFile and threshold
      clusterer.parseArgs(args);
      //each point is a cluster, and we will combine two clusters in each iteration
      clusterer.createInitialClusters();

      //repeat until all clusters have been clustered into one
      while (clusterer.clusters.size() > 1) {

         //populate distancesMatrix
         for (int distRow = 0; distRow < clusterer.clusters.size(); distRow++) {
            ArrayList<Double> distancesRow = new ArrayList<Double>();

            for (int distCol = 0; distCol < distRow; distCol++) {
               if (clusterer.distanceMode != null) {
                  distancesRow.add(clusterer.clusters.get(distRow).distance(
                   clusterer.clusters.get(distCol),
                   clusterer.distanceMode, clusterer.clusterDistType));
               }
            }

            clusterer.distancesMatrix.add(distancesRow);
         }


         //(s, r) = index of minimum distance value(cluster s, cluster r);
         double minDist = Double.MAX_VALUE;
         //x-value of point represents row, y-value represents column
         Point minNdx = new Point(-1, -1);
         for (int row = 0; row < clusterer.distancesMatrix.size(); row++) {
            for (int col = 0; col < clusterer.distancesMatrix.get(row).size(); col++) {
               if (clusterer.distancesMatrix.get(row).get(col) < minDist) {
                  minDist = clusterer.distancesMatrix.get(row).get(col);
                  minNdx.setLocation(row, col);
               }
            }
         }
         //so that it can be re-used on next iteration
         clusterer.distancesMatrix.clear();


         //move clusters to new cluster list, when one of the clusters that will be merged
         //is found, merge it with the other cluster, then add to new cluster list
         //only do this for one cluster to be merged to avoid duplicates
         ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
         ArrayList<Dendogram> newDendogram = new ArrayList<Dendogram>();
         for (int clusterNdx = 0; clusterNdx < clusterer.clusters.size(); clusterNdx++) {

            if (clusterNdx != (int) minNdx.getX() && clusterNdx != (int) minNdx.getY()) {
               newClusters.add(clusterer.clusters.get(clusterNdx));
               newDendogram.add(clusterer.dendogram.get(clusterNdx));
            }

            else if (clusterNdx == (int) minNdx.getX()) {
               newClusters.add(clusterer.clusters.get(clusterNdx).unionWith(
                clusterer.clusters.get((int) minNdx.getY())));
               newDendogram.add(new DendogramNode(minDist,
                clusterer.dendogram.get(clusterNdx),
                clusterer.dendogram.get((int) minNdx.getY())));
            }

         }
         //make the new cluster set into the current cluster set for next iteration
         clusterer.clusters = newClusters;
         clusterer.dendogram = newDendogram;
      }

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

   private double[][] getClusterCentroids(ArrayList<Dendogram> clusters) {
      double[][] centroid = new double[clusters.size()][];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         centroid[clustNdx] = clusters.get(clustNdx).toCluster().getCentroid();
      }

      return centroid;
   }

   private int[] getClusterCounts(ArrayList<Dendogram> clusters) {
      int[] counts = new int[clusters.size()];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         counts[clustNdx] = clusters.get(clustNdx).toCluster().size();
      }

      return counts;
   }

   private double[] getSSEs(ArrayList<Dendogram> clusters) {
      double[] SSEs= new double[clusters.size()];

      for (int clustNdx = 0; clustNdx < clusters.size(); clustNdx++) {
         SSEs[clustNdx] = clusters.get(clustNdx).toCluster().getSSE();
      }

      return SSEs;
   }

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

   private void createInitialClusters() {
      //foreach datapoint in the data, create a cluster containing that point
      if (dataFile != null) {
         CsvParser parser = new CsvParser(dataFile);

         for (double[] tuple : parser.extractData()) {
            tupleLength = tuple.length;
            clusters.add(new Cluster(tuple));
            dendogram.add(new DendogramLeaf(tuple));
         }
      }
   }

   private void parseArgs(String[] args) {
      if (args.length < 1 || args.length > 4) {
         System.out.println("Usage: java hclustering <Filename> [<threshold>] [EuclideanDistanceMeasure] [single|average|complete]");
         System.exit(1);
      }

      try {
         dataFile = new File(args[0]);
         threshold = args.length >= 2 ? Double.parseDouble(args[1]) : -1;

         //use reflection for distance measure
         distanceMode = args.length >= 3 ? 
          (DistanceMeasure) Class.forName(args[2]).newInstance() :
          new EuclideanDistanceMeasure();

         clusterDistType = args.length >= 4 ?
          Cluster.distType.valueOf(args[3].toUpperCase()) : Cluster.distType.AVERAGE;
      }
      catch (NumberFormatException e2) {
         System.out.println("Invalid threshold value: " + args[1]);
         System.exit(1);
      }
      catch (ClassNotFoundException e3) {
         System.out.println("Invalid distance measure: " + args[2]);
         System.exit(1);
      }
      catch (InstantiationException e4) {
         System.out.println("Could not instantiate class: " + args[2]);
         System.exit(1);
      }
      catch (IllegalAccessException e5) {
         System.out.println("Could not access class: " + args[2]);
         System.exit(1);
      }

   }
}
