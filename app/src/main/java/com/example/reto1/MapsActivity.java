package com.example.reto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final int REQUEST_CODE = 11;

    private GoogleMap mMap;
    private Geocoder geocoder;
    private LocationManager manager;

    private Marker actualMarker;
    private List<Address> actualAddres;
    private LatLng actualPosition,customPosition;
    private Location userLocation,customLocation;

    private ArrayList<Marker> myMarkers;

    private EditText locationText;
    private TextView textInformation;
    private FloatingActionButton fab;

    private boolean control = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Initialize variables
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        textInformation = findViewById(R.id.textInformation);
        textInformation.setText("No hay lugares disponibles");
        fab = findViewById(R.id.fab);
        locationText = findViewById(R.id.locationText);
        locationText.setVisibility(View.INVISIBLE);
        myMarkers = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Add functionality to fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control = true;
                locationText.setVisibility(View.VISIBLE);
                Snackbar.make(view, "Seleccione el lugar que desea guardar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        } else {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (actualMarker == null) {
                actualPosition = new LatLng(location.getLatitude(), location.getLongitude());

                try {
                    actualAddres = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String addres = actualAddres.get(0).getAddressLine(0);
                    actualMarker = mMap.addMarker(new MarkerOptions().position(actualPosition).title(addres));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(actualPosition));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                userLocation = location;
                actualPosition = null;
                actualPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actualPosition, 15));
                actualMarker.setPosition(actualPosition);
                try {
                    actualAddres = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String addres = actualAddres.get(0).getAddressLine(0);
                    actualMarker.setTitle(addres);

                } catch (IOException e) {
                    Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if(control){
            customPosition = latLng;
            customLocation= new Location("custom Location");
            customLocation.setLongitude(latLng.longitude);
            customLocation.setLatitude(latLng.latitude);
            Marker customMarker = null;
            double distance = Math.round((userLocation.distanceTo(customLocation)*100))/100d;
            try {
                locationText = findViewById(R.id.locationText);
                String locText = locationText.getText().toString();
                String addres = locText+" ubicado en: "+geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0).getAddressLine(0).split(",")[0];
                if (customMarker==null){
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(customPosition).title(addres).snippet("La distancia es "+distance+" m").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    myMarkers.add(newMarker);
                }

                control = false;
                locationText.setVisibility(View.INVISIBLE);
                locationText.setText(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getNear();
    }

    public void getNear(){

        double distance = Double.MAX_VALUE;
        Location myLocation = new Location("variable location");
        Marker near = null;


        for (int i = 0; i < myMarkers.size();i++){
            myLocation.setLatitude(myMarkers.get(i).getPosition().latitude);
            myLocation.setLongitude(myMarkers.get(i).getPosition().longitude);
            double auxDistance = Math.round((userLocation.distanceTo(myLocation)*100)/100d);

            if (auxDistance<distance){
                near = myMarkers.get(i);
                distance=auxDistance;
            }

        }
        if (near!=null){
            Log.i("NEAR",""+distance);
            if(distance<80){
                String message = "Estas en: " + near.getTitle();
                textInformation.setText(message);
            }else {
                String message = "El lugar más cercano es " + near.getTitle() + " a " + distance +" m";
                textInformation.setText(message);
            }
        }

    }
}
