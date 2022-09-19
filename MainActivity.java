package com.example.demoappforlocation;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView latText, lonText ;
    Button btnStart, btnStop, btnRestart ;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    double lastLatitudeValue;
    double lastLongitudeValue;
    boolean isLocationUpdating ;
    double prevLat;
    double prevLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latText = findViewById(R.id.Lat);
        lonText = findViewById(R.id.Lon);
        btnStart = findViewById(R.id.StartUpdate);
        btnStop = findViewById(R.id.StopUpdate);
        btnRestart = findViewById(R.id.RestartUpdate);
        isLocationUpdating = false;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermission();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdates();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
            }
        });

        btnRestart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {stopLocationUpdates();startLocationUpdates();}
        });

    }

    private void stopLocationUpdates() {
        if(!isLocationUpdating)
        {
            Toast.makeText(this.getApplicationContext(),"No Location Request is alive!!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            isLocationUpdating=false;
            Toast.makeText(this.getApplicationContext(),"Stopped Location Updates!!",Toast.LENGTH_SHORT).show();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
            }
            else if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)){
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates()
    {
        if(isLocationUpdating)
        {Toast.makeText(this.getApplicationContext(),"Location is Already Updating",Toast.LENGTH_SHORT).show();
        return;}
        else {
            isLocationUpdating = true;
            Toast.makeText(this.getApplicationContext(), "Starting Location Updates", Toast.LENGTH_SHORT).show();
            locationCallback = new LocationCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location lastLocation = locationResult.getLastLocation();
                    if (lastLocation != null) {
                        Log.e("startLocationUpdates", lastLocation.getLatitude() + "/" + lastLocation.getLongitude());
                        Double lat = lastLocation.getLatitude();
                        Double lng = lastLocation.getLongitude();
                        float acc = lastLocation.getAccuracy();
                        try {
                            Date currentTime = Calendar.getInstance().getTime();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lastLatitudeValue = lat;
                        lastLongitudeValue = lng;
                        String temp = "lat :" + lastLatitudeValue + ", lng :" + lastLongitudeValue;
//                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                        System.out.println("lat :" + lastLatitudeValue + ", lng :" + lastLongitudeValue);
                        latText.setText("Latitude : "+ lastLatitudeValue);
                        lonText.setText("Longitude : " + lastLongitudeValue);
                        if(prevLat != lastLatitudeValue && prevLon != lastLongitudeValue)
                        callDriverLocationUpdateAPI(lastLatitudeValue,lastLongitudeValue,acc);
                        prevLat = lastLatitudeValue;
                        prevLon = lastLongitudeValue;

                    } else {

                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    private void callDriverLocationUpdateAPI( double latitude, double longitude, float acc){
        String token = "546d785d-e2e1-4621-a40e-2f47b3623c11";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() ->{
            StringBuilder result = new StringBuilder();
            try {
                String orderUrl = "https://api.sandbox.beckn.juspay.in/dobpp/ui" + "/driver/location";
                System.out.print("in driver Location UPdate API"+ orderUrl);
                HttpURLConnection connection = (HttpURLConnection) (new URL(orderUrl).openConnection());
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("token", token);
                connection.setDoOutput(true);


                final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                System.out.println(" in SimpleDateFormat : " + f);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                String getCurrTime = f.format(new Date());

                JSONArray payload = new JSONArray();
                JSONObject point = new JSONObject();
                JSONObject locationData = new JSONObject();
                point.put("lat", latitude);
                point.put("lon", longitude );
                locationData.put("pt",point);
                locationData.put("ts",getCurrTime);
                locationData.put("acc",acc);
                payload.put(locationData);

                OutputStream stream = connection.getOutputStream();
                stream.write(payload.toString().getBytes());
                connection.connect();
                int respCode = connection.getResponseCode();
                InputStreamReader respReader;

                if ((respCode < 200 || respCode >= 300) && respCode != 302) {
                    respReader = new InputStreamReader(connection.getErrorStream());
                    System.out.print("in error : "+ respReader);
                } else {
                    respReader = new InputStreamReader(connection.getInputStream());
                    System.out.print("in 200 : "+ respReader);
                }

                BufferedReader in = new BufferedReader(respReader);
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                }
                System.out.print("in result : "+ result.toString());
            } catch (Exception ignored) {
                System.out.println("Catch in call Driver Location Update API : " +ignored);
            }
            handler.post(()->{
//                onDestroy();
                executor.shutdown();
            });
        });
    }

}

