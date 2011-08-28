package TemporalHClustering.dataTypes;

import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.dataTypes.SampleMethod;
import TemporalHClustering.distanceMeasures.IsolateDistance;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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

   //closest distances are being calculated using "distance" and then recalculated
   //to be the correlation between those two distances. I think this is correct
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

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  wardDist += IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double correlation = IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
            break;
      }

      return closestDist;
   }

   public double corrDistance(Cluster otherCluster, distType type) {
      double closestDist = -1;
      switch(type) {

         //this used to be SINGLE but then I realized that with correlations being 100 based
         //instead of 0 based, it is actually COMPLETE 
         case COMPLETE:
            closestDist = Double.MAX_VALUE;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.min(closestDist, 100 - IsolateDistance.findCorrelation(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         //this used to be COMPLETE but then I realized that with correlations being 100 based
         //instead of 0 based, it is actually SINGLE
         case SINGLE:
            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  closestDist = Math.max(closestDist, 100 - IsolateDistance.findCorrelation(
                   this.isolates.get(dataNdx), otherCluster.isolates.get(otherNdx)));
               }
            }

            break;

         case AVERAGE:
            double totalDist = 0, totalSize = 0;

            //System.err.println("calculating average...\n");

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  //totalDist += 100 - IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                  totalDist += IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  totalSize++;
                  //System.err.println("totalDist: " + totalDist);
               }
            }

            //System.err.println(totalDist + "/" + totalSize + " = " + (totalDist/totalSize));

            closestDist = totalDist/totalSize;
            break;

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  wardDist += IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double correlation = IsolateDistance.findCorrelation(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
            break;

         /*
          * this case doesn't apply since there is no centroid
         case CENTROID:
            closestDist = distMode.evaluateDistance(
             this.centroid,
             otherCluster.centroid);
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

         case WARD:
            double wardDist = 0, wardSize = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  wardDist += IsolateDistance.getDistance(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  wardSize++;
               }
            }

            double avgCorrelation = wardDist/wardSize;

            closestDist = 0;

            for (int dataNdx = 0; dataNdx < this.isolates.size(); dataNdx++) {
               for (int otherNdx = 0; otherNdx < otherCluster.isolates.size(); otherNdx++) {
                  double correlation = IsolateDistance.getDistance(this.isolates.get(dataNdx),
                   otherCluster.isolates.get(otherNdx));

                  closestDist += (correlation - avgCorrelation) * (correlation - avgCorrelation);
               }
            }
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
         str += isolates.get(clusterNdx) + ", ";
      }

      return str;
   }

   public static String cytoscapeFormatHeader() {
      String[] column = new String[] {"e.coli_A", "e.coli_B", "interaction", "correlation"};
      String cytoFormat = String.format("%s\t%s\t%s\t%s\n",
       column[0], column[1], column[2], column[3]);

      return cytoFormat;
   }

   public String toCytoscapeCluster(String clusterName) {
      String cytoFormat = "";

      for (IsolateSample sample : isolates) {
         cytoFormat += String.format("%s\t%s\n",
          clusterName, sample);
      }

      return cytoFormat;
   }

   public String toCytoscapeFormat() {
      String cytoFormat = "";
      String interactionType = "pp";

      if (isolates.size() == 1) {
         return cytoFormat + String.format("%s\t%s\t%s\t%d\n",
          isolates.get(0), isolates.get(0), interactionType, -1);
      }

      for (int srcNdx = 0; srcNdx < isolates.size(); srcNdx++) {
         for (int dstNdx = srcNdx + 1; dstNdx < isolates.size(); dstNdx++) {
            double isolateCorr = -1;

            if (isolates.get(srcNdx).hasCorr(isolates.get(dstNdx))) {
               //use source isolate's correlation map
               Map<IsolateSample, Double> corrMap = isolates.get(srcNdx).getCorrMap();

               cytoFormat += String.format("%s\t%s\t%s\t%.03f\n",
                isolates.get(srcNdx), isolates.get(dstNdx), interactionType, corrMap.get(isolates.get(dstNdx)));
            }
            else if (isolates.get(dstNdx).hasCorr(isolates.get(srcNdx))) {
               //use destination isolate's correlation map
               Map<IsolateSample, Double> corrMap = isolates.get(dstNdx).getCorrMap();

               cytoFormat += String.format("%s\t%s\t%s\t%.03f\n",
                isolates.get(dstNdx), isolates.get(srcNdx), interactionType, corrMap.get(isolates.get(srcNdx)));
            }

         }
      }

      return cytoFormat;
   }

   public String toTemporalFormat(int clusterNum) {
      String tempOutput = "";
      int numDays = 14;

      //will display Day:, 1, 2, 3, ... for csv formatted temporal diagram
      String diagramHeader = "Day:";
      for (int day = 1; day <= numDays; day++) {
         diagramHeader += ", " + day;
      }

      //map representing isolateNum -> {days -> String}
      Map<Integer, Map<Integer, String>> fecalMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> immMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> laterMap = new LinkedHashMap<Integer, Map<Integer, String>>();
      Map<Integer, Map<Integer, String>> deepMap = new LinkedHashMap<Integer, Map<Integer, String>>();

      for (IsolateSample sample : isolates) {
         Map<Integer, Map<Integer, String>> sampleMap = null;

         String sampleName = sample.getName();
         //if isolate name is 'f14-1' then extract 14 as the day
         int day = Integer.parseInt(sampleName.substring(1, sampleName.indexOf("-")));
         //if isolate name is 'f14-1' then extract 1 as the isolateNum
         int isolateNum = Integer.parseInt(sampleName.substring(sampleName.indexOf("-") + 1, sampleName.length()));
         String marker = ", X";
         
         //just so that we are adding to the correct map
         switch (sample.getSampleMethod()) {
            case FECAL:
               sampleMap = fecalMap;
               marker = ", F";
               break;
            case IMM:
               sampleMap = immMap;
               marker = ", I";
               break;
            case LATER:
               sampleMap = laterMap;
               marker = ", L";
               break;
            case DEEP:
               sampleMap = deepMap;
               marker = ", D";
         }

         if (!sampleMap.containsKey(isolateNum)) {
            Map<Integer, String> newTickMap = new LinkedHashMap<Integer, String>();

            for (int dayCol = 1; dayCol <= numDays; dayCol++) {
               newTickMap.put(dayCol, ", ");
            }

            sampleMap.put(isolateNum, newTickMap);
         }

         Map<Integer, String> tickMap = sampleMap.get(isolateNum);

         tickMap.put(day, marker);
      }

      tempOutput += String.format("%s\n%s\n%s%s%s%s\n", "cluster_" + clusterNum,
       diagramHeader, toIsolateTable(fecalMap), toIsolateTable(immMap), toIsolateTable(laterMap), toIsolateTable(deepMap));

      /*
       * auto generate some partially completed g.raphael bar chart code
      String barOptions = "{stacked: true, type: \"soft\"}).hoverColumn(fin2, fout2);";

      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(fecalMap), barOptions));
      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(immMap), barOptions));
      System.out.println(String.format("r.g.barchart(%d, %d, 400, 220, [%s], %s",
       (450 * (clusterNum % 2)), (50 + (220 * (clusterNum / 2))), toIsolateBars(laterMap), barOptions));
       */

      return tempOutput;
   }

   private String toIsolateTable(Map<Integer, Map<Integer, String>> sampleMap) {
      String tableOutput = "";

      //for (int level = 1; sampleMap.containsKey(level); level++) {
      for (int level : sampleMap.keySet()) {
         Map<Integer, String> tickMap = sampleMap.get(level);

         for (int day : tickMap.keySet()) {
            tableOutput += tickMap.get(day);
         }

         tableOutput += "\n";
      }

      //return hasOutput ? tableOutput : "";
      return tableOutput;
   }
   
   private String toIsolateBars(Map<Integer, Map<Integer, String>> sampleMap) {
      String barOutput = "";

      //for (int level = 1; sampleMap.containsKey(level); level++) {
      for (int level : sampleMap.keySet()) {
         //System.err.println("level: " + level);
         if (level > 1) {
            barOutput += ",\n";
         }

         Map<Integer, String> tickMap = sampleMap.get(level);
         barOutput += "[";
         for (int day: tickMap.keySet()) {
            if (day > 1) {
               barOutput += tickMap.get(day).equals(", X") ? ", 1" : ", 0";
            }
            else {
               barOutput += tickMap.get(day).equals(", X") ? "1" : "0";
            }
         }

         barOutput += "]";
      }
      
      return barOutput;
   }

   public enum distType {
      SINGLE,
      COMPLETE,
      AVERAGE,
      //CENTROID,
      WARD
   }
}
