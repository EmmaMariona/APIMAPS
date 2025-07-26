package com.example.mapacentroseducativos;

import androidx.fragment.app.FragmentActivity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng puntoInicio = null;
    private LatLng puntoFin = null;
    private Marker marcadorInicio, marcadorFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng[] escuelas = {
                new LatLng(15.5018, -88.0308),
                new LatLng(15.5032, -88.0229),
                new LatLng(15.5023, -88.0331),
                new LatLng(15.5059, -88.0250),
                new LatLng(15.4995, -88.0179)
        };

        String[] nombres = {
                "Instituto José Trinidad Reyes",
                "Instituto Perla del Ulúa",
                "Instituto Genaro Muñoz",
                "Escuela Experimental UNAH-VS",
                "Escuela Marco Aurelio Soto"
        };

        for (int i = 0; i < escuelas.length; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(escuelas[i])
                    .title(nombres[i])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(escuelas[0], 15));

        mMap.setOnMapClickListener(latLng -> {
            if (puntoInicio == null) {
                puntoInicio = latLng;
                marcadorInicio = mMap.addMarker(new MarkerOptions()
                        .position(puntoInicio)
                        .title("Inicio")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            } else if (puntoFin == null) {
                puntoFin = latLng;
                marcadorFin = mMap.addMarker(new MarkerOptions()
                        .position(puntoFin)
                        .title("Destino")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                String url = getDirectionsUrl(puntoInicio, puntoFin);
                new DownloadTask().execute(url);
            } else {
                puntoInicio = null;
                puntoFin = null;
                mMap.clear();

                for (int i = 0; i < escuelas.length; i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(escuelas[i])
                            .title(nombres[i])
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }

                Toast.makeText(this, "Ruta reiniciada. Toca un nuevo punto de inicio.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String key = "key=TU_API_KEY"; // Reemplaza con tu API Key
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private String downloadUrl(String strUrl) throws Exception {
        String data = "";
        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        data = sb.toString();
        reader.close();
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                return downloadUrl(url[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (List<HashMap<String, String>> path : result) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    points.add(new LatLng(lat, lng));
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }

            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
        }
    }
}