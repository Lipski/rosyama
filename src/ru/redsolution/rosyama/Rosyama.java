package ru.redsolution.rosyama;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Application;
import android.content.Context;

public class Rosyama extends Application {
	private static final Pattern CSRF_PATTERN = Pattern.compile(
			".*name=\'csrfmiddlewaretoken\' value=\'(.+)\'.*", Pattern.DOTALL);
	private static final Pattern LOGIN_PATTERN = Pattern.compile(
			".*name=\"USER_LOGIN\" maxlength=\"255\" value=\"(.*)\" />.*",
			Pattern.DOTALL);

	private static final String PDF_PATTERN_TEMPLATE = ".*id=\"pdf_form_%1$s\" name=\"%1$s\">(.*)</textarea>.*";
	private static final String[] PDF_PATTERN_FIELDS = new String[] { "to",
			"from", "postaddress", "address", };
	private static final Pattern PDF_PATTERN_SIGNATURE = Pattern.compile(
			".*id=\"pdf_form_signature\" name=\"signature\" value=\"(.*)\">.*",
			Pattern.DOTALL);
	private static final Map<String, Pattern> PDF_PATTERNS = new HashMap<String, Pattern>();

	static {
		PDF_PATTERNS.put("signature", PDF_PATTERN_SIGNATURE);
		for (String field : PDF_PATTERN_FIELDS) {
			PDF_PATTERNS
					.put(field, Pattern.compile(
							String.format(PDF_PATTERN_TEMPLATE, field),
							Pattern.DOTALL));
			System.out.print(Pattern.compile(String.format(
					PDF_PATTERN_TEMPLATE, field)));
		}
	}

	private static final String LOGIN = "http://rosyama.ru/personal/holes.php";
	// http://www.foryou-vip.ru:8000/journal/login/
	private static final String ADD = "http://rosyama.ru/personal/add.php";

	public final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");

	private String csrf;
	private DefaultHttpClient client;

	public Rosyama() {
		csrf = null;
		client = null;
	}

	private void shutdown() {
		if (client != null) {
			client.getConnectionManager().shutdown();
			client = null;
		}
	}

	public boolean login(String login, String password) {
		client = new DefaultHttpClient();
		HttpGet get;
		HttpPost post;
		HttpResponse response;
		HttpEntity entity;
		Matcher matcher;
		String content;
		boolean done = false;
		try {
			get = new HttpGet(LOGIN);
			response = client.execute(get);
			System.out.println("Login form get: " + response.getStatusLine());
			entity = response.getEntity();
			matcher = CSRF_PATTERN.matcher(EntityUtils.toString(entity));
			if (matcher.matches())
				csrf = matcher.group(1);
			else
				csrf = null;
			if (entity != null)
				entity.consumeContent();

			post = new HttpPost(LOGIN);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			if (csrf == null) {
				nameValuePairs.add(new BasicNameValuePair("AUTH_FORM", "Y"));
				nameValuePairs.add(new BasicNameValuePair("TYPE", "AUTH"));
				nameValuePairs.add(new BasicNameValuePair("backurl",
						"/personal/holes.php"));
				nameValuePairs.add(new BasicNameValuePair("USER_LOGIN", login));
				nameValuePairs.add(new BasicNameValuePair("USER_PASSWORD",
						password));
			} else {
				nameValuePairs.add(new BasicNameValuePair(
						"csrfmiddlewaretoken", csrf));
				nameValuePairs.add(new BasicNameValuePair("USERNAME", login));
				nameValuePairs
						.add(new BasicNameValuePair("PASSWORD", password));
			}
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			response = client.execute(post);
			System.out.println("Login form post: " + response.getStatusLine());
			entity = response.getEntity();
			if (entity != null) {
				content = EntityUtils.toString(entity);
				FileOutputStream stream = openFileOutput(
						FILE_NAME_FORMAT.format(new Date()) + ".html",
						Context.MODE_WORLD_WRITEABLE);
				stream.write(content.getBytes());
				if (!LOGIN_PATTERN.matcher(content).matches())
					done = true;
			}
			if (entity != null)
				entity.consumeContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (done)
			return true;
		shutdown();
		return false;
	}

	public boolean add(String path, String address, String comment,
			String coordinates) {
		HttpPost post;
		HttpResponse response;
		HttpEntity entity;
		Matcher matcher;
		String content;
		boolean done = false;
		try {
			post = new HttpPost(ADD);
			File file = new File(path);
			MultipartEntity multipartEntity = new MultipartEntity();
			// HttpMultipartMode.STRICT, null, Charset.forName(HTTP.UTF_8));
			ContentBody contentBody = new FileBody(file, "image/jpeg");
			String ID = "0";
			String type = "holeonroad";
			String adr_subjectrf = "Челябинская область";
			String adr_city = "Челябинск";
			System.out.println("id: " + ID);
			System.out.println("type: " + type);
			System.out.println("address: " + address);
			System.out.println("adr_subjectrf: " + adr_subjectrf);
			System.out.println("adr_city: " + adr_city);
			System.out.println("comment: " + comment);
			System.out.println("coordinates: " + coordinates);
			multipartEntity.addPart("ID", new StringBody(ID));
			multipartEntity.addPart("TYPE", new StringBody(type));
			multipartEntity.addPart("PHOTOS_0", contentBody);
			multipartEntity.addPart("ADDRESS", new StringBody(address));
			multipartEntity.addPart("adr_subjectrf", new StringBody(
					adr_subjectrf));
			multipartEntity.addPart("adr_city", new StringBody(adr_city));
			multipartEntity.addPart("COMMENT", new StringBody(comment));
			multipartEntity.addPart("COORDINATES", new StringBody(coordinates));
			post.setEntity(multipartEntity);
			response = client.execute(post);
			System.out.println("File form post: " + post.getRequestLine());
			entity = response.getEntity();
			Map<String, String> values = new HashMap<String, String>();
			if (entity != null) {
				content = EntityUtils.toString(entity);
				FileOutputStream stream = openFileOutput(
						FILE_NAME_FORMAT.format(new Date()) + ".html",
						Context.MODE_WORLD_WRITEABLE);
				stream.write(content.getBytes());
				done = true;
				for (Entry<String, Pattern> entry : PDF_PATTERNS.entrySet()) {
					matcher = entry.getValue().matcher(
							EntityUtils.toString(entity));
					if (matcher.matches()) {
						values.put(entry.getKey(), matcher.group(1));
						System.out.println(entry.getKey() + ": "
								+ matcher.group(1));
					} else {
						System.out.println(entry.getKey() + ": not found");
						done = false;
					}
				}
			}
			if (entity != null)
				entity.consumeContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (done)
			return true;
		shutdown();
		return false;
	}
}
