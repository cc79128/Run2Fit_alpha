package gps.trajectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import library.UserFunctions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Map extends FragmentActivity implements OnClickListener{

	
	public MySQLite dbHelper; //透過MySQL宣告物件dbHelper
	//即時顯示區
	TextView tv_show_gps0;
	TextView tv_show_gps1;
	TextView tv_show_gps2;
	TextView tv_show_gps3;
	TextView tv_show_gps4;
	TextView tv_show_gps5;
	
	private LocationManager mLocationManager;// 定位管理器
	private ProgressDialog MyDialog; //顯示GPS定位進度
	private String mLocationProvider=""; //GPS Provider
	private Location mLocation; //GPS座標點
	private GoogleMap map;
	
	private Button mButton_start;
	private Button mButton_finish;
	private Button mButton_route;
	private Button mButton_restart;
	private Button mButton_cancel;
	
	LatLng p1;	//出發點
	LatLng p2;	//結束點
	LatLng mylocation;//現在位置
	LatLng lastlocation; //上次打點位置
	
	public long srate = 2; //每幾秒打一次點 (可更改)
	public long msrate = 2000; //每幾毫秒打一次點
	
	int first_draw = 0;
	int is_km = 0; //判斷是否為公里
	int no_network = 0; //看是否為無網路狀態
	int no_gps = 0; //看是否為無GPS
	int is_start=0; //正在紀錄中
	
	private boolean _run = false; //是否正在記錄
	private double distance = 0; //總長度
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.map); 
		
		dbHelper = new MySQLite(this); //建立dbHelper物件
		//判斷是否可以上網
		ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		final LocationManager cLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		tv_show_gps0=(TextView)findViewById(R.id.showgeo0);
		tv_show_gps1=(TextView)findViewById(R.id.showgeo1);
		tv_show_gps2=(TextView)findViewById(R.id.showgeo2);
		tv_show_gps3=(TextView)findViewById(R.id.showgeo3);
		tv_show_gps4=(TextView)findViewById(R.id.showgeo4);
		
		if ((info == null || !info.isAvailable()) && is_start ==0){ //不能上網
			if(!cLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER ) ){
				Log.d("HsinHsi","無網路無GPS");
				no_network = 1;
				// 到系統開啟GPS與WIFI服務的畫面
				no_gps =1;
				Intent mystory = new Intent();
			     mystory.setClass( Map.this, NoGPS.class );  
			     // 呼叫新的 Activity Class
			    	startActivity( mystory );
			     // 結束原先的 Activity Class
			    	Map.this.finish();
			}
			else if(is_start!=1){//如果已經在紀錄中就不會跑進來這
				Log.d("HsinHsi","無網路但有GPS");
					new AlertDialog.Builder(Map.this)
					.setTitle("系統訊息")
					.setMessage("無網路連線，Google Maps無法使用！請確認WiFi/3G網路已連線。\n但軌跡記錄程式仍可繼續使用")
					.setPositiveButton("確認",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						no_network = 1; //無網路模式
						findviewsnonet();
					}
				})
					.show();
					
					createCancelProgressDialog("定位中","定位中..請稍待！","取消");
					try{
						Log.d("HsinHsi","無網路有GPS");
						mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
						// Provider 初始化
						getLocationPrivider();
						// 設定GPS的Listener
						mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener);
						if(mLocation!=null) //第一次顯示
						{
							// 取得速度
							Log.d("HsinHsi","進入無網路第一次顯示");
							
							mButton_cancel.setVisibility(View.INVISIBLE);//隱藏取消按鈕
							
							double speed=mLocation.getSpeed()/1000*60*60; //原單位是m/s
							double altitude = mLocation.getAltitude();
							p1 = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
							p2 = p1;
							//showlocation((double)p1.latitude,(double)p1.longitude,"結束定位點","",3);
						
							tv_show_gps0.setText("移動距離 : " + "0" + "m");
							tv_show_gps1.setText("緯度：" + formatgeo(mLocation.getLatitude()));
							tv_show_gps2.setText("經度：" + formatgeo(mLocation.getLongitude()));
							tv_show_gps3.setText("高度：" + formatheight(altitude) + "m");
							tv_show_gps4.setText("速度：" + formatspeed(speed) + "km/h");
						
						}
						}catch(Exception e){
							Log.d("HsinHsi","exception");
							new AlertDialog.Builder(Map.this)
							.setTitle("系統訊息")
							.setMessage("無法取得GPS座標")
							.setPositiveButton("確認",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							MyDialog.dismiss();
							}
						})
							.show();
						}
			}
		}
		else
		{
			//對應xml畫面
			findviews();
			
			createCancelProgressDialog("定位中","定位中..請稍待！","取消");
			try{
				//如果沒有開啟GPS或WiFi---------------------
				mLocationManager =(LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
				if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				}else{
					Log.d("HsinHsi","有網路無GPS");
					// 到系統開啟GPS與WIFI服務的畫面
					no_gps =1;
					//startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //bug
					Intent mystory = new Intent();
				     mystory.setClass( Map.this, NoGPS.class );  
				     // 呼叫新的 Activity Class
				    	startActivity( mystory );
				     // 結束原先的 Activity Class
				    	Map.this.finish();
				}
			//--------------------如果沒有開啟GPS
				Log.d("HsinHsi","TEST");
				mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				// Provider 初始化
				getLocationPrivider();
				// 設定GPS的Listener
				mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener);
				if(mLocation!=null) //第一次顯示
				{
					// 取得速度
					Log.d("HsinHsi","進入第一次顯示");
					
					mButton_cancel.setVisibility(View.INVISIBLE);//隱藏取消按鈕
					
					double speed=mLocation.getSpeed()/1000*60*60; //原單位是m/s
					double altitude = mLocation.getAltitude();
					p1 = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
					p2 = p1;
					showlocation((double)p1.latitude,(double)p1.longitude,"結束定位點","",3);
				
					//tv_show_gps.setText("緯度：" + formatgeo(mLocation.getLatitude()) + " 經度：" + formatgeo(mLocation.getLongitude()) + "高度：" + altitude + " m 速度：" + formatspeed(speed) + "km/h");
					tv_show_gps0.setText("移動距離 : " + "0" + "m");
					tv_show_gps1.setText("緯度：" + formatgeo(mLocation.getLatitude()));
					tv_show_gps2.setText("經度：" + formatgeo(mLocation.getLongitude()));
					tv_show_gps3.setText("高度：" + formatheight(altitude) + "m");
					tv_show_gps4.setText("速度：" + formatspeed(speed) + "km/h");
				
				}
				}catch(Exception e){
					
					new AlertDialog.Builder(Map.this)
					.setTitle("系統訊息")
					.setMessage("無法取得GPS座標")
					.setPositiveButton("確認",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					MyDialog.dismiss();
					}
				})
					.show();
				}
		}
	}
	
	// 產生定位中視窗
	private void createCancelProgressDialog(String title,String message,String buttonText){
		MyDialog = new ProgressDialog(this);
		MyDialog.setTitle(title);
		MyDialog.setMessage(message);
		MyDialog.setButton(buttonText, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		MyDialog.show(); //顯示進度
	}
	
	// 取得LocationProvider
	public void getLocationPrivider() 
	{ 
		Log.d("HsinHsi","進入getlocationprovider");
		Criteria mCriteria01 = new Criteria();
		mCriteria01.setAccuracy(Criteria.ACCURACY_FINE);
		mCriteria01.setAltitudeRequired(true); //需要高度
		mCriteria01.setBearingRequired(false); //方向角
		mCriteria01.setSpeedRequired(true); //速度
		mCriteria01.setCostAllowed(true); 
		mCriteria01.setPowerRequirement(Criteria.POWER_LOW);
		mLocationProvider = mLocationManager.getBestProvider(mCriteria01, true); 
		mLocation = mLocationManager.getLastKnownLocation(mLocationProvider); 
	}
	
	// 偵測位置改變
	// 顯示即時資訊
	public final LocationListener mLocationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			// 取得速度
			double speed=location.getSpeed()/1000*60*60; //原單位是m/s
			double altitude = location.getAltitude();
			
			//show即時資訊
			tv_show_gps1.setText("緯度：" + formatgeo(location.getLatitude()));
			tv_show_gps2.setText("經度：" + formatgeo(location.getLongitude()));
			tv_show_gps3.setText("高度：" + formatheight(altitude) + "m");
			tv_show_gps4.setText("速度：" + formatspeed(speed) + "km/h");
			
			p2 = new LatLng(location.getLatitude(),location.getLongitude());
			if(_run)
			{ //開始記錄 
				distance+=GetDistance(p1,p2); //計算距離
				if (distance < 1000)
				{ 
					tv_show_gps0.setText("移動距離 : " + disformat(distance)+ "m");
					tv_show_gps1.setText("緯度：" + formatgeo(location.getLatitude()));
					tv_show_gps2.setText("經度：" + formatgeo(location.getLongitude()));
					tv_show_gps3.setText("高度：" + formatheight(altitude) + "m");
					tv_show_gps4.setText("速度：" + formatspeed(speed) + "km/h");
				}
					//tv_show_gps.setText("移動距離:" + disformat(distance)+" m " + "緯度：" + formatgeo(location.getLatitude()) + " 經度：" + formatgeo(location.getLongitude()) + " 高度：" + formatspeed(altitude) + " m 速度：" + formatspeed(speed) + "km/h"); }
				else{ 
						//tv_show_gps.setText("移動距離:" + disformat((double)distance/1000)+" km " + "緯度：" + formatgeo(location.getLatitude()) + " 經度：" + formatgeo(location.getLongitude()) + " 高度：" + formatspeed(altitude) + " m 速度：" + formatspeed(speed) + "km/h");
					is_km = 1;
					tv_show_gps0.setText("移動距離 : " + disformat((double)distance/1000)+"km");
					tv_show_gps1.setText("緯度：" + formatgeo(location.getLatitude()));
					tv_show_gps2.setText("經度：" + formatgeo(location.getLongitude()));
					tv_show_gps3.setText("高度：" + formatheight(altitude) + "m");
					tv_show_gps4.setText("速度：" + formatspeed(speed) + "km/h");
				}
			}
			if(no_network == 0){
				showlocation((double)(location.getLatitude()),(double)(location.getLongitude()),"目前GPS座標點","",2);//2=話路徑
			}
			// 記錄GPS 打點
				android.text.format.DateFormat df = new android.text.format.DateFormat();
				try {
					FileWriter fw = new FileWriter( ppath +"/gps.csv",true);//使用檔案記錄gps //.nema改成.csv
					BufferedWriter bw=new BufferedWriter(fw);
					// 24小時制時間
					java.util.Date current=new java.util.Date();
					java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//時間格式
					//String c1=sdf.format(current); //原本時間
					long UTC = location.getTime(); //long type
					//bw.write(p2.latitude + "," + p2.longitude + "," + c1 + "," + altitude + "," + speed );//寫檔
					//寫檔欄位 :  UTC(ms)、緯度、經度、高度、速度、方向及定位精度
					bw.write(UTC + "," + p2.latitude + "," + p2.longitude + "," + altitude + "," + speed +"," + location.getBearing() +"," + location.getAccuracy()  );
					bw.newLine();//換行
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
					}
			p1=p2;
			
			MyDialog.dismiss(); //結束定位中的進度視窗
		}
		public void onProviderDisabled(String provider)
		{
		}
		public void onProviderEnabled(String provider)
		{
		}
		public void onStatusChanged(String provider,int status,Bundle extras)
		{
		}
	};
	
	// format GPS座標的方法
	public String formatgeo(double num) 
	{ 
		NumberFormat formatter = new DecimalFormat("###.########"); 
		String s=formatter.format(num); return s; 
	} 
	
	// format speed的方法，將double轉成string
	public String formatspeed(double num) 
	{ 
		NumberFormat formatter = new DecimalFormat("###.#");
		String s=formatter.format(num); 
		return s;
	}
	//轉換高度為只顯示小數點後一位
	public String formatheight(double num) 
	{ 
		DecimalFormat df=new DecimalFormat("#.#");
		String s=df.format(num);   
		return s;
	}
	
	@Override 
	protected void onDestroy() { 
		Log.d("HsinHsi","onDestroy");
		if (mLocationManager != null) 
			mLocationManager.removeUpdates(mLocationListener); 
			super.onDestroy();
		}
	@Override 
	protected void onPause() { 
		Log.d("HsinHsi","onPause");
		if (mLocationManager != null) mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener); //2000ms=2s更新一次，越短越耗電
		super.onPause();
	}
		
	//GPS位置多久更新一次
	@Override 
	protected void onResume() { 
		Log.d("HsinHsi","onResume");
		if (mLocationManager != null) mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener); //2000ms=2s更新一次，越短越耗電
			super.onResume();
		}
	
	// 設定物件與事件(當網路不通時) 
	private void findviewsnonet(){ 
		Log.d("HsinHsi","進入無網路模式");
		mButton_start = (Button)findViewById(R.id.button_start); //開始記錄 
		mButton_finish = (Button)findViewById(R.id.button_finish); //結束記錄
		mButton_restart = (Button)findViewById(R.id.button_restart); //重新定位
		mButton_route = (Button)findViewById(R.id.button_route); //我的路徑 
		mButton_cancel = (Button)findViewById(R.id.button_cancel);
		
		mButton_start.setVisibility(View.VISIBLE); //顯示開始記錄按鈕
		mButton_finish.setVisibility(View.INVISIBLE); //隱藏結束記錄按鈕
		mButton_restart.setVisibility(View.INVISIBLE);//隱藏重新定位按鈕
		mButton_cancel.setVisibility(View.INVISIBLE);//隱藏取消按鈕
		
		
		mButton_start.setOnClickListener((OnClickListener) this); 
		mButton_finish.setOnClickListener((OnClickListener) this); 
		// 設定onclick事件(我的路徑)
		mButton_route.setOnClickListener((OnClickListener) this); 
		mButton_restart.setOnClickListener((OnClickListener) this); 
		mButton_cancel.setOnClickListener((OnClickListener) this); 
		}
	
	Marker mk=null; 
	// 顯示座標點
	private void showlocation(double d,double e,String title, String snip,int type)
	{ 
		Log.d("HsinHsi","進入showlocation");
		LatLng CTU = new LatLng(d, e); 
		//取得地圖物件
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
		map.setMyLocationEnabled(true);//秀出mylcation按鈕
		//map.setTrafficEnabled(true); //顯示交通流量
		//map.setIndoorEnabled(true);
		/* 设置地图类型三种：
		 * 1：MAP_TYPE_NORMAL: Basic map with roads.
		 * 2：MAP_TYPE_SATELLITE: Satellite view with roads.
		 * 3:MAP_TYPE_TERRAIN: Terrain view without roads.
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);*/
		switch(type)
		{
			case 1: //起始點
				// 設定縮放大小是16，且將標示點放在正中央
				mk = map.addMarker(new MarkerOptions().position(CTU).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)).title("").snippet(""));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(CTU, 16));
			break;
			case 2: //畫路徑
				//建立紅色氣球標示
				if(first_draw == 0){
					first_draw = 1;
				}
				else{
					/* marker格式
					map.addMarker(new MarkerOptions()
					.position(new LatLng(25.046254,121.51752)
					.icon()
					.title("台北火車站")
					.snippet("Taipei City Land Mark")));*/
					
					//打點
					//if(GetDistance(p1,p2) > 0.5 ){ //兩點間的距離超過5公尺才打點
						//Log.d("HsinHsi","兩點距離超過5m");
						mk = map.addMarker(new MarkerOptions().position(CTU).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)).title(title).snippet(snip));
						PolylineOptions line=new PolylineOptions().add(p1,p2).width(5).color(Color.GREEN);
						map.addPolyline(line);
						lastlocation = CTU; //把最後一次打點位置存起來
					//}
					//map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16)); //不要一直移動鏡頭
				}
				break;
			case 3: //結束點
				// 設定縮放大小是16，且將標示點放在正中央
				mk = map.addMarker(new MarkerOptions().position(CTU).icon(BitmapDescriptorFactory.fromResource(R.drawable.finish)).title("").snippet(""));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(CTU, 16));
				break;
		}
		
		/*
		//建立紅色氣球標示
		if(mk != null) mk.remove();
		mk = map.addMarker(new MarkerOptions().position(CTU).title(title).snippet(snip)); 
		// 設定縮放大小是16，且將標示點放在正中央
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CTU, 16)); 
		//加入畫線
		PolylineOptions line = new	PolylineOptions().add(p1,p2).width(5).color(Color.RED);
		map.addPolyline(line);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));
		*/
	}
	
	//設定物件與事件
	private void findviews()
	{
		mButton_start = (Button)findViewById(R.id.button_start);
		//mButton_start.setVisibility(View.INVISIBLE);
		mButton_finish = (Button)findViewById(R.id.button_finish);
		mButton_finish.setVisibility(View.INVISIBLE);
		mButton_route = (Button)findViewById(R.id.button_route);
		mButton_restart = (Button)findViewById(R.id.button_restart);
		mButton_cancel = (Button)findViewById(R.id.button_cancel);
		
		//設定onClick事件
		mButton_start.setOnClickListener(this);
		mButton_finish.setOnClickListener(this);
		mButton_route.setOnClickListener(this);
		mButton_restart.setOnClickListener(this);
		mButton_cancel.setOnClickListener(this);
		
	}
	
	//按鈕事件
	String sdate;	//日期
	String ppath;	//存檔路徑
	
	@Override
	public void onClick(View v)
	{
		tv_show_gps5=(TextView)findViewById(R.id.showgoe02);
		
		switch(v.getId())
		{
			
			case R.id.button_start:	 	//開始記錄
				Log.d("HsinHsi","start");
				is_start=1;
				tv_show_gps5.setText(" ...記錄軌跡中...");
				// 決定存檔路徑，取出日期時間當成此次路徑的存檔目錄名稱
				SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyyMMdd-HH:mm:ss");
				//nowdate.setTimeZone(TimeZone.getTimeZone("GMT+8"));//時區
				nowdate.setTimeZone(TimeZone.getDefault()); //時區看程式在哪執行決定
				sdate = nowdate.format(new java.util.Date());
				sdate.replaceAll("\\s+", "");
				ppath="/sdcard/NOL/GPSLog/" + sdate; //存檔目錄
				// 判斷目錄是否存在
				File vPath = new File(ppath);
				if(!vPath.exists()) vPath.mkdirs(); //產生資料夾
				//產生故事
				SimpleDateFormat nowdate1 = new java.text.SimpleDateFormat("yyyyMMdd-HH:mm:ss");
				nowdate1.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				String sdate1 = nowdate1.format(new java.util.Date()); 
				sdate1.replaceAll("\\s+", "");
				String cmd="insert into story2 (_id,createday,title,desc,distance) values ('" + sdate + "','" + sdate1 + "','','','');";
				dbHelper.db.execSQL(cmd); // 執行SQL指令，進行資料新增
				if(map !=null) map.clear();
				p1=p2;
				_run=true; //是否開始記錄
				if(no_network == 0){
				showlocation(p1.latitude,(double)p1.longitude,"這是起點","",1);//1=畫起點
				}
				distance=0; //總長度=0
				//切換按鈕狀態
				mButton_start.setVisibility(View.INVISIBLE); //開始記錄
				mButton_finish.setVisibility(View.VISIBLE); //結束記錄
				mButton_restart.setVisibility(View.INVISIBLE); //我的路徑
				mButton_cancel.setVisibility(View.VISIBLE); //取消記錄
				
				break;
				
			case R.id.button_finish:	//結束記錄
				is_start = 0;
				tv_show_gps5.setText("");
				_run=false; //結束紀錄 
				if(no_network == 0){
				showlocation((double)p2.latitude,(double)p2.longitude,"這是終點","",3); //3=畫結束 
				}
				// 輸入故事名稱與說明
				Context mContext = getApplicationContext(); 
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE); 
				View layout = inflater.inflate(R.layout.storydesc, (ViewGroup) findViewById(R.id.storylayout)); 
				final EditText title_input = (EditText)layout.findViewById(R.id.storytitle); 
				final EditText memo_input = (EditText)layout.findViewById(R.id.storymemo); 
				new AlertDialog.Builder(Map.this).setTitle("填寫與說明") .setMessage("請填寫路徑的標題與說明") .setPositiveButton("確定",new DialogInterface.OnClickListener() 
				{ 
					public void onClick(DialogInterface dialog, int which) 
					{ 
						String distance_str;
						if(is_km == 1){
							distance_str = "移動距離 : " + disformat1((double)distance/1000) + " km" ;
						}else{
							distance_str = "移動距離 : " + disformat(distance) + " m" ;
						}
						try 
						{ 
							String m_title = title_input.getText().toString(); //標題
							String m_memo = memo_input.getText().toString()+""; //說明
							if(m_title.equals(""))
							{ 
								m_title = sdate ; //標題為日期
								//m_memo = "";
								String cmd="update story2 set title='" + m_title + "',desc='" + m_memo + "',distance='" + distance_str +"' where _id='" + sdate + "';";
								dbHelper.db.execSQL(cmd); // 執行SQL指令，進行資料修改
								Toast.makeText(Map.this, "標題未填寫，名稱自動存為日期", Toast.LENGTH_LONG).show(); 
								}
							else
							{
								String cmd="update story2 set title='" + m_title + "',desc='" + m_memo + "',distance='" + distance_str +"' where _id='" + sdate + "';";
								dbHelper.db.execSQL(cmd); // 執行SQL指令，進行資料修改
							}
							
							//將標題及說明寫入到.txt檔							
							try{
						        //FileWriter fw = new FileWriter("/sdcard/output.txt", false);
								FileWriter fw = new FileWriter("/sdcard/NOL/GPSLog/"+ sdate +"/info.txt", true);
						        BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
						        bw.write("標題 : "+ m_title + "\n" + "說明 : " +m_memo + "\n" + distance_str + "\n" + "日期時間 : " + sdate );
						        bw.newLine();
						        bw.close();
						    }catch(IOException e){
						       e.printStackTrace();
						       Toast.makeText(Map.this, "寫入info失敗!", Toast.LENGTH_LONG).show();
						    }
							
						}catch(Exception e)
						{
							Toast.makeText(Map.this, "標題與說明儲存失敗!", Toast.LENGTH_LONG).show();
						}
					}
				})
				.setNegativeButton("取消不存檔",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
					String cmd="delete from story2 where _id='" + sdate + "';";
					dbHelper.db.execSQL(cmd); // 執行SQL指令，進行資料修改
					File dir=new File(ppath);
					deleteDir(dir); //刪除目錄
					}
				})
				.setView(layout)
				.create()
				.show();
				mButton_start.setVisibility(View.VISIBLE); //開始記錄
				mButton_finish.setVisibility(View.INVISIBLE); //結束記錄
				mButton_restart.setVisibility(View.VISIBLE); //重新定位
				mButton_route.setVisibility(View.VISIBLE); //我的記錄
				mButton_cancel.setVisibility(View.INVISIBLE);//取消記錄
				if(map !=null) map.clear();

				break;
				
			case R.id.button_route:		//我的路徑
				Log.d("HsinHsi","click route");
				Intent it=new Intent();
				it.setClass(Map.this, Mystory.class);
				startActivity(it);
				Map.this.finish();
				
				break;
			case R.id.button_restart:	//重新定位
				createCancelProgressDialog("定位中","定位中..請稍待！","取消");
				// Provider 初始化
				getLocationPrivider();
				// 設定GPS的Listener
				mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener);
				p1 = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(p1, 16));
				break;
				
			case R.id.button_cancel: //取消記錄
				Log.d("HsinHsi","進入取消記錄");
				if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);//暫停記錄軌跡
				Log.d("HsinHsi","cancel1");
				 AlertDialog.Builder ad=new AlertDialog.Builder(Map.this);
			        ad.setTitle("軌跡記錄暫停中...");
			        ad.setMessage("確定要取消記錄軌跡?");
			        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
			            public void onClick(DialogInterface dialog, int i) {
			            	is_start = 0;
			            	//取消存檔
			            	String cmd="delete from story2 where _id='" + sdate + "';";
							dbHelper.db.execSQL(cmd); // 執行SQL指令，進行資料修改
							File dir=new File(ppath);
							deleteDir(dir); //刪除目錄
			            	mButton_start.setVisibility(View.VISIBLE); //開始記錄
							mButton_finish.setVisibility(View.INVISIBLE); //結束記錄
							mButton_restart.setVisibility(View.VISIBLE); //重新定位
							mButton_route.setVisibility(View.VISIBLE); //我的記錄
							mButton_cancel.setVisibility(View.INVISIBLE);//取消記錄

							tv_show_gps0.setText("移動距離 : " + "0" + "m");
							tv_show_gps5.setText("");

			            }
			        });
			        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int i) {
			            	if (mLocationManager != null) mLocationManager.requestLocationUpdates(mLocationProvider, msrate, 0, mLocationListener);//恢復記錄軌跡
			            }
			        });
			        ad.show();//示對話框
				
				break;
		}
	}
	
	//刪除目錄
	public static void deleteDir (File dir) 
	{ 
		if (dir.isDirectory()) 
		{ 
			File[] files = dir.listFiles(); 
			for (File f:files) 
			{ 
				deleteDir(f); 
			} 
			dir.delete(); 
			} 
		else dir.delete(); 
	}
	//取得兩點間的距離 ，單位為m
	public double GetDistance(LatLng gp1,LatLng gp2) 
	{ 
		double earthRadius = 3958.75; 
		double latDiff = Math.toRadians(gp2.latitude-gp1.latitude); 
		double lngDiff = Math.toRadians(gp2.longitude-gp1.longitude); 
		double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) + Math.cos(Math.toRadians(gp1.latitude)) * Math.cos(Math.toRadians(gp2.latitude)) * Math.sin(lngDiff /2) * Math.sin(lngDiff /2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double distance = earthRadius * c; int meterConversion = 1609; 
		return new Float(distance * meterConversion).floatValue(); 
	}
	
	//轉換角度
	private double ConvertDegreeToRadians(double degrees) 
	{ 
		return (Math.PI/180)*degrees;
	}
	
	// format 移動距離的方法(公尺) 
	public String disformat(double num) 
	{ 
		NumberFormat formatter = new DecimalFormat("###"); 
		String s=formatter.format(num); 
		return s; 
	}
	// format 移動距離的方法(公里) 
	public String disformat1(double num) 
	{ 
		NumberFormat formatter = new DecimalFormat("###.##"); 
		String s=formatter.format(num); 
		return s;
	}
	
	
	//控制返回按鈕
	  public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
	        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
	            ConfirmExit();//按返回鍵，則執行退出確認
	            return true;   
	        }   
	        return super.onKeyDown(keyCode, event);   
	    }
	    public void ConfirmExit(){//退出確認
	        AlertDialog.Builder ad=new AlertDialog.Builder(Map.this);
	        ad.setTitle("離開");
	        ad.setMessage("確定要離開?");
	        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
	            public void onClick(DialogInterface dialog, int i) {
	                // TODO Auto-generated method stub
	            	Map.this.finish();//關閉activity
	  
	            }
	        });
	        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int i) {
	                //不退出不用執行任何操作
	            }
	        });
	        ad.show();//示對話框
	    }
	
	private void openSettingDialog(){
		LayoutInflater inflater = LayoutInflater.from(Map.this);
		final View v = inflater.inflate(R.layout.alertdialog_edittext,null );
		new AlertDialog.Builder(Map.this)
		.setTitle("打點間隔時間")
		.setView(v)
		.setNegativeButton("取消", null)
		.setPositiveButton("確定",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				EditText edittext = (EditText) (v.findViewById(R.id.edittext));
				try
				{
					srate = Integer.parseInt(edittext.getText().toString());
					Log.d("HsinHsi","srate:"+srate);
					msrate = srate*1000; //將秒轉成毫秒
				}
				catch (NumberFormatException e)
				{
					System.out.println("pase int error!! "+ e);
				}
			}
		})
		.show();
	}
	
	//=====================MenuOption選項=============================
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0,"SETTING");
        menu.add(0,1,0,"ABOUT");
        menu.add(0,2,0,"LOG OUT");
        menu.add(0,3,0,"EXIT");
        return true;
   }
	//MenuOption選項實做
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    	super.onOptionsItemSelected(item);  	
	    	Log.d("HsinHsi","進入MENU");
	        switch(item.getItemId()){
	    	 case 0:
	    		 openSettingDialog();     
	            break;
	    	 case 1:
	    		// 指定要呼叫的 Activity Class
	            Intent about = new Intent();
	            about.setClass( this, About.class );	            
	            // 呼叫新的 Activity Class
	            startActivity( about );	 
		         // 結束原先的 Activity Class
		   	     Map.this.finish();
	    		break;
	    	 case 2: // LOG OUT
	    		UserFunctions userFunctions;
	    		userFunctions = new UserFunctions();
	    		userFunctions.logoutUser(getApplicationContext());
 				Intent login = new Intent(getApplicationContext(), LoginActivity.class);
 	        	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	        	startActivity(login);
 	        	// Closing dashboard screen
 	        	finish();
 	        	break;
	    		
	    	 case 3:
	    		 AlertDialog.Builder ad=new AlertDialog.Builder(Map.this);
	 	        ad.setTitle("離開");
	 	        ad.setMessage("確定要離開?");
	 	        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
	 	            public void onClick(DialogInterface dialog, int i) {
	 	                // TODO Auto-generated method stub
	 	            	Map.this.finish();//關閉activity
	 	            }
	 	        });
	 	        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
	 	            public void onClick(DialogInterface dialog, int i) {
	 	                //不退出不用執行任何操作
	 	            }
	 	        });
	 	        ad.show();//示對話框
	    		 break;
	    }
			return true;
	}
	
}
			
