package gps.trajectory;

import library.DatabaseHandler;
import library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import java.lang.NumberFormatException;
import java.lang.Character;


public class RegisterActivity extends Activity {
	Button btnRegister;
	Button btnLinkToLogin;
	RadioButton radiobutton_man,radiobutton_woman;
	EditText inputFullName;
	EditText inputEmail;
	EditText inputPassword;
	EditText inputRePassword;
	TextView registerErrorMsg;
	
	EditText inputHeight;
	EditText inputWeight;
	
	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static String KEY_UID = "uid";
	private static String KEY_NAME = "name";
	private static String KEY_GENDER = "gender";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "created_at";
	
	private static String KEY_HEIGHT = "height";
	private static String KEY_WEIGHT = "weight";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		// Importing all assets like buttons, text fields
		inputFullName = (EditText) findViewById(R.id.registerName);
		radiobutton_man = (RadioButton) findViewById(R.id.Sex_Man);
		radiobutton_woman = (RadioButton) findViewById(R.id.Sex_Woman);
		inputEmail = (EditText) findViewById(R.id.registerEmail);
		inputPassword = (EditText) findViewById(R.id.registerPassword);
		inputRePassword = (EditText)findViewById(R.id.registerRePassword);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
		registerErrorMsg = (TextView) findViewById(R.id.register_error);
		
		inputHeight = (EditText) findViewById(R.id.reg_height);
		inputWeight = (EditText) findViewById(R.id.reg_weight);
		
	        	
		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View view) {
				
			/*check  invalidate*/
				boolean ErrorDetect = false;
				String name_str = inputFullName.getText().toString();
				String password_str = inputPassword.getText().toString();
				String repassword_str = inputRePassword.getText().toString();
				String inputWeight_str = inputWeight.getText().toString();
				String inputHeight_str = inputHeight.getText().toString();
				//check name
				boolean valid_name = true;
				for(int count = 0 ; count < name_str.length() ; count++)
					if(!Character.isLetter(name_str.charAt(count)) && name_str.charAt(count)!= ' ')
						valid_name = false;	
				if(name_str.length() == 0)
				{
					ErrorDetect = true;
					inputFullName.setError("Name is Empty!");
				}
				else if(!valid_name)
				{
					ErrorDetect = true;
					inputFullName.setError("Invalid Name!");
				}
				//check gender
				if(radiobutton_man.isChecked() == false && radiobutton_woman.isChecked() == false)
				{
					ErrorDetect = true;
					radiobutton_woman.setError("You must choose one.");
				}
				else
					radiobutton_woman.setError(null);
				//check email
				if (!Linkify.addLinks(inputEmail.getText(), Linkify.EMAIL_ADDRESSES))
				{
					ErrorDetect = true;
					inputEmail.setError("Email Format Error!");
				}
				//check password
				if(password_str.length() == 0)
				{
					ErrorDetect = true;
					inputPassword.setError("Password is Empty!"); 
				}
				if(repassword_str.length() == 0)
				{
					ErrorDetect = true;
					inputRePassword.setError("Password is Empty!"); 
				}
				else if(!password_str.equals(repassword_str))
				{
					ErrorDetect = true;
					inputRePassword.setError("Inconsistent Password!");  
				}
				
				//check weight
				int weight;
				try	{
					weight = Integer.parseInt(inputWeight_str);
				}
				catch(NumberFormatException e) {
					weight = 0;
				}	
				if( weight > 150 || weight < 20 )
				{
					ErrorDetect = true;
					inputWeight.setError("Weight Range Error!");  
				}

				//check height
				int height;
				try	{
					height = Integer.parseInt(inputHeight_str);
				}
				catch(NumberFormatException e) {
					height = 0;
				}
				if( height > 250 || height < 100 )
				{
					ErrorDetect = true;
					inputHeight.setError("Height Range Error!");  
				}
				
				//if everything OK...
				if(!ErrorDetect)
					new JSONParse().execute();

				
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(i);
				// Close Registration View
				finish();
			}
		});
	}
	
	private class JSONParse extends AsyncTask<String, String, JSONObject> {
	       private ProgressDialog pDialog;
	      @Override
	        protected void onPreExecute() {
	            super.onPreExecute();	
	            pDialog = new ProgressDialog(RegisterActivity.this);
	            pDialog.setMessage("Register ing ...");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(true);
	            pDialog.show();
	      }
	      @Override
	        protected JSONObject doInBackground(String... args) {  
	    	  	String name = inputFullName.getText().toString();
				String email = inputEmail.getText().toString();
				String gender = (radiobutton_man.isChecked())? "Male" : "Female";
				String password = inputPassword.getText().toString();
				String height = inputHeight.getText().toString();
				String weight = inputWeight.getText().toString();
				
				UserFunctions userFunction = new UserFunctions();
				JSONObject json = userFunction.registerUser(name, gender, email, password, height, weight);
				return json;
	      }
	       @Override
	         protected void onPostExecute(JSONObject json) {
	    	   pDialog.dismiss();    	   
	    	// check for login response
				try {
					
					if (json.getString(KEY_SUCCESS) != null) {
						registerErrorMsg.setText("");
						String res = json.getString(KEY_SUCCESS); 
						
						if(Integer.parseInt(res) == 1){
							// user successfully registred
							// Store user details in SQLite Database
							DatabaseHandler db = new DatabaseHandler(getApplicationContext());
							JSONObject json_user = json.getJSONObject("user");
							
							// Clear all previous data in database
							UserFunctions userFunction = new UserFunctions();
							userFunction.logoutUser(getApplicationContext());
							db.addUser(json_user.getString(KEY_NAME),json_user.getString(KEY_GENDER) ,json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT), json_user.getString(KEY_HEIGHT),json_user.getString(KEY_WEIGHT));						
			
							/*
							// Launch Dashboard Screen
							Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);
							// Close all views before launching Dashboard
							dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//當Intent對象包含這個標記時，如果在線程中發現存在Activity實例，則清空這個實例之上的Activity，使其處於線頂。
							startActivity(dashboard);*/
							Intent map = new Intent(getApplicationContext(), Map.class);
							// Close all views before launching Dashboard
							map.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//當Intent對象包含這個標記時，如果在線程中發現存在Activity實例，則清空這個實例之上的Activity，使其處於線頂。
							startActivity(map);
							// Close Registration Screen
							finish();
						}else{
							// Error in registration
							registerErrorMsg.setText("Error occured in registration");
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}     
	    }
	}
}
