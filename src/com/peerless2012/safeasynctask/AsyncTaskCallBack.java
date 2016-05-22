package com.peerless2012.safeasynctask;

import android.support.annotation.UiThread;


/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月22日 上午9:11:49
* @Version V1.0
* @Description: 异步任务的接口回调
*/
@UiThread
public interface AsyncTaskCallBack<T> {
	
	/**
	 * 当异步任务将要开始执行的时候执行
	 */
	public void onPreExecute();
	
	/**
	 * 当结果返回的时候执行
	 * @param t 返回的结果
	 */
	public void onPostExecute(T t);
	
	/**
	 * 当请求被取消的时候执行
	 */
	public void onCancled();
	
}
