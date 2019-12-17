package org.apache.cordova.geolocation;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//import com.bestsellers.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationAvailability;

public class LocationTracker implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private GoogleApiClient		mGoogleApiClient;
	private LocationRequest		mLocationRequest;
	private Location			mLastLocation;
	private Location			mCurrentLocation;
	//private String				mLastUpdateTime;
	private Context				mContext;
	private boolean isGPSEnabled = false;
	private LocationManager locationManager;

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int	CONNECTION_FAILURE_RESOLUTION_REQUEST	= 9000;
	private static final String	TAG										= LocationTracker.class.getSimpleName();

	public LocationTracker(Context context) {

		mContext = context;
		locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        //getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPSEnabled){
        	showSettingsAlert();
        }

	}

	  /**
     * Function to show settings alert dialog
     */
    private void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle("GPS Alert");

        //Setting Dialog Message
        alertDialog.setMessage("Please open your GPS");

        //On Pressing Setting button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton("Are you Sure to cancel?", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

	public void init() {
    	buildGoogleApiClient();
		createLocationRequest();
		connectClient();
		//TODO: HACK for Location issue on Oneplus
//		new android.os.Handler().postDelayed(
//				new Runnable() {
//					public void run()  {
//						Log.i("VIJ","test" + mGoogleApiClient.isConnected());
//
//						try {
//							while (LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient) == null) {
//								try {
//									Thread.sleep(500);
//									Log.d("VIJ", "Not Connected...");
//								} catch (InterruptedException e) {
//									Log.d("VIJ", "This is happening");
//									e.printStackTrace();
//								}
//							}
//							LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
//							if (locationAvailability != null && !locationAvailability.isLocationAvailable()) {
//								Log.d("VIJ", "IllegalStateException >>");
//								throw new IllegalStateException("Location not Available");
//							}
//						} catch (SecurityException se) {
//
//						}
//
//					}
//				}, 1000);
	}



	private void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(mContext).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
	}

	private void createLocationRequest() {
    	Log.d("VIJ", "Create Location Request");
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(60000); // 60 sec
		//mLocationRequest.setFastestInterval(1000); // 60 sec
		//mLocationRequest.setMaxWaitTime(4000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		Log.d("VIJ", "Create Location end");

	}

	public boolean isClientConnected() {
		return mGoogleApiClient == null ? false : mGoogleApiClient.isConnected();
	}

	public void disconnectClient() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	public void connectClient() {
		if (!isClientConnected()) {
			Log.d("VIJ", "Came to connect google api client");
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d("VIJ", "connection falied!");
		try {
			// Start an Activity that tries to resolve the error
			connectionResult.startResolutionForResult((Activity) mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			/*
			 * Thrown if Google Play services canceled the original
			 * PendingIntent
			 */
		} catch (IntentSender.SendIntentException e) {
			// Log the error

			e.printStackTrace();

		}

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult((Activity) mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();

			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */

		}

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult((Activity) mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */

		}

	}

	@Override
	public void onConnected(Bundle arg0) {
    	Log.d("VIJ", "onConnected true");
		locationRequest();
	}

	private void locationRequest(){

		try {
			Log.d("VIJ", " >>> On Connected >>> " + LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient));
			if (LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient).isLocationAvailable()) {
				//if (true) {
				PendingResult<Status> statusPendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
				statusPendingResult.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(@NonNull Status status) {
						Log.d("VIJ", "Status : " + status);
					}
				});
			} else {
				Log.d("VIJ", "Not Connected");
				Intent locationIntent = new Intent(ApiConstants.LOCAL_BRAODCAST_LOCATION_ERROR);
				locationIntent.putExtra("error", "We are unable to fetch your location");
				LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
				localBroadcastManager.sendBroadcast(locationIntent);
			}


			LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
			PendingResult<LocationSettingsResult> locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
			locationSettingsResultPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
				  @Override
				  public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
					  Log.d("VIJ",locationSettingsResult.getLocationSettingsStates()+" >>>> "+locationSettingsResult.getStatus());
					  if(!locationSettingsResult.getStatus().isSuccess()){
						  Intent locationIntent = new Intent(ApiConstants.LOCAL_BRAODCAST_LOCATION_ERROR);
						  locationIntent.putExtra("error", "Please check your location accuracy settings");
						  LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
						  localBroadcastManager.sendBroadcast(locationIntent);
					  }
				  }
			  }
			);
//			mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//			if (mLastLocation != null) {
////			broadcastLocation(mLastLocation);
//			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		Log.d("VIJ", "onConnectionSuspended ");

	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	}
	
	public void StopSendingUpdates(){
		this.stopLocationUpdates();
	}
	

	@Override
	public void onLocationChanged(Location location) {
		Log.d("VIJ", location.getLatitude() +":"+ location.getLongitude());
		mCurrentLocation = location;
		//mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		broadcastLocation(mCurrentLocation);
		this.stopLocationUpdates();
	}

	private void broadcastLocation(Location location) {
		if (location == null) {
			return;
		}
		Intent locationIntent = new Intent(ApiConstants.LOCAL_BRAODCAST_LOCATION);
		locationIntent.putExtra(ApiConstants.EXTRA_LOCATION_LATITUDE, location.getLatitude());
		locationIntent.putExtra(ApiConstants.EXTRA_LOCATION_LONGITUDE, location.getLongitude());

		Log.e(TAG, "LatLng" + location.getLatitude() + " ---- " + location.getLongitude() + " ---- " + System.currentTimeMillis());

		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		localBroadcastManager.sendBroadcast(locationIntent);
	}

	/**
	 * To know the location of the current marker, if searched from LatLng
	 * @param location
	 * @param context
	 * @return
	 */
	public String getLocality(LatLng location, Context context) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
			StringBuilder builder = new StringBuilder();
			if (addresses != null && addresses.size() > 0) {
				int addressLineCount = addresses.get(0).getMaxAddressLineIndex();
				for (int i = 0; i < addressLineCount; i++) {
					builder.append(addresses.get(0).getAddressLine(i));
					if (i != (addressLineCount - 1)) {
						builder.append(", ");
					}
				}
				return builder.toString();
			}
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

}
