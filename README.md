# SafeAsyncTask
Android中更安全的使用AsyncTask

## 0x00 背景
我们都知道，Android中需要注意内存泄露的操作有：

* __BroadCastReceiver__，注册和取消注册要成对存在。
* __BindService__，绑定服务和解绑服务要成对出现。
* __Thread__，一般在四大组件中创建的Thread在组建销毁的时候要Stop掉。
* __Handler__，通过Handler发送的消息未在组件生命周期结束的时候及时移出。
* __AsyncTask__，组件生命周期结束，但是其子线程仍旧在执行，导致组件不能及时释放。

我们今天就来针对AsyncTask来解决其有可能导致的内存泄露问题。


## 0x01 分析
想解决问题，首先得分析问题产生的原因。

AsyncTask之所以可能会产生内存泄露，是因为：

1. 我们一般在组件内部，以内部类的方式创建`AsyncTask`，而java里面，内部类是默认持有外部类的引用。
2. 我们的加载数据是在子线程中执行，但是java里面有没有提供很好的中断机制来中断线程 __（有中断方法，但是如果你不针对性的处理，这个方法也没什么卵用）__，这样就导致组件生命周期已经结束，应该被GC回收，但是由于子线程正在执行，内部类无法回收，进而导致组件无法正常回收，所以造成了内存泄露 __（Handler导致内存泄露也是类似的道理）__。

## 0x02 问题分析与解决
针对问题一个一个解决

1. 针对内部类持有外部类引用，解决的方式是用 __静态内部类__ 或者把类单独抽出来（本文中做法是把`AsyncTask`抽出来）。
2. 针对组件声明周期结束的时候，子线程仍在执行的问题，应该在组件生命周期结束的时候取消掉（`cancel(boolean mayInterruptIfRunning)`）异步任务, __并且__ 在子线程执行任务的时候要及时判断当前任务是否被取消了，及时中断异步任务。

上面两个方法解决了导致内存泄露的问题，但是又产生了一个新的问题，组件跟异步任务怎么通讯，异步任务怎么通知组件自己当前的状态（准备执行，执行结束，执行取消等）。

1. 组件跟异步任务通许，可以通过基类定义构造方法传递数据和pulic方法供组件调用。
2. 异步任务跟组件通讯可以通过 __接口__，这里的灵感来自最近大火的 __MVP__ 中的接口隔离的思想（不知道这么说准确不准确）。

构造异步任务的时候传入接口，取消任务的时候把接口置空，这样就切断了异步任务和组件之间的关系。子线程执行的时候及时判断当前状态，如果是取消的话就及时中断任务。

完美解决！！！2333

## 0x03 代码实现
最终解决需要以下几个要素：

1. 回调接口 __AsyncTaskCallBack__ 
	
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

2. 异步任务基类 __BaseAsyncTask__
		
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

3. 使用

	使用起来也很简单
		
	1. 实现 __AsyncTaskCallBack__ 接口
	2. 写异步任务继承 __BaseAsyncTask__

			protected List<Person> doInBackground(String... params) {
				List<Person> persons = new ArrayList<Person>();
				for (int i = 0; i < 10; i++) {
					//如果是取消了，则直接跳出循环，减少无用功。
					if (isCancelled()) break;
					SystemClock.sleep(500);
					Person person = new Person();
					person.setName("Name   "+i);
					persons.add(person);
				}
				return persons;
			}			

	3. 创建异步任务并执行
		
			//执行异步任务前别忘了先取消上次的任务
			if (mLoadDataTask != null) mLoadDataTask.cancelTask();
			mLoadDataTask = new LoadDataTask(this);
			//AsyncTask的excute方法是历尽转折，详情请参照 其他 中对于他的介绍。
			mLoadDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "url");

具体代码详见Demo，欢迎提出意见。

## 0x04 其他

使用AsyncTask时发现一个奇怪的现象，即创建多个任务的时候，他是一个一个按顺序执行的，查资料之后发现：

__在1.5中初始引入的时候__， AsyncTask 执行( AsyncTask.execute() )起来是顺序的，当同时执行多个 AsyncTask的时候，他们会按照顺序一个一个执行。前面一个执行完才会执行后面一个。这样当同时执行多个比较耗时的任务的时候 可能不是您期望的结果，具体情况就像是execute的task不会被立即执行，要等待前面的task执行完毕后才可以执行。

__在android 1.6(Donut) 到 2.3.2(Gingerbread)中__，AsyncTask的执行顺序修改为并行执行了。如果同时执行多个任务，则这些任务会并行执行。 当任务访问同一个资源的时候 会出现并发问题.

__而在Android 3.0(Honeycomb)以后的版本中__，AsyncTask又修改为了顺序执行，并且新添加了一个函数 executeOnExecutor(Executor)，如果您需要并行执行，则只需要调用该函数，并把参数设置为并行执行即可。

即创建一个单独的线程池(Executors.newCachedThreadPool())。或者最简单的方法法就是使用executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)，这样起码不用等到前面的都结束了再执行了。executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)则与execute()是一样的。

## 0x05 关于
Author peerless2012

Email  [peerless2012@126.con](mailto:peerless2012@126.con)

Blog   [peerless2012.github.io](https://peerless2012.github.io)