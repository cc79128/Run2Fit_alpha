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

	ImageButton imageButton_skip; //skip���s
	 private TextView timmer; //�˼ƭp��
	 
	 //����O�_�}�_Map_activity
	 int map_open =0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		
		// ���s����
	      
	      imageButton_skip = (ImageButton)findViewById(R.id.imagebutton_skip);
	      imageButton_skip.setOnClickListener(imageButtonOnClickListener);
	      imageButton_skip.setOnFocusChangeListener(imageButtonOnFocusChangeListener);
	            
	      
	    //�˼ưO�ɥ\��
	      timmer = (TextView)findViewById(R.id.textview_timmer);
		  new CountDownTimer(10000,1000){ //10��
	          
	          @Override
	          public void onFinish() { //�ɶ���ɤ�����Map(Home)����
	             // TODO Auto-generated method stub
	        	// ���w�n�I�s�� Activity Class
	     	     Intent mystory = new Intent();
	     	     mystory.setClass( About.this, Map.class );
	     	     
	     	     // �I�s�s�� Activity Class
	     	     if(map_open == 0){
	     	     startActivity( mystory );
	     	     map_open=1;
	     	     }
	     	     
	     	     //��������� Activity Class
	     	     About.this.finish();	     		
	          }

	          @Override
	          public void onTick(long millisUntilFinished) {
	              // TODO Auto-generated method stub
	              //timmer.setText("");
	          }          
	      }.start();

	}
	
	  //ImageButton ���s (Skip)
	  private ImageButton.OnClickListener imageButtonOnClickListener
	  	= new ImageButton.OnClickListener(){
		  @Override
		  public void onClick(View v) {
		   // TODO Auto-generated method stub
		   imageButton_skip.setImageResource(R.drawable.skip_focused);
		   
		 //���s������
		 // ���w�n�I�s�� Activity Class
	     Intent mystory = new Intent();
	     mystory.setClass( About.this, Map.class );
	     
	     // �I�s�s�� Activity Class
	     //Map Activity�w�g���}�N���|�A���ƶ}�Ҥ@��
	     if(map_open == 0){
	    	startActivity( mystory );
	     	map_open =1;
	     }	     
	     // ��������� Activity Class
	     About.this.finish();
		  }	  
	  };
	  
	  private ImageButton.OnFocusChangeListener imageButtonOnFocusChangeListener
	   = new ImageButton.OnFocusChangeListener(){
	  //skip���s���ܼ˦�
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
	  
	  //�����^���s
	  public boolean onKeyDown(int keyCode, KeyEvent event) {//������^��
	        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
	            ConfirmExit();//����^��A�h����h�X�T�{
	            return true;   
	        }   
	        return super.onKeyDown(keyCode, event);   
	    }
	  public void ConfirmExit(){//�h�X�T�{
	        AlertDialog.Builder ad=new AlertDialog.Builder(About.this);
	        ad.setTitle("���}");
	        ad.setMessage("�T�w�n���}?");
	        ad.setPositiveButton("�O", new DialogInterface.OnClickListener() {//�h�X���s
	            public void onClick(DialogInterface dialog, int i) {
	                // TODO Auto-generated method stub
	            	About.this.finish();//����activity
	            }
	        });
	        ad.setNegativeButton("�_",new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int i) {
	                //���h�X���ΰ������ާ@
	            }
	        });
	        ad.show();//�ܹ�ܮ�
	    }
}
