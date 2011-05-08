package TemporalHClustering.dendogram;

public class DendogramNode implements Dendogram {
   private double height;
   private Dendogram left, right;
   
   public DendogramNode(double height, Dendogram left, Dendogram right) {
      this.height = height;
      this.left = left;
      this.right = right;
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

   public Cluster toCluster() {
      return left.toCluster().unionWith(right.toCluster());
   }

   public String getXML() {
      String xmlStr = String.format("<tree height = \"%.1f\" >\n", height);

      xmlStr += left.toXML("\t");
      xmlStr += right.toXML("\t");

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   public String toXML(String spacing) {
      String xmlStr = String.format("%s<node height = \"%.1f\">\n", spacing, height);

      xmlStr += left.toXML(spacing + "\t");
      xmlStr += right.toXML(spacing + "\t");

      xmlStr += spacing + "</node>\n";
      return xmlStr;
   }

}
