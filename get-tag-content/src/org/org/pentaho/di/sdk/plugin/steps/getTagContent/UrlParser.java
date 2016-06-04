package org.pentaho.di.sdk.plugin.steps.getTagContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlParser {
	String htmldata;

	UrlParser() {
	}

	public String getTagData(String htmldata, String requiredTag, boolean textOnly) {
		Document doc = Jsoup.parse(htmldata);
		if ( textOnly ) {
			return doc.select(requiredTag).text();
		} else {
			return doc.select(requiredTag).toString();
		}
		
	}
}
