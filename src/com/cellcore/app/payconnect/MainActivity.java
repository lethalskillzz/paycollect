package com.cellcore.app.payconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class MainActivity extends SherlockActivity {
	private EditText agentId, password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		agentId = (EditText)findViewById(R.id.agentId);
		password = (EditText)findViewById(R.id.password);
		final CheckBox rememberme = (CheckBox)findViewById(R.id.remembermebox);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		String is_remember_me = prefs.getString("remember_me", null);
		
		if (is_remember_me != null) {
			agentId.setText(prefs.getString("id", ""));
			password.setText(prefs.getString("password", ""));
		}
		
		Button login = (Button)findViewById(R.id.buttonLogin);
		login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final String agentid = agentId.getText().toString();
				final String passwordd = password.getText().toString();
				
				if (!agentid.trim().equals("") && !passwordd.equals("")) {
					
					//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					final SharedPreferences.Editor edit = prefs.edit();
					
					if (rememberme.isChecked()) {
						edit.putString("remember_me", "yes");
						edit.commit();
					} else {
						edit.remove("remember_me");
						edit.commit();
					}
					
					new Thread() {
						public void run() {
							
							
							edit.putString("id", agentid);
							edit.putString("password", passwordd);
							edit.commit();
						}
					}.start();
					
					
					Intent intent = new Intent(MainActivity.this, TransactionOptions.class);
					startActivity(intent);
					
					finish();
				} else Toast.makeText(getBaseContext(), "Fill text correctly.", Toast.LENGTH_LONG).show();
				
				
			}
		});
		
	}

}
