package web;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;

public class WebQuerySender {
	
	private HttpResponse response = null;
	private final static Logger logger = Logger.getLogger(WebQuerySender.class.getName());
	
	 private WebQuerySender() {
		 
	 }
	 
	 static WebQuerySender instance = null;
	 
	 public static WebQuerySender getInstance() {
		 
		 if(instance == null)
			 return new WebQuerySender();
		 return instance;
		 
	 }
	 
	 
	 private String getPametriziedUrl(Map<String, String> params, String url) {
			StringBuilder sb = new StringBuilder();
			sb.append(url+"?");
	params.forEach((k,v) -> sb.append(k+"="+v+"&"));
	sb.deleteCharAt(sb.length()-1);
	return sb.toString();
		}
			
	 
	 public JSONObject getJson(String url, Map<String, String> params, String target){
			try {
				return new JSONObject(IOUtils.toString(new URL(getPametriziedUrl(params,  url + "/" + target)), Charset.forName("UTF-8")));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();

			}
			return null;
		}
	 public String getResponse() throws Exception {
		 
		 return this.response.toString();
		 
	 }
	 
	 public void send(String url, Map<String, String> params) throws ClientProtocolException, IOException {
		 HttpClient client = HttpClientBuilder.create().build();
		 
		    HttpGet request = new HttpGet(getPametriziedUrl(params, url ));
		    logger.info(getPametriziedUrl(params,  url  ));
		    HttpResponse response = client.execute(request);
		    this.response = response;

				
	 }

}
