package com.moco.magnet.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Document;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.moco.magnet.encounter.JoinActivity.Devices;
import com.moco.magnet.encounter.MyLocationListener.NotifyInterface;

public class MapActivity extends FragmentActivity implements NotifyInterface {
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	GoogleMap mMap;
	MyLocationListener.Coordinates other_coord = new MyLocationListener.Coordinates();
	String deviceID = "";
	String otherID = "";
	Collection<String> otherDevices = new ArrayList<String>();
	Collection<String> oldDevices = new ArrayList<String>();

	String roomNum = "";
	int createOrJoin = 0; // Create = 0, Join = 1

	private final String sessionsUrl = "https://encounter-sessions.firebaseIO.com/";
	private final String usersUrl = "https://encounter-users.firebaseIO.com/";

	private Firebase sessions = new Firebase(sessionsUrl);
	private Firebase users = new Firebase(usersUrl);

	DataSnapshot sessionsSnapshot;
	DataSnapshot usersSnapshot;

	MyLocationListener a; 
	Marker location;

	boolean foundOtherID = false;
	boolean foundPersonAlready = false;
	boolean newDeviceFound = false;

	GMapV2Direction md;

	HashMap<String, Polyline> mapLines = new HashMap<String, Polyline>();
	HashMap<String, Marker> mapMarkers = new HashMap<String, Marker>();
	Polyline last;

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

		// Move the camera instantly to currLocation with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(15));



		// Set session listener to get other device id
		//		sessions.addChildEventListener(new ChildEventListener() { // This is not responding
		//			@Override
		//			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
		//				sessionsSnapshot = snapshot;
		//				updateFromSnapshots();
		//			}
		//			
		//			@Override
		//			public void onChildRemoved(DataSnapshot snapshot) {
		//				sessionsSnapshot = snapshot;
		//				updateFromSnapshots();
		//			}
		//			
		//			@Override
		//			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
		//				sessionsSnapshot = snapshot;
		//				updateFromSnapshots();
		//			}
		//			
		//			@Override
		//			public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
		//				sessionsSnapshot = snapshot;
		//				updateFromSnapshots();
		//			}
		//
		//			@Override
		//			public void onCancelled() {
		//				System.err.println("Listener was cancelled");
		//			}
		//		});

		sessions.addValueEventListener(new ValueEventListener() { // This is not responding
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				sessionsSnapshot = snapshot;
				updateFromSnapshots();
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
				usersSnapshot = snapshot;
				updateFromSnapshots();
			}

			@Override
			public void onCancelled() {
				System.err.println("Listener was cancelled");
			}
		});


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		getActionBar().setDisplayHomeAsUpEnabled(false);



		deviceID = getIntent().getStringExtra("DEVICEID");		  
		roomNum = getIntent().getStringExtra("ROOMSTRING");
		createOrJoin = getIntent().getIntExtra("CREATEORJOIN", 0);

		setTitle("Room " + roomNum + ": 1 person");

		a = new MyLocationListener(this, this, deviceID);


		// Show your marker
		a.showCurrentLocation(); 
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		if(a != null) {
			LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
			location = map.addMarker(new MarkerOptions().position(currLocation)
					.title("Current Location"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void updateFromSnapshots() {

		if(sessionsSnapshot != null) {
			HashMap<String,ArrayList<String>> deviceMap = (HashMap) sessionsSnapshot.child(roomNum).getValue();

			if(deviceMap != null) {
				ArrayList<String> deviceIDs = deviceMap.get("list");	


				if(deviceIDs != null) {
					if(deviceIDs.size() > otherDevices.size() + 1) {
						newDeviceFound = true;
					} else {
						newDeviceFound = false;
					}

					// This is to know which markers to remove later
					Collection<String> tempIDs = otherDevices;
					tempIDs.removeAll(deviceIDs);
					oldDevices = tempIDs;

					// Set new device IDs
					otherDevices = deviceIDs;
					otherDevices.remove(deviceID);
					otherDevices.remove(null);

					Toast.makeText(MapActivity.this, "Old: " + oldDevices.toString(), Toast.LENGTH_SHORT).show();
					Toast.makeText(MapActivity.this, "Other: " + otherDevices.toString(), Toast.LENGTH_SHORT).show();

					foundOtherID = true;
				} else {
					foundOtherID = false;
				}

			}
		}

		if(usersSnapshot != null) {
			if(otherDevices != null) {

				int roomSize = 1 + otherDevices.size();
				setTitle("Room " + roomNum + ": " + roomSize + " people");


				// Set color for other markers
				BitmapDescriptor bitmapDescriptor 
				= BitmapDescriptorFactory.defaultMarker(
						BitmapDescriptorFactory.HUE_AZURE);

				// Remove old device markers and paths
				for(String oldID: oldDevices) {
					if(mapMarkers.get(oldID) != null) {
						mapMarkers.get(oldID).remove();
						mapMarkers.remove(oldID);
						Toast.makeText(MapActivity.this, "Marker has been removed!", Toast.LENGTH_SHORT).show();
					}
					if(mapLines.get(oldID) != null) {
						mapLines.get(oldID).remove();
						mapLines.remove(oldID);
						Toast.makeText(MapActivity.this, "Path has ben removed!", Toast.LENGTH_SHORT).show();
					}


				}


				// Change and update markers that still exist in this room
				for(String otherID: otherDevices) {
					if(otherID != null && usersSnapshot.child(otherID) != null) {

						HashMap<String, String> coord = ((HashMap) usersSnapshot.child(otherID).getValue()); // Need if check or when otherID is invalid
						coord.put("deviceID", otherID);

						LatLng otherLocation = new LatLng(Double.parseDouble(coord.get("latitude")), Double.parseDouble(coord.get("longitude")));
						Marker otherPerson = mapMarkers.get(otherID);

						// Adding a new marker
						if(otherPerson == null) {
							otherPerson = map.addMarker(new MarkerOptions()
							.position(otherLocation)
							.icon(bitmapDescriptor)
							.title("Other Person"));

							mapMarkers.put(otherID, otherPerson);

							// Zoom camera to include both markers
							LatLngBounds.Builder builder = new LatLngBounds.Builder();
							builder.include(location.getPosition());

							for(Marker m: mapMarkers.values()) {
								builder.include(m.getPosition());
							}

							LatLngBounds bounds = builder.build();

							int padding = 350; // offset from edges of the map in pixels

							Display display = getWindowManager().getDefaultDisplay(); 
							int width = display.getWidth();
							int height = display.getHeight();

							CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

							map.animateCamera(cu);

						} else {
							otherPerson.setPosition(otherLocation);
						}

						// Remove previous line
						if(mapLines.get(otherID) != null){
							mapLines.get(otherID).remove();
						}
						new accessDirectionsTask().execute(coord);

					}
				}
			}
		}		



	}

	@Override
	public void notifyChange() {
		a.showCurrentLocation(); 

		LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
		location.setPosition(currLocation);
	}

	@Override
	public void onBackPressed() {
		leaveActivity();
	}

	public void doneAction(View v) {
		leaveActivity();
	}

	public void leaveActivity() {
		if(otherDevices.size() > 0) {
			// Make new device to update database with
			Devices dev = new Devices(deviceID);
			dev.addAllToList(otherDevices);
			dev.removeFromList(deviceID);

			// Update to Firebase
			sessions.child(roomNum).setValue(dev);

			foundPersonAlready = true;
		} else {
			sessions.child(roomNum).removeValue();
		}
		NavUtils.navigateUpFromSameTask(this);
	}

	public void createChat(View v) {
		Intent intent = new Intent(v.getContext(), SplashActivity.class);
		startActivity(intent);
	}

	public void moveToCurrLocation(View v) {
		// Move the camera instantly to currLocation
		LatLng currLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLng(currLocation));
	}



	// This AsyncTask obtains directions from Gmaps behind the scenes
	private class accessDirectionsTask extends AsyncTask<HashMap<String, String>, Void, Document> {
		HashMap<String, String> coord;

		@Override
		protected Document doInBackground(HashMap<String, String>... arg0) {
			// If other person's marker is available, draw path!
			coord = arg0[0];

			LatLng sourceLocation = new LatLng(a.location.getLatitude(),a.location.getLongitude()); 
			LatLng oppLocation = new LatLng(Double.parseDouble(coord.get("latitude")), Double.parseDouble(coord.get("longitude")));

			md = new GMapV2Direction();

			SupportMapFragment mapFrag = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map));

			if(mapFrag != null) {
				mMap = mapFrag.getMap();
			}

			if(last != null) {
				last.remove();
			}

			GMapV2Direction direct = new GMapV2Direction();
			return direct.getDocument(sourceLocation, oppLocation,
					GMapV2Direction.MODE_WALKING);
		}

		@Override
		protected void onPostExecute(Document doc) {
			if(doc != null) {

				ArrayList<LatLng> directionPoint = md.getDirection(doc);
				PolylineOptions rectLine = new PolylineOptions().width(5).color(
						Color.RED);

				for (int i = 0; i < directionPoint.size(); i++) {
					rectLine.add(directionPoint.get(i));
				}

				String otherID = coord.get("deviceID");

				mapLines.put(otherID, mMap.addPolyline(rectLine)); // Null Pointer Exception Here
			}
		}
	}

} 