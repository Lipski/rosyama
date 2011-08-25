package ru.redsolution.rosyama.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import android.location.Location;
import android.net.Uri;

public class Hole {
	/**
	 * Идентификатор на сайте.
	 */
	private Integer id;

	/**
	 * Дата размещения на сайте.
	 */
	private Date date;

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
	private ArrayList<Photo> photos;

	/**
	 * Конструктор неотправленного дефекта.
	 * 
	 * @param path
	 *            путь до фотографии.
	 * @param location
	 *            координаты, может быть <code>null</code>.
	 */
	public Hole(Uri path, Location location) {
		id = null;
		date = null;
		this.location = location;
		address = null;
		type = null;
		status = Status.fresh;
		comment = null;
		photos = new ArrayList<Photo>();
		photos.add(new Photo(path, null));
	}

	public Integer getId() {
		return id;
	}

	void setId(Integer id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Status getStatus() {
		return status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Collection<Photo> getPhotos() {
		return Collections.unmodifiableCollection(photos);
	}

	/**
	 * Добавляет новую фотографию для неотправленного дефекта.
	 * 
	 * @param photo
	 */
	public void createPhoto(Uri path) {
		photos.add(new Photo(path, null));
	}

	/**
	 * Удаляет фотографию.
	 * 
	 * @param photo
	 */
	public void removePhoto(Photo photo) {
		photos.remove(photo);
	}
}
