/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.language;

import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import eu.europeana.normalization.NormalizeDetails;
import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.ValueNormalization;
import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.language.nal.LanguageMatcher;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class LanguageNormalizer implements ValueNormalization {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(LanguageNormalizer.class.getName());

    TargetLanguagesVocabulary               targetVocab;
    LanguageMatcher                         normalizer;

    /**
     * Creates a new instance of this class.
     * 
     * @param targetVocab
     */
    public LanguageNormalizer(TargetLanguagesVocabulary targetVocab) {
        super();
        this.targetVocab = targetVocab;
        normalizer = new LanguageMatcher(new EuropeanLanguagesNal(), targetVocab);

    }

    public List<String> normalize(String value) {
        List<NormalizeDetails> normalizeDetailedRes = normalizeDetailed(value);
        List<String> res = new ArrayList<>(normalizeDetailedRes.size());
        for (NormalizeDetails dtl : normalizeDetailedRes) {
            res.add(dtl.getNormalizedValue());
        }
        return res;
    }

    public List<NormalizeDetails> normalizeDetailed(String lbl) {
        List<NormalizeDetails> res = new ArrayList<>();

        String normalized = normalizer.findIsoCodeMatch(lbl);
        if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
            res.add(new NormalizeDetails(normalized, 1));
        } else if (normalized != null) {
            res.add(new NormalizeDetails(normalized, 0.98f));
        } else {
            List<String> normalizeds = normalizer.findLabelMatches(lbl);
            if (!normalizeds.isEmpty()) {
                res.addAll(NormalizeDetails.newList(normalizeds, 0.95f));
            } else {
// if (!lbl.endsWith("[Metadata]") && !lbl.endsWith("[Resource]")) {// Some invalid values that were
// present when research was underway. Ingestion will clean these values later
                normalizeds = normalizer.findLabelWordMatches(lbl);
                if (!normalizeds.isEmpty()) {
                    res.addAll(NormalizeDetails.newList(normalizeds, 0.85f));
                }
            }
        }
        return res;
    }

	public RecordNormalization toEdmRecordNormalizer() {
		XpathQuery dcLanguageQuery=new XpathQuery(
				new HashMap<String, String>() {{
					put("dc", "http://purl.org/dc/elements/1.1/");
					put("ore", "http://www.openarchives.org/ore/terms/");
				}}, "//ore:Proxy/dc:language");
    	ValueToRecordNormalizationWrapper dcLanguageNorm=new ValueToRecordNormalizationWrapper(this, false, dcLanguageQuery);
    	return dcLanguageNorm;
	}

}
