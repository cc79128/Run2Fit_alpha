package gps.trajectory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class NoGPS extends Activity {

	Button button_opengps,button_resume;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nogps);

		button_opengps = (Button)findViewById(R.id.button_opengps);
		button_resume = (Button)findViewById(R.id.button_resume);
		
		button_opengps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
            	//�}�Ҩt�Ωw��A��
	            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
		
		button_resume.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
            	 // ���w�n�I�s�� Activity Class
	            Intent map = new Intent();
	            map.setClass( NoGPS.this, Map.class );
	            
	            // �I�s�s�� Activity Class
	            startActivity( map );
            }
        });
		
	}
	

}
