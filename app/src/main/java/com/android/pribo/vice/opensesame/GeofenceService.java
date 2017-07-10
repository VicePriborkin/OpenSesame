package com.android.pribo.vice.opensesame;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by user on 07/07/2017.
 */

public class GeofenceService extends IntentService {

    public static final String TAG = "GeofenceService";

    public GeofenceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Toast.makeText(this, "Error handeling intent", Toast.LENGTH_SHORT).show();
        } else {

            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "Entering Geofence - " + requestId);
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: 0546277833"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
            }else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                Log.d(TAG , "Exiting Geofence - " + requestId);
            }
        }
    }
}
