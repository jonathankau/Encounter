package com.moco.magnet.encounter;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class CreateActivity extends Activity {
	private int randNum = 0;
	private final String sessionsUrl = "https://encounter-sessions.firebaseIO.com/";
	private final String usersUrl = "https://encounter-users.firebaseIO.com/";

	private Firebase sessions = new Firebase(sessionsUrl);
	private Firebase users = new Firebase(usersUrl);

	String new_code = "";
	String deviceID = "";
	boolean foundNewCode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set listener
		sessions.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				if(foundNewCode == false) {
					Random r = new Random();
					randNum = r.nextInt(1001);
					new_code = String.format("%04d", randNum);

					while(snapshot.child(new_code).getValue() != null) {
						randNum = r.nextInt(1001);
						new_code = String.format("%04d", randNum);
					}

					foundNewCode = true;

					deviceID = Secure.getString(CreateActivity.this.getContentResolver(),
							Secure.ANDROID_ID);

					sessions.child(new_code).setValue(deviceID);

					// Update the actual textview with the new pin
					TextView code = (TextView) CreateActivity.this.findViewById(R.id.new_pin);
					code.setText(new_code);
				}
			}

			@Override
			public void onCancelled() {
				System.err.println("Listener was cancelled");
			}
		});
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
			if(sessions != null && sessions.child(new_code) != null) {
				sessions.child(new_code).removeValue();
			}
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void createRoom(View v) {
		if(foundNewCode) {		
			Intent intent = new Intent(CreateActivity.this, MapActivity.class);
			intent.putExtra("CREATEORJOIN", 0);
			intent.putExtra("ROOMSTRING", new_code);
			intent.putExtra("DEVICEID", deviceID);
			startActivity(intent);
		}
	}

}
