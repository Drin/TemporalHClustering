package TemporalHClustering.dataTypes;

/*
 * This class represents the correlation value between two isolates. This gets
 * its own class to best abstract the regions and correlation-centric
 * organization.
 */
public class IsolateCorrelation {
   private Isolate mIsolateOne, mIsolateTwo;
   private Double mCorrelation_16_23, mCorrelation_23_5;

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

   public Double getCorrelation() {
      if (mCorrelation_16_23 != null && mCorrelation_23_5 != null)
         return mCorrelation_16_23 + mCorrelation_23_5;

      else if (mCorrelation_16_23 == null && mCorrelation_23_5 == null)
         return null;

      else 
         return mCorrelation_16_23 != null ? mCorrelation_16_23 : mCorrelation_23_5;
   }

   public Isolate getIsolateOne() {
      return mIsolateOne;
   }

   public Isolate getIsolateTwo() {
      return mIsolateTwo;
   }
}
