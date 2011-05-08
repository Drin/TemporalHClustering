package TemporalHClustering.dataTypes;

public class IsolateSample {
   private String isolateName;
   private SampleMethod group;
   private int dayCollected;

   public IsolateSample(String name) {
      isolateName = name;
      group = new SampleMethod(isolateName.charAt(0));
      dayCollected = isolateName.charAt(1) - '0';
   }
}
