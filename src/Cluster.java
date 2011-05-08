import java.util.ArrayList;

public class Cluster {
   private ArrayList<double[]> dataPoints;
   private double[] centroid = null;
   private double sumSquaredError, minDist, maxDist, avgDist;

   public Cluster() {
      dataPoints = new ArrayList<double[]>();
      sumSquaredError = 0;
   }

   public Cluster(double[] firstPoint) {
      dataPoints = new ArrayList<double[]>();
      dataPoints.add(firstPoint);
      setCentroid(firstPoint);
   }

   public int size() {
      return dataPoints.size();
   }

   public Cluster(Cluster copyCluster) {
      dataPoints = new ArrayList<double[]>();

      for (double[] dataPoint : copyCluster.dataPoints) {
         this.dataPoints.add(dataPoint);
      }

      this.centroid = copyCluster.centroid;
   }

   public double[] getCentroid() {
      return centroid;
   }

   public void setCentroid(double[] centroid) {
      this.centroid = centroid;

      sumSquaredError = 0;
      for (double[] dataPoint : dataPoints) {
         double squaredError = 0;
         for (int dataNdx = 0; dataNdx < dataPoint.length; dataNdx++) {
            squaredError += Math.pow(dataPoint[dataNdx] - centroid[dataNdx], 2);
         }
         sumSquaredError += squaredError;
      }
   }

   public double minDist(DistanceMeasure dist) {
      double min = Double.MAX_VALUE;

      for (double[] dataPoint : dataPoints) {
         min = Math.min(min, dist.evaluateDistance(dataPoint, centroid));
      }

      return min;
   }

   public double maxDist(DistanceMeasure dist) {
      double max = 0;

      for (double[] dataPoint : dataPoints) {
         max = Math.max(max, dist.evaluateDistance(dataPoint, centroid));
      }

      return max;
   }

   public double avgDist(DistanceMeasure dist) {
      double total = 0;

      for (double[] dataPoint : dataPoints) {
         total += dist.evaluateDistance(dataPoint, centroid);
      }

      return total/dataPoints.size();
   }

   public void setSSE(double error) {
      sumSquaredError = error;
   }

   public double getSSE() {
      return sumSquaredError;
   }

   public Cluster addPoint(double[] newPoint) {
      dataPoints.add(newPoint);

      setCentroid(getNewCentroid(this.centroid, newPoint));

      return this;
   }

   public Cluster unionWith(Cluster otherCluster) {
      Cluster newCluster = new Cluster(this);

      for (double[] dataPoint : otherCluster.dataPoints) {
         newCluster.dataPoints.add(dataPoint);
      }

      if (this.centroid != null && otherCluster.centroid != null) {
         newCluster.setCentroid(getNewCentroid(this.centroid, otherCluster.centroid));
      }

      return newCluster;
   }

   public double distance(Cluster otherCluster, DistanceMeasure distMode, distType type) {
      double closestDist = -1;
      switch(type) {

         case SINGLE:
            closestDist = Double.MAX_VALUE;
            for (int dataNdx = 0; dataNdx < this.dataPoints.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.dataPoints.size();
                otherNdx++) {
                  closestDist = Math.min(closestDist, distMode.evaluateDistance(
                   this.dataPoints.get(dataNdx),
                   otherCluster.dataPoints.get(otherNdx)));
               }
            }
            break;

         case COMPLETE:
            for (int dataNdx = 0; dataNdx < this.dataPoints.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.dataPoints.size();
                otherNdx++) {
                  closestDist = Math.max(closestDist, distMode.evaluateDistance(
                   this.dataPoints.get(dataNdx),
                   otherCluster.dataPoints.get(otherNdx)));
               }
            }
            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;
            for (int dataNdx = 0; dataNdx < this.dataPoints.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.dataPoints.size();
                otherNdx++) {
                  totalDist += distMode.evaluateDistance(
                   this.dataPoints.get(dataNdx),
                   otherCluster.dataPoints.get(otherNdx));
                  totalSize++;
               }
            }
            closestDist = totalDist/totalSize;
            break;

         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
            break;

         case WARD:
            closestDist = this.unionWith(otherCluster).sumSquaredError -
             (this.sumSquaredError + otherCluster.sumSquaredError);
            break;
      }

      return closestDist;
   }

   public double[] getNewCentroid(double[] centroidOne, double[] centroidTwo) {
      double[] newCentroid = new double[centroidOne.length];

      if (centroidOne == null && centroidTwo != null)
         newCentroid = centroidTwo;
      else if (centroidOne != null && centroidTwo == null)
         newCentroid = centroidOne;
      else {
         for (int ndx = 0; ndx < centroidOne.length; ndx++) {
            newCentroid[ndx] = (centroidOne[ndx] + centroidTwo[ndx]) / 2;
         }
      }

      return newCentroid;
   }

   public String toString() {
      String str = "";

      for (int clusterNdx = 0; clusterNdx < dataPoints.size(); clusterNdx++) {
         double[] dataPoint = dataPoints.get(clusterNdx);
         str += "point " + clusterNdx + ": ";
         for (int dataNdx = 0; dataNdx < dataPoint.length; dataNdx++) {
            str += dataPoint[dataNdx] + ", ";
         }
         str = str.substring(0, str.length() - 2) + "\n";
      }

      return str;
   }

   public String toPlot() {
      String str = "";

      for (int clusterNdx = 0; clusterNdx < dataPoints.size(); clusterNdx++) {
         double[] dataPoint = dataPoints.get(clusterNdx);
         for (int dataNdx = 0; dataNdx < dataPoint.length; dataNdx++) {
            str += String.format("%.3f ", dataPoint[dataNdx]);
         }
         str = str + "\n";
      }

      return str;
   }

   public enum distType {
      SINGLE,
      COMPLETE,
      AVERAGE,
      CENTROID,
      WARD
   }
}
