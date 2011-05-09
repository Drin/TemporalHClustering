package TemporalHClustering.dataParser;

import TemporalHClustering.dataTypes.IsolateSample;

import java.io.File;
import java.util.Map;
import java.util.List;

public class ParserDriver {
   public static void main(String[] args) {
      IsolateFileParser parser = new IsolateFileParser(new File(args[0]));

      Map<Integer, List<IsolateSample>> data = parser.extractData();

      for (int day : data.keySet()) {
         System.out.printf("day %d: %s\n", day, printList(data.get(day)));
      }
   }

   public static String printList(List<IsolateSample> dataList) {
      String dataStr = "";

      if (!dataList.isEmpty()) {
         for (int sampleNdx = 0; sampleNdx < dataList.size(); sampleNdx++) {
            dataStr += String.format("\n\t%s", dataList.get(sampleNdx));
         }
      }

      return dataStr;
   }
}
