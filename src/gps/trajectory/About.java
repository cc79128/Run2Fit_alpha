package gps.trajectory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class About extends Activity {

	ImageButton imageButton_skip; //skip按鈕
	 private TextView timmer; //倒數計時
	 
	 //控制是否開起Map_activity
	 int map_open =0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		
		// 按鈕物件
	      
	      imageButton_skip = (ImageButton)findViewById(R.id.imagebutton_skip);
	      imageButton_skip.setOnClickListener(imageButtonOnClickListener);
	      imageButton_skip.setOnFocusChangeListener(imageButtonOnFocusChangeListener);
	            
	      
	    //倒數記時功能
	      timmer = (TextView)findViewById(R.id.textview_timmer);
		  new CountDownTimer(10000,1000){ //10秒
	          
	          @Override
	          public void onFinish() { //時間到時切換到Map(Home)頁面
	             // TODO Auto-generated method stub
	        	// 指定要呼叫的 Activity Class
	     	     Intent mystory = new Intent();
	     	     mystory.setClass( About.this, Map.class );
	     	     
	     	     // 呼叫新的 Activity Class
	     	     if(map_open == 0){
	     	     startActivity( mystory );
	     	     map_open=1;
	     	     }
	     	     
	     	     //結束原先的 Activity Class
	     	     About.this.finish();	     		
	          }

	          @Override
	          public void onTick(long millisUntilFinished) {
	              // TODO Auto-generated method stub
	              //timmer.setText("");
	          }          
	      }.start();

	}
	
	  //ImageButton 按鈕 (Skip)
	  private ImageButton.OnClickListener imageButtonOnClickListener
	  	= new ImageButton.OnClickListener(){
		  @Override
		  public void onClick(View v) {
		   // TODO Auto-generated method stub
		   imageButton_skip.setImageResource(R.drawable.skip_focused);
		   
		 //按鈕做的事
		 // 指定要呼叫的 Activity Class
	     Intent mystory = new Intent();
	     mystory.setClass( About.this, Map.class );
	     
	     // 呼叫新的 Activity Class
	     //Map Activity已經打開就不會再重複開啟一次
	     if(map_open == 0){
	    	startActivity( mystory );
	     	map_open =1;
	     }	     
	     // 結束原先的 Activity Class
	     About.this.finish();
		  }	  
	  };
	  
	  private ImageButton.OnFocusChangeListener imageButtonOnFocusChangeListener
	   = new ImageButton.OnFocusChangeListener(){
	  //skip按鈕改變樣式
	  @Override
	  public void onFocusChange(View v, boolean hasFocus) {
		   // TODO Auto-generated method stub
		   if (hasFocus==true)
		   {
		    imageButton_skip.setImageResource(R.drawable.skip_focused);
		   }
		   else
		   {
		    imageButton_skip.setImageResource(R.drawable.skip);
		   }
		  }
	  };
	  
	  //控制返回按鈕
	  public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
	        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
	            ConfirmExit();//按返回鍵，則執行退出確認
	            return true;   
	        }   
	        return super.onKeyDown(keyCode, event);   
	    }
	  public void ConfirmExit(){//退出確認
	        AlertDialog.Builder ad=new AlertDialog.Builder(About.this);
	        ad.setTitle("離開");
	        ad.setMessage("確定要離開?");
	        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
	            public void onClick(DialogInterface dialog, int i) {
	                // TODO Auto-generated method stub
	            	About.this.finish();//關閉activity
	            }
	        });
	        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int i) {
	                //不退出不用執行任何操作
	            }
	        });
	        ad.show();//示對話框
	    }
}
