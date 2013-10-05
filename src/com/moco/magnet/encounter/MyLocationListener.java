package com.moco.magnet.encounter;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {
		protected LocationManager lm;
		public String message="Hello";
		Location location;
		private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; 
		private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000000; 
		Context mContext;
		public MyLocationListener(Context mContext) {
		    // TODO Auto-generated constructor stub
		    this.mContext = mContext;
		    String context=Context.LOCATION_SERVICE;
		    lm = (LocationManager)mContext.getSystemService(context);
		    lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,this);
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
		    /*  String message = String.format(
		              "New Location \n Longitude: %1$s \n Latitude: %2$s",
		              location.getLongitude(), location.getLatitude()
		      );
		      t1.setText(message);
		    Toast.makeText(WinUirep.this,
		              "Location Changed !",
		              Toast.LENGTH_LONG).show();*/

		  }

		  public void onStatusChanged(String s, int i, Bundle b) 
		  {

		  }

		  public void onProviderDisabled(String s) 
		  {

		  }

		  public void onProviderEnabled(String s) 
		  {

		  }


		
			
		}

