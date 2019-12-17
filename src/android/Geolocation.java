/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */


package org.apache.cordova.geolocation;

import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import java.util.Arrays;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.IntentFilter;
import javax.security.auth.callback.Callback;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.location.LocationManager;
import android.provider.Settings;


public class Geolocation extends CordovaPlugin {
    public CallbackContext callbackContext;
    String TAG = "GeolocationPlugin";
    CallbackContext context;
    Context mAndroidContext;
    static LocationTracker locationTracker;
    BroadcastReceiver mLocationReceiver, mLocationReceiverError;
    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };


    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "We are entering execute");
        LOG.d(TAG, callbackContext.toString());
        mAndroidContext=this.cordova.getActivity().getApplicationContext();
        context = callbackContext;
        this.callbackContext = callbackContext;
        //int locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

        if(action.equals("getCurrentLocationNativeAction")){
            if(hasPermisssion()){
                LOG.d("VIJ", "Came in the action i need ankit");
                boolean retVal = true;

                try {
                  getLatLongLocal();
                }catch (IllegalStateException il){
                    LOG.d("VIJ", "came in exceptoin");
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Unable to find location");
                    result.setKeepCallback(true);
                    this.callbackContext.sendPluginResult(result);
                    retVal = true;
                }
                return retVal;
            }
            else{
                PermissionHelper.requestPermissions(this, 0, permissions);
                return true;
            }

            
        }
        if(action.equals("getPermission"))
        {
            if(hasPermisssion())
            {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(r);
                getLatLongLocal();
                return true;
            }
            else {
                PermissionHelper.requestPermissions(this, 0, permissions);
            }
            return true;
        }
        Log.d("VIJ", "Fucked!!");
        return false;
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        LOG.d(TAG, grantResults.toString());
        LOG.d("VIJ", "Came in on request permission");

        Log.d("MYINT", "value: " + requestCode);
        //Log.d("this is my array", "arr: " + Arrays.toString(permissions));
        Log.d("this is my array2", "arr: " + Arrays.toString(grantResults));
        if(grantResults[0]==0){

            try {
                getLatLongLocal();
            }catch (IllegalStateException il){
               // LOG.d("VIJ", "came in exceptoin");
                //retVal = false;
            }
        }else{
            PluginResult result = new PluginResult(PluginResult.Status.ERROR);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
    }

    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    /*
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     */

    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }


    private boolean sendEvent(Double lat, Double lng ) {
        if (this.callbackContext == null) {
            return false;
        }
        JSONObject event = new JSONObject();
        try {
            event.put("latitude", lat);
            event.put("longitude", lng);
            JSONObject obj = new JSONObject();
            obj.put("coords",event);
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
            return  true;
            //this.callbackContext.success();
        } catch (JSONException e) {
            return false;
            //logger.error("Error sending event {}: {}", name, e.getMessage());
        }
    }

    private void getLatLongLocal(){
        registerLocationBroadCast();
        registerLocationErrorBroadCast();
        //context.sendPluginResult(r);


        locationTracker	=	new LocationTracker(mAndroidContext);
        locationTracker.init();



    }


    private void registerLocationBroadCast(){
        IntentFilter filter = new IntentFilter(ApiConstants.LOCAL_BRAODCAST_LOCATION);

        mLocationReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                LocalBroadcastManager.getInstance(context).unregisterReceiver(mLocationReceiver);
                LatLng newLatLng = new LatLng(intent.getDoubleExtra(ApiConstants.EXTRA_LOCATION_LATITUDE, 0.0), intent.getDoubleExtra(ApiConstants.EXTRA_LOCATION_LONGITUDE, 0.0));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("latitude", newLatLng.latitude);
                    jsonObject.put("longitude", newLatLng.longitude);
                } catch (JSONException e) {
                    try {
                        jsonObject.put("errorMsg","unable to fetch");
                        jsonObject.put("errorCode","2");
                    } catch (JSONException e1) {
                    }
                }

                String result	=	jsonObject.toString();


                // mWebView.loadUrl("javascript:latLong('"+result+"')");
                locationTracker.StopSendingUpdates();

                Log.i("shobhit","lattitude:"+newLatLng.latitude+" longitude:"+newLatLng.longitude);
                //return newLatLng.latitude;
                //this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "jkdfkjjjk"));
                sendEvent(newLatLng.latitude, newLatLng.longitude );
            }
        };
        // register the receiver:
        LocalBroadcastManager.getInstance(mAndroidContext).registerReceiver(mLocationReceiver, filter);
    }


    //TODO: Oneplus fix
    private void registerLocationErrorBroadCast(){
        IntentFilter filter = new IntentFilter(ApiConstants.LOCAL_BRAODCAST_LOCATION_ERROR);

        mLocationReceiverError = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent){
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mLocationReceiverError);
                intent.getStringExtra("error");
                Log.d("VIJ", "cust error" + intent.getStringExtra("error"));
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, intent.getStringExtra("error"));
                result.setKeepCallback(true);
                Geolocation.this.callbackContext.sendPluginResult(result);

            }
        };
        // register the receiver:
        LocalBroadcastManager.getInstance(mAndroidContext).registerReceiver(mLocationReceiverError, filter);
    }
}

