package eu.europeana.normalization.client;


import java.io.File;

import eu.europeana.normalization.client.NormalizationClient;

/**
 * Command line test for the NormalizationLanguageClient
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class TestNormalizationClient {

    public static void main(String[] args) {
    	try {
            NormalizationClient client = new NormalizationClient();
            
            String normalizedEdm = client.normalize(new File("src/test/samples/edm-record.xml"));
                System.out.println(normalizedEdm);
                System.out.println("#############################################");
                 normalizedEdm = client.normalize(new File("src/test/samples/edm-record-internal.xml"));
                System.out.println(normalizedEdm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
