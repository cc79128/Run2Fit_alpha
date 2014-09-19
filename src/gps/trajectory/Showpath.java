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
	String storyID; //�G�ƽs��
	String story_title; //�G�Ƽ��D
	private double distance = 0; //�`����
	public double maxh = 0;//�̤j����
	public double minh = 0;//�̧C����
	public double maxs = 0;//�̤j�t��
	public double mins = 0;//�̤p�t��
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showpath);
		//�إ�dbHelper����
		dbHelper = new MySQLite(this);
		// ����xml�e��
		findviews();
		// �P�_�O�_��gps.csv
		storyID = this.getIntent().getExtras().getString("STORY_ID"); //�n�ק諸�G�ƽs��
		String cmd_select="select * from story2 where _id='" + storyID + "';";
		Cursor c = dbHelper.db.rawQuery(cmd_select, null); // ����SQL���O�A�i���Ƭd��
		c.moveToNext();
		story_title=c.getString(2).toString();
		
		File file = new File("/sdcard/NOL/GPSLog/" + storyID + "/gps.csv");
		if (!file.exists()){
		Toast.makeText(Showpath.this, "�ثe�S������O��!", Toast.LENGTH_LONG).show();
		}else{
			drawmap(); //�e�X���|
		}
	}
	
	//����xml�e��
	private void findviews()
	{ 
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); 
		
		myButton_home = (Button)findViewById(R.id.myButton_home); //�^�쭺��
		myButton_home.setOnClickListener(click_home); 
		
		myButton1 = (Button)findViewById(R.id.myButton1); //�ڪ����|
		tv_show_gps=(TextView)findViewById(R.id.showgeo); // �]�wonclick�ƥ�
		myButton1.setOnClickListener(this); 
		
	}
	public void onClick(View v) 
	{ 
		Log.d("HsinHsi","onClick");
		switch (v.getId()) 
		{ 
			case R.id.myButton1: //�ڪ����|
				Intent it=new Intent(); 
				it.setClass(Showpath.this, Mystory.class); 
				startActivity(it); Showpath.this.finish(); 
				break; 
				} 
	}
	
	//�e�X�ɮפ����|
	private void drawmap() 
	{
		// ø�X�ثe�ɮפ����| 
		LatLng p1=null; 
		LatLng p2=null; 
		
		try { 
			FileReader fr=new FileReader("/sdcard/NOL/GPSLog/" + storyID + "/gps.csv"); 			
			BufferedReader br=new BufferedReader(fr); 
			String temp_data=br.readLine(); 
			String[] dd=temp_data.split(","); //Ū�X�Ĥ@����ƨä��� 
			double lot=Double.parseDouble(dd[1]); //0
			double lgt=Double.parseDouble(dd[2]); //1
			maxh=Double.parseDouble(dd[3]); //�̤j���� 3
			minh=Double.parseDouble(dd[3]); //�̤p���� 3
			maxs=Double.parseDouble(dd[4]); //�̤j�t�� 4
			mins=Double.parseDouble(dd[4]); //�̤p�t�� 4
			p1=new LatLng(lot,lgt); 
				int k=1; 
			while(temp_data !=null)
			{ 
				if(k==1){ //���|���Ĥ@���I 
					p2=p1; 
					distance += GetDistance(p1,p2); //�p��Z�� 
					//�[�J�_�l�I 
					//Marker mk = map.addMarker(new MarkerOptions().position(p1).icon(BitmapDescriptorFactory.fromResource(R.drawable.s38)).title("").snippet("")); map.moveCamera(CameraUpdateFactory.newLatLngZoom(p1, 16)); 
					//�e���| 
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
					distance += GetDistance(p1,p2); //�p��Z��
					//�e���|
					PolylineOptions line=new PolylineOptions().add(p1,p2).width(5).color(Color.BLUE);
					map.addPolyline(line);
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));
					p1=p2;
					//�p�Ⱚ�׻P�t�ת�����
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
				//�e�X�����I
				//Marker mk = map.addMarker(new MarkerOptions().position(p2).icon(BitmapDescriptorFactory.fromResource(R.drawable.e39)).title("").snippet(""));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(p2, 16));
				
				disformat1(maxh);
				disformat1(minh);
				disformat1(maxs);
				disformat1(mins);
				
				//��ܸ�T
				if (distance < 1000){
					tv_show_gps.setText(" ���D�G"+story_title + "\n ���ʶZ���G" + disformat(distance)+" m \n �̤j���סG" + disformat1(maxh) + " m            �̤p���סG" + disformat1(minh)+ " m \n �̤j�t�סG" + disformat1(maxs) + " km/h        �̤p�t�סG" + disformat1(mins) + " km/h");
				}
				else{
					tv_show_gps.setText(" ���D�G"+story_title + "\n ���ʶZ���G" + disformat((double)distance/1000)+" km \n �̤j���סG" + disformat1(maxh) + " m            �̤p���סG" + disformat1(minh) + " m \n �̤j�t�סG" + disformat1(maxs) + " km/h        �̤p�t�סG" + disformat1(mins) + " km/h");
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
	
	//���o���I�����Z��
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
		
		//�ഫ����
		private double ConvertDegreeToRadians(double degrees) 
		{ 
			return (Math.PI/180)*degrees;
		}
		
		// format ���ʶZ������k(����) 
		public String disformat(double num) 
		{ 
			NumberFormat formatter = new DecimalFormat("###"); 
			String s=formatter.format(num); 
			return s; 
		}
		// format ���ʶZ������k(����) 
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
		
		    
		    //�^�쭺��
		    private View.OnClickListener click_home = new View.OnClickListener(){
				public void onClick(View v){
					// ���w�n�I�s�� Activity Class
					
				     Intent mystory = new Intent();
				     mystory.setClass( Showpath.this, Map.class );
				     
				     // �I�s�s�� Activity Class
				    	startActivity( mystory );

				     // ��������� Activity Class
				    	Showpath.this.finish();
					  }
					
			};
			
			
			//��^����
			 public boolean onKeyDown(int keyCode, KeyEvent event) {//������^��
			        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
			            
			        	Intent mystory = new Intent();
					     mystory.setClass( Showpath.this, Mystory.class );
					     
					     // �I�s�s�� Activity Class
					    	startActivity( mystory );

					     // ��������� Activity Class
					    	Showpath.this.finish();
			            return true;   
			        }   
			        return super.onKeyDown(keyCode, event);   
			    }
			 
			 
			//=====================MenuOption�ﶵ=============================
			@Override
			public boolean onCreateOptionsMenu(Menu menu){
		        super.onCreateOptionsMenu(menu);
		        menu.add(0,0,0,"ABOUT");
		        menu.add(0,1,0,"LOG OUT");
		        menu.add(0,2,0,"EXIT");
		        return true;
		   }
			//MenuOption�ﶵ�갵
			@Override
			public boolean onOptionsItemSelected(MenuItem item){
			    	super.onOptionsItemSelected(item);  	
			    	Log.d("HsinHsi","�i�JMENU");
			        switch(item.getItemId()){
			    	 case 0:
			    		// ���w�n�I�s�� Activity Class
			            Intent about = new Intent();
			            about.setClass( this, About.class );	            
			            // �I�s�s�� Activity Class
			            startActivity( about );
			         // ��������� Activity Class
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
				 	        ad.setTitle("���}");
				 	        ad.setMessage("�T�w�n���}?");
				 	        ad.setPositiveButton("�O", new DialogInterface.OnClickListener() {//�h�X���s
				 	            public void onClick(DialogInterface dialog, int i) {
				 	                // TODO Auto-generated method stub
				 	            	Showpath.this.finish();//����activity
				 	            }
				 	        });
				 	        ad.setNegativeButton("�_",new DialogInterface.OnClickListener() {
				 	            public void onClick(DialogInterface dialog, int i) {
				 	                //���h�X���ΰ������ާ@
				 	            }
				 	        });
				 	        ad.show();//�ܹ�ܮ�
			    		 break;
			    }
					return true;
			}
}
