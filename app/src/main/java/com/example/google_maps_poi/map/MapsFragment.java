package com.example.google_maps_poi.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.exifinterface.media.ExifInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.google_maps_poi.R;
import com.example.google_maps_poi.details.DetailsActivity;
import com.example.google_maps_poi.form.FormActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MapsFragment extends Fragment implements LocationListener {

    Toolbar mapsAppBar;
    LocationManager locationManager;
    Location lastKnownLocation;
    GoogleMap map;
    Marker clickedMarker;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {

            map = googleMap;

            populateSavedMarkers();

            if (lastKnownLocation != null) {
                LatLng lastLoc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLoc, 15));
            }

            googleMap.setInfoWindowAdapter(new MapsInfoWindowAdapter(requireContext()));
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    clickedMarker = marker;
                    String photoFile = (String) marker.getTag();
                    Intent intent = new Intent(requireContext(), DetailsActivity.class);
                    intent.putExtra("photoFile", photoFile);
                    getContent.launch(intent);
                }
            });


            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(@NonNull LatLng latLng) {

                    Location clickedLocation = new Location("clicked_location");
                    clickedLocation.setLatitude(latLng.latitude);
                    clickedLocation.setLongitude(latLng.longitude);

                    Intent intent = new Intent(requireContext(), FormActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("location", clickedLocation);
                    intent.putExtras(bundle);
                    getContent.launch(intent);

                }
            });

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        requestPermission.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});

        mapsAppBar = view.findViewById(R.id.mapAppBar);
        setMapsAppBarOnClick();

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

    }

    private void setMapsAppBarOnClick() {
        mapsAppBar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (lastKnownLocation != null) {
                    Intent intent = new Intent(requireContext(), FormActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("location", lastKnownLocation);
                    intent.putExtras(bundle);
                    getContent.launch(intent);
                }
                else {
                    Toast.makeText(requireContext(), "No Location Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void updateLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    10,
                    this);

            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastKnownLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    private ActivityResultLauncher<Intent> getContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        addNewMarker(result);
                    }
                    else if (result.getResultCode() == Activity.RESULT_FIRST_USER) {
                        clickedMarker.remove();
                    }
                }
            });

    private ActivityResultLauncher<String[]> requestPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    if (result.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //Permission granted
                        updateLocation();
                    }
                }
            });

    private void addNewMarker(ActivityResult result) {
        assert result.getData() != null;
        String photoFile = result.getData().getAction();
        try {
            assert photoFile != null;
            ExifInterface exifInterface = new ExifInterface(photoFile);
            double[] latLong = exifInterface.getLatLong();
            LatLng latLng = new LatLng(latLong[0], latLong[1]);
            String title = exifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT);
            Marker newMarker = map.addMarker(new MarkerOptions().position(latLng).title(title));
            newMarker.setTag(photoFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateSavedMarkers() {
        String folderName = "map_images";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                folderName);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    ExifInterface exifInterface = new ExifInterface(file);
                    String tag = exifInterface.getAttribute(ExifInterface.TAG_MAKER_NOTE);
                    if(tag != null && tag.equals("SAVED")) {
                        double[] latLong = exifInterface.getLatLong();
                        assert latLong != null;
                        LatLng latLng = new LatLng(latLong[0], latLong[1]);
                        String title = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID);
                        Marker newMarker = map.addMarker(new MarkerOptions().position(latLng).title(title));
                        if (newMarker != null) {
                            newMarker.setTag(file.getAbsolutePath());
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

}