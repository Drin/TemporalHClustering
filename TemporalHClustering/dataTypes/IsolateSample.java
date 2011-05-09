package TemporalHClustering.dataTypes;

import java.util.Map;

public class IsolateSample {
   private String mIsolateName;
   private SampleMethod mGroup;
   private int day;
   private Map<IsolateSample, Double> mCorrMap; //mapping from isolateName to correlation

   public IsolateSample(String name) {
      mIsolateName = name;
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(0));
      /*
       * grabs the integer that is between the method encoding (f | i | l) and
       * the '-' character, this is necessary since the day the sample was taken
       * is of variable length (depending on the sampling time frame)
       */
      day = Integer.parseInt(mIsolateName.substring(1, mIsolateName.indexOf('-')));
      mCorrMap = null;
   }

   public IsolateSample(String name, Map<IsolateSample, Double> newCorrMap) {
      mIsolateName = name;
      mGroup = SampleMethod.getMethod(mIsolateName.charAt(0));
      day = Integer.parseInt(String.valueOf(mIsolateName.charAt(1)));
      mCorrMap = newCorrMap;
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

   public Map<IsolateSample, Double> getCorrMap() {
      return mCorrMap;
   }

   public void setCorrMap(Map<IsolateSample, Double> newCorrMap) {
      mCorrMap = newCorrMap;
   }

   public double compareTo(IsolateSample otherIsolate) {
      return mCorrMap.containsKey(otherIsolate.getName()) ?
       mCorrMap.get(otherIsolate.getName()) :
       otherIsolate.getCorrMap().get(mIsolateName);
   }

   public String toString() {
      String printStr = String.format("%s", mIsolateName);
      /*
      printStr += String.format("\n\t\t* %s *", mGroup);

      for (IsolateSample sample : mCorrMap.keySet()) {
         printStr += String.format("\n\t\t\t%s : %.02f",
          sample.getName(), mCorrMap.get(sample));
      }
      */

      return printStr;
   }
}
