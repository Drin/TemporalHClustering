package TemporalHClustering.dataParser;

import java.io.File;
import java.util.Map;

public class ParserDriver {
   public static void main(String[] args) {
      CsvParser parser = new CsvParser(new File(args[0]));

      Map<String, double[]> data = parser.extractData();

      for (String isolateName : data.keySet()) {
         System.out.println("isolate " + isolateName + ": " +
          printArr(data.get(isolateName)));
      }
   }

   public static String printArr(double[] arr) {
      String arrStr = "";

      if (arr.length == 0)
         return arrStr;

      for (int arrNdx = 0; arrNdx < arr.length - 1; arrNdx++) {
         arrStr += arr[arrNdx] + ", ";
      }
      return arrStr + arr[arr.length - 1];
   }
}
