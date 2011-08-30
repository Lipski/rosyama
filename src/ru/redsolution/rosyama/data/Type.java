package ru.redsolution.rosyama.data;

import ru.redsolution.rosyama.R;

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
	 * Возвращает ресурс с названием дефекта.
	 * 
	 * @return
	 */
	public int getResourceID() {
		if (this == badroad)
			return R.string.type_badroad;
		if (this == holeonroad)
			return R.string.type_holeonroad;
		if (this == hatch)
			return R.string.type_hatch;
		if (this == rails)
			return R.string.type_rails;
		if (this == policeman)
			return R.string.type_policeman;
		if (this == holeinyard)
			return R.string.type_holeinyard;
		return 0;
	}
}