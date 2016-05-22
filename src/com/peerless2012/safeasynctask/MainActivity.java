package com.peerless2012.safeasynctask;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月22日 上午9:13:21
* @Version V1.0
* @Description: 测试Activity
*/
public class MainActivity extends Activity implements AsyncTaskCallBack<List<Person>>,OnClickListener{

private TextView mTextView;
	
	private LoadDataTask mLoadDataTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.result);
		findViewById(R.id.start_load).setOnClickListener(this);
		findViewById(R.id.cancel_load).setOnClickListener(this);
	}

	@Override
	public void onPreExecute() {
		mTextView.setText("开始加载！");
	}

	@Override
	public void onPostExecute(List<Person> t) {
		mTextView.setText("获取结果 ： " + t == null? "空" : t.toString());
	}

	@Override
	public void onCancled() {
		mTextView.setText("取消 ");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadDataTask != null) {
			mLoadDataTask.cancelTask();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_load:
			if (mLoadDataTask != null) mLoadDataTask.cancelTask();
			mLoadDataTask = new LoadDataTask(this);
			mLoadDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "url");
			break;
			
		case R.id.cancel_load:
			if (mLoadDataTask != null) mLoadDataTask.cancelTask();
			break;

		default:
			break;
		}
	}
}
