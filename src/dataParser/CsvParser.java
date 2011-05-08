package TemporalHClustering.dataParser;

import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class CsvParser {
   File file = null;

   public CsvParser(File parseFile) {
      file = parseFile;
   }

   public Map<Integer, Map<String, double[]>> extractData() {
      List<double[]> correlationMatrix = new ArrayList<double[]>();
      Map<Integer, Map<String, double[]>> dataMap = new LinkedHashMap<Integer, <String, double[]>>();

      Scanner fileParser = null;

      try {
         fileParser = new Scanner(file);
      }
      catch (java.io.FileNotFoundException fileErr) {
         System.out.println("could not find file: " + file);
         return null;
      }

      //this is the first line
      if (fileParser.hasNextLine()) {
         String[] isolateTuple = fileParser.nextLine().replaceAll(" ", "").split(",");

         //the first column is empty, every other column will have the name of an isolate
         for (int isolateNdx = 1; isolateNdx < isolateTuple.length; isolateNdx++) {
            csvData.put(isolateTuple[isolateNdx].replaceAll("\"", ""), null);
         }
      }

      while (fileParser.hasNextLine()) {
         String[] tuplesString = fileParser.nextLine().replaceAll(" ", "").split(",");
         double[] tuple = new double[tuplesString.length - 1];

         String isolateName = tuplesString[0].replaceAll("\"", "");

         if (!csvData.containsKey(isolateName)) {
            System.err.println("isolate " + isolateName + " mismatch?");
            System.exit(1);
         }

         for (int colNdx = 1; colNdx < tuplesString.length; colNdx++) {
            try {
               tuple[colNdx - 1] = Double.parseDouble(tuplesString[colNdx]);
            }
            catch(NumberFormatException err) {
               System.err.println("invalid double value for isolate " + isolateName);
               System.exit(1);
            }
         }

         csvData.put(isolateName, tuple);
         correlationMatrix.add(tuple);
      }

      return csvData;
   }
}
