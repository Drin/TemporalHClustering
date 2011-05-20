package TemporalHClustering.dendogram;

import TemporalHClustering.dataTypes.Cluster;
import TemporalHClustering.dataTypes.IsolateSample;

import java.util.List;

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
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n",
       mCorrelation);

      xmlStr += String.format("\t<leaf correlation = \"%.02f\" " +
       "isolate = \"%s\"/>\n", mCorrelation, toString());

      xmlStr += "</tree>\n";
      return xmlStr;
   }


   public String toXML(String spacing) {
      String xmlStr = String.format("%s<leaf correlation = \"%.02f\"" +
       " data = \"%s\"/>\n", spacing, mCorrelation, toString());

      return xmlStr;
   }

   public String toClusterGraph(String spacing) {
      return spacing + mIsolate;
   }

   public String defaultStyle(String spacing) {
      String style = "style=filled;";
      String color = "color=lightgrey;";
      String nodeStyle = "node [style=filled, color=white];";

      return String.format("%s%s\n%s%s\n%s%s\n", spacing, style,
       spacing, color, spacing, nodeStyle);
   }
}





   /*
   public String getGXL() {
      String xmlStr = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
      xmlStr += "<gxl>\n";
      xmlStr += String.format("\t<graph id=\"%s\" edgeids=\"true\" " +
       "edgemode = \"undirected\">\n", mIsolate.getName() + "_Cluster");

      xmlStr += String.format("\t\t<node id=\"%s\">\n", mIsolate.getName());

      xmlStr += "</gxl>\n";
      return xmlStr;
   }
   */

   /*
   public String toGXL(String spacing) {
      String xmlStr = String.format("%s<node id=\"%s\">\n", spacing, mIsolate.getName());

      return xmlStr;
   }
   */
