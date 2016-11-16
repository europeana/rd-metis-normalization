package eu.europeana.normalization.language.client;


import java.io.File;

import eu.europeana.normalization.language.client.NormalizationLanguageClient;

/**
 * Command line test for the NormalizationLanguageClient
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class TestNormalizationClient {

    public static void main(String[] args) {
    	try {
            NormalizationLanguageClient client = new NormalizationLanguageClient();
            
            String normalizedEdm = client.normalize(new File("src/test/samples/edm-record.xml"));
                System.out.println(normalizedEdm);
                System.out.println("#############################################333");
                 normalizedEdm = client.normalize(new File("src/test/samples/edm-record-internal.xml"));
                System.out.println(normalizedEdm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
