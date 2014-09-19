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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	Button btnLogin;
	Button btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	TextView loginErrorMsg;

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
		setContentView(R.layout.login);

		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.loginEmail);
		inputPassword = (EditText) findViewById(R.id.loginPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		loginErrorMsg = (TextView) findViewById(R.id.login_error);

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				
				//若點選Log in按鈕則會執行JSONParse看是否帳號有註冊過
				 new JSONParse().execute();
				 
			}
			
		});
		

		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
	
	//要繼承AsyncTask（轉圈圈）
	private class JSONParse extends AsyncTask<String, String, JSONObject> {
	       private ProgressDialog pDialog;
	      @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
			
	            pDialog = new ProgressDialog(LoginActivity.this);
	            pDialog.setMessage("Login ing ...");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(true);
	            pDialog.show();
	      }
	      @Override
	        protected JSONObject doInBackground(String... args) {  
	            String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
				UserFunctions userFunction = new UserFunctions();
				Log.d("Button", "Login");
				JSONObject json = userFunction.loginUser(email, password); //得到json查找後的結果
				return json;
	      }
	       @Override
	         protected void onPostExecute(JSONObject json) {
	    	   pDialog.dismiss();    	   
	    	// check for login response
				try {
					if (json.getString(KEY_SUCCESS) != null) { //有找到此帳號
						loginErrorMsg.setText("");
						String res = json.getString(KEY_SUCCESS); 
						if(Integer.parseInt(res) == 1){
							// user successfully logged in
							// Store user details in SQLite Database
							DatabaseHandler db = new DatabaseHandler(getApplicationContext());
							JSONObject json_user = json.getJSONObject("user");
							
							// Clear all previous data in database
							UserFunctions userFunction = new UserFunctions();
							userFunction.logoutUser(getApplicationContext());
							db.addUser(json_user.getString(KEY_NAME),json_user.getString(KEY_GENDER), json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT), json_user.getString(KEY_HEIGHT),json_user.getString(KEY_WEIGHT));						
							
							/*
							// Launch Dashboard Screen
							Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);					
							// Close all views before launching Dashboard
							dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(dashboard);*/
							
							Intent map = new Intent(getApplicationContext(), Map.class);					
							// Close all views before launching Dashboard
							map.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(map);
							
							// Close Login Screen
							finish();
						}else{
							// Error in login
							loginErrorMsg.setText("Incorrect username/password");
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
	         
	    }
	}
}
