#Android 多线程断点下载project总结
											2015.11.8  youngwm
###写在前头的前言O(∩_∩)O：
<br>此断点续传下载是以我通过慕课网
[Android-Service系列之断点续传下载](http://www.imooc.com/learn/363) 的学习后，在实现的基础上，解决了
<br>其中的bug，并对代码进行优化。之前未曾接触过的同学可以通过该视频了解断点续传下载的相关原理。
<br>**功能介绍：**
<br>1、下拉刷新发送下载请求给service端，等待接收到应答后，解析返回的json数据并显示在Listview中；
<br>2、点击【下载】下载该文件，点击【暂停】暂停下载；
<br>3、下载结束后，下载文件信息在【已完成】界面显示；
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/pullRefresh-downAndPause.gif)
<br>4、点击【全部下载】，开启全部文件下载，点击【全部暂停】暂停所有下载；
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/allDownload.gif)
<br>5、listView获取并显示下载文件后，点击【下载】按键后在数据库记录下载线程信息，可通过RE浏览器查下；
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/lookDatabase.gif)
<br>6、在退出时，在OnDestroy（）调用全部暂停方法，存储下载进度；
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/allPause.gif)
<br>
<br>
##一、listView的baseAdapter的使用机制：
<br>
**1、ListView中getView的原理：**
<br>http://www.cnblogs.com/xiaowenji/archive/2010/12/08/1900579.html
<br>Recycler的构件：视图复用
<br>
``` java
	* ViewHolder是一个临时的储存器，把每次getView方法中的每次返回的View缓存起来.可以下次再用。
	* 这样的好处是不必每次都到布局文件中查找控件，只 需调用ViewHoldr即可
	
	private static class ViewHolder{}  // 定义为static内部静态类，这样就只生成一次，比较不耗内存
```
``` java
	@Override
	public View getView(final int position, View view, ViewGroup ViewGroup) {
		if(view == null){
			view = LayoutInflater.from(mContext).inflate(R.layout.item_download, null);
			holder = new ViewHolder();
			holder.tvFile = (TextView) view.findViewById(R.id.tvfilename);
			view.setTag(holder);  // view中setTag表示给View添加一个格外的数据，以后可以用getTag()
		} // 将这个数据取出来，把查找的view缓存起来方便多次重用
		else {	
			holder = (ViewHolder) view.getTag();
		}
		holder.btnstate.setText(DOWNLOAD);
		return view;
	}
```
<br>
<br>
**2、ListView中文API:** 
<br>http://www.cnblogs.com/over140/archive/2010/11/19/1881445.html
<br>notifyDataSetChanged()方法
<br>
<br>
<br>
**3、BaseAdapter中文API：**
<br>http://www.cnblogs.com/over140/archive/2010/12/03/1895128.html
<br>
<br>
<br>
**4、item数量大时，解决LIstView更新进度条致ui卡顿问题：**
<br>思路：**局部刷新**——判断需更新的进度条是否在listView可见视图，是，才更新。
<br>https://github.com/slidese/SGU/blob/3be1888115ba56b4a015b127b249c35af5dc11d0/src/se/slide/sgu/ContentFragment.java
<br>（116行代码处）
<br>
```java
	public void updateListView(int id, double progres, double rate) {
		// 判断下载中的文件是否在可视区，是则更新进度和下载速度
		FileInfo fileInfo = mFileList.get(id);
		int start = mListView.getFirstVisiblePosition(); 
		// 可见视图的首个item的位置
		int end   = mListView.getLastVisiblePosition(); 
		// 可见视图的最后item的位置
		int  position = mFileList.indexOf(fileInfo);
		// 获取需要更新进度的下载文件fileInfo的位置
		//----------
		if( position -start >= 0 && end -position >= 0 ){
			View view = mListView.getChildAt(position -start);
			if( view == null ){
			return;
			}
			//------------
			fileInfo.setRate( rate);
			fileInfo.setFinished( progres);
			ViewHolder holder = (ViewHolder) view.getTag();
			setData( holder, position);
		}
	}
``` 
<br>
<br>
<br>
<br>
##二、数据库存储文件下载进度：
<br>
<br>**1、SQLiteOpenHelper中文API文档：**
<br> http://www.cnblogs.com/over140/archive/2011/11/30/2268591.html
<br>
<br>
<br>		   
**2、SQL语言：**
<br>http://hunankeda110.iteye.com/blog/1143258/
<br>
<br>
<br>
**3、数据库帮助类：多线程的数据库操作——单例模式**
<br> http://blog.csdn.net/zs234/article/details/7203141
<br>
<br>多线程操作数据库时注意使用**【单例模式】**
<br>1、构造方法定为private
<br>2、定义该类的一个静态对象用以应用
<br>3、通过getInstance（）方法返回该类对象，使该方法无论调用多少次，该类都是唯一。
``` java
	/**
	* 数据库帮助类的静态对象引用
	*/
	private static DBHelper sHelper = null ;
	
	/**
	* 构造方法
	*/
	private DBHelper(Context context){ // 将public改为private，防止在其它地方被new出来，保证db的单例，防止数据库被锁定
		super(context, DB_NAME, null, VERSION);	
	}
	
	/**
	* 获得类对象sHelper
	*/
	public static DBHelper getInstance(Context context){
	// 单例模式，DBHelper只会被实例化一次
		if (sHelper == null ) {
		// 静态方法访问数据库，无论创建多少个数据库访问对象，
		// 里面的Helper只有一个，保证程序中只有一个DBHelper对数据库进行访问
			sHelper = new DBHelper(context);
		}
		return sHelper ;
	}
``` 
<br>
<br>
<br>
**4、数据库访问接口：**
```java
	* DAO：data access object 数据访问对象，DAO一定是和数据库的每张表一一对应;
	* 是一种应用程序表承接口（api）,可以访问其它的结构化查询语言（SQL）数据库；
	* J2EE用DAO设计模块，把底层数据访问和高层的商务逻辑分开；典型的DAO有以下几个组件：
		* 1、一个DAO工厂类
		* 2、一个DAO接口
		* 3、一个实现DAO接口的具体类
		* 4、数据传递对象
	/**
	* 【数据访问接口】
	*/
	public interface ThreadDAO{}
	/**
	* 【数据库访问接口的实现】
	*/
	public class ThreadDAOIImpl implements ThreadDAO{}
	
	synchronized ：给数据库加锁，同一时刻最多只有一个线程执行这个段代码
	* 多线程数据库的增、删、改（更新）方法用synchronized修饰，以保证线程安全：
	* 保证同一时间段不会有多个（只有一个）线程对数据库进行增删改，需等待线程执
	* 行完后再开启线程执行下一个功能；而查询因为不用操作数据库，不会导致数据库
	* 死锁，所以不用.
``` 
<br>
<br>
<br>
<br>
<br>**三、Serializable序列化:**将一个对象的状态（各个属性量）保存起来，然后在适当的时候再获得。
<br>http://developer.android.com/intl/zh-cn/reference/java/io/Serializable.html
<br>http://www.oschina.net/question/4873_23270（java序列化作用）
``` jav
	public class FileInfo  implements Serializable{
		private static final long serialVersionUID = 1L;
		private int id ;
		// get和set方法
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		// 构造方法
		public FileInfo() {
		}
		public FileInfo(int id) {
			this.id = id;
		}
      }
```
<br>
<br>
<br>
**四、Client与Service端通讯——HttpURLConnection之中文乱码问题：**
``` java
	**1.1、client发送给service的中文——对中文进行base64编码：**
	/**
	* 对字符串进行Base64编码
	* @param content 要编码的中文
	* @return Base64编码后的string
	*/
	public String base64(String content){
		try{
			content=Base64.encodeToString(content.getBytes("utf-8"), Base64.DEFAULT);//对字符串进行Base64编码
			content=URLEncoder.encode(content);//对字符串进行URL编码
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}return content;
	}
```
<br>
<br>
<br>
**1.2、Service端对中文解码：**
``` java
	String content = request.getParameter("content");// 获取client输入的信息
	content=content.replaceAll("%2B","+");// 替换content中的加号，这是由于在进行URL编码时，将+号转换为%2B了
	BASE64Decoder decoder = new BASE64Decoder();
	content = new String(decoder.decodeBuffer(content),"utf-8");// 进行base64解码
``` 
<br>
<br>
<br>
**2、service端返回的url含有中文——URLEncoder转码和URLDeder解码**
``` java
	Service端：URLEncoder.encode(content,”UTF-8”);
	client端：URLDeder.decode(content,”UTF-8”);
```
<br>
<br>
<br>
**3、json数据的生成和解析：**
<br>http://www.cnblogs.com/jianrong-zheng/archive/2013/07/26/3217228.html
<br>
<br>
<br>
**4、文件md5值的生成：**
<br>http://linyu19872008.iteye.com/blog/1825343
<br>
<br>
<br>
**5、下拉刷新功能解析：**
<br>http://blog.csdn.net/sziicool/article/details/18727999
<br>
<br>
<br>
<br>
##五、UI和下载服务间的通讯：
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/ActivityAndService.png)
<br> **1、多文件下载原理：每点击【开始】按键，开启一个新的service进程，进行对应文件的下载。**
<br> http://www.ruanyifeng.com/blog/2013/04/processes_and_threads.html（线程和进程的区别，一个易懂的比喻）
<br>
<br>
<br>
**2、多线程下载原理：**将下载文件长度分成三段（段数自定义），在service中开启三个线程，下载文件
<br>相应范围长度的数据，总和即为一个完整文件。
<br>
<br>
<br>
<br>
<br>
<br>
##六、Service中的线程下载：
![](https://github.com/youngWM/Download/blob/master/tomcat_servlet/service.png)
<br>**1、断点续传的实现**
```java
	1、暂停时，数据库存储文件的下载的进度；
	2、开启下载任务时，读取数据库中文件下载进度，通过RandomAccessFile.seek(start)从指定位置开始写文件。
```
<br>
<br>
<br>
**2、网络下载关键点：**
```java
	// 获取文件长度
	conn = (HttpURLConnection) url.openConnection();
	length = conn.getContentLength();
	// 在本地创建文件
	File file = new File( dir, mFileInfo.getFileName() );
	raf = new RandomAccessFile( file, "rwd" );
	// 设置本地文件长度
	raf.setLength( length ); 
	mFileInfo.setLength( length );
	// 读取数据库中文件下载的信息，没有则创建下载线程信息
```
<br>
<br>
<br>
**3、文件下载的实现——下载线程（Thread）：**
```java
	1、打开网络连接URL
	2、设置每个线程URL上的下载范围/位置
		conn.setRequestProperty("Range", "bytes=" +start +"-" + End() );
	3、设置本地文件的写入位置raf.seek(start); //从下载结束位置开始下载
	4、文件的读取与写入、广播进度
		input = conn.getInputStream();//读取数据
		raf.write(buffer,0,len); //写入文件
	5、下载结束关闭连接
```			
<br>
<br>
<br>
**4、多线程文件下载的优化——线程池：**解决线程生命周期开销问题和资源不足问题
<br>http://blog.csdn.net/jszhangyili/article/details/34799309   （Java线程池原理和使用）
<br>
<br>
<br>
**5、任意访问文件RandomAccessFile：**
<br>http://blog.csdn.net/funneies/article/details/9311027
<br>http://blog.csdn.net/baoyonwei/article/details/7829688
<br>seek()方法
<br>
<br>
<br>
<br>
<br>
<br>
#问题集
**1、servlet已导入包，但运行时报错，提示该包未发现：**
<br>**解决：**将包放在tomcat的子目录lib中，软件即可自动导入
<br>
<br>
<br>
**2、javaEE中servlet工程的创建：**
<br>**解决：**
<br>右击空白/file ——>new ——>web project ——>同Android project类似步骤
<br>建完工程后——> 右击工程名 ——> new ——>servlet
<br>http://www.cnblogs.com/xdp-gacl/p/3760336.html
<br>
<br>
<br>
**3、运行时，调用HttpsUrlConnection连接http时报错：**
<br>**解决：**
<br>调用时注意HttpsUrlConnection和HttpUrlConnection的差别
<br>http://blog.csdn.net/xiechengfa/article/details/38794487  （关于HttpUrlConnection与HttpClient的选择）
<br>http://blog.csdn.net/Silver__Lining/article/details/45489051  （HttpUrlConnection详解）
<br>
<br>
<br>
**4、文件完整下载完毕，但获取数据库中下载进度却总在99%：**
<br>**原因：**逻辑错误：数据库存储下载字节长度时——存储比总长缺少两个字节
```java
	1、假若一个文件长100字节： y = 100
	2、分三段线程下载下载，则每段需下载字节：33 、33、34   （ 100 / 3 = 33 ）
	3、则每个线程下载字节范围（start ~ end）：  0~33、 34~66、 67~100
	4、当所有线程下载结束，记录的下载长度为： end – start
		                                33 =33-0， 32=66-34， 33=100-67
	5、结果当获取下载进度时，(int) progress = （33+ 32 +34）*100 / y = 99
```
<br>
<br>
<br>
**5、文件总长度为正整数，已下载长度为正整数，但计算后的下载进度有时却变成负数：**
<br>**原因：**类型使用错误 +逻辑错误
```java
	1、文件字节总长度： int   fileLength；
	2、文件已下载字节： int   finished；
	3、文件下载进度：   int   progress；
	4、计算 progress = finished / fileLength *100 ；
	5、因为 finished <= fileLength且为都int型 ,所以progress错误，不为正数。
	解决：progress = finished  *100 / fileLength ；
```
<br>
<br>
<br>
**6、快速连续点击不同文件的下载按键，程序崩溃，系统提示：**
```java
    Java.lang.IllegalStateException: Cannot perform this operation because the connection 
    Pool has been closed.
```
<br>
<br>
<br>
**7、在fragment和fragmentAdapter中import包不同，分别为Android.app.Fragmnet和Android.support.v4.app.Fragmnet，
<br>导致fragment调用fragmentAdapter中方法时提示方法类型错误，百思不得其解。**
<br>
<br>
<br>
**8、在下载tomcat中文件时，提示：java.net.malformedUrlException:unknow protocol :d  at java.net.URL<init>**
<br>**原因：**传给client的url为绝对路径，应改为虚拟路径。
<br>
<br>
<br>
**9、下拉刷新添加新的url后，原有的url文件下载进度被清空：**
<br>**原因：**在下拉刷新的数据处理handler中new 的listview adapter，导致每次下拉刷新后原有的DB下载进度被清空。
<br>**解决：**new addapter放在onCreateView中,每次下拉刷新获取的新url通过调用adapter的set方法添加进去。
<br>**思路：**
<br>
<br>
<br>
**10、client下拉后延时一会，将会发送多个请求命令给service，此时service返回的文件md5值改变，发生错误：**
<br>**原因：**service端文件md5值生成方法名为 public static String getFileMD5String( File file ){... }
<br>当有多个请求时，同一时间段多次调用该方法，致使md5生成异常。
<br>**解决:** 加锁，用`synchronized`修饰，使该方法同一时间段只能调用一次。
```java
	public static synchronized String getFileMD5String( File file ){... } 
```
<br>
<br>
<br>
**11、当文件下载线程结束下载后，finally会关闭httpURLConnection、inputStream、RadomAccess。
<br>突发情况下：HttpURLConnection连接成功，但InputStream却没有获取到对象为null，系统报错：
<br>java java.lang.NullPointerException.**
<br>**原因：**finally中直接close()这三个对象，没有判断是否为空。
<br>**解决：**finally加上判断，在非空情况下才调用close()。并且在线程中判断是否为空，是则
<br>清空下载进度，重新下载文件。
<br>
<br>
<br>
**12、当【开始】【暂停】独立为2个按键时，间隔较长时间只点击【开始】按键2次，下载进度条左右跳动。**
<br>**原因：**代码中存储下载进度的情况为：暂停时和下载完成2种。
```java
	1、假设初始进入应用时，下载进度为10
	2、点击【开始】按键，开启编号为①的进程下载文件
	3、当进程①下载进度为30时，再次点击【开始】按键，开启进程②下载文件
	4、而此时，进程①的下载进度未保存，进程②获取数据库的进度为10
	5、两个进程同时广播下载进度，导致进度条跳动	
```
<br>**解决：**将【开始】【暂停】合并为一个按键
<br>
<br>
<br>
**13、由于Activity和service的生命周期不同，退出UI后,Activity被销毁，而service**
<br>**进程在后台运行，导致进入再次进入应用后，进度条在变化，但按键被初始化，显示状态出错。**
<br>**解决：**在fragment退出时，在onDestroyView（）调用全部暂停，记住下载进度
<br>
<br>
**14、The connection pool for +data+data+com_example_download+databases+download_db 
<br>has been closed but there are still 1 connections in use.They will beclosed as they are 
<br>released back to the pool.**
<br>
<br>
<br>
**15、android获取数据库某字段名的数据时，系统崩溃，报错：**
```java
	java.lang.IllegalStateException: Couldn't read row 0, col -1 from CursorWindow.
	Make sure the Cursor is initialized correctly before accessing data from it.**
```
<br>**原因：**字段名引用出错。在数据库帮助类DBHelper中设置字段名为“over_time ”，即字
<br>符串名之后多加了一个空格。在DBHelper中插入该字段名，空格被省略掉，而在DAO中空格未
<br>被省略掉，使两者出现难以察觉的错误。
<br>
<br>
<br>
<br>
#小知识点
**1、设置文字显示的数字含两位小数：**
```java
	DecimalFormat decimalFormat = = new DecimalFormat("#0.00"); 
	tvProgress.setText( decimalFormat.format());
```
<br>
<br>
<br>
**2、设置textView的显示长度，超出部分显示为省略号：**
```java
      android:maxEms="10" 
      android:singleLine="true"
      android:ellipsize="end"
```
<br>
<br>
<br>
**3、广播接收器的注册与注销：**
```java
	BroadcastReceiver mReceiver = new BroadcastReceiver() {...}
	//广播接收器
	IntentFilter filter = new IntentFilter();
	filter.addAction( IntentAction.ACTION_UPDATE);
	getActivity().registerReceiver(mReceiver, filter);
	
	getActivity().unregisterReceiver(mReceiver);
```
<br>
<br>
<br>
**4、Fragment  onCreateView()中控件的获取：**
```java
      View mView = inflater.inflate(R.layout.fragment_download, null);
      ListView mListView=(ListView) mView.findViewById(R.id.lv_download);
```
<br>
<br>
<br>
**5、HttpUrlConnection连接Url并获取输入流：**
```java
	URL  url = new URL("http://222.76.33.169:8080/...");
	// 创建一个HTTP连接
	HttpURLConnection urlConn=(HttpURLConnection)url.openConnection();
	InputStreamReader in  = new InputStreamReader( urlConn.getInputStream()); // 获得读取的内容
	BufferedReader buffer = new BufferedReader(in); // 获取输入流对象
	String inputLine = null;
	// 通过循环逐行读取输入流中的内容
	while ((inputLine = buffer.readLine()) != null) {
		result += inputLine + "\n";
	}
	// 关闭字符输入流对象,断开连接
	in.close();
	urlConn.disconnect();
```
<br>
<br>
<br>
**6、文件夹和文件的创建、重命名：**
```java
	// 创建文件夹
	if (!(new File(Save_Location).isDirectory())){ //如果文件夹不存在，则新建
      		File myFilePath = null;
        	myFilePath = new File("D:\Tomcat 8.0\webapps");
        	myFilePath.mkdir();
        }

	// 创建文件并写入信息
	String filename = "D://Tomcat 8.0//webapps//文件.txt";
	if (!(new File(filename).isDirectory()) ){
		File f = new File(filename) ;
		try{
			out = new PrintWriter(new FileWriter(f) ) ; // 由 FileWriter 实例化，则向文件中写入内容
			out. print ("该txt文件名为：file"+unixtime+".txt"+"\r\n");
		}catch (IOException e){
			e.printStackTrace();
	}

	// 对文件名重命名
	File file = new   File("/downloads/文件.txt"); 
	File file2 = new File("/downloads/文件2.txt");
	file.renameTo(file2);
```
<br>
<br>
<br>
**7、注意不同地方的路径格式可能不同：**
<br>电脑上路径为：D:\Tomcat 8.0\webapps
<br>Servlet上路径格式为：D://Tomcat 8.0//webapps
<br>
<br>
<br>
**8、获取URL中的文件名：**
```java
      /**
      * 获取完整http的URL中的含扩展名的文件名
      * @param url 	    完整http的URL
      * @param symbol  url中路径是正斜杠“/”或反斜杠“\”或者本地路径的"\\"
      */
      public String getURLFileName(String url,String symbol){
	      String fileName   = url.substring(url.lastIndexOf(symbol) + 1,url.lastIndexOf(".")); 
	      String expandName = url.substring(url.lastIndexOf(".") + 1,url.length()).toLowerCase(); 
	      return fileName+"."+expandName;
      }
```
