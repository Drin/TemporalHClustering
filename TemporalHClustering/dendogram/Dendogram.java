package TemporalHClustering.dendogram;

public interface Dendogram {
   public double getCorrelation();
   public Dendogram getLeft();
   public Dendogram getRight();
   public Cluster toCluster();
   public String getXML();
   public String toXML(String spacing);
}
