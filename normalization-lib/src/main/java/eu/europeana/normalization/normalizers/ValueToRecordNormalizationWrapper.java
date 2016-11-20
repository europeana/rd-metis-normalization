package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.ValueNormalization;
import eu.europeana.normalization.util.Namespaces;
import eu.europeana.normalization.util.XPathUtil;
import eu.europeana.normalization.util.XmlUtil;

public class ValueToRecordNormalizationWrapper implements RecordNormalization {

	public static class XpathQuery {
		Map<String, String> namespacesPrefixes;
		String expression;
		
		public XpathQuery(String expression) {
			super();
			this.expression = expression;
		}

		public XpathQuery(Map<String, String> namespacesPrefixes, String expression) {
			super();
			this.namespacesPrefixes = namespacesPrefixes;
			this.expression = expression;
		}
		
		public Map<String, String> getNamespacesPrefixes() {
			return namespacesPrefixes;
		}
		public void setNamespacesPrefixes(Map<String, String> namespacesPrefixes) {
			this.namespacesPrefixes = namespacesPrefixes;
		}
		public String getExpression() {
			return expression;
		}
		public void setExpression(String expression) {
			this.expression = expression;
		}
		public void setPrefix(String prefix, String namespace) {
			if(namespacesPrefixes==null)
				namespacesPrefixes=new HashMap<>();
			namespacesPrefixes.put(prefix, namespace);
		}
	}

	protected static final XpathQuery EUROPEANA_PROXY_QUERY=new XpathQuery(
			new HashMap<String, String>() {{
				put("rdf",Namespaces.RDF);
				put("ore", Namespaces.ORE);
				put("edm", Namespaces.EDM);
			}}, "/rdf:RDF/ore:Proxy[edm:europeanaProxy='true']");
	
	protected static final XpathQuery PROVIDER_PROXY_QUERY=new XpathQuery(
			new HashMap<String, String>() {{
				put("rdf",Namespaces.RDF);
				put("ore", Namespaces.ORE);
				put("edm", Namespaces.EDM);
			}}, "/rdf:RDF/ore:Proxy[not(edm:europeanaProxy='true')]");
	
	List<XpathQuery> targetElements;
	ValueNormalization normalization;
	boolean normalizeToEuropeanaProxy;
	
	public ValueToRecordNormalizationWrapper(ValueNormalization normalization, boolean normalizeToEuropeanaProxy, XpathQuery... targetElements) {
		this.normalization = normalization;
		this.targetElements=new ArrayList<>();
		for(XpathQuery q: targetElements) {
			this.targetElements.add(q);
		}
	}
	
	@Override
	public void normalize(Document edm) {
		Element europeanaProxy;
		Element providerProxy;
		try {
			europeanaProxy = XPathUtil.queryDomForElement(EUROPEANA_PROXY_QUERY.getNamespacesPrefixes(), EUROPEANA_PROXY_QUERY.getExpression(), edm);
			providerProxy = XPathUtil.queryDomForElement(PROVIDER_PROXY_QUERY.getNamespacesPrefixes(), PROVIDER_PROXY_QUERY.getExpression(), edm);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		for(XpathQuery q: targetElements) { 
			NodeList elements;
			try {
				elements = XPathUtil.queryDom(q.getNamespacesPrefixes(), q.getExpression(), edm);
			} catch (XPathExpressionException e) { throw new RuntimeException(e.getMessage(), e); }
			for(int i=0; i<elements.getLength(); i++) {
				Element el=(Element) elements.item(i);
				String value = XmlUtil.getElementText(el);
				List<String> normalizedValue = normalization.normalize(value);
		       
				if(normalizedValue.isEmpty()) {
					if(el.getAttributes().getLength()==0 || (el.getAttributes().getLength()==1 && !StringUtils.isEmpty(el.getAttributeNS(Namespaces.XML, "lang"))) )
						el.getParentNode().removeChild(el);
					else {
						NodeList childNodes = el.getChildNodes();
				        for (int j = 0; j < childNodes.getLength(); j++) {
				          Node node = childNodes.item(j);
				          if (node.getNodeType() == Node.TEXT_NODE) 
				        	  el.removeChild(node);
				        }
					}
				} else {
					if(normalizeToEuropeanaProxy && el.getParentNode()==providerProxy) {
						if(europeanaProxy==null) {
							europeanaProxy=edm.createElementNS(Namespaces.ORE, "Proxy");
							edm.getDocumentElement().appendChild(europeanaProxy);
							Element europeanaProxyProp=edm.createElementNS(Namespaces.EDM, "europeanaProxy");
							europeanaProxyProp.appendChild(edm.createTextNode("true"));
							europeanaProxy.appendChild(europeanaProxyProp);
						}
			        	for(int k=0 ; k<normalizedValue.size() ; k++) {
			        		Node newEl = el.cloneNode(false);
			        		newEl.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(k)));
			        		europeanaProxy.appendChild(newEl);
			        	}
					} else {
						NodeList childNodes = el.getChildNodes();
				        for (int j = 0; j < childNodes.getLength(); j++) {
				          Node node = childNodes.item(j);
				          if (node.getNodeType() == Node.TEXT_NODE) 
				        	  el.removeChild(node);
				        }
				        el.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(0)));
				        if(normalizedValue.size()>1) {
				        	for(int k=1 ; k<normalizedValue.size() ; k++) {
				        		Node newEl = el.cloneNode(false);
				        		newEl.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(k)));
				        		el.getParentNode().insertBefore(newEl, el.getNextSibling());
				        	}
				        }
					}
				}
			}
		}
	}

}
