package ru.redsolution.rosyama.data;

import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Абстрактное задание. Обновляет интерфейс до выполнения операции, после её
 * завершения в случае отмены. В случае возникновения ошибки отображает тост.
 * Предполагает выполнение операции, которая может завершиться успехом и вернут
 * значение, либо может завершиться с ошибкой.
 * 
 * @author alexander.ivanov
 * 
 */
public abstract class AbstractTask<Params, Result> extends
		AsyncTask<Params, Integer, ResultOrException<Result>> {
	/**
	 * Приложение.
	 */
	private final Rosyama rosyama;

	AbstractTask(Rosyama rosyama) {
		this.rosyama = rosyama;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		rosyama.onUpdate();
	}

	@Override
	protected ResultOrException<Result> doInBackground(Params... params) {
		try {
			return new ResultOrException<Result>(process(params));
		} catch (LocalizedException e) {
			return new ResultOrException<Result>(e);
		}
	}

	@Override
	protected void onPostExecute(ResultOrException<Result> result) {
		super.onPostExecute(result);
		if (result.exception != null)
			Toast.makeText(rosyama, result.exception.getString(rosyama),
					Toast.LENGTH_LONG).show();
		else
			onSuccess(result.result);
		onFinish();
		rosyama.onUpdate();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		onFinish();
		rosyama.onUpdate();
	}

	/**
	 * Выполняет необходимую задачу в фоне.
	 * 
	 * @param params
	 * @return
	 * @throws LocalizedException
	 */
	abstract Result process(Params... params) throws LocalizedException;

	/**
	 * Вызывается в UI потоке в случае успешного завершения операции.
	 */
	abstract void onSuccess(Result result);

	/**
	 * Вызывается в UI потоке после выполнения операции или после её отмены,
	 * непосредственно перед отправкой уведомления.
	 */
	abstract void onFinish();
}