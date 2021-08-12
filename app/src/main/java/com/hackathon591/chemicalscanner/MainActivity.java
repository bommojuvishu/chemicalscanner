package com.hackathon591.chemicalscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Matrix;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.camerakit.CameraKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}

public class MainActivity extends AppCompatActivity {

    // One Button
    Button BSelectImage, BCaptureImage ,Rotate,Process;
    ImageView IVPreviewImage;
    private CameraKitView cameraKitView;
    Button cameraBtn;


    Bitmap bitmap;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;
    String TAG ="com.hackathon591.chemicalscanner";

    ArrayList<String> myList = new ArrayList<String>();
    HashMap<String, Object> firebaseData;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register the UI widgets with their appropriate IDs
        BSelectImage = findViewById(R.id.BSelectImage);
        BCaptureImage = findViewById(R.id.BCaptureImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        Rotate =findViewById(R.id.Rotate);
        Process =findViewById(R.id.Process);
        cameraKitView = findViewById(R.id.camera);
        cameraBtn = findViewById(R.id.cameraBtn);

        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        Sprite CubeGrid = new Wave();
        progressBar.setIndeterminateDrawable(CubeGrid);




        View.OnClickListener photoOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, final byte[] photo) {
                        Log.d(TAG, "onImage: ");
                        Bitmap bitmap2 = BitmapFactory.decodeByteArray(photo,0,photo.length);
                        runTextRecognition(bitmap2);

                    }
                });
            }
        };

        cameraBtn.setOnClickListener(photoOnClickListener);

        // handle the Choose Image button to trigger
        // the image chooser function

        readdatabase();


        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();

            }
        });

        BCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cameraCapture();
//                Intent intent = new Intent(getApplicationContext(), Camerakit.class);
//                startActivity(intent);
                BSelectImage.setVisibility(View.INVISIBLE);
                BCaptureImage.setVisibility(View.INVISIBLE);
                IVPreviewImage.setVisibility(View.INVISIBLE);
                Rotate.setVisibility(View.INVISIBLE);
                Process.setVisibility(View.INVISIBLE);

                cameraKitView.setVisibility(View.VISIBLE);
                cameraBtn.setVisibility(View.VISIBLE);



            }
        });

        Process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition(bitmap);


            }
        });

        Rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IVPreviewImage.setRotation(90);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                bitmap = rotatedBitmap;
                IVPreviewImage.setImageBitmap(rotatedBitmap);



            }
        });



    }

    private void readdatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("chemicaldata");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Firebase DATA Value is: " + value);
                firebaseData  = (HashMap<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "Value is: " + firebaseData);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Firebase DATA Failed to read value.", error.toException());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void cameraCapture() {

        Intent camera_intent
                = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);

        // Start the activity with camera_intent,
        // and request pic id
        startActivityForResult(camera_intent, 123);
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {


        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);


        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageURI = data.getData();
//                File imageFile = new File(getRealPathFromURI(selectedImageURI));
                Uri selectedImageUri = data.getData( );
                String picturePath = getPath( getApplicationContext( ), selectedImageUri );
                Log.d("Picture Path", picturePath);

                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                     bitmap = null;
                    // update the preview image in the layout
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    IVPreviewImage.setImageURI(selectedImageUri);
//                    runTextRecognition(bitmap);
                    IVPreviewImage.setImageBitmap(bitmap);

                }
            }

            if (requestCode == 123) {


                Bitmap photo = (Bitmap) data.getExtras()
                        .get("data");

                // Set the image in imageview for display
                IVPreviewImage.setImageBitmap(photo);
                runTextRecognition(photo);

            }

        }
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    private void runTextRecognition(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...

                                Log.d(TAG, "onSuccess: ");
                                extractText(visionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }

    private void extractText(Text result) {

        String resultText = result.getText();
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();

                    myList.add(elementText);
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }

        Log.d(TAG, "extractText: " + myList.size());
        Log.d(TAG, "extractText: " + firebaseData.size());

        Intent intent = new Intent(getApplicationContext(), ingredients.class);
        intent.putExtra("mylist", myList);
        intent.putExtra("myfirebaseData", firebaseData);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();

        BSelectImage.setVisibility(View.VISIBLE);
        BCaptureImage.setVisibility(View.VISIBLE);
        IVPreviewImage.setVisibility(View.VISIBLE);
        Rotate.setVisibility(View.VISIBLE);
        Process.setVisibility(View.VISIBLE);

        cameraKitView.setVisibility(View.INVISIBLE);
        cameraBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        BSelectImage.setVisibility(View.VISIBLE);
        BCaptureImage.setVisibility(View.VISIBLE);
        IVPreviewImage.setVisibility(View.VISIBLE);
        Rotate.setVisibility(View.VISIBLE);
        Process.setVisibility(View.VISIBLE);

        cameraKitView.setVisibility(View.INVISIBLE);
        cameraBtn.setVisibility(View.INVISIBLE);

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}