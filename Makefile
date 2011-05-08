dataDir="data/"

compile:
	javac TemporalHClustering/HClustering.java

dataParser:
	javac TemporalHClustering/dataParser/ParserDriver.java

parseData: dataParser
	java TemporalHClustering/dataParser/ParserDriver data/PilotCorrelation.csv

clean:
	rm -rf TemporalHClustering/*.class
	rm -rf TemporalHClustering/*/*.class
