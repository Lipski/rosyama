package ru.redsolution.rosyama.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.location.Location;
import android.net.Uri;

public class Hole {
	/**
	 * Приложение.
	 */
	private final Rosyama rosyama;

	/**
	 * Идентификатор на сайте.
	 */
	private String id;

	/**
	 * Дата размещения на сайте.
	 */
	private Date created;

	/**
	 * Координаты.
	 */
	private Location location;

	/**
	 * Адрес.
	 */
	private String address;

	/**
	 * Тип.
	 */
	private Type type;

	/**
	 * Статус.
	 */
	private Status status;

	/**
	 * Комментарий.
	 */
	private String comment;

	/**
	 * Список фотографий.
	 */
	private ArrayList<AbstractPhoto> photos;

	/**
	 * Глава ГИБДД.
	 */
	private String to;

	/**
	 * Конструктор неотправленного дефекта.
	 * 
	 * @param rosyama
	 *            Приложение.
	 * @param path
	 *            Путь до фотографии.
	 * @param location
	 *            Координаты, может быть <code>null</code>.
	 */
	public Hole(Rosyama rosyama, Uri path, Location location) {
		this.rosyama = rosyama;
		id = null;
		created = null;
		this.location = location;
		address = "";
		type = null;
		status = Status.fresh;
		comment = "";
		to = "";
		photos = new ArrayList<AbstractPhoto>();
		photos.add(new LocalPhoto(rosyama, path));
	}

	public Hole(Rosyama rosyama, String id, Date created, Location location,
			String address, Type type, Status status, String comment,
			ArrayList<RemotePhoto> photos) {
		this.rosyama = rosyama;
		this.id = id;
		this.created = created;
		this.location = location;
		this.address = address;
		this.type = type;
		this.status = status;
		this.comment = comment;
		this.to = "";
		this.photos = new ArrayList<AbstractPhoto>(photos);
	}

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public String getCreatedText() {
		return Rosyama.DATE_FORMAT.format(created);
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
		rosyama.onUpdate();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
		rosyama.onUpdate();
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		rosyama.onUpdate();
	}

	public Status getStatus() {
		return status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		rosyama.onUpdate();
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
		rosyama.onUpdate();
	}

	/**
	 * Возвращает список фотографий помеченных для удаления.
	 * 
	 * @return
	 */
	public List<RemotePhoto> getPhotosToRemove() {
		ArrayList<RemotePhoto> result = new ArrayList<RemotePhoto>();
		for (AbstractPhoto photo : photos)
			if (photo instanceof RemotePhoto) {
				RemotePhoto remotePhoto = (RemotePhoto) photo;
				if (remotePhoto.isToRemove())
					result.add(remotePhoto);
			}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Возвращает список фотографий для отправки.
	 * 
	 * @return
	 */
	public List<LocalPhoto> getPhotosToSend() {
		ArrayList<LocalPhoto> result = new ArrayList<LocalPhoto>();
		for (AbstractPhoto photo : photos)
			if (photo instanceof LocalPhoto)
				result.add((LocalPhoto) photo);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Возвращает список видимых фотографий.
	 * 
	 * @return
	 */
	public List<AbstractPhoto> getVisiblePhotos() {
		ArrayList<AbstractPhoto> result = new ArrayList<AbstractPhoto>();
		for (AbstractPhoto photo : photos)
			if (!(photo instanceof RemotePhoto)
					|| !((RemotePhoto) photo).isToRemove())
				result.add(photo);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Добавляет новую фотографию для неотправленного дефекта.
	 * 
	 * @param photo
	 */
	public void createPhoto(Uri path) {
		photos.add(new LocalPhoto(rosyama, path));
		rosyama.onUpdate();
	}

	/**
	 * Удаляет фотографию.
	 * 
	 * @param photo
	 */
	public void removePhoto(AbstractPhoto photo) {
		if (photo instanceof RemotePhoto)
			((RemotePhoto) photo).remove();
		else
			photos.remove(photo);
		rosyama.onUpdate();
	}
}
