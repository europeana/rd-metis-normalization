package eu.europeana.normalization.language.client;

import java.io.File;
import java.io.FileInputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

/**
 * A Client to the REST API of the language normalization service
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class NormalizationLanguageClient {
    private Client client = ClientBuilder.newBuilder().build();
    private Config config = new Config();
 
    public String normalize(String edmXmlRecord) throws Exception {
        WebTarget target = client.target(config.getNormalizationServiceUrl()).path(
                "normalize");

        Form form = new Form();
        form.param("record", edmXmlRecord);
//        target., form)
//        String requestResult =
//        target.request(MediaType.APPLICATION_XML)
//            .post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE),
//                String.class);
        
        Response response = target.request(MediaType.APPLICATION_XML).post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatus() == 200) {
            String normalizedEdmXmlRecord = response.readEntity(String.class);
            return normalizedEdmXmlRecord;
        } else
            throw handleInvalidResponse(target, "GET", edmXmlRecord, response);
    }

    private Exception handleInvalidResponse(WebTarget trg, String method, String message,
            Response response) {
        return new RuntimeException(method + " " + trg.getUri() + "\n " +
                                    (message == null ? "" : message) + "\nHTTPstatus: " +
                                    response.getStatus() + "\n" + response.readEntity(String.class));
    }

	public String normalize(File edmXmlRecord) throws Exception {
		FileInputStream in = new FileInputStream(edmXmlRecord);
		String edmRecStr=IOUtils.toString(in, "UTF-8");
		in.close();
		return normalize(edmRecStr);
	}

}
