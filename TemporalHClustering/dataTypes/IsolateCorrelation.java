package TemporalHClustering.dataTypes;

/*
 * This class represents the correlation value between two isolates. This gets
 * its own class to best abstract the regions and correlation-centric
 * organization.
 */
public class IsolateCorrelation {
   private Isolate mIsolateOne, mIsolateTwo;
   private Double mCorrelation_16_23, mCorrelation_23_5;
   private double upperThreshold_16_23 = 99.7, lowerThreshold_16_23 = 95;
   private double upperThreshold_23_5 = 99.7, lowerThreshold_23_5 = 95;

   private String mAggregationMethod = "average";

   public IsolateCorrelation(Isolate isolateA, Isolate isolateB) {
      super();

      mIsolateOne = isolateA;
      mIsolateTwo = isolateB;

      mCorrelation_16_23 = null;
      mCorrelation_23_5 = null;
   }

   public Double compareTo(IsolateCorrelation otherCorrelation) {
      Double myCorrelationVal = getCorrelation();
      Double otherCorrelationVal = otherCorrelation.getCorrelation();

      if (myCorrelationVal != null && otherCorrelationVal != null)
         return myCorrelationVal - otherCorrelationVal;

      else
         return null;
   }

   public void setThresholds_16_23(double lower, double upper) {
      upperThreshold_16_23 = upper;
      lowerThreshold_16_23 = lower;
   }

   public void setThresholds_23_5(double lower, double upper) {
      upperThreshold_23_5 = upper;
      lowerThreshold_23_5 = lower;
   }

   public void set16_23(double correlation) {
      mCorrelation_16_23 = correlation;
   }

   public void set23_5(double correlation) {
      mCorrelation_23_5 = correlation;
   }

   public Double get16_23() {
      return mCorrelation_16_23;
   }

   public Double get23_5() {
      return mCorrelation_23_5;
   }

   public boolean has16_23() {
      return mCorrelation_16_23 != null;
   }

   public boolean has23_5() {
      return mCorrelation_23_5 != null;
   }

   public boolean isCompleteCorrelation() {
      return has16_23() && has23_5();
   }

   public Double getCorrelation() {
      if (mCorrelation_16_23 != null && mCorrelation_23_5 != null) {
         if (mCorrelation_16_23 < lowerThreshold_16_23 || mCorrelation_23_5 < lowerThreshold_23_5) {
            return 0.0;
         }
         else if (mCorrelation_16_23 > upperThreshold_16_23 && mCorrelation_23_5 > upperThreshold_23_5) {
            return 100.0;
         }

         else {
            if (mAggregationMethod.equals("minimum")) {
               return Math.min(mCorrelation_16_23, mCorrelation_23_5);
            }
            else if (mAggregationMethod.equals("average")) {
               //System.out.println("average?");
               return (mCorrelation_16_23 + mCorrelation_23_5) / 2.0;
            }
         }
      }

      else if (mCorrelation_16_23 == null && mCorrelation_23_5 == null)
         return null;

      return mCorrelation_16_23 != null ? mCorrelation_16_23 : mCorrelation_23_5;
   }

   public Isolate getIsolateOne() {
      return mIsolateOne;
   }

   public Isolate getIsolateTwo() {
      return mIsolateTwo;
   }

   public int hashCode() {
      return mIsolateOne.hashCode() + mIsolateTwo.hashCode();
   }

   public boolean equals(Object otherCorrelation) {
      if (otherCorrelation instanceof IsolateCorrelation) {
         IsolateCorrelation tmpCorr = (IsolateCorrelation) otherCorrelation;

         if (mIsolateOne.equals(tmpCorr.getIsolateOne())) {
            return mIsolateTwo.equals(tmpCorr.getIsolateTwo());
         }
         else if (mIsolateOne.equals(tmpCorr.getIsolateTwo())) {
            return mIsolateTwo.equals(tmpCorr.getIsolateOne());
         }
      }

      return false;
   }
}
