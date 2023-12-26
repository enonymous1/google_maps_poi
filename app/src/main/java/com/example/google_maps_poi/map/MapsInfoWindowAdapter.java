package com.example.google_maps_poi.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.example.google_maps_poi.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;

public class MapsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;

    public MapsInfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        String photoPath = marker.getTag().toString();
        View view = LayoutInflater.from(context).inflate(R.layout.photo_info_window, null);
        TextView title = view.findViewById(R.id.photoTitleView);
        TextView desc = view.findViewById(R.id.photoDescView);
        try {
            ExifInterface exifInterface = new ExifInterface(photoPath);
            title.setText(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID));
            desc.setText(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }


}
