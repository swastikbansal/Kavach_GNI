package com.example.kavach;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationHandler {
    public interface LocationListener {
        void onLocationChanged(double latitude, double longitude);
        void onLocationChanged(Location location);
    }

    String userLocationMsg = "I am in an Emergency Situation. I need Help.";
    private LocationRequest locationRequest;
    private Context context;

    private LocationListener locationListener;

    // Constructor
    public LocationHandler(Context context, LocationListener listener) {
        this.context = context;
        this.locationListener = listener;

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
    }

    // Function for getting Current Location
    public void getCurrentLocation() {
        userLocationMsg = "I am in an Emergency Situation. I need Help.";
        SmsHandler.sendSMS(context, userLocationMsg, Manifest.permission.SEND_SMS);

        // Checking for Location Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    if (context instanceof Activity) {
                        LocationServices.getFusedLocationProviderClient(context)
                                .requestLocationUpdates(locationRequest, createLocationCallback(), Looper.getMainLooper());
                    } else {
                        LocationServices.getFusedLocationProviderClient(context.getApplicationContext())
                                .requestLocationUpdates(locationRequest, createLocationCallback(), Looper.getMainLooper());
                    }
                } else {
                    turnOnGPS();
                }
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(context.getApplicationContext())
                        .removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int index = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(index).getLatitude();
                    double longitude = locationResult.getLocations().get(index).getLongitude();

                    userLocationMsg = "Here is my location :\nhttps://www.google.com/maps?q=";
                    locationListener.onLocationChanged(latitude, longitude);

                    String lastmsg = userLocationMsg;
                    userLocationMsg = userLocationMsg + latitude + "," + longitude;

                    if (!lastmsg.equals(userLocationMsg)) {
                        Log.d("SMS", userLocationMsg);
                        SmsHandler.sendSMS(context, userLocationMsg, Manifest.permission.SEND_SMS);
                        userLocationMsg = lastmsg;
                    }
                }
            }
        };
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(context, "GPS is already turned on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                if (context instanceof Activity) {
                                    resolvableApiException.startResolutionForResult((Activity) context, 2);
                                }
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
