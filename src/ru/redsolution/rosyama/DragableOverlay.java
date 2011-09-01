/***
  Copyright (c) 2008-2011 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

package ru.redsolution.rosyama;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Resource:
 * https://github.com/commonsguy/cw-advandroid/tree/master/Maps/NooYawkTouch/
 */
public class DragableOverlay extends ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> items;
	private Drawable marker;
	private OverlayItem inDrag;
	private ImageView dragImage;
	private int xDragTouchOffset;
	private int yDragTouchOffset;
	private final int xDragImageSize;
	private final int yDragImageSize;
	private final int xDragImageOffset;
	private final int yDragImageOffset;
	private OnDropMarkerListener onDropMarkerListener;

	public DragableOverlay(Drawable marker, ImageView dragImage) {
		super(marker);
		this.items = new ArrayList<OverlayItem>();
		this.marker = marker;
		this.inDrag = null;
		this.dragImage = dragImage;
		this.xDragTouchOffset = 0;
		this.yDragTouchOffset = 0;
		this.xDragImageSize = dragImage.getDrawable().getIntrinsicWidth();
		this.yDragImageSize = dragImage.getDrawable().getIntrinsicHeight();
		this.xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
		this.yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();
		this.onDropMarkerListener = null;
		populate();
	}

	/**
	 * Устанавливает слушатель за установкой нового положения маркера.
	 * 
	 * @param onDropMarkerListener
	 */
	public void setOnDropMarkerListener(
			OnDropMarkerListener onDropMarkerListener) {
		this.onDropMarkerListener = onDropMarkerListener;
	}

	/**
	 * Добавляет новый элемент для отображения.
	 * 
	 * @param overlayItem
	 */
	public void addItem(OverlayItem overlayItem) {
		items.add(overlayItem);
		populate();
	}

	/**
	 * Добавляет группу элементов для отображения.
	 * 
	 * @param overlayItems
	 */
	public void addItems(Collection<OverlayItem> overlayItems) {
		items.addAll(overlayItems);
		populate();
	}

	/**
	 * Удаляет элемент для отображения.
	 * 
	 * @param overlayItem
	 */
	public void removeItem(OverlayItem overlayItem) {
		items.remove(overlayItem);
		populate();
	}

	/**
	 * Удаляет группу элементов для отображения.
	 * 
	 * @param overlayItems
	 */
	public void removeItems(Collection<OverlayItem> overlayItems) {
		items.removeAll(overlayItems);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return (items.get(i));
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		boundCenterBottom(marker);
	}

	@Override
	public int size() {
		return (items.size());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		boolean result = false;

		if (action == MotionEvent.ACTION_DOWN) {
			for (OverlayItem item : items) {
				Point p = new Point(0, 0);

				mapView.getProjection().toPixels(item.getPoint(), p);

				if (hitTest(item, marker, x - p.x, y - p.y)) {
					result = true;
					inDrag = item;
					items.remove(inDrag);
					populate();

					xDragTouchOffset = 0;
					yDragTouchOffset = 0;

					setDragImagePosition(p.x, p.y);
					dragImage.setVisibility(View.VISIBLE);

					xDragTouchOffset = x - p.x;
					yDragTouchOffset = y - p.y;

					break;
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
			setDragImagePosition(x, y);
			result = true;
		} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
			dragImage.setVisibility(View.GONE);

			GeoPoint pt = mapView.getProjection().fromPixels(
					x - xDragTouchOffset, y - yDragTouchOffset);
			OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
					inDrag.getSnippet());

			items.add(toDrop);
			populate();

			inDrag = null;
			result = true;
			if (onDropMarkerListener != null)
				onDropMarkerListener.onDropMarker(toDrop);
		}

		return (result || super.onTouchEvent(event, mapView));
	}

	private void setDragImagePosition(int x, int y) {
		MarginLayoutParams lp = (MarginLayoutParams) dragImage
				.getLayoutParams();

		// Правый и нижний отступ позволяют избежать изменения размеров
		// изображения на краях экрана.
		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
				- yDragImageOffset - yDragTouchOffset, -xDragImageSize,
				-yDragImageSize);
		dragImage.setLayoutParams(lp);
	}
}
