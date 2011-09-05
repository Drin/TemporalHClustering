package TemporalHClustering.dataStructures;

import TemporalHClustering.dataTypes.IsolateRegion;
import TemporalHClustering.dataTypes.Isolate;

import java.util.Map;
import java.util.HashMap;

public class IsolateSimilarityMatrix {
   private Map<Isolate, Map<Isolate, Double>> similarityMatrix_16_23 = null;
   private Map<Isolate, Map<Isolate, Double>> similarityMatrix_23_5 = null;
   private final double INVALID_MAPPING = -2;

   public IsolateSimilarityMatrix() {
      super();

      similarityMatrix_16_23 = new HashMap<Isolate, Map<Isolate, Double>>();
      similarityMatrix_23_5 = new HashMap<Isolate, Map<Isolate, Double>>();
   }

   public boolean hasCorrelation(Isolate sample1, Isolate sample2) {
      boolean has16_23Correlation = similarityMatrix_16_23.containsKey(sample1) &&
       similarityMatrix_16_23.get(sample1).containsKey(sample2);

      boolean has23_5Correlation = similarityMatrix_23_5.containsKey(sample1) &&
       similarityMatrix_23_5.get(sample1).containsKey(sample2);

      return has16_23Correlation || has23_5Correlation;
   }

   public void addSimilarity(IsolateRegion region, Isolate sample1, Isolate sample2, double similarity) {
      add(region, sample1, sample2, similarity);
      add(region, sample2, sample1, similarity);
   }

   private void add(IsolateRegion region, Isolate sample1, Isolate sample2, double similarity) {
      Map<Isolate, Map<Isolate, Double>> similarityMatrix = null;
      Map<Isolate, Double> correlationMap = null;

      switch (region) {
         case ITS_16_23:
            similarityMatrix = similarityMatrix_16_23;
            break;
         case ITS_23_5:
            similarityMatrix = similarityMatrix_23_5;
            break;
         default:
            System.err.println("Invalid region: " + region);
            break;
      }

      //the isolate does not yet exist in the mapping, add a new map for it
      correlationMap = similarityMatrix.containsKey(sample1) ?
       similarityMatrix.get(sample1) : new HashMap<Isolate, Double>();

      //add the similarity value between sample 1 and sample 2
      correlationMap.put(sample2, similarity);
      //associate sample2 with sample 1
      similarityMatrix.put(sample1, correlationMap);
   }

   public double getSimilarity(Isolate sample1, Isolate sample2) {
      Map<Isolate, Double> correlationMap_16_23 = null;
      Map<Isolate, Double> correlationMap_23_5 = null;

      double ITS_16_23_val = 3, ITS_23_5_val = 3;

      /*
       * Its possible that using 3 for a missing region is not the correct
       * thing to do
       */

      if (similarityMatrix_16_23.containsKey(sample1)) {
         correlationMap_16_23 = similarityMatrix_16_23.get(sample1);

         ITS_16_23_val = correlationMap_16_23.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_16_23.get(sample2)) : 3;

         //System.out.printf("16_23: %.03f, 16_23_corr: %.03f\n",
          //ITS_16_23_val, correlationMap_16_23.get(sample2));
      }
      
      if (similarityMatrix_23_5.containsKey(sample1)) {
         correlationMap_23_5 = similarityMatrix_23_5.get(sample1);

         ITS_23_5_val = correlationMap_23_5.containsKey(sample2) ?
          standardizeCorrelation(correlationMap_23_5.get(sample2)) : 3;
      }


      return ITS_16_23_val + ITS_23_5_val;
   }

   private double standardizeCorrelation(Double correlation) {
      if (correlation < 95) return 0;
      else if (correlation < 98) return 0;
      else if (correlation < 99.7) return 3;
      else return 5;
   }

   //TODO add toString()
   private enum SimilarityScore {
      //MATCH(7), PROBABLE(3), UNPROBABLE(1), UNMATCH(0);
      MATCH(5), PROBABLE(3), UNPROBABLE(0), UNMATCH(0);

      private int mScore = -1;

      private SimilarityScore(int score) {
         mScore = score;
      }

      public static SimilarityScore getScore(double correlation) {
         if (correlation < 95) return UNMATCH;
         else if (correlation < 98) return UNPROBABLE;
         else if (correlation < 99.7) return PROBABLE;
         else return MATCH;
      }

      public int combine(SimilarityScore otherRegionScore) {
         return mScore + otherRegionScore.mScore;
      }
   }
}
