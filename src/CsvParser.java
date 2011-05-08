import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvParser {
	File file = null;

	public CsvParser(File parseFile) {
		file = parseFile;
	}

	public List<double[]> extractData() {
		List<double[]> csvData = new ArrayList<double[]>();
		Scanner fileParser = null;

		try {
			fileParser = new Scanner(file);
		}
		catch (java.io.FileNotFoundException fileErr) {
			System.out.println("could not find file: " + file);
			return null;
		}
		
		boolean[] restrictions = getRestrictions(fileParser);
		
		int columnCount = 0;
		for (boolean b : restrictions)
			if (b) columnCount++;

		while (fileParser.hasNextLine()) {
			String[] tuplesString = fileParser.nextLine().replaceAll(" ", "").split("[,\\t]");
			double[] tuple = new double[columnCount];
			int c = 0;
			for (int i = 0; i < tuplesString.length; i++)
				if (restrictions[i])
					tuple[c++] = Double.parseDouble(tuplesString[i]);
			csvData.add(tuple);
		}

		return csvData;
	}
	
	private boolean[] getRestrictions(Scanner fileParser) {
		String[] attList = fileParser.nextLine().replaceAll(" ", "").split("[,\\t]");
		boolean[] restrictions = new boolean[attList.length];
		for (int i = 0; i < attList.length; i++)
			restrictions[i] = attList[i].equals("1");
		return restrictions;
	}
}
