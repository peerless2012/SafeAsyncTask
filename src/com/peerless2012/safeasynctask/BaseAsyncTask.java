package com.peerless2012.safeasynctask;

import android.os.AsyncTask;
/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月22日 上午9:12:28
* @Version V1.0
* @Description: AsyncTask基类
*/
public abstract class BaseAsyncTask<Params, Result> extends
		AsyncTask<Params, Void, Result> {

	private AsyncTaskCallBack<Result> mLoadDataCallBack;

	public BaseAsyncTask(AsyncTaskCallBack<Result> callBack) {
		mLoadDataCallBack = callBack;
	}

	/**
	 * 取消异步任务，一般在Activity或者Fragment的生命周期结束({@link android.app.Activity#onDestroy() onDestroy()})中调用此方法
	 * <p>
	 * <b>请调用此方法取消任务，而不是 {@link #cancel(boolean)}}方法</b>
	 */
	final public void cancelTask() {
		if (getStatus() != Status.FINISHED) {
			cancel(true);
		}
	}
	
	@Override
	final protected void onPreExecute() {
		super.onPreExecute();
		if (mLoadDataCallBack != null) {
			mLoadDataCallBack.onPreExecute();
		}
	}
	
	@Override
	final protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		if (mLoadDataCallBack != null) {
			mLoadDataCallBack.onPostExecute(result);
		}
	}
	
	@Override
	final protected void onCancelled() {
		super.onCancelled();
		if (mLoadDataCallBack != null) {
			mLoadDataCallBack.onCancled();
		}
		//无法放到 cancelTask()中。
		//因为此方法会在cancelTask()后执行，所以如果放到cancelTask()中，则此字段永远是空，也就不会调用 onCancel()方法了
		mLoadDataCallBack = null;
	}
	
}
