package com.ldsilver.mapboxtrial;



//import android.graphics.Point;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, MapboxMap.OnMapClickListener {


    FloatingActionButton fabEdit;
    ExtendedFloatingActionButton fabMain;

    TextView tvFabMenu, fabEdit_text;
    Boolean isAllFabsVisible;

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point originPosition; //3:15
    private Point destinationPosition;
    private Marker destinationMarker;
    private Button startButton;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG="MainActivity";
    //private CategorySearchEngine categorySearchEngine;
    //private SearchRequestTask searchRequestTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView=findViewById(R.id.mapView);
        startButton = findViewById(R.id.start_button);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationLauncherOptions options= NavigationLauncherOptions.builder()
                        .origin(originPosition)
                        .destination(destinationPosition)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this,options);
            }
        });

        fabMain = findViewById(R.id.fab_MAIN);

        // button
        fabEdit = findViewById(R.id.fab_edit);

        // textView
        fabEdit_text = findViewById(R.id.textView_fabEdit);

        // visibility
        fabEdit.setVisibility(View.GONE);
        fabEdit_text.setVisibility(View.GONE);

        isAllFabsVisible = false;

        fabMain.shrink();
        fabMain.setOnClickListener(v -> {
            if (!isAllFabsVisible) {
                fabEdit.setVisibility(View.VISIBLE);
                fabEdit_text.setVisibility(View.VISIBLE);

                fabMain.extend();

                isAllFabsVisible = true;
            } else {
                fabEdit.setVisibility(View.GONE);
                fabEdit_text.setVisibility(View.GONE);

                fabMain.shrink();

                isAllFabsVisible = false;
            }
        });

        // click on fab edit
        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditProfile_Activity.class);
            startActivity(intent);
        });
    }




    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener(this);
        enableLocation();
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location LastLocation = locationEngine.getLastLocation();
        if (LastLocation != null) {
            originLocation = LastLocation;
            setCameraPosition(LastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);

        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 13.0));
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

        if (destinationMarker != null) {
            map.removeMarker(destinationMarker);
        }
        destinationMarker = map.addMarker(new MarkerOptions().position(point));

        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
        getRoute(originPosition,destinationPosition);
        startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.mapbox_blue);
    }

    private void getRoute(Point origin, Point destination){
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body()==null){
                            Log.e(TAG,"No routes found, check right user and access token");
                        return;
                        }else if(response.body().routes().size()==0)
                        {
                            Log.e(TAG,"No routes found");
                            return;
                        }
                        DirectionsRoute currentRoute= response.body().routes().get(0);


                        if(navigationMapRoute!=null){
                            navigationMapRoute.removeRoute();
                        }else{
                            navigationMapRoute= new NavigationMapRoute(null,mapView,map);

                        }
                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG,"Error:"+t.getMessage());
                    }
                });

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //present toast to grant access

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();

        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }

        mapView.onDestroy();
    }

    // back press button to exit the app
    int counter = 0;
    @Override
    public void onBackPressed() {
        // time is in milliseconds
        counter++;
        if(counter == 1){
            Toast.makeText(this, "Press Back again to exit the application.", Toast.LENGTH_SHORT).show();
        }else if(counter == 2){
            super.onBackPressed();
            finishAffinity();
        }
    }
}




