package ru.redsolution.rosyama;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class Main extends Activity implements OnClickListener {
	private static final Pattern CSRF = Pattern.compile(
			".*name=\'csrfmiddlewaretoken\' value=\'(.+)\'.*", Pattern.DOTALL);
	private final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");
	private static final String LOGIN = "http://rosyama.ru/personal/holes.php";
	private static final String ACTION = "http://rosyama.ru/personal/add.php";
	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";

	private static final int OPTION_MENU_PREFERENCE_ID = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	private DefaultHttpClient client;
	private Uri requestedUri;
	private String login;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.make_photo).setOnClickListener(this);
		findViewById(R.id.send).setOnClickListener(this);

		requestedUri = Uri
				.fromFile(new File("/sdcard/2011-06-02-13-08-09.jpg"));
		if (savedInstanceState != null) {
			String stringUri = savedInstanceState
					.getString(SAVED_REQUESTED_URI);
			if (stringUri != null)
				requestedUri = Uri.parse(stringUri);
		}
		client = new DefaultHttpClient();
		findViewById(R.id.send).setEnabled(requestedUri != null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		login = settings.getString("login", "");
		password = settings.getString("password", "");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// When HttpClient instance is no longer needed,
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		client.getConnectionManager().shutdown();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVED_REQUESTED_URI, requestedUri == null ? null
				: requestedUri.toString());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
			findViewById(R.id.send).setEnabled(resultCode == RESULT_OK);
			if (resultCode != RESULT_OK)
				requestedUri = null;
			else {
				Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
						.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
				Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
						.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
				Location best = net;
				if (gps != null && net != null) {
					if (gps.getTime() > net.getTime())
						best = gps;
					else if (net.hasAccuracy()) {
						float[] results = new float[1];
						Location.distanceBetween(net.getLatitude(),
								net.getLongitude(), gps.getLatitude(),
								gps.getLongitude(), results);
						if (results[0] < net.getAccuracy())
							best = gps;
					}

				} else if (net == null)
					best = gps;
				if (best != null)
					((EditText) findViewById(R.id.coordinates))
							.setText(Location.convert(best.getLatitude(),
									Location.FORMAT_DEGREES).replace(',', '.')
									+ ","
									+ Location.convert(best.getLongitude(),
											Location.FORMAT_DEGREES));
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_MENU_PREFERENCE_ID, 0, "Настройки").setIcon(
				android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_MENU_PREFERENCE_ID:
			Intent intent = new Intent(this, Preference.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.make_photo:
			requestedUri = getNextUri(".jpg");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		case R.id.send:
			login();
			break;
		}
	}

	private Uri getNextUri(String extention) {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				FILE_NAME_FORMAT.format(new Date()) + extention));
	}

	private boolean login() {
		BufferedReader in = null;
		HttpGet get;
		HttpPost post;
		HttpResponse response;
		HttpEntity entity;
		String content;
		String csrf;
		Matcher matcher;
		try {
			get = new HttpGet(LOGIN);
			response = client.execute(get);
			System.out.println("Login form get: " + response.getStatusLine());
			entity = response.getEntity();
			content = EntityUtils.toString(entity);
			matcher = CSRF.matcher(content);
			if (matcher.matches())
				csrf = matcher.group(1);
			else
				csrf = null;
			System.out.println("csrf: " + csrf);
			if (entity != null)
				entity.consumeContent();

			post = new HttpPost(LOGIN);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("AUTH_FORM", "Y"));
			nameValuePairs.add(new BasicNameValuePair("TYPE", "AUTH"));
			nameValuePairs.add(new BasicNameValuePair("backurl",
					"/personal/holes.php"));
			nameValuePairs.add(new BasicNameValuePair("USER_LOGIN", login));
			nameValuePairs
					.add(new BasicNameValuePair("USER_PASSWORD", password));
			if (csrf != null)
				nameValuePairs.add(new BasicNameValuePair(
						"csrfmiddlewaretoken", csrf));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			response = client.execute(post);
			System.out.println("Login form post: " + response.getStatusLine());
			entity = response.getEntity();
			if (entity != null)
				System.out.println(EntityUtils.toString(entity));
			if (entity != null)
				entity.consumeContent();

			post = new HttpPost(ACTION);
			File file = new File(requestedUri.getPath());

			MultipartEntity multipartEntity = new MultipartEntity(
					HttpMultipartMode.STRICT, null, Charset.forName(HTTP.UTF_8));
			ContentBody contentBody = new FileBody(file, "image/jpeg");
			multipartEntity.addPart("PHOTOS_0", contentBody);
			multipartEntity.addPart("ID", new StringBody("0"));
			multipartEntity.addPart("TYPE", new StringBody("holeonroad"));
			multipartEntity.addPart("ADDRESS", new StringBody(
					((EditText) findViewById(R.id.address)).getText()
							.toString()));
			multipartEntity.addPart("COMMENT", new StringBody(
					((EditText) findViewById(R.id.comment)).getText()
							.toString()));
			multipartEntity.addPart("COORDINATES", new StringBody(
					((EditText) findViewById(R.id.coordinates)).getText()
							.toString()));
			post.setEntity(multipartEntity);
			response = client.execute(post);
			System.out.println("File form post: " + post.getRequestLine());
			entity = response.getEntity();
			if (entity != null)
				System.out.println(EntityUtils.toString(entity));
			if (entity != null)
				entity.consumeContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}