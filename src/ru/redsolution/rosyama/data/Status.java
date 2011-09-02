package ru.redsolution.rosyama.data;

import ru.redsolution.rosyama.R;

/**
 * Статус дефекта.
 * 
 * @author alexander.ivanov
 * 
 */
public enum Status {
	/**
	 * Свежий дефект, только добавлен на сайт
	 */
	fresh,

	/**
	 * В процессе, заявление в ГИБДД
	 */
	inprogress,

	/**
	 * Отремонтирован
	 */
	fixed,

	/**
	 * Просрочен (прошло более 37 дней с момента подачи заявления в ГИБДД,
	 * никакого результата не видно)
	 */
	achtung,

	/**
	 * Отправлена жалоба в прокуратуру на бездействие органов ГИБДД
	 */
	prosecutor,

	/**
	 * Получен ответ из ГИБДД, но дефект не отремонтирован
	 */
	gibddre;

	/**
	 * Возвращает ресурс с названием статуса дефекта.
	 * 
	 * @return
	 */
	public int getResourceID() {
		if (this == fresh)
			return R.string.status_fresh;
		if (this == inprogress)
			return R.string.status_inprogress;
		if (this == fixed)
			return R.string.status_fixed;
		if (this == achtung)
			return R.string.status_achtung;
		if (this == prosecutor)
			return R.string.status_prosecutor;
		if (this == gibddre)
			return R.string.status_gibddre;
		return 0;
	}
}
