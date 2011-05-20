package TemporalHClustering.dataParser;

import TemporalHClustering.dataTypes.IsolateSample;

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

   public IsolateFileParser(File parseFile, double lower, double upper) {
      mFile = parseFile;
      mLowerThreshold = lower;
      mUpperThreshold = upper;
   }

   public Map<Integer, List<IsolateSample>> extractData() {
      Map<Integer, IsolateSample> isolateIdMap = new HashMap<Integer, IsolateSample>();
      Map<Integer, List<IsolateSample>> dataMap = new LinkedHashMap<Integer, List<IsolateSample>>();

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
            IsolateSample newIsolate =
             new IsolateSample(isolateTuple[isolateNdx].replaceAll("\"", ""));

            isolateIdMap.put(isolateNdx - 1, newIsolate);
            if (!dataMap.containsKey(newIsolate.getDay())) {
               dataMap.put(newIsolate.getDay(), new ArrayList<IsolateSample>());
            }
         }
      }

      /*
       * create correlations between Isolate Samples
       */
      for (int tupleNdx = 0; fileParser.hasNextLine(); tupleNdx++) {
         HashMap<IsolateSample, Double> corrMap = new HashMap<IsolateSample, Double>();
         String[] tuplesString = fileParser.nextLine().replaceAll(" ", "").split(",");
         //double[] tuple = new double[tuplesString.length - 1];

         IsolateSample currentIsolate = isolateIdMap.get(tupleNdx);
         //String isolateName = tuplesString[0].replaceAll("\"", "");
         //IsolateSample currentIsolate = new IsolateSample(isolateName);

         if (!isolateIdMap.containsKey(tupleNdx)) {
            System.err.printf("isolateId %d not found in IdMap?\n", tupleNdx);
            System.exit(1);
         }

         for (int colNdx = 1; colNdx < tuplesString.length; colNdx++) {
            try {
               if (mTransform) {
                  double correlation = Double.parseDouble(tuplesString[colNdx]);
                  //transform the correlation thusly:
                  //    if > upperThreshold replace with 100 (exact match)
                  //    if < lowerThreshold replace with 0 (different)
                  //    else use original value (squishy area)
                  correlation = correlation > mUpperThreshold ? 100 :
                   correlation < mLowerThreshold ? 0 : correlation;

                  corrMap.put(isolateIdMap.get(colNdx - 1), correlation);
               }
               else {
                  corrMap.put(isolateIdMap.get(colNdx - 1),
                   Double.parseDouble(tuplesString[colNdx]));
               }
            }
            catch(NumberFormatException err) {
               System.err.println("invalid double value for isolate " +
                currentIsolate.getName());
               System.exit(1);
            }
         }

         //give the isolateSample a correlation map to be used later
         currentIsolate.setCorrMap(corrMap);

         //add the isolateSample to the list of isolateSamples for its day
         dataMap.get(currentIsolate.getDay()).add(currentIsolate);

         //dataMap.put(currentIsolate.getDay(), currentIsolate);
         //csvData.put(isolateName, tuple);
         //correlationMatrix.add(tuple);
      }

      return dataMap;
   }
}
