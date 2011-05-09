dataFile="data/PilotCorrelation.csv"

compile:
	javac TemporalHClustering/HClustering.java

run:
	java TemporalHClustering/HClustering ${dataFile}

dataParser:
	javac TemporalHClustering/dataParser/ParserDriver.java

parseData: dataParser
	java TemporalHClustering/dataParser/ParserDriver data/PilotCorrelation.csv

clean:
	rm -rf TemporalHClustering/*.class
	rm -rf TemporalHClustering/*/*.class
