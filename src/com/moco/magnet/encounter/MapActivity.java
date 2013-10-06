package com.moco.magnet.encounter;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.moco.magnet.encounter.MyLocationListener.NotifyInterface;

public class MapActivity extends FragmentActivity implements NotifyInterface {
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	MyLocationListener.Coordinates other_coord = new MyLocationListener.Coordinates();
	String deviceID = "";
	String otherID = "";
	String roomNum = "";
	int createOrJoin = 0; // Create = 0, Join = 1

	private final String sessionsUrl = "https://encounter-sessions.firebaseIO.com/";
	private final String usersUrl = "https://encounter-users.firebaseIO.com/";

	private Firebase sessions = new Firebase(sessionsUrl);
	private Firebase users = new Firebase(usersUrl);

	MyLocationListener a; 
	Marker location;
	Marker otherPerson = null;

	boolean foundOtherID = false;

	@Override
	public void onPause() {
		super.onPause();
		a.lm.removeUpdates(a);
	}

	@Override
	public void onStart() {
		super.onStart();


		// Show your marker
		a.showCurrentLocation(); 
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
		location.setPosition(currLocation);

		// Set session listener to get other device id
		sessions.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {

				try {
					HashMap<String, String> deviceIDs = (HashMap <String, String>) snapshot.child(roomNum).getValue();
					if(createOrJoin == 0) {
						otherID = deviceIDs.get("deviceID2");
					} else {
						otherID = deviceIDs.get("deviceID1");
					}

					foundOtherID = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					foundOtherID = false;
				}

			}

			@Override
			public void onCancelled() {
				System.err.println("Listener was cancelled");
			}
		});

		// Set users listener
		users.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				if(foundOtherID == true && snapshot.child(otherID) != null) {
					setTitle("Room " + roomNum + ": 2 people");
					
					// Show other person's marker
					BitmapDescriptor bitmapDescriptor 
					= BitmapDescriptorFactory.defaultMarker(
							BitmapDescriptorFactory.HUE_AZURE);

					HashMap<String, String> coord = ((HashMap) snapshot.child(otherID).getValue());

					LatLng otherLocation = new LatLng(Double.parseDouble(coord.get("latitude")), Double.parseDouble(coord.get("longitude")));

					if(otherPerson == null) {
						otherPerson = map.addMarker(new MarkerOptions()
						.position(otherLocation)
						.icon(bitmapDescriptor)
						.title("Other Person"));				

					} else {
						otherPerson.setPosition(otherLocation);
					}

					// If other person's marker is available, draw path!
					LatLng sourceLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 

					GMapV2Direction md = new GMapV2Direction();
					GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager()
							.findFragmentById(R.id.map)).getMap();
					final Document doc = md.getDocument(sourceLocation, otherLocation,
							GMapV2Direction.MODE_WALKING);

					if(doc != null) {

						ArrayList<LatLng> directionPoint = md.getDirection(doc);
						PolylineOptions rectLine = new PolylineOptions().width(3).color(
								Color.RED);

						for (int i = 0; i < directionPoint.size(); i++) {
							rectLine.add(directionPoint.get(i));
						}
						Polyline polylin = mMap.addPolyline(rectLine);

					}
				}
			}

			@Override
			public void onCancelled() {
				System.err.println("Listener was cancelled");
			}
		});




		// Move the camera instantly to currLocation with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(15), 4000, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		getActionBar().setDisplayHomeAsUpEnabled(true);



		deviceID = getIntent().getStringExtra("DEVICEID");		  
		roomNum = getIntent().getStringExtra("ROOMSTRING");
		createOrJoin = getIntent().getIntExtra("CREATEORJOIN", 0);

		setTitle("Room " + roomNum + ": 1 people");

		a = new MyLocationListener(this, this, deviceID);


		// Show your marker
		a.showCurrentLocation(); 
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
		location = map.addMarker(new MarkerOptions().position(currLocation)
				.title("Current Location"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void notifyChange() {
		a.showCurrentLocation(); 

		LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
		location.setPosition(currLocation);
	}

	public void doneAction(View v) {
		NavUtils.navigateUpFromSameTask(this);
	}

	public void createChat(View v) {
		Intent intent = new Intent(v.getContext(), SplashActivity.class);
		startActivity(intent);
	}

} 