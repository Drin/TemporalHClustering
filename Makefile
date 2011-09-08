dataFile=data/PilotCorrelation.csv
region=16s-23s
delimiter=&
threshold=99.7

compile:
	javac TemporalHClustering/HClustering.java

compileGUI:
	javac TemporalHClustering/gui/ClusteringGUI.java

run:
	java TemporalHClustering/HClustering "${dataFile}${delimiter}${region}${delimiter}${threshold}"

runGUI:
	java TemporalHClustering/gui/ClusteringGUI

dataParser:
	javac TemporalHClustering/dataParser/ParserDriver.java

parseData: dataParser
	java TemporalHClustering/dataParser/ParserDriver data/PilotCorrelation.csv

clean:
	rm -rf TemporalHClustering/*.class
	rm -rf TemporalHClustering/*/*.class
	rm -rf TemporalHClustering/*/*/*.class
	rm -rf TemporalHClustering/*/*/*/*.class
