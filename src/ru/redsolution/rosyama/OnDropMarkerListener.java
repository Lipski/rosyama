package ru.redsolution.rosyama;

import com.google.android.maps.OverlayItem;

/**
 * Слушатель отпускания маркера на новом месте.
 * 
 * @author alexander.ivanov
 * 
 */
public interface OnDropMarkerListener {

	/**
	 * Маркер был установлен в новое место.
	 * 
	 * @param overlayItem
	 */
	void onDropMarker(OverlayItem overlayItem);
}
