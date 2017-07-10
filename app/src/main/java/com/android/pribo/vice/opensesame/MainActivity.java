package com.android.pribo.vice.opensesame;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String GEOFENCE_ID = "SesameGeofenceID";

    GoogleApiClient googleApiClient  = null;
    FusedLocationProviderClient client;

    TextView tvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tvMain = (TextView) findViewById(R.id.tvMain);
        client = new FusedLocationProviderClient(this);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).
                addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                        Toast.makeText(MainActivity.this, "Connected to GoogleApiClient", Toast.LENGTH_SHORT).show();
                        startLocationMonitoring();
                        startGeofenceMonitoring();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended connection to GoogleApiClient");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "Failed to connect to GoogleApiClient - " + connectionResult.getErrorMessage());
                        Toast.makeText(MainActivity.this,
                                "GoogleApiClient connection failed" + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }
                }).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.reconnect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return false;
        }

        return true;
    }

    private void startLocationMonitoring() {
        Log.d(TAG, "startLocationCalled");

        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(5 * 1000)
                .setFastestInterval(2 * 1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "Location update: \n" + location.getLatitude()
                                + " " + location.getLongitude());
                        tvMain.setText("Location update: \n" + location.getLatitude()
                                + " " + location.getLongitude());
                    }
                });
        if (!checkLocationPermission()) return;
        //client.requestLocationUpdates(locationRequest, callback, null);


    }

    private void startGeofenceMonitoring() {
        Log.d(TAG, "startMonitoringCalled");
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(32.1542598, 35.1020015, 200)//32.1498725 , 35.1095868
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build();

        Intent intent = new Intent(this, GeofenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "GoogleApiClient is NOT connected");
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "Successfully added geofence");
                                Toast.makeText(MainActivity.this, "Successfully added geofence", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Failed to add Geofence " + status.getStatus());
                                Toast.makeText(MainActivity.this, "Failed to add Geofence", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    LocationCallback callback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();

            String result = String.format(Locale.getDefault() ,
                    "(%e , %e) \n Speed: %e\n Time: %d\n",
                    location.getLatitude() , location.getLongitude()
                    , location.getSpeed() , location.getTime());
            tvMain.setText(result);
        }
    };
}
