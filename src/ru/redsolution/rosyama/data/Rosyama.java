package ru.redsolution.rosyama.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.redsolution.rosyama.R;
import ru.redsolution.rosyama.StateListener;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Класс приложения.
 * 
 * @author alexander.ivanov
 * 
 */
public class Rosyama extends Application implements Runnable {
	/**
	 * Писать логи, использовать точку по умолчанию для геокодинга.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Состояние приложения.
	 * 
	 * @author alexander.ivanov
	 * 
	 */
	public static enum State {
		/**
		 * Ожидание настроек для авторизации.
		 */
		idle,

		/**
		 * Проверка авторизации.
		 */
		authRequest,

		/**
		 * Авторизация пройдена. Доступно фотографирование.
		 */
		authComplited,

		/**
		 * Создается фотография (не используется).
		 */
		photoRequest,

		/**
		 * Фотография сделана.
		 */
		photoComplited,

		/**
		 * Определение адреса по координатам.
		 */
		geoRequest,

		/**
		 * Адрес определен. Доступно редактирование информации и отправка
		 * дефекта.
		 */
		geoComplited,

		/**
		 * Отправка дефекта.
		 */
		holeRequest,

		/**
		 * Отправка дефекта завершена.
		 */
		holeComplited,

		/**
		 * Запрос главы ГИБДД.
		 */
		headRequest,

		/**
		 * Адресат (глава ГИБДД) определен. Доступно редактирование информации и
		 * запрос pdf.
		 */
		headComplited,

		/**
		 * Запрос pdf файла.
		 */
		pdfRequest,

		/**
		 * PDF получен. Всё выполнено.
		 */
		pdfComplited;

		/**
		 * Логин задан и проверен.
		 * 
		 * @return
		 */
		public boolean isAuthorized() {
			return this != idle && this != authRequest;
		}

		/**
		 * Имеется фото для отправки.
		 * 
		 * @return
		 */
		public boolean canHole() {
			return this != idle && this != authRequest && this != authComplited
					&& this != photoRequest && this != photoComplited
					&& this != geoRequest;
		}

		/**
		 * Дефект был успешно отправлен.
		 * 
		 * @return
		 */
		public boolean canPDF() {
			return this == headComplited || this == pdfRequest
					|| this == pdfComplited;

		}

		/**
		 * Возвращает текст выполняемой операции.
		 * 
		 * @return <code>null</code> если сейчас нет выполняемых операций.
		 */
		public Integer getAction() {
			if (this == State.authRequest)
				return R.string.auth_request;
			else if (this == State.geoRequest)
				return R.string.geo_request;
			else if (this == State.headRequest)
				return R.string.head_request;
			else if (this == State.holeRequest)
				return R.string.hole_request;
			else if (this == State.pdfRequest)
				return R.string.pdf_request;
			else if (this == State.photoRequest)
				return R.string.photo_request;
			else
				return null;
		}
	};

	/**
	 * Исключения, генерируемые приложением, содержащие ресурс с описанием
	 * ошибки.
	 * 
	 * @author alexander.ivanov
	 * 
	 */
	public class LocalizedException extends Exception {
		private static final long serialVersionUID = 1L;
		private final int resourceID;
		private final String extra;

		public LocalizedException(int resourceID, String extra) {
			this.resourceID = resourceID;
			this.extra = extra;
		}

		public LocalizedException(int resourceID) {
			this(resourceID, null);
		}

		public String getString(Context context) {
			if (extra == null)
				return context.getString(resourceID);
			else
				return context.getString(resourceID, extra);
		}
	}

	/**
	 * Писать заголовки запросов в catlog.
	 */
	private static final boolean LOG = DEBUG;

	/**
	 * Писать запросы и ответы в файлы.
	 */
	private static final boolean WRITE = DEBUG;

	/**
	 * Формат имени файла для записи запросов и ответов.
	 */
	public final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");

	/**
	 * Ключ авторизации для геокодинга.
	 */
	private static final String YANDEX_KEY = "ACG3DU0BAAAA6wkJGgQAgRY2ChXSECRoRN9yDmLrz_wSRYQAAAAAAAAAAABC1YjXH-X5KWN_NyvBCtLgrvoZSg==";

	/**
	 * URL геокодера.
	 */
	private static final String YANDEX_URL = "http://geocode-maps.yandex.ru/1.x/?format=json&key=%s&geocode=%s%%2C%s&results=1";

	/**
	 * Адрес регистрации через веб.
	 */
	public static final String REGISTER_URL = "http://st1234.greensight.ru/personal/holes.php?register=yes";

	/**
	 * XML хост РосЯмы.
	 */
	private static final String HOST;
	static {
		if (DEBUG)
			HOST = "http://xml-st1234.greensight.ru";
		else
			HOST = "http://xml.rosyama.ru";
	}

	/**
	 * Адрес для авторизации.
	 */
	private static final String AUTHORIZE_PATH = "/authorize/";

	/**
	 * Адрес для добавления дефекта.
	 */
	private static final String HOLE_PATH = "/add/";

	/**
	 * Адрес для получения ФИО главы ГИБДД.
	 */
	private static final String HEAD_PATH = "/my/%s/getgibddhead/";

	/**
	 * Адрес для запроса PDF файла.
	 */
	private static final String PDF_PATH = "/my/%s/pdf_gibdd/";

	/**
	 * Неправильный логин и/или пароль.
	 */
	private static final Object WRONG_CREDENTIALS = "WRONG_CREDENTIALS";

	/**
	 * Требуется авторизация (не прошла авторизация).
	 */
	private static final Object AUTHORIZATION_REQUIRED = "AUTHORIZATION_REQUIRED";

	/**
	 * Не возможно добавить деффект.
	 */
	private static final Object CANNOT_ADD_DEFECT = "CANNOT_ADD_DEFECT";

	/**
	 * Состояние приложения.
	 */
	private State state;

	/**
	 * Используемый HTTP клиент.
	 */
	private DefaultHttpClient client;

	/**
	 * Имя пользователя.
	 */
	private String login;

	/**
	 * Пароль.
	 */
	private String password;

	/**
	 * Путь до фотографии.
	 */
	private String path;

	/**
	 * Место съемки.
	 */
	private Location location;

	/**
	 * Адрес дефекта.
	 */
	private String address;

	/**
	 * Комментарий к дефекту.
	 */
	private String comment;

	/**
	 * Тип дефекта.
	 */
	private Type type;

	/**
	 * Присвоенный номер ямы.
	 */
	private String id;

	/**
	 * Собственное ФИО.
	 */
	private String from;

	/**
	 * Глава ГИБДД.
	 */
	private String to;

	/**
	 * Собственный почтовый адрес.
	 */
	private String postAddress;

	/**
	 * Собственная подпись (Фамилия, инициалы).
	 */
	private String signature;

	/**
	 * Полученный pdf файл.
	 */
	private File pdfFile;

	/**
	 * Системные настройки.
	 */
	private SharedPreferences settings;

	/**
	 * Фабрика для DOM парсера.
	 */
	private DocumentBuilderFactory documentBuilderFactory;

	/**
	 * Слушатель изменения состояния.
	 */
	private StateListener stateListener;

	/**
	 * Обработчик событий из фона.
	 */
	private Handler handler;

	/**
	 * Диалог для отображения состояния приложения.
	 */
	private ProgressDialog progressDialog;

	public Rosyama() {
		client = new DefaultHttpClient();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		state = State.idle;
		login = null;
		password = null;
		from = null;
		signature = null;
		postAddress = null;

		path = null;
		location = null;
		address = null;
		type = Type.holeonroad;
		comment = null;
		id = null;
		to = null;
		pdfFile = null;

		stateListener = null;
		handler = new Handler();

		// NEW

		holes = new ArrayList<Hole>();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		login = settings.getString(getString(R.string.login_key), "");
		password = settings.getString(getString(R.string.password_key), "");
		from = settings.getString(getString(R.string.from_key), "");
		signature = settings.getString(getString(R.string.signature_key), "");
		postAddress = settings.getString(getString(R.string.postaddress_key),
				"");
		if (!"".equals(password))
			state = State.authComplited;

		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
	}

	/**
	 * Обработчик изменения состояния приложения в UI потоке.
	 */
	@Override
	public void run() {
		if (stateListener != null) {
			stateListener.onStateChange();
			Integer action = state.getAction();
			if (action == null) {
				try {
					progressDialog.dismiss();
				} catch (IllegalArgumentException e) {
				}
			} else {
				progressDialog.setMessage(getString(action));
				progressDialog.show();
			}
		}
	}

	/**
	 * Устанавливает случателя за изменения состояния приложения. Обычно
	 * вызывается из {@link Activity#onResume} и из {@link Activity#onPause}.
	 * 
	 * @param activity
	 */
	public void setStateListener(StateListener stateListener) {
		this.stateListener = stateListener;
		run();
	}

	/**
	 * Устанавливает статус и вызывает обработчик в UI потоке.
	 * 
	 * @param state
	 */
	private void setState(State state) {
		synchronized (this) {
			this.state = state;
			handler.post(this);
		}
	}

	/**
	 * Возвращает состояние приложения.
	 * 
	 * @return
	 */
	public State getState() {
		return state;
	}

	/**
	 * Адрес.
	 * 
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Комментарий.
	 * 
	 * @return
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Собственное имя.
	 * 
	 * @return
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * ФИО главы ГИБДД.
	 * 
	 * @return
	 */
	public String getTo() {
		return to;
	}

	/**
	 * Собственный почтовый адрес.
	 * 
	 * @return
	 */
	public String getPostAddress() {
		return postAddress;
	}

	/**
	 * Собственная подпись.
	 * 
	 * @return
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * pdf файл.
	 * 
	 * @return
	 */
	public File getPdfFile() {
		return pdfFile;
	}

	/**
	 * Возвращает прочитанный контент. Не забудьте вызвать
	 * {@link #consumeEntity(HttpEntity)} после использования контента.
	 * 
	 * @param request
	 * @return
	 * @throws LocalizedException
	 */
	private HttpEntity getResponse(HttpUriRequest request)
			throws LocalizedException {
		try {
			if (LOG)
				System.out.println("Request: "
						+ request.getRequestLine().toString());
			HttpResponse response = client.execute(request);
			if (LOG)
				System.out.println("Response: " + response.getStatusLine());
			HttpEntity entity = response.getEntity();
			if (entity == null)
				throw new LocalizedException(R.string.data_fail);
			return entity;
		} catch (ClientProtocolException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.connection_fail);
		} catch (IOException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.io_fail);
		}
	}

	/**
	 * Освобождает ресурсы, занятые наполнение для запроса.
	 * 
	 * @param entity
	 * @throws LocalizedException
	 */
	private void consumeEntity(HttpEntity entity) throws LocalizedException {
		try {
			entity.consumeContent();
		} catch (IOException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.io_fail);
		}
	}

	/**
	 * Возвращает прочитанный контент.
	 * 
	 * @param request
	 * @return
	 * @throws LocalizedException
	 */
	private String getContent(HttpUriRequest request) throws LocalizedException {
		HttpEntity entity = getResponse(request);
		try {
			String content = EntityUtils.toString(entity);
			if (WRITE) {
				try {
					FileOutputStream stream = openFileOutput(
							getNextFileName(".raw"),
							Context.MODE_WORLD_WRITEABLE);
					stream.write(content.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return content;
		} catch (IOException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.io_fail);
		} finally {
			consumeEntity(entity);
		}
	}

	/**
	 * Добавляет наполнение в запрос и логирует действие. Не забудьте вызвать
	 * {@link #consumeEntity(HttpEntity)} после отправки запроса.
	 * 
	 * @param post
	 * @param entity
	 */
	private void setEntity(HttpPost post, HttpEntity entity) {
		if (WRITE) {
			try {
				FileOutputStream stream;
				stream = openFileOutput(getNextFileName(".post"),
						Context.MODE_WORLD_WRITEABLE);
				entity.writeTo(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		post.setEntity(entity);
	}

	/**
	 * Возвращает прочитанный контент.
	 * 
	 * @param post
	 * @param entity
	 * @return
	 * @throws LocalizedException
	 */
	private String getContent(HttpPost post, HttpEntity entity)
			throws LocalizedException {
		setEntity(post, entity);
		try {
			return getContent(post);
		} finally {
			consumeEntity(entity);
		}
	}

	/**
	 * Возвращает корневой DOM элемент.
	 * 
	 * @param post
	 * @param entity
	 * @return
	 * @throws LocalizedException
	 */
	private Element getElement(HttpPost post, HttpEntity entity)
			throws LocalizedException {
		String content = getContent(post, entity);
		Document dom;
		try {
			DocumentBuilder builder;
			builder = documentBuilderFactory.newDocumentBuilder();
			dom = builder.parse(new ByteArrayInputStream(content
					.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.data_fail);
		} catch (SAXException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.data_fail);
		} catch (IOException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.io_fail);
		}
		Element element = dom.getDocumentElement();
		NodeList nodeList = element.getElementsByTagName("error");
		for (int index = 0; index < nodeList.getLength(); index++) {
			Node node = nodeList.item(index);
			String code = node.getAttributes().getNamedItem("code")
					.getNodeValue();
			System.out.println(code);
			if (code.equals(WRONG_CREDENTIALS))
				throw new LocalizedException(R.string.auth_fail);
			if (code.equals(AUTHORIZATION_REQUIRED))
				throw new LocalizedException(R.string.auth_required);
			if (code.equals(CANNOT_ADD_DEFECT)) {
				StringBuffer buffer = new StringBuffer();
				NodeList chars = node.getChildNodes();
				for (int piece = 0; piece < chars.getLength(); piece++)
					buffer.append(chars.item(piece).getNodeValue());
				throw new LocalizedException(R.string.cannot_add_defect,
						buffer.toString());
			}
		}
		return element;
	}

	/**
	 * Возвращает тело поста с данными из формы.
	 * 
	 * @param form
	 *            сожержит имена аргументов и их значения
	 * @return
	 */
	private HttpEntity getEntity(Map<String, String> form) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : form.entrySet())
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry
					.getValue()));
		UrlEncodedFormEntity encodedFormEntity;
		try {
			encodedFormEntity = new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return encodedFormEntity;
	}

	/**
	 * /** Возвращает тело поста с данными из формы и контентом из файлов.
	 * 
	 * @param form
	 *            сожержит имена аргументов и их значения
	 * @param files
	 *            содержит имена файлов и путь до файла
	 * @return
	 */
	private HttpEntity getEntity(Map<String, String> form,
			Map<String, String> files) {
		MultipartEntity multipartEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		// Не следует передавать в конструктор boundary и charset.
		try {
			for (Entry<String, String> entry : form.entrySet())
				multipartEntity.addPart(
						entry.getKey(),
						new StringBody(entry.getValue(), Charset
								.forName(HTTP.UTF_8)));
			for (Entry<String, String> entry : files.entrySet())
				multipartEntity.addPart(entry.getKey(), new FileBody(new File(
						entry.getValue()), "image/jpeg"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return multipartEntity;
	}

	/**
	 * Выйти и забыть пароль.
	 */
	public void logout() {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getString(R.string.password_key), "");
		editor.commit();
		setState(State.idle);
	}

	/**
	 * Проверка авторизации.
	 * 
	 * @param login
	 * @param password
	 * @return
	 * @throws LocalizedException
	 */
	public void authorize(String login, String password)
			throws LocalizedException {
		synchronized (this) {
			if (state != State.idle && state != State.authComplited
					&& state != State.photoComplited
					&& state != State.geoComplited
					&& state != State.holeComplited
					&& state != State.headComplited
					&& state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.authRequest);
		}
		try {
			HttpPost post = new HttpPost(HOST + AUTHORIZE_PATH);
			HashMap<String, String> form = new HashMap<String, String>();
			form.put("login", login);
			form.put("password", password);
			getElement(post, getEntity(form));
			this.login = login;
			this.password = password;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(getString(R.string.login_key), login);
			editor.putString(getString(R.string.password_key), password);
			editor.commit();
			setState(State.authComplited);
		} catch (LocalizedException e) {
			setState(State.idle);
			throw e;
		}
	}

	/**
	 * Фотография сделана.
	 * 
	 * @param path
	 * @throws LocalizedException
	 */
	public void photo(String path) throws LocalizedException {
		synchronized (this) {
			if (state != State.authComplited && state != State.photoComplited
					&& state != State.geoComplited
					&& state != State.holeComplited
					&& state != State.headComplited
					&& state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.photoComplited);
		}
		this.path = path;
	}

	/**
	 * Определение текущего местоположения и геокодинг.
	 * 
	 * @throws LocalizedException
	 */
	public void geo() throws LocalizedException {
		synchronized (this) {
			if (state != State.photoComplited && state != State.geoComplited
					&& state != State.holeComplited
					&& state != State.headComplited
					&& state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.geoRequest);
		}
		try {
			address = "";
			comment = "";

			// Получение гео данных
			Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
			Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
					.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
			location = net;
			// Определение лучших данных
			if (gps != null && net != null) {
				if (gps.getTime() > net.getTime())
					location = gps;
				else if (net.hasAccuracy()) {
					float[] results = new float[1];
					Location.distanceBetween(net.getLatitude(),
							net.getLongitude(), gps.getLatitude(),
							gps.getLongitude(), results);
					if (results[0] < net.getAccuracy())
						location = gps;
				}
			} else if (net == null)
				location = gps;
			// TODO: предусмотреть определение координат, по введенному
			// пользователем адресу.
			if (location == null) {
				location = new Location("GPS");
				location.setLatitude(55.693163);
				location.setLongitude(37.707086);
				if (!DEBUG)
					return;
			}

			HttpGet get = new HttpGet(String.format(YANDEX_URL, YANDEX_KEY,
					getPoint(location.getLongitude()),
					getPoint(location.getLatitude())));
			String content = getContent(get);
			Object json = new JSONTokener(content).nextValue();
			JSONArray array = (JSONArray) ((JSONObject) ((JSONObject) ((JSONObject) json)
					.get("response")).get("GeoObjectCollection"))
					.get("featureMember");
			if (array.length() != 1)
				throw new LocalizedException(R.string.geo_fail);
			JSONObject object = (JSONObject) (JSONObject) ((JSONObject) array
					.get(0)).get("GeoObject");
			this.address = ((JSONObject) ((JSONObject) object
					.get("metaDataProperty")).get("GeocoderMetaData"))
					.getString("text");
		} catch (JSONException e) {
			if (LOG)
				e.printStackTrace();
			throw new LocalizedException(R.string.data_fail);
		} finally {
			setState(State.geoComplited);
		}
	}

	/**
	 * Отправка дефекта.
	 * 
	 * @param address
	 * @param comment
	 * @throws LocalizedException
	 */
	public void hole(String address, String comment) throws LocalizedException {
		synchronized (this) {
			if (state != State.geoComplited && state != State.holeComplited
					&& state != State.headComplited
					&& state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.holeRequest);
		}
		this.address = address;
		this.comment = comment;
		try {
			HttpPost post = new HttpPost(HOST + HOLE_PATH);
			HashMap<String, String> form = new HashMap<String, String>();
			form.put("login", login);
			form.put("password", password);
			form.put("address", address);
			form.put("latitude", getPoint(location.getLatitude()));
			form.put("longitude", getPoint(location.getLongitude()));
			form.put("comment", comment);
			form.put("type", type.toString());
			HashMap<String, String> files = new HashMap<String, String>();
			files.put("photo", path);
			Element element = getElement(post, getEntity(form, files));
			NodeList nodeList = element.getElementsByTagName("callresult");
			if (nodeList.getLength() != 1)
				throw new LocalizedException(R.string.hole_fail);
			Node node = nodeList.item(0);
			if (!"1".equals(node.getAttributes().getNamedItem("result")
					.getNodeValue()))
				throw new LocalizedException(R.string.hole_fail);
			id = node.getAttributes().getNamedItem("inserteddefectid")
					.getNodeValue();
			setState(State.holeComplited);
		} catch (LocalizedException e) {
			setState(State.geoComplited);
			throw e;
		}
	}

	/**
	 * Запрос главы района.
	 * 
	 * @throws LocalizedException
	 */
	public void head() throws LocalizedException {
		synchronized (this) {
			if (state != State.holeComplited && state != State.headComplited
					&& state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.headRequest);
		}
		try {
			HttpPost post = new HttpPost(HOST + String.format(HEAD_PATH, id));
			HashMap<String, String> form = new HashMap<String, String>();
			form.put("login", login);
			form.put("password", password);
			Element element = getElement(post, getEntity(form));
			NodeList nodeList = element.getElementsByTagName("gibddhead");
			if (nodeList.getLength() != 1)
				throw new LocalizedException(R.string.head_fail);
			Node node = nodeList.item(0);
			nodeList = node.getChildNodes();
			to = "";
			for (int index = 0; index < nodeList.getLength(); index++) {
				node = nodeList.item(index);
				if (node.getNodeName().equals("dative")) {
					String toPost = node.getAttributes().getNamedItem("post")
							.getNodeValue();
					StringBuffer buffer = new StringBuffer(toPost);
					if (buffer.length() > 0)
						buffer.append(", ");
					NodeList chars = node.getChildNodes();
					for (int piece = 0; piece < chars.getLength(); piece++)
						buffer.append(chars.item(piece).getNodeValue());
					to = buffer.toString();
					break;
				}
			}
			if ("".equals(to))
				throw new LocalizedException(R.string.head_fail);
		} finally {
			setState(State.headComplited);
		}
	}

	public void pdf(String to, String from, String postAddress, String address,
			String signature) throws LocalizedException {
		synchronized (this) {
			if (state != State.headComplited && state != State.pdfComplited)
				throw new LocalizedException(R.string.status_fail);
			setState(State.pdfRequest);
		}
		this.address = address;
		this.to = to;
		this.from = from;
		this.signature = signature;
		this.postAddress = postAddress;
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getString(R.string.from_key), from);
		editor.putString(getString(R.string.signature_key), signature);
		editor.putString(getString(R.string.postaddress_key), postAddress);
		editor.commit();
		try {
			HttpPost post = new HttpPost(HOST + String.format(PDF_PATH, id));
			HashMap<String, String> form = new HashMap<String, String>();
			form.put("login", login);
			form.put("password", password);
			form.put("to", this.to);
			form.put("from", this.from);
			form.put("postaddress", this.postAddress);
			form.put("holeaddress", this.address);
			form.put("signature", this.signature);
			HttpEntity postEntity = getEntity(form);
			setEntity(post, postEntity);
			HttpEntity postResponse;
			try {
				postResponse = getResponse(post);
			} finally {
				consumeEntity(postEntity);
			}
			pdfFile = new File(Environment.getExternalStorageDirectory(), id
					+ ".pdf");
			FileOutputStream stream;
			try {
				stream = new FileOutputStream(pdfFile);
				postResponse.writeTo(stream);
			} catch (IOException e) {
				throw new LocalizedException(R.string.pdf_fail);
			}
			consumeEntity(postResponse);
			setState(State.pdfComplited);
		} catch (LocalizedException e) {
			setState(State.headComplited);
			throw e;
		}
	}

	private static String getPoint(double value) {
		return Location.convert(value, Location.FORMAT_DEGREES).replace(',',
				'.');
	}

	private static String getNextFileName(String extention) {
		return Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention;
	}

	public static Uri getNextJpegUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				getNextFileName(".jpeg")));
	}

	/**
	 * Список дефектов.
	 */
	ArrayList<Hole> holes;

	/**
	 * Возвращает все дефекты.
	 * 
	 * @return
	 */
	public Collection<Hole> getHoles() {
		return Collections.unmodifiableCollection(holes);
	}

	/**
	 * Возвращает дефект по ещё номеру.
	 * 
	 * @param id
	 *            используйте <code>null</code> для нового дефекта
	 * @return может вернуть <code>null</code>, если дефект не найден.
	 */
	public Hole getHole(Integer id) {
		for (Hole hole : holes)
			if (hole.getId() == id)
				return hole;
		return null;
	}

	/**
	 * Создание не отправленного дефекта.
	 * 
	 * @param path
	 *            путь до фотографии.
	 */
	public void createHole(Uri path) {
		// Получение гео данных
		Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
				.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
		Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
				.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
		Location location = net;
		// Определение лучших данных
		if (gps != null && net != null) {
			if (gps.getTime() > net.getTime())
				location = gps;
			else if (net.hasAccuracy()) {
				float[] results = new float[1];
				Location.distanceBetween(net.getLatitude(), net.getLongitude(),
						gps.getLatitude(), gps.getLongitude(), results);
				if (results[0] < net.getAccuracy())
					location = gps;
			}
		} else if (net == null)
			location = gps;
		// Удаление неотправленного дефекта.
		Hole hole = getHole(null);
		if (hole != null)
			holes.remove(hole);
		// Добавление нового дефекта.
		holes.add(new Hole(path, location));
	}
}
