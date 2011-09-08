package TemporalHClustering.dataTypes;

import TemporalHClustering.dataTypes.SampleMethod;
//import TemporalHClustering.distanceMeasures.IsolateDistance;

import java.util.Map;

//TODO trying to migrate similarity matrix to
//dataStructures.IsolateSimilarityMatrix. as such I have commented out old
//code.
public class Isolate {
   private String mIsolateName;
   private SampleMethod mGroup;
   private int day;
   private boolean isClustered;
   //private Map<Isolate, Double> mCorrMap; //mapping from isolateName to correlation

   public Isolate(String name) {

      /*
       * super short term hack. Will try to put these into "namespaces" of
       * sorts
       */
      if (name.charAt(0) == 'a' ||
          name.charAt(0) == 'b' ||
          name.charAt(0) == 'c') {
         name = name.substring(1);
      }


      mIsolateName = name;
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(0));
      /*
       * grabs the integer that is between the method encoding (f | i | l) and
       * the '-' character, this is necessary since the day the sample was taken
       * is of variable length (depending on the sampling time frame)
       */
      day = Integer.parseInt(mIsolateName.substring(1, mIsolateName.indexOf('-')));
      //mCorrMap = null;

      isClustered = false;
   }

   public Isolate(String name, Map<Isolate, Double> newCorrMap) {
      mIsolateName = name;
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(0));
      day = Integer.parseInt(String.valueOf(mIsolateName.charAt(1)));
      //mCorrMap = newCorrMap;

      isClustered = false;
   }

   public String getName() {
      return mIsolateName;
   }

   public SampleMethod getSampleMethod() {
      return mGroup;
   }

   public int getDay() {
      return day;
   }

   public boolean hasBeenClustered() {
      return isClustered;
   }

   public void setClustered(boolean status) {
      isClustered = status;
   }

   /*
   public boolean hasCorr(Isolate otherIsolate) {
      return mCorrMap.containsKey(otherIsolate);
   }

   public Map<Isolate, Double> getCorrMap() {
      return mCorrMap;
   }

   public void setCorrMap(Map<Isolate, Double> newCorrMap) {
      mCorrMap = newCorrMap;
   }
   */

   /*
   public double compareTo(Isolate otherIsolate) {
      /*
       * Try using IsolateDistance for now
       *
      double correlation = 0;
      //System.out.printf("comparing '%s' to '%s'\n", mIsolateName, otherIsolate.getName());
      //System.out.printf("am I contained in the other's correlation? %s\n", (otherIsolate.getCorrMap().containsKey(this)));
      if (mCorrMap.containsKey(otherIsolate)) {
         correlation = 100 - mCorrMap.get(otherIsolate);
      }
      else {
         correlation = 100 - otherIsolate.getCorrMap().get(this);
      }

      return correlation + mGroup.dist(otherIsolate.getSampleMethod());
      *
      return IsolateDistance.getDistance(this, otherIsolate);
   }
   */

   public boolean isSameIsolate(Isolate otherIsolate) {
      return mIsolateName.equals(otherIsolate.mIsolateName);
   }

   /*
   public String printCorrs() {
      String corrMap = "";

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + sample;
      }
      corrMap += "\n" + mIsolateName + "";

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + mCorrMap.get(sample);
      }

      return corrMap;
   }
   *
   public String printCorrs() {
      String corrMap = "";

      /*
      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += ",\t" + sample;
      }
      corrMap += "\n" + mIsolateName + "";
      *

      for (Isolate sample : mCorrMap.keySet()) {
         corrMap += "," + mCorrMap.get(sample);
      }

      return corrMap;
   }
   */

   public int hashCode() {
      return mIsolateName.hashCode();
   }

   public String toString() {
      String printStr = String.format("%s", mIsolateName);
      /*
      printStr += String.format("\n\t\t* %s *", mGroup);

      for (Isolate sample : mCorrMap.keySet()) {
         printStr += String.format("\n\t\t\t'%s' : %.02f",
          sample.getName(), mCorrMap.get(sample));
      }
      */

      return printStr;
   }
}
