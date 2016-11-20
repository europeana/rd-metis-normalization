package eu.europeana.normalization.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.basic.BasicTreeUI.TreeIncrementAction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.europeana.normalization.NormalizationService;
import eu.europeana.normalization.cleaning.DuplicateStatementCleaning;
import eu.europeana.normalization.cleaning.TrimAndEmptyValueCleaning;
import eu.europeana.normalization.language.LanguageNormalizer;
import eu.europeana.normalization.language.TargetLanguagesVocabulary;
import eu.europeana.normalization.normalizers.ChainedNormalization;
import eu.europeana.normalization.util.XmlUtil;

public class NormalizationRestServlet extends HttpServlet {

	private NormalizationService service;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String targetVocabString = getServletContext().getInitParameter("normalization.language.target.vocabulary");
		LanguageNormalizer languageNorm = new LanguageNormalizer(TargetLanguagesVocabulary.valueOf(targetVocabString));

		TrimAndEmptyValueCleaning spacesCleaner=new TrimAndEmptyValueCleaning();
		DuplicateStatementCleaning dupStatementsCleaner=new DuplicateStatementCleaning();
		
		ChainedNormalization chainedNormalizer = new ChainedNormalization(spacesCleaner.toEdmRecordNormalizer(), dupStatementsCleaner, languageNorm.toEdmRecordNormalizer());

		service = new NormalizationService(chainedNormalizer);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processNormalize(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		respondWithError(400, "HTTP method not supported: GET", request, response);
	}

	private void processNormalize(HttpServletRequest request, HttpServletResponse response) {
		try {
			String record = request.getParameter("record");
			if (record == null) {
				respondWithError(400, "Missing required parameter 'record'", request, response);
				return;
			}
			Document recordDom = null;
			try {
				recordDom = XmlUtil.parseDom(new StringReader(record));
			} catch (Exception e) {
				respondWithError(400, "Error parsing XML in parameter 'record': " + e.getMessage(), request, response);
				return;
			}
			service.normalize(recordDom);
	
			PrintWriter out;
			out = response.getWriter();
			response.setStatus(200);
			response.setContentType("application/xml");
			String writeDomToString = XmlUtil.writeDomToString(recordDom);
			out.print(writeDomToString);
			out.flush();
			out.close();
		} catch (Throwable e) {
			respondWithError(500, "Unexpected error: " + e.getMessage(), request, response);
			return;
		}
	}

	private void respondWithError(int httpStatus, String message, HttpServletRequest request,
			HttpServletResponse response) {
		response.setStatus(httpStatus);
		response.setContentType("application/xml");
		
		Document errorMsgDom=XmlUtil.newDocument();
		Element errorEl = errorMsgDom.createElement("Error");
		errorMsgDom.appendChild(errorEl);
		
		Element errorMsgEl = errorMsgDom.createElement("message");
		errorMsgEl.appendChild(errorMsgDom.createTextNode(message));
		errorEl.appendChild(errorMsgEl);
		
		Element errorCodeEl = errorMsgDom.createElement("code");
		errorCodeEl.appendChild(errorMsgDom.createTextNode(String.valueOf(httpStatus)));
		errorEl.appendChild(errorCodeEl);
		
		String writeDomToString = XmlUtil.writeDomToString(errorMsgDom);
		PrintWriter out;
		try {
			out = response.getWriter();
		} catch (IOException e) {
//			log.debug(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		response.setStatus(httpStatus);
		response.setContentType("application/xml");
		out.print(writeDomToString);
		out.flush();
		out.close();
	}

}
