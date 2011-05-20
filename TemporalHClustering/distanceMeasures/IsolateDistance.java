package TemporalHClustering.distanceMeasures;

import TemporalHClustering.dataTypes.IsolateSample;
import TemporalHClustering.dataTypes.SampleMethod;

public class IsolateDistance {

   public static double getCorrelation(IsolateSample sample1, IsolateSample sample2) {
      double correlation = 100 - findCorrelation(sample1, sample2);
      return correlation - sample1.getSampleMethod().dist(sample2.getSampleMethod());
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
      else {
         correlation = sample2.getCorrMap().get(sample1);
      }

      return correlation;
   }
}
