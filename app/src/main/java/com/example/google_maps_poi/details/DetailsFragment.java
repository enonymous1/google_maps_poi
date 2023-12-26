package com.example.google_maps_poi.details;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.google_maps_poi.R;

import java.io.IOException;

public class DetailsFragment extends Fragment {

    Toolbar appBar;
    TextView title;
    TextView description;
    ImageView imageView;
    String photoFile;

    public DetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoFile = requireActivity().getIntent().getStringExtra("photoFile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        appBar = view.findViewById(R.id.detailsAppBar);
        title = view.findViewById(R.id.detailsTitleView);
        description = view.findViewById(R.id.detailsDescView);
        imageView = view.findViewById(R.id.detailsImageView);

        if(photoFile != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(photoFile);
                title.setText(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID));
                description.setText(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                Bitmap photoImage = BitmapFactory.decodeFile(photoFile);
                imageView.setImageBitmap(photoImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        setAppBarOnClick();

        return view;
    }

    private void setAppBarOnClick() {
        appBar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {

                if(photoFile != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Delete Marker");
                    builder.setMessage("Are you sure you want to delete this marker?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                ExifInterface exifInterface = new ExifInterface(photoFile);
                                exifInterface.setAttribute(ExifInterface.TAG_MAKER_NOTE, "DELETED");
                                exifInterface.saveAttributes();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            requireActivity().setResult(Activity.RESULT_FIRST_USER, new Intent(photoFile));
                            requireActivity().finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                return false;
            }
        });
    }
}