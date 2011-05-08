package TemporalHClustering.dendogram;

public class DendogramLeaf implements Dendogram {
   private double[] data;
   private double height;
   private Dendogram left, right;

   public DendogramLeaf(double[] dataPoint) {
      data = dataPoint;
      left = null;
      right = null;
      height = 0;
   }

   public double getHeight() {
      return height;
   }

   public Dendogram getLeft() {
      return left;
   }

   public Dendogram getRight() {
      return right;
   }

   public double[] getData() {
      return data;
   }

   public Cluster toCluster() {
      return new Cluster(data);
   }

   public String toString() {
      String outStr = "";

      for (int dataNdx = 0; dataNdx < data.length; dataNdx++) {
         outStr += data[dataNdx] + ", ";
      }

      return outStr.substring(0, outStr.length() - 2);
   }

   public String getXML() {
      String xmlStr = String.format("<tree height = \"%.1f\" >\n", height);

      xmlStr += String.format("\t<leaf height = \"%.1f\" data = \"%s\"/>\n",
       height, toString());

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   public String toXML(String spacing) {
      String xmlStr = String.format("%s<leaf height = \"%.1f\" data = \"%s\"/>\n",
       spacing, height, toString());

      return xmlStr;
   }
}
