package com.caitu99.push.util.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class HttpClientUtils {

	private static HttpClient httpClient = null;

	private static HttpClientUtils instance = null;

	private final static Logger _logger = LoggerFactory.getLogger(HttpClientUtils.class);

	private HttpClientUtils(){
	}
	
	public static HttpClientUtils getInstances(){
		if (null == instance) {
			instance = new HttpClientUtils();
		}
        ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager();
        httpClient = new DefaultHttpClient(threadSafeClientConnManager);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
		return instance;
	}
	
	public String doGet(String url, String charset) throws Exception {
		HttpGet get = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(get);
		int status = httpResponse.getStatusLine().getStatusCode();
		_logger.info("status =" + status);
		if (200 != status) {
			throw new RuntimeException("访问失败！");
		}
		String returnString = EntityUtils.toString(httpResponse.getEntity());
		return returnString;
	}
	/**
	 * Post发送https请求
	 * @Title: doSSLPost 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param url	   url
	 * @param charset  字符编码
	 * @param paramMap 参数集合
	 * @return
	 * @throws Exception
	 * @date 2014年5月8日 下午4:22:22  
	 * @author dzq
	 */
	public String doSSLPost(String url, String charset,Map<String, String> paramMap) throws Exception {
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER ;
		SchemeRegistry registry =  new  SchemeRegistry ();
		SSLSocketFactory socketFactory =  SSLSocketFactory . getSocketFactory ();
		socketFactory.setHostnameVerifier (( X509HostnameVerifier ) hostnameVerifier );
		registry.register (new Scheme("https",443,socketFactory));
		httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET,charset);
		BasicClientConnectionManager bccm =  new  BasicClientConnectionManager (registry );
		DefaultHttpClient client =  new  DefaultHttpClient (bccm, httpClient.getParams());
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nvps = converForMap(paramMap);
		post.setEntity(new UrlEncodedFormEntity(nvps, charset));
		HttpResponse httpResponse = client.execute(post);
		int status = httpResponse.getStatusLine().getStatusCode();
		_logger.info("status =" + status);
		String returnString = EntityUtils
				.toString(httpResponse.getEntity());
		return returnString;
	}
	
	/**
	 * Post发送https请求
	 * @Title: doSSLPost 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param url	   url
	 * @param charset  字符编码
	 * @param jsonString json参数
	 * @return
	 * @throws Exception
	 * @date 2014年5月8日 下午4:22:22  
	 * @author dzq
	 */
	public String doSSLPost(String url,String charset,String jsonString) throws Exception{
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER ;
		SchemeRegistry registry =  new  SchemeRegistry ();
		SSLSocketFactory socketFactory =  SSLSocketFactory . getSocketFactory ();
		socketFactory.setHostnameVerifier (( X509HostnameVerifier ) hostnameVerifier );
		registry.register (new Scheme("https",443,socketFactory));
		httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET,charset);
		BasicClientConnectionManager bccm =  new  BasicClientConnectionManager (registry );
		DefaultHttpClient client =  new  DefaultHttpClient (bccm, httpClient.getParams());
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		HttpPost post = new HttpPost(url);
		StringEntity s = new StringEntity(jsonString.toString(),charset);
		s.setContentEncoding(charset);
		s.setContentType("application/json");
		post.setEntity(s);
		post.addHeader("Content-Type","application/json;charset=UTF-8");
		HttpResponse httpResponse = client.execute(post);
		int status = httpResponse.getStatusLine().getStatusCode();
		_logger.info("status ="+status);
		String returnString = EntityUtils.toString(httpResponse.getEntity());
		System.err.println(returnString);
		return returnString;
	}
	
	/**
	 * Post发送http请求
	 * @Title: doPost 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param url	   url
	 * @param charset  字符编码
	 * @param jsonString json参数
	 * @return
	 * @throws Exception
	 * @date 2014年5月8日 下午4:23:29  
	 * @author dzq
	 */
	public String doPost(String url,String charset,String jsonString) throws Exception{
		HttpPost post = new HttpPost(url);
		StringEntity s = new StringEntity(jsonString.toString());
		s.setContentEncoding(charset);
		s.setContentType("application/json");
		post.setEntity(s);
		HttpResponse httpResponse = httpClient.execute(post);
		int status = httpResponse.getStatusLine().getStatusCode();
		_logger.info("status ="+status);
		String returnString = EntityUtils.toString(httpResponse.getEntity());
		return returnString;
	}
	
	/**
	 * Post发送http请求
	 * @Title: doPost 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param url	   url
	 * @param charset  字符编码
	 * @param paramMap 参数集合
	 * @return
	 * @throws Exception
	 * @date 2014年5月8日 下午4:24:03  
	 * @author dzq
	 */
	public String doPost(String url, String charset,Map<String, String> paramMap) throws Exception {
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nvps = converForMap(paramMap);
		post.setEntity(new UrlEncodedFormEntity(nvps, charset));
		HttpResponse httpResponse = httpClient.execute(post);
		int status = httpResponse.getStatusLine().getStatusCode();
		_logger.info("status =" + status);
		String returnString = EntityUtils
				.toString(httpResponse.getEntity());
		return returnString;
	}

	/**
	 * 参数转换接口 Map转换
	 * 
	 * @Title: converForMap
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param signParams
	 * @return
	 * @date 2014年4月8日 上午11:53:09
	 * @author dzq
	 */
	public List<NameValuePair> converForMap(Map<String, String> signParams) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Set<String> keys = signParams.keySet();
		for (String key : keys) {
			nvps.add(new BasicNameValuePair(key, signParams.get(key)));
		}
		return nvps;
	}

}
