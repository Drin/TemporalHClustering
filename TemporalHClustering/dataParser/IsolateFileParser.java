package TemporalHClustering.dataParser;

import TemporalHClustering.dataTypes.Isolate;
import TemporalHClustering.dataTypes.IsolateCorrelation;
import TemporalHClustering.dataTypes.IsolateRegion;
import TemporalHClustering.dataTypes.Connectivity;

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
   private double mDistThreshold, mLowerThreshold, mUpperThreshold;
   private IsolateRegion mRegion;

   public IsolateFileParser(File parseFile, IsolateRegion region, double dist, double lower, double upper) {
      mFile = parseFile;
      mRegion = region;
      mDistThreshold = dist;
      mLowerThreshold = lower;
      mUpperThreshold = upper;
   }

   /*
    * New return value:
    *    Data structure that should contain two separate mappings for "green"
    *    links (strongly connected isolates) and "yellow" links (squishily
    *    connected isolates).
    *
    *    Each mapping should provide the ability to walk the isolates in a way
    *    appropriate for the experimentally imposed structure. Each mapping
    *    should also contain the correlation values.
    *
    *    Correlations should be indexed by isolate names. Isolates should be
    *    stored in sample groups, days, and strength of connectivity.
    *
    *    Foreach strength of connectivity {
    *       Foreach day {
    *          Foreach sample group {
    *             cluster(group)
    *
    *          }
    *
    *          cluster(group 1 & 2)
    *          cluster(group 1-2 & 3)
    *          ...
    *
    *          cluster(day 1, 2, ..., n - 1 & n)
    *       }
    *    }
    *
    *
    */
   //IsolateSimilarityMatrix can contain a mapping of days to isolates
   //Connectivity can be used as an index into several matrices of correlations
   //MARKER
   //public Map<Integer, List<Isolate>> extractData(IsolateSimilarityMatrix similarityMatrix) {
   public Map<Connectivity, IsolateSimilarityMatrix> extractData() {
      Map<Integer, Isolate> isolateIdMap = new HashMap<Integer, Isolate>();
      //Map<Integer, List<Isolate>> dataMap = new LinkedHashMap<Integer, List<Isolate>>();

      Map<Connectivity, IsolateSimilarityMatrix> isolateNetworks =
       new HashMap<Connectivity, IsolateSimilarityMatrix>();
      isolateNetworks.put(Connectivity.STRONG, new IsolateSimilarityMatrix());
      isolateNetworks.put(Connectivity.WEAK, new IsolateSimilarityMatrix());

      //IsolateSimilarityMatrix strongSimilarityMatrix = new IsolateSimilarityMatrix();
      //IsolateSimilarityMatrix weakSimilarityMatrix = new IsolateSimilarityMatrix();
      //Map<Integer, List<Isolate>> strongIsolateMap = new LinkedHashMap<Integer, List<Isolate>>();
      //Map<Integer, List<Isolate>> weakIsolateMap = new LinkedHashMap<Integer, List<Isolate>>();

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
            //TODO add new Lists for both isolate maps
            /*
             * MARKER
            if (!dataMap.containsKey(newIsolate.getDay())) {
               dataMap.put(newIsolate.getDay(), new ArrayList<Isolate>());
            }
            */
         }
      }

      //TODO
      //IsolateSimilarityMatrix similarityMatrix will be declared here and used
      //as an alias to strong similarity matrix or weak similarity matrix

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
            Isolate otherIsolate = isolateIdMap.get(colNdx - 1);

            try {
               double correlation = Double.parseDouble(tuplesString[colNdx]);
               if (mTransform) {
                  correlation = correlation > mUpperThreshold ? 100 :
                   correlation < mLowerThreshold ? 0 : correlation;
               }

               //MARKER new code
               IsolateSimilarityMatrix similarityMatrix = null;
               IsolateCorrelation isolateCorr = new IsolateCorrelation(currentIsolate, otherIsolate);

               switch (mRegion) {
                  case ITS_16_23:
                     isolateCorr.set16_23(correlation);
                     break;
                  case ITS_23_5:
                     isolateCorr.set23_5(correlation);
                     break;
                  default:
                     System.err.println("Invalid region: " + mRegion);
                     break;
               }

               /* MARKER
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
                   *
               }
               */

               //MARKER this is new code
               if (correlation > mUpperThreshold) {
                  similarityMatrix = isolateNetworks.get(Connectivity.STRONG);
               }
               /*
               else if (correlation > mLowerThreshold) {
                  similarityMatrix = isolateNetworks.get(Connectivity.WEAK);
               }
               */
               else {
                  similarityMatrix = isolateNetworks.get(Connectivity.WEAK);
                  //similarityMatrix = isolateNetworks.get(Connectivity.NONE);
               }

               //colNdx - 1 is because the csv is 1-indexed while the
               //dataStructures used here are 0-indexed
               //MARKER
               //similarityMatrix.addSimilarity(mRegion, currentIsolate, isolateIdMap.get(colNdx - 1), correlation);

               similarityMatrix.addCorrelation(isolateCorr);
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
         //MARKER similarityMatrix now has the dataMap
         //dataMap.get(currentIsolate.getDay()).add(currentIsolate);

         //dataMap.put(currentIsolate.getDay(), currentIsolate);
         //csvData.put(isolateName, tuple);
         //correlationMatrix.add(tuple);
      }

      return isolateNetworks;
   }
}
