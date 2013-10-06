package com.moco.magnet.encounter;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class MyLocationListener implements LocationListener {
		public LocationManager lm;
		public String message="Hello";
		Location location;
		private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; 
		private static final long MINIMUM_TIME_BETWEEN_UPDATES = 0; 
		Context mContext;
				
		private final String sessionsUrl = "https://encounter-sessions.firebaseIO.com/";
		private final String usersUrl = "https://encounter-users.firebaseIO.com/";

		private Firebase sessions = new Firebase(sessionsUrl);
		private Firebase users = new Firebase(usersUrl);
		
		String deviceID = "";
		NotifyInterface dataPasser;
		
		
		/******************************************************************/
		public interface NotifyInterface {
			public void notifyChange();
		}
		
		/******************************************************************/
		public static class Coordinates {

			private String latitude;
			private String longitude;

			public Coordinates() { }

			public Coordinates(double latitude, double longitude) {
				this.latitude = Double.toString(latitude);
				this.longitude = Double.toString(longitude);
			}

			public String getLatitude() {
				return latitude;
			}

			public String getLongitude() {
				return longitude;
			}
		}
		/******************************************************************/
		
		public MyLocationListener(NotifyInterface notify, Context mContext, String deviceID) {
		    dataPasser = notify;
		    this.mContext = mContext;
		    this.deviceID = deviceID;
		    String context=Context.LOCATION_SERVICE;
		    lm = (LocationManager)mContext.getSystemService(context);
		    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,this);
		    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,this);
		}


		  public void showCurrentLocation() 
		  {

		        location=lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		       if (location != null) {
		             message = String.format(
		                    "Longitude: %1$s \n Latitude: %2$s",
		                    location.getLongitude(), location.getLatitude()
		            );

		          // t1.setText(message);

		      }


		    }   

		  public void onLocationChanged(Location location) 
		  {
			  Coordinates update = new Coordinates(location.getLatitude(), location.getLongitude());
			  users.child(deviceID).setValue(update);
			  
			  dataPasser.notifyChange();	   

		  }

		  public void onStatusChanged(String s, int i, Bundle b) 
		  {

		  }

		  public void onProviderDisabled(String s) 
		  {
			  Toast.makeText(mContext, "Location Provider has stopped working", Toast.LENGTH_SHORT).show();
		  }

		  public void onProviderEnabled(String s) 
		  {
			  Toast.makeText(mContext, "Location Provider has been enabled", Toast.LENGTH_SHORT).show();
		  }


		
			
		}

