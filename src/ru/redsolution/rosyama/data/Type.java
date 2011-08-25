package ru.redsolution.rosyama.data;

/**
 * Тип дефекта.
 * 
 * @author alexander.ivanov
 * 
 */
public enum Type {
	/**
	 * Разбитая дорога.
	 */
	badroad,

	/**
	 * Яма на дороге.
	 */
	holeonroad,

	/**
	 * Люк.
	 */
	hatch,

	/**
	 * Переезд (не используется).
	 */
	crossing,

	/**
	 * Отсутствие разметки (не используется).
	 */
	nomarking,

	/**
	 * Рельсы.
	 */
	rails,

	/**
	 * Лежачий полицейский.
	 */
	policeman,

	/**
	 * Ограждение (не используется).
	 */
	fence,

	/**
	 * Яма во дворе.
	 */
	holeinyard,

	/**
	 * Неисправный светофор (не используется).
	 */
	light;

	/**
	 * Возвращает ресурс с изображением типа дефекта.
	 * 
	 * @return
	 */
	int getResourceID() {
		return 0;
	}
}