
package org.pentaho.di.sdk.plugin.steps.pagedownload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlData {

	String url;
	String data;

	UrlData() {

	}

	UrlData(String url) throws MalformedURLException, IOException {
		this.url = url;
		scrap();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getData() {
		return data;
	}

	public void scrap() throws MalformedURLException, IOException {
		
		URL conn = new URL(url);
		HttpURLConnection connection = (HttpURLConnection)conn.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", 
                   "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Language", "en-US"); 
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //send Request 
        DataOutputStream dataout = new DataOutputStream(connection.getOutputStream());
        dataout.writeBytes("");
        dataout.flush();
        dataout.close();
		
		StringBuffer sb = new StringBuffer();
		
		//get response
        InputStream is = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

		// read each line and write to System.out
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		this.data = sb.toString();
	}

}
