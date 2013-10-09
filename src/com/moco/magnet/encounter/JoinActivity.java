package com.moco.magnet.encounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class JoinActivity extends Activity {
	private final String sessionsUrl = "https://encounter-sessions.firebaseIO.com/";
	private final String usersUrl = "https://encounter-users.firebaseIO.com/";

	private Firebase sessions = new Firebase(sessionsUrl);
	private Firebase users = new Firebase(usersUrl);
	
	String join_code = "";
	String firstDeviceID = "";
	String deviceID = "";

	/******************************************************************/
	public static class Message {

		private String device_id1;
		private String device_id2;

		private Message() { }

		public Message(String device_id1, String device_id2) {
			this.device_id1 = device_id1;
			this.device_id2 = device_id2;
		}

		public String getDeviceID1() {
			return device_id1;
		}

		public String getDeviceID2() {
			return device_id2;
		}
	}
	/******************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        sessions.child(join_code).removeValue();
	        sessions.child(join_code).setValue(firstDeviceID);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	public void joinRoom(View v) {
		if(!isNetworkAvailable()) {
			Toast.makeText(this, "Network connection is not available.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Set listener
		sessions.addValueEventListener(new ValueEventListener() {
			boolean hasConfirmed = false;
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				if(hasConfirmed == false) {

					join_code = ((EditText) JoinActivity.this.findViewById(R.id.join_pin)).getText().toString();

					if(snapshot.child(join_code).getValue() == null) { // If room does not exist					
						((TextView) JoinActivity.this.findViewById(R.id.invalid_pin)).setVisibility(View.VISIBLE);

					} else {
						deviceID = Secure.getString(JoinActivity.this.getContentResolver(),
								Secure.ANDROID_ID);

						firstDeviceID = snapshot.child(join_code).getValue().toString();

						hasConfirmed = true;

						sessions.child(join_code).setValue(new Message(firstDeviceID, deviceID));
						
						Intent intent = new Intent(JoinActivity.this, MapActivity.class);
						intent.putExtra("CREATEORJOIN", 1);
						intent.putExtra("ROOMSTRING", join_code);
						intent.putExtra("DEVICEID", deviceID);
						startActivity(intent);
					}

				}

			}

			@Override
			public void onCancelled() {
				System.err.println("Listener was cancelled");
			}
		});
	}


}
