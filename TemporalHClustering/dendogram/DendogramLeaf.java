package TemporalHClustering.dendogram;

import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.IsolateSample;

public class DendogramLeaf implements Dendogram {
   private double mCorrelation;
   private IsolateSample mIsolate;
   private Dendogram mLeft, mRight;

   public DendogramLeaf(IsolateSample sample) {
      mCorrelation = 100;
      mIsolate = sample;
      mLeft = null;
      mRight = null;
   }

   public double getCorrelation() {
      return mCorrelation;
   }

   public IsolateSample getIsolate() {
      return mIsolate;
   }

   public Dendogram getLeft() {
      return mLeft;
   }

   public Dendogram getRight() {
      return mRight;
   }

   public Cluster toCluster() {
      return new Cluster(mIsolate);
   }

   public String toString() {
      return String.format("%s", mIsolate);
   }

   public String getXML() {
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n", mCorrelation);

      xmlStr += String.format("\t<leaf correlation = \"%.02f\" isolate = \"%s\"/>\n",
       mCorrelation, toString());

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   public String toXML(String spacing) {
      String xmlStr = String.format("%s<leaf correlation = \"%.02f\" data = \"%s\"/>\n",
       spacing, mCorrelation, toString());

      return xmlStr;
   }
}
