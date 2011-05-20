package TemporalHClustering.dataTypes;

import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.distanceMeasures.IsolateDistance;

import java.util.List;
import java.util.ArrayList;

//TODO it is possible to represent each data point (isolateSample)
//as a point in n-dimensional space where n is the number of other isolates
//it has correlations (or any distance) to. This would use a full
//correlation matrix as opposed to the half correlation matrix that is
//provided in the data file. But certainly something to keep in mind.
public class Cluster {
   private ArrayList<IsolateSample> isolates;
   //private double[] centroid = null;
   private double sumSquaredError, minDist, maxDist, avgDist;

   public Cluster() {
      isolates = new ArrayList<IsolateSample>();
      sumSquaredError = 0;
   }

   public Cluster(IsolateSample firstIsolate) {
      isolates = new ArrayList<IsolateSample>();
      isolates.add(firstIsolate);
      //setCentroid(firstIsolate);
   }

   public Cluster(Cluster copyCluster) {
      isolates = new ArrayList<IsolateSample>();

      for (IsolateSample sample : copyCluster.isolates) {
         this.isolates.add(sample);
      }

      //this.centroid = copyCluster.centroid;
   }

   public int size() {
      return isolates.size();
   }

   public List<IsolateSample> getIsolates() {
      return isolates;
   }

   /*
   public double[] getCentroid() {
      return centroid;
   }

   public void setCentroid(double[] centroid) {
      this.centroid = centroid;

      sumSquaredError = 0;
      for (double[] dataPoint : isolates) {
         double squaredError = 0;
         for (int dataNdx = 0; dataNdx < dataPoint.length; dataNdx++) {
            squaredError += Math.pow(dataPoint[dataNdx] - centroid[dataNdx], 2);
         }
         sumSquaredError += squaredError;
      }
   }
   */

   public double minDist() {
      double min = Double.MAX_VALUE;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            min = Math.min(min, isolates.get(sampleOne).compareTo(isolates.get(sampleTwo)));
         }
      }

      return min;
   }

   public double maxDist() {
      double max = 0;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            max = Math.max(max, isolates.get(sampleOne).compareTo(isolates.get(sampleTwo)));
         }
      }

      return max;
   }

   public double avgDist() {
      double total = 0;

      for (int sampleOne = 0; sampleOne < isolates.size(); sampleOne++) {
         for (int sampleTwo = sampleOne + 1; sampleTwo < isolates.size(); sampleTwo++) {
            total += isolates.get(sampleOne).compareTo(isolates.get(sampleTwo));
         }
      }

      return total/isolates.size();
   }

   /*
    * not sure these apply
   public void setSSE(double error) {
      sumSquaredError = error;
   }

   public double getSSE() {
      return sumSquaredError;
   }
   */

   public Cluster addIsolate(IsolateSample newSample) {
      isolates.add(newSample);

      return this;
   }

   public Cluster unionWith(Cluster otherCluster) {
      Cluster newCluster = new Cluster(this);

      for (IsolateSample sample : otherCluster.isolates) {
         newCluster.isolates.add(sample);
      }

      /*
      if (this.centroid != null && otherCluster.centroid != null) {
         newCluster.setCentroid(getNewCentroid(this.centroid, otherCluster.centroid));
      }
      */

      return newCluster;
   }

   public double actualDistance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      int closestNdxOne = -1, closestNdxTwo = -1;

      switch(type) {

         case SINGLE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double dist = IsolateDistance.getDistance(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  if (dist < closestDist) {
                     closestDist = dist;
                     closestNdxOne = dataNdx;
                     closestNdxTwo = otherNdx;
                  }
               }
            }

            closestDist = IsolateDistance.getCorrelation(
             this.isolates.get(closestNdxOne), otherCluster.isolates.get(closestNdxTwo));

            break;

         case COMPLETE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double dist = IsolateDistance.getDistance(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  if (dist > closestDist) {
                     closestDist = dist;
                     closestNdxOne = dataNdx;
                     closestNdxTwo = otherNdx;
                  }
               }
            }

            closestDist = IsolateDistance.getCorrelation(
             this.isolates.get(closestNdxOne), otherCluster.isolates.get(closestNdxTwo));

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double dist = IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalDist += dist;

                  totalSize++;
               }
            }

            closestDist = totalDist/totalSize;
            break;
      }

      return closestDist;
   }

   public double corrDistance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      switch(type) {

         case SINGLE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.min(closestDist, 100 - IsolateDistance.findCorrelation(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case COMPLETE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.max(closestDist, 100 - IsolateDistance.findCorrelation(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  totalDist += 100 - IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalSize++;
               }
            }

            closestDist = totalDist/totalSize;
            break;

         /*
          * Neither of these cases apply since there is no centroid
         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
            break;

         case WARD:
            closestDist = this.unionWith(otherCluster).sumSquaredError -
             (this.sumSquaredError + otherCluster.sumSquaredError);
            break;
         */
      }

      return closestDist;
   }

   public double distance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      switch(type) {

         case SINGLE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.min(closestDist, IsolateDistance.getDistance(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case COMPLETE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.max(closestDist, IsolateDistance.getDistance(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  totalDist += IsolateDistance.getDistance(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalSize++;
               }
            }

            closestDist = totalDist/totalSize;
            break;

         /*
          * Neither of these cases apply since there is no centroid
         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
            break;

         case WARD:
            closestDist = this.unionWith(otherCluster).sumSquaredError -
             (this.sumSquaredError + otherCluster.sumSquaredError);
            break;
         */
      }

      return closestDist;
   }

   /*
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
   */

   public String toString() {
      String str = "";

      for (int clusterNdx = 0; clusterNdx < isolates.size(); clusterNdx++) {
         str += isolates.get(clusterNdx);
      }

      return str;
   }

   public enum distType {
      SINGLE,
      COMPLETE,
      AVERAGE
      //CENTROID,
      //WARD
   }
}
