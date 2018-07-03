package io.github.placedetection_standardapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;


/**
 * Monitor location updates periodically, and check whether the user stays in a certain place or not.
 */
public class MainActivity extends AppCompatActivity {
    Location location;
    LocationManager locationManager;
    String bestProvider;

    private static final int GPS_TIME_INTERVAL = 10000;
    private static final int GPS_DISTANCE = 10;
    int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // permission checking and requesting
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,},
                    MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.d("Log", "The first location: "+String.valueOf(location.getLatitude())+", "+String.valueOf(location.getLongitude()));

        // find out best provider
        List<String> providers = locationManager.getAllProviders();
        for (String provider : providers) {
            Log.d("Log", "Provider: " + locationManager.getProvider(provider));
        }
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d("Log", "BestProvider: " + bestProvider);

        // update location
        locationManager.requestLocationUpdates(bestProvider, GPS_TIME_INTERVAL, GPS_DISTANCE, locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = 0;
            double lng = 0;
            double placeLat = 0;
            double placeLng = 0;

            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                Log.d("Log", "The location has changed...");
                Log.d("Log", "Your location: "+lat+", "+lng);
            } else {
                Log.d("Log", "Your current location is temporarily unavailable.");
            }

            // Get the latitude and longitude from the place name
            Geocoder geocoder = new Geocoder(getApplicationContext());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocationName("Newell Simon Hall", 5);
                if (addresses == null) return;
                Address placeLocation = addresses.get(0);
                placeLat = placeLocation.getLatitude();
                placeLng = placeLocation.getLongitude();
                Log.d("Log", "Place location: "+placeLat+", "+placeLng);
            } catch (IOException e) {
                e.printStackTrace();
            }

            float[] results = new float[1];
            Location.distanceBetween(lat, lng, placeLat, placeLng, results);
            Log.d("Log", String.valueOf(results[0]));
            if (results[0] <= 20) {
                Log.d("Log", "In Newell Simon Hall");
                Log.d("Log", "Current time"+System.currentTimeMillis());
            }
            else
                Log.d("Log", "Out of Newell Simon Hall");

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("Log", "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d("Log", "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d("Log", "onProviderDisabled");
        }
    };

}
