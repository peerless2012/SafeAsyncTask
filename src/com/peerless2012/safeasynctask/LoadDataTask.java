package com.peerless2012.safeasynctask;

import java.util.ArrayList;
import java.util.List;
import android.os.SystemClock;

/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月22日 上午9:12:49
* @Version V1.0
* @Description: 加载数据的异步任务类
*/
public class LoadDataTask extends BaseAsyncTask<String, List<Person>> {

	public LoadDataTask(AsyncTaskCallBack<List<Person>> callBack) {
		super(callBack);
	}
	
	@Override
	protected List<Person> doInBackground(String... params) {
		List<Person> persons = new ArrayList<Person>();
		for (int i = 0; i < 10; i++) {
			if (isCancelled()) break;
			SystemClock.sleep(500);
			Person person = new Person();
			person.setName("Name   "+i);
			persons.add(person);
		}
		return persons;
	}
}
