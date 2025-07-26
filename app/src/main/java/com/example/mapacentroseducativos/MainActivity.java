package com.example.mapacentroseducativos;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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

        // Marcadores de centros educativos
        LatLng[] escuelas = {
                new LatLng(15.5018, -88.0308), // Instituto José Trinidad Reyes (JTR)
                new LatLng(15.5032, -88.0229), // Instituto Perla del Ulúa
                new LatLng(15.5023, -88.0331), // Instituto Gubernamental Genaro Muñoz Hernández
                new LatLng(15.5059, -88.0250), // Escuela Experimental de la UNAH-VS
                new LatLng(15.4995, -88.0179)  // Escuela Marco Aurelio Soto
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

        // Centrar cámara en el primer centro
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(escuelas[0], 15));

        // Mostrar coordenadas al tocar el mapa
        mMap.setOnMapClickListener(latLng -> {
            String coords = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
            Toast.makeText(MainActivity.this, coords, Toast.LENGTH_SHORT).show();
        });
    }
}