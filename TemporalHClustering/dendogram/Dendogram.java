package TemporalHClustering.dendogram;

import TemporalHClustering.dataTypes.Cluster;

import java.util.List;

public interface Dendogram {
   public double getCorrelation();
   public Dendogram getLeft();
   public Dendogram getRight();
   public Cluster toCluster();
   public String getXML();
   public String toXML(String spacing);
   public String defaultStyle(String spacing);
   public String toClusterGraph(String spacing);
}
