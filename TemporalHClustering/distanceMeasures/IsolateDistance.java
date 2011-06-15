package TemporalHClustering.distanceMeasures;

import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.dataTypes.SampleMethod;

public class IsolateDistance {

   public static double getCorrelation(IsolateSample sample1, IsolateSample sample2) {
      double correlation = 100 - findCorrelation(sample1, sample2);
      return correlation;
      //why would I think that subtracting from the correlation value gives me correlation?
      //return correlation - sample1.getSampleMethod().dist(sample2.getSampleMethod());
   }

   public static double getDistance(IsolateSample sample1, IsolateSample sample2) {
      double correlation = 100 - findCorrelation(sample1, sample2);
      return correlation + sample1.getSampleMethod().dist(sample2.getSampleMethod());
   }

   public static double findCorrelation(IsolateSample sample1, IsolateSample sample2) {
      double correlation = 0;

      if (sample1.getCorrMap().containsKey(sample2)) {
         correlation = sample1.getCorrMap().get(sample2);
      }
      else if (sample2.getCorrMap().containsKey(sample1)) {
         correlation = sample2.getCorrMap().get(sample1);
      }
      else {
         //this is 101 because this method is only called by the above
         //two methods. so this will propagate to be -1
         correlation = -1;
         /*
         System.err.println("invalid mapping");
         System.err.println(sample1 + " and " + sample2);
         */
      }

      return correlation;
   }
}
