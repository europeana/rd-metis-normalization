package research;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;

public class CsvExporter implements Closeable {

	File exportFolder;
	CSVPrinter codeMatchPrinter;
	CSVPrinter labelMatchPrinter;
	CSVPrinter labelWordMatchPrinter;
	CSVPrinter labelWordAllMatchPrinter;
	CSVPrinter noMatchPrinter;
	EuropeanLanguagesNal europaEuLanguagesNal;
	
	public CsvExporter(File exportFolder, EuropeanLanguagesNal europaEuLanguagesNal) {
		super();
		try {
			this.exportFolder = exportFolder;
			FileWriter out = new FileWriter(new File(exportFolder, "LangCodeMatch.csv"));
			out.write('\ufeff');
			codeMatchPrinter=new CSVPrinter(out,CSVFormat.EXCEL );
			out=new FileWriter(new File(exportFolder, "LangLabelMatch.csv"));
			out.write('\ufeff');
			labelMatchPrinter=new CSVPrinter(out,CSVFormat.EXCEL );
			out=new FileWriter(new File(exportFolder, "LangLabelWordMatch.csv"));
			out.write('\ufeff');
			labelWordMatchPrinter=new CSVPrinter(out,CSVFormat.EXCEL);
			out=new FileWriter(new File(exportFolder, "LangLabelWordAllMatch.csv"));
			out.write('\ufeff');
			labelWordAllMatchPrinter=new CSVPrinter(out,CSVFormat.EXCEL);
			out=new FileWriter(new File(exportFolder, "LangNoMatch.csv"));
			out.write('\ufeff');
			noMatchPrinter=new CSVPrinter(out,CSVFormat.EXCEL );
			this.europaEuLanguagesNal = europaEuLanguagesNal;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public void exportCodeMatch(String label, String match) {
		try {
			codeMatchPrinter.printRecord(label, getMatchDescription(match));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private String getMatchDescription(String match) {
		return match+"("+europaEuLanguagesNal.lookupNormalizedLanguageId(match).getPrefLabel("eng")+")";
	}

	public void exportLabelMatch(String label, List<String> normalizeds) {
		try {
			labelMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	private Iterable<String> createCsvRecord(String label, List<String> normalizeds) {
		ArrayList<String> ret=new ArrayList<>(normalizeds.size()+1);
		ret.add(label);
		for(String normVal: normalizeds) {
			ret.add(getMatchDescription(normVal));
		}
		return ret;
	}

	public void exportLabelWordMatch(String label, List<String> normalizeds) {
		try {
			labelWordMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public void exportLabelWordAllMatch(String label, List<String> normalizeds) {
		try {
			labelWordAllMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public void exportNoMatch(String label) {
		try {
			noMatchPrinter.printRecord(label);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	

	@Override
	public void close() throws IOException {
		codeMatchPrinter.close();
		labelMatchPrinter.close();
		labelWordMatchPrinter.close();
		labelWordAllMatchPrinter.close();
		noMatchPrinter.close();
	}

	
	
	
	
}
