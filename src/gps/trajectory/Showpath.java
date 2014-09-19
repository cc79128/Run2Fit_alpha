package gps.trajectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import library.UserFunctions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class Showpath extends FragmentActivity	implements OnClickListener, android.view.View.OnClickListener {

	MySQLite dbHelper;
	TextView tv_show_gps;
	private GoogleMap map;
	private Button myButton1,myButton_home;
	String storyID; //故事編號
	String story_title; //故事標題
	private double distance = 0; //總長度
	public double maxh = 0;//最大高度
	public double minh = 0;//最低高度
	public double maxs = 0;//最大速度
	public double mins = 0;//最小速度
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showpath);
		//建立dbHelper物件
		dbHelper = new MySQLite(this);
		// 對應xml畫面
		findviews();
		// 判斷是否有gps.csv
		storyID = this.getIntent().getExtras().getString("STORY_ID"); //要修改的故事編號
		String cmd_select="select * from story2 where _id='" + storyID + "';";
		Cursor c = dbHelper.db.rawQuery(cmd_select, null); // 執行SQL指令，進行資料查詢
		c.moveToNext();
		story_title=c.getString(2).toString();
		
		File file = new File("/sdcard/NOL/GPSLog/" + storyID + "/gps.csv");
		if (!file.exists()){
		Toast.makeText(Showpath.this, "目前沒有任何記錄!", Toast.LENGTH_LONG).show();
		}else{
			drawmap(); //畫出路徑
		}
	}
	
	//對應xml畫面
	private void findviews()
	{ 
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); 
		
		myButton_home = (Button)findViewById(R.id.myButton_home); //回到首頁
		myButton_home.setOnClickListener(click_home); 
		
		myButton1 = (Button)findViewById(R.id.myButton1); //我的路徑
		tv_show_gps=(TextView)findViewById(R.id.showgeo); // 設定onclick事件
		myButton1.setOnClickListener(this); 
		
	}
	public void onClick(View v) 
	{ 
		Log.d("HsinHsi","onClick");
		switch (v.getId()) 
		{ 
			case R.id.myButton1: //我的路徑
				Intent it=new Intent(); 
				it.setClass(Showpath.this, Mystory.class); 
				startActivity(it); Showpath.this.finish(); 
				break; 
				} 
	}
	
	//畫出檔案內路徑
	private void drawmap() 
	{
		// 繪出目前檔案內路徑 
		LatLng p1=null; 
		LatLng p2=null; 
		
		try { 
			FileReader fr=new FileReader("/sdcard/NOL/GPSLog/" + storyID + "/gps.csv"); 			
			BufferedReader br=new BufferedReader(fr); 
			String temp_data=br.readLine(); 
			String[] dd=temp_data.split(","); //讀出第一筆資料並切割 
			double lot=Double.parseDouble(dd[1]); //0
			double lgt=Double.parseDouble(dd[2]); //1
			maxh=Double.parseDouble(dd[3]); //最大高度 3
			minh=Double.parseDouble(dd[3]); //最小高度 3
			maxs=Double.parseDouble(dd[4]); //最大速度 4
			mins=Double.parseDouble(dd[4]); //最小速度 4
			p1=new LatLng(lot,lgt); 
				int k=1; 
			while(temp_data !=null)
			{ 
				if(k==1){ //路徑的第一個點 
					p2=p1; 
					distance += GetDistance(p1,p2); //計算距離 
					//加入起始點 
					//Marker mk = map.addMarker(new MarkerOptions().position(p1).icon(BitmapDescriptorFactory.fromResource(R.drawable.s38)).title("").snippet("")); map.moveCamera(CameraUpdateFactory.newLatLngZoom(p1, 16)); 
					//畫路徑 
					PolylineOptions line=new PolylineOptions().add(p1,p2).width(5).color(Color.BLUE); 
					map.addPolyline(line); 
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));		
				}
				else
				{
					dd=temp_data.split(",");
					lot=Double.parseDouble(dd[1]);
					lgt=Double.parseDouble(dd[2]);
					p2=new LatLng(lot,lgt);
					distance += GetDistance(p1,p2); //計算距離
					//畫路徑
					PolylineOptions line=new PolylineOptions().add(p1,p2).width(5).color(Color.BLUE);
					map.addPolyline(line);
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));
					p1=p2;
					//計算高度與速度的極值
						if(maxh < Double.parseDouble(dd[3])){
						maxh=Double.parseDouble(dd[3]);
						}
						if(minh > Double.parseDouble(dd[3])){
							minh=Double.parseDouble(dd[3]);
						}
						if(maxs < Double.parseDouble(dd[4])){
							maxs=Double.parseDouble(dd[4]);
						}
						if(mins > Double.parseDouble(dd[4])){
							mins=Double.parseDouble(dd[4]);
						}
				}
				temp_data=br.readLine();
				k=k+1;
				//畫出結束點
				//Marker mk = map.addMarker(new MarkerOptions().position(p2).icon(BitmapDescriptorFactory.fromResource(R.drawable.e39)).title("").snippet(""));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));
				
				disformat1(maxh);
				disformat1(minh);
				disformat1(maxs);
				disformat1(mins);
				
				//顯示資訊
				if (distance < 1000){
					tv_show_gps.setText(" 標題："+story_title + "\n 移動距離：" + disformat(distance)+" m \n 最大高度：" + disformat1(maxh) + " m            最小高度：" + disformat1(minh)+ " m \n 最大速度：" + disformat1(maxs) + " km/h        最小速度：" + disformat1(mins) + " km/h");
				}
				else{
					tv_show_gps.setText(" 標題："+story_title + "\n 移動距離：" + disformat((double)distance/1000)+" km \n 最大高度：" + disformat1(maxh) + " m            最小高度：" + disformat1(minh) + " m \n 最大速度：" + disformat1(maxs) + " km/h        最小速度：" + disformat1(mins) + " km/h");
				}
		}
	}
		catch (IOException e) {
			return;
		}
	}
	
	/*
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}
	*/
	
	//取得兩點間的距離
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

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
		
		    
		    //回到首頁
		    private View.OnClickListener click_home = new View.OnClickListener(){
				public void onClick(View v){
					// 指定要呼叫的 Activity Class
					
				     Intent mystory = new Intent();
				     mystory.setClass( Showpath.this, Map.class );
				     
				     // 呼叫新的 Activity Class
				    	startActivity( mystory );

				     // 結束原先的 Activity Class
				    	Showpath.this.finish();
					  }
					
			};
			
			
			//返回控制
			 public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
			        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
			            
			        	Intent mystory = new Intent();
					     mystory.setClass( Showpath.this, Mystory.class );
					     
					     // 呼叫新的 Activity Class
					    	startActivity( mystory );

					     // 結束原先的 Activity Class
					    	Showpath.this.finish();
			            return true;   
			        }   
			        return super.onKeyDown(keyCode, event);   
			    }
			 
			 
			//=====================MenuOption選項=============================
			@Override
			public boolean onCreateOptionsMenu(Menu menu){
		        super.onCreateOptionsMenu(menu);
		        menu.add(0,0,0,"ABOUT");
		        menu.add(0,1,0,"LOG OUT");
		        menu.add(0,2,0,"EXIT");
		        return true;
		   }
			//MenuOption選項實做
			@Override
			public boolean onOptionsItemSelected(MenuItem item){
			    	super.onOptionsItemSelected(item);  	
			    	Log.d("HsinHsi","進入MENU");
			        switch(item.getItemId()){
			    	 case 0:
			    		// 指定要呼叫的 Activity Class
			            Intent about = new Intent();
			            about.setClass( this, About.class );	            
			            // 呼叫新的 Activity Class
			            startActivity( about );
			         // 結束原先的 Activity Class
			   	     	Showpath.this.finish();
			            break;
			    	 case 1: // LOG OUT
				    		UserFunctions userFunctions;
				    		userFunctions = new UserFunctions();
				    		userFunctions.logoutUser(getApplicationContext());
			 				Intent login = new Intent(getApplicationContext(), LoginActivity.class);
			 	        	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 	        	startActivity(login);
			 	        	// Closing dashboard screen
			 	        	finish();
			 	        	break;
			    	 case 2:
			    		 AlertDialog.Builder ad=new AlertDialog.Builder(Showpath.this);
				 	        ad.setTitle("離開");
				 	        ad.setMessage("確定要離開?");
				 	        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
				 	            public void onClick(DialogInterface dialog, int i) {
				 	                // TODO Auto-generated method stub
				 	            	Showpath.this.finish();//關閉activity
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
