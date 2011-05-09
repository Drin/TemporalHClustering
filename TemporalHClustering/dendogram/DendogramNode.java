package TemporalHClustering.dendogram;

public class DendogramNode implements Dendogram {
   private double mCorrelation;
   private Dendogram mLeft, mRight;
   
   public DendogramNode(double corr, Dendogram left, Dendogram right) {
      mCorrelation = height;
      mLeft = left;
      mRight = right;
   }

   public double getCorrelation() {
      return mCorrelation;
   }

   public Dendogram getLeft() {
      return mLeft;
   }

   public Dendogram getRight() {
      return mRight;
   }

   public Cluster toCluster() {
      return mLeft.toCluster().unionWith(mRight.toCluster());
   }

   public String getXML() {
      String xmlStr = String.format("<tree correlation = \"%.02f\" >\n", mCorrelation);

      xmlStr += mLeft.toXML("\t");
      xmlStr += mRight.toXML("\t");

      xmlStr += "</tree>\n";
      return xmlStr;
   }

   public String toXML(String spacing) {
      String xmlStr = String.format("%s<node correlation = \"%.02f\">\n", spacing, mCorrelation);

      xmlStr += mLeft.toXML(spacing + "\t");
      xmlStr += mRight.toXML(spacing + "\t");

      xmlStr += spacing + "</node>\n";
      return xmlStr;
   }

}
