package TemporalHClustering.dataParser;

import TemporalHClustering.dataTypes.Isolate;
import TemporalHClustering.dataTypes.IsolateRegion;

import TemporalHClustering.dataStructures.IsolateSimilarityMatrix;

import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class IsolateFileParser {
   private boolean mTransform = true;
   private File mFile = null;
   private double mLowerThreshold, mUpperThreshold;
   private IsolateRegion mRegion;

   public IsolateFileParser(File parseFile, IsolateRegion region, double lower, double upper) {
      mFile = parseFile;
      mRegion = region;
      mLowerThreshold = lower;
      mUpperThreshold = upper;
   }

   public Map<Integer, List<Isolate>> extractData(IsolateSimilarityMatrix similarityMatrix) {
      Map<Integer, Isolate> isolateIdMap = new HashMap<Integer, Isolate>();
      Map<Integer, List<Isolate>> dataMap = new LinkedHashMap<Integer, List<Isolate>>();

      Scanner fileParser = null;

      try {
         fileParser = new Scanner(mFile);
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.println("could not find file: " + mFile);
         return null;
      }

      /*
       * This is where the isolateIdMap and dataMap get seeded with initial values:
       *    isolateIdMap gets a mapping of attribute (column) index to corresponding isolateSample
       *    dataMap gets a mapping of days to an empty (initially) list of isolateSamples for that day
       */
      if (fileParser.hasNextLine()) {
         String[] isolateTuple = fileParser.nextLine().replaceAll(" ", "").split(",");

         for (int isolateNdx = 1; isolateNdx < isolateTuple.length; isolateNdx++) {
            //the first column is empty, every other column will have the name of an isolate
            Isolate newIsolate =
             new Isolate(isolateTuple[isolateNdx].replaceAll("\"", "").toLowerCase());

            isolateIdMap.put(isolateNdx - 1, newIsolate);
            if (!dataMap.containsKey(newIsolate.getDay())) {
               dataMap.put(newIsolate.getDay(), new ArrayList<Isolate>());
            }
         }
      }

      /*
       * create correlations between Isolate Samples
       */
      for (int tupleNdx = 0; fileParser.hasNextLine() && tupleNdx < isolateIdMap.size(); tupleNdx++) {
         HashMap<Isolate, Double> corrMap = new HashMap<Isolate, Double>();
         String[] tuplesString = fileParser.nextLine().replaceAll(" ", "").split(",");
         //double[] tuple = new double[tuplesString.length - 1];

         Isolate currentIsolate = isolateIdMap.get(tupleNdx);
         //String isolateName = tuplesString[0].replaceAll("\"", "");
         //Isolate currentIsolate = new Isolate(isolateName);

         if (!isolateIdMap.containsKey(tupleNdx)) {
            System.err.printf("isolateId %d not found in IdMap?\n", tupleNdx);
            System.exit(1);
         }

         for (int colNdx = 1; colNdx < tuplesString.length; colNdx++) {
            try {
               double correlation = Double.parseDouble(tuplesString[colNdx]);

               if (mTransform) {
                  //transform the correlation thusly:
                  //    if > upperThreshold replace with 100 (exact match)
                  //    if < lowerThreshold replace with 0 (different)
                  //    else use original value (squishy area)
                  correlation = correlation > mUpperThreshold ? 100 :
                   correlation < mLowerThreshold ? 0 : correlation;

                  //corrMap.put(isolateIdMap.get(colNdx - 1), correlation);
               }
               else {
                  /*
                  corrMap.put(isolateIdMap.get(colNdx - 1),
                   Double.parseDouble(tuplesString[colNdx]));
                   */
               }
               similarityMatrix.addSimilarity(mRegion, currentIsolate, isolateIdMap.get(colNdx - 1), correlation);
               //System.out.printf("correlation: %.03f beta: %.02f alpha: %.02f\n",
                //correlation, mUpperThreshold, mLowerThreshold);
            }
            catch(NumberFormatException err) {
               System.err.println("invalid double value for isolate " +
                currentIsolate.getName());
               System.exit(1);
            }
         }

         //give the isolateSample a correlation map to be used later
         //currentIsolate.setCorrMap(corrMap);

         //add the isolateSample to the list of isolateSamples for its day
         dataMap.get(currentIsolate.getDay()).add(currentIsolate);

         //dataMap.put(currentIsolate.getDay(), currentIsolate);
         //csvData.put(isolateName, tuple);
         //correlationMatrix.add(tuple);
      }

      return dataMap;
   }
}
