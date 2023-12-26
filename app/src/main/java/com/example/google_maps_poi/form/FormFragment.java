package com.example.google_maps_poi.form;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.google_maps_poi.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormFragment extends Fragment {

    Toolbar formAppBar;
    ImageView formImageView;
    EditText titleText;
    EditText descText;
    String currentPhotoPath;
    Location lastKnownLocation;

    public FormFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = requireActivity().getIntent().getExtras();
        if (bundle != null) {
            lastKnownLocation = bundle.getParcelable("location");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        formAppBar = view.findViewById(R.id.formAppBar);
        setFormAppBarMenuOnClick();

        formImageView = view.findViewById(R.id.formImage);
        titleText = view.findViewById(R.id.formTitle);
        descText = view.findViewById(R.id.formDesc);

        return view;
    }

    public void setFormAppBarMenuOnClick() {


        //Camera
        formAppBar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                dispatchTakePictureIntent();
                return false;
            }
        });

        //Save
        formAppBar.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if(isFormFilled()) {
                    requireActivity().setResult(Activity.RESULT_OK, new Intent(currentPhotoPath));
                    requireActivity().finish();
                }
                return false;
            }
        });
    }

    public boolean isFormFilled() {
        boolean response = false;
        String title = titleText.getText().toString();
        String desc = descText.getText().toString();
        Drawable photo = formImageView.getDrawable();
        if(title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields =)", Toast.LENGTH_SHORT).show();
            return response;
        }
        else if (photo == null) {
            Toast.makeText(requireContext(), "Don't forget to snap a photo =)", Toast.LENGTH_SHORT).show();
            return response;
        }
        else {
            try {
                ExifInterface exifInterface = new ExifInterface(currentPhotoPath);
                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID, title);
                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, desc);
                exifInterface.setAttribute(ExifInterface.TAG_MAKER_NOTE, "SAVED");


                /*exifInterface.setLatLong(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());*/

                exifInterface.saveAttributes();
                MediaScannerConnection.scanFile(requireContext(), new String[]{currentPhotoPath}, null, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return !response;
        }
    }

    private ActivityResultLauncher<Intent> getContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap takenImage = BitmapFactory.decodeFile(currentPhotoPath);
                        formImageView.setImageBitmap(takenImage);
                    }
                }
            });

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String folderName = "map_images";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                folderName);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                folder
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
        }

        if (photoFile != null) {

            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.example.google_maps_poi",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            getContent.launch(takePictureIntent);
        }
    }
}