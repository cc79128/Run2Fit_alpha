package gps.trajectory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import library.UserFunctions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class Mystory extends Activity {
	
	//List<string> list;
	//ListView lv;
	//List<Boolean> listShow;    // 這個用來記錄哪幾個 item 是被打勾的
	
  	//TextView tv = null;
    ListView lv = null;
    Button btn_selectAll = null;
    //Button btn_inverseSelect = null; //反選
    Button btn_calcel = null;
    Button home =null;
    //String name[];
    String name[] = new String[1000]; //存放標題名稱
     
    ArrayList<String> listStr = null;
    //int check_position; //checkbox選中項目
    String keyword="";
    String keyword_id="";
    String upload_path[] = new String[1000];
    String folder_name="";//資料夾名稱，供上傳用
    int upload_item = 0; //檔案的編號
    
    private List<HashMap<String, Object>> list = null;
    private MyAdapter adapter;
	
	
	Button back,button_clean,button_choose,button_cleanchoose;
	Button btn_upload;
	MySQLite dbHelper;//透過MySQLite宣告物件dbHelper
	
	
	private static final int FTP=1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystory);
		dbHelper = new MySQLite(this);
		
		//tv = (TextView) this.findViewById(R.id.tv);
        lv = (ListView) this.findViewById(R.id.listView1);
        btn_selectAll = (Button) this.findViewById(R.id.selectall);
        //btn_inverseSelect = (Button) this.findViewById(R.id.inverseselect);
        btn_calcel = (Button) this.findViewById(R.id.cancel);
        showCheckBoxListView("select * from story2;");
        
		//lv=(ListView)findViewById(R.id.listView1);
		//getdataByListView("select * from story;");

		 //全選
        btn_selectAll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                listStr = new ArrayList<String>();
                for(int i=0;i<list.size();i++){
                    MyAdapter.isSelected.put(i,true);
                    listStr.add(name[i]);
                } 
                adapter.notifyDataSetChanged();//注意這一句必須加上，否則checkbox無法正常更新狀態
                //tv.setText("已選中"+listStr.size()+"項");
            }
        });
         
        //反選
        /*
        btn_inverseSelect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                for(int i=0;i<list.size();i++){
                    if(MyAdapter.isSelected.get(i)==false){
                        MyAdapter.isSelected.put(i, true);
                        listStr.add(name[i]);
                    }
                    else{
                        MyAdapter.isSelected.put(i, false);
                        listStr.remove(name[i]);
                    }
                }
                adapter.notifyDataSetChanged();
                tv.setText("已選中"+listStr.size()+"項");
            }
             
        });*/
         
        //取消已選
        btn_calcel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                for(int i=0;i<list.size();i++){
                    if(MyAdapter.isSelected.get(i)==true){
                        MyAdapter.isSelected.put(i, false);
                        listStr.remove(name[i]);
                    }
                }
                adapter.notifyDataSetChanged();
                //tv.setText("已選中"+listStr.size()+"項");
            }
             
        });
        
        home = (Button)findViewById(R.id.home); //回到首頁
		home.setOnClickListener(click_home); 
		
		btn_upload = (Button)findViewById(R.id.upload);//上傳資料
		btn_upload.setOnClickListener(click_upload);
		
	}
	
	    
	 // 顯示带有checkbox的listview
	    public void showCheckBoxListView(String sql) {
	    	Cursor c=dbHelper.db.rawQuery(sql, null);
	        
	    	getdata(sql);
	    	
	        list = new ArrayList<HashMap<String, Object>>();

	        for (int i = 0; i < c.getCount(); i++) {
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put("item_tv", name[i]);
	            map.put("item_cb", false);
	            //map.put("item_up", "已上傳");
	            
	            c.moveToPosition(i);
				int upload_num = c.getInt(5);
				Log.d("upload","upload: " + upload_num);
				if(upload_num ==1){
					map.put("item_up","UPLOAD");
					//map.put("item_tv","==========此檔案已上傳==========\n"+ name[i] );	
					//Log.d("upload","進入upload_num");
				}
				else map.put("item_up","NOT_UPLOAD");
	            
	            list.add(map);
	           
	            Log.d("upload","list to string:"+list.toString());
	 
	            adapter = new MyAdapter(this, list, R.layout.listviewitem,
	                    new String[] { "item_tv", "item_cb", "item_up"  }, new int[] {
	                             R.id.item_tv, R.id.item_cb,R.id.imageView_up  });
	            
	            lv.setAdapter(adapter);
	            listStr = new ArrayList<String>();
	            lv.setOnItemClickListener(new OnItemClickListener() {
	 
	                @Override
	                public void onItemClick(AdapterView<?> arg0, View view,
	                        int position, long arg3) {
	                	
	                    ViewHolder holder = (ViewHolder) view.getTag();
	                    holder.cb.toggle();// 在每次获取点击的item时改变checkbox的状态
	                    MyAdapter.isSelected.put(position, holder.cb.isChecked()); // 同时修改map的值保存状态
	                    if (holder.cb.isChecked() == true) {
	                        listStr.add(name[position]);
	                    } else {
	                        listStr.remove(name[position]);
	                    }
	                    //check_position = position;
	                   
	                    //tv.setText("已選中"+listStr.size()+"項"); 不需要
	                    
	                    keyword = listStr.toString();
	                }
	 
	            });
	        }

	        //按鈕對應動作
			//button_clean = (Button)findViewById(R.id.button_clean);
			//button_clean.setOnClickListener(click_clean);

			//觀看勾選項目	
			button_choose = (Button)findViewById(R.id.button_choose);
			button_choose.setOnClickListener(click_choose);
				
			//清除勾選項目
			button_cleanchoose = (Button)findViewById(R.id.button_cleanchoose);
			button_cleanchoose.setOnClickListener(click_cleanchoose);
	    }
	    
		//click觀看勾選項目
		private View.OnClickListener click_choose = new View.OnClickListener(){
			public void onClick(View v){
				if(listStr.size()==1){
					//進入勾選項目
					
					Log.d("Keyword",keyword);
					Toast.makeText(Mystory.this, "您選擇了: " + keyword, Toast.LENGTH_LONG).show();

					getdata("select * from story2;");
					
					Intent it=new Intent();
					it.setClass(Mystory.this, Showpath.class);
					it.putExtra("STORY_ID",keyword);
					startActivity(it);
					Mystory.this.finish();

				}
				else if(listStr.size()<1){
					new AlertDialog.Builder(Mystory.this)  //顯示資料
			    	   .setTitle("請勾選一個項目").show(); 
				}
				else if(listStr.size()>1){
					new AlertDialog.Builder(Mystory.this)  //顯示資料
			    	   .setTitle("只能勾選一個項目").show(); 
				}
				
		    }
		};
	    
		//click 清除勾選(ING)
		private View.OnClickListener click_cleanchoose = new View.OnClickListener(){
			public void onClick(View v){
				//進入清除勾選
				Cursor c=dbHelper.db.rawQuery("select * from story2;", null);  //透過Cursor取得資料
		    	c.moveToNext();// 將指標移動到第一筆資料
		    	Log.d("HsinHsi","進入清除勾選");
				for(int i=0;i<list.size();i++){
					if(MyAdapter.isSelected.get(i)==true){
						c.moveToPosition(i);
						
						Log.d("HsinHsi","進入delete");
						String id = c.getString(0);
						String cmd= ("DELETE FROM story2 WHERE _id='"+id+"';" );
						dbHelper.db.execSQL(cmd);
						Log.d("HsinHsi","日期格式"+c.getString(1));
						//刪除檔案
						String ppath="/sdcard/NOL/GPSLog/" + c.getString(1); //存檔目錄
						File dir=new File(ppath);
						deleteDir(dir); //刪除目錄

                    }
                } 
				
				Intent it=new Intent();
				it.setClass(Mystory.this, Mystory.class);
				startActivity(it);
				Mystory.this.finish();
				
		    }
		};
	    //取得資料庫資料
	    private void getdata(String sql){
	    	   Cursor c=dbHelper.db.rawQuery(sql, null);  //透過Cursor取得資料
	    	   c.moveToNext();  // 將指標移動到第一筆資料
	    	   String data="";
	    	   
	    	   for(int i=1;i<=c.getCount();i++){  //取回資料
	    	    	 //int j=2;
	    	    	 data =c.getString(1)+"     "+c.getString(2) +"\n"+ c.getString(4)+"          "+ c.getString(3); //日期+標題名稱
	    	    	 name[i-1]=data;
	    	    	 //如果選項符合勾選內容 
	    	    	 if(keyword.equals(("["+data+"]")) ) //比較字串是否相等要用equals
	    	    	 {
	    	    		 Log.d("HsinHsi","進入keyword");
	    	    		 keyword = c.getString(0);
	    	    		 Log.d("keyword",keyword);
	    	    	 }
   	    	 
		    	     c.moveToNext();
	    	   }
	    	   
	    	   /*
	    	   new AlertDialog.Builder(Mystory.this)  //顯示資料
	    	   .setTitle("data")
	    	   .setMessage(data)
	    	   .setPositiveButton("確認",new DialogInterface.OnClickListener() {
	    	        public void onClick(DialogInterface dialog, int which) {
	    	               // TODO Auto-generated method stub
	    	            }
	    	    })
	    	   .show(); 
	    	   */
	    	   
	    	}
	    //刪除檔案
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
	    
	    //回到首頁
	    private View.OnClickListener click_home = new View.OnClickListener(){
			public void onClick(View v){
				// 指定要呼叫的 Activity Class
				Log.d("HsinHsi","進入HOME");
			     Intent mystory = new Intent();
			     mystory.setClass( Mystory.this, Map.class );
			     
			     // 呼叫新的 Activity Class
			    	startActivity( mystory );

			     // 結束原先的 Activity Class
			    	Mystory.this.finish();
				  }
				
		};
		
		//Upload module
	    private View.OnClickListener click_upload = new View.OnClickListener(){
			public void onClick(View v){
				
					Log.d("upload","upload");
					Log.d("upload",keyword);
					//進入upload module
					Cursor c=dbHelper.db.rawQuery("select * from story2;", null);  //透過Cursor取得資料
			    	c.moveToNext();// 將指標移動到第一筆資料
			    	Log.d("HsinHsi","進入upload module");
					for(int i=0;i<list.size();i++){
						if(MyAdapter.isSelected.get(i)==true){
							c.moveToPosition(i);
							
							Log.d("HsinHsi","進入upload");
							String id = c.getString(0);
							//String cmd= ("DELETE FROM story2 WHERE _id='"+id+"';" );
							//dbHelper.db.execSQL(cmd);
							Log.d("HsinHsi","upload:"+c.getString(1));
							
							upload_path[upload_item]="/sdcard/NOL/GPSLog/" + c.getString(1)+ "/";//檔案目錄
							upload_item ++;

	                    }
	                } 
					for(int j=0;j<upload_item ; j++){
						if(j==0){
							folder_name = upload_path[j];
						}
						else
							folder_name +=(","+upload_path[j]);
					}
					Log.d("upload","folder_name:"+folder_name);
					
					Intent intent = new Intent(Mystory.this, Tasklist.class);
					Bundle bData = new Bundle();
					bData.putString("ftpHost", "140.113.207.101"); 
					bData.putString("ftpUser", "nol");
					bData.putString("ftpPasswd", "netopt56680");
					bData.putString("ftpRemoteDir", "/fight2fit");//fight2fit
					bData.putString("localDir", folder_name);
					bData.putInt("port", 56687);
					bData.putString("delete", "no");
					intent.putExtras(bData);
					startActivityForResult(intent,FTP); //FTP==1

					
					upload_item = 0;

				  }
				
		};
		
		//返回控制
		 public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
		        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
		            
		        	Intent mystory = new Intent();
				     mystory.setClass( Mystory.this, Map.class );
				     
				     // 呼叫新的 Activity Class
				    	startActivity( mystory );

				     // 結束原先的 Activity Class
				    	Mystory.this.finish();
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
			   	     Mystory.this.finish();
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
		    		 AlertDialog.Builder ad=new AlertDialog.Builder(Mystory.this);
			 	        ad.setTitle("離開");
			 	        ad.setMessage("確定要離開?");
			 	        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
			 	            public void onClick(DialogInterface dialog, int i) {
			 	                // TODO Auto-generated method stub
			 	            	Mystory.this.finish();//關閉activity
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
		
		//========onActivityResult===============
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	           super.onActivityResult(requestCode, resultCode, data);

	           switch(requestCode){
	           case FTP:
        	   		if(data.getExtras().getBoolean("finish")==true){
        	   			
        	   			Toast.makeText(this, "檔案已成功上傳", 0).show();
        	   			
        	   			Cursor c=dbHelper.db.rawQuery("select * from story2;", null);  //透過Cursor取得資料
    			    	c.moveToNext();// 將指標移動到第一筆資料
    			    	//Log.d("HsinHsi","進入upload module");
    					for(int i=0;i<list.size();i++){
    						if(MyAdapter.isSelected.get(i)==true){
    							c.moveToPosition(i);

    							String id = c.getString(0);
								String cmd="update story2 set upload= 1 where _id='" + id + "';";
    							//String cmd= ("DELETE FROM story2 WHERE _id='"+id+"';" );
    							dbHelper.db.execSQL(cmd);
    	                    }
    	                } 
        	   			
    					showCheckBoxListView("select * from story2;");
        	   		}
                   //Toast.makeText(this, (String)data.getExtras().getBoolean("finish"), 0).show();
	          }
		}
	
}