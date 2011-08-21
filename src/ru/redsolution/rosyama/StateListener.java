package ru.redsolution.rosyama;

/**
 * Интерфейс для получения извещения об изменениях состояния.
 * 
 * @author alexander.ivanov
 * 
 */
public interface StateListener {
	/**
	 * Должно вызываться после каждого изменения статуса приложения в UI потоке.
	 */
	void onStateChange();
}
