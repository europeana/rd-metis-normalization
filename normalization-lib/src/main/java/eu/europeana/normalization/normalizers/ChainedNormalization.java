package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import eu.europeana.normalization.RecordNormalization;

public class ChainedNormalization implements RecordNormalization {

	List<RecordNormalization> normalizations;
	
	public ChainedNormalization() {
		super();
		this.normalizations = new ArrayList<>();
	}
	
	public ChainedNormalization(List<RecordNormalization> normalizations) {
		super();
		this.normalizations = new ArrayList<>(normalizations);
	}

	public ChainedNormalization(RecordNormalization... normalizations) {
		this();
		for(RecordNormalization norm: normalizations) {
			addNormalization(norm);
		}
	}
	
	public void addNormalization(RecordNormalization norm) {
		normalizations.add(norm);
	}
	
	@Override
	public void normalize(Document edm) {
		for(RecordNormalization normOp: normalizations) {
			normOp.normalize(edm);
		}
	}


}
