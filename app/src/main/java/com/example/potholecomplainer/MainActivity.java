package com.example.potholecomplainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button gallery,location,register;
    TextView lati,longi,adress,stacon;
    EditText phno;
    static int Preqcode = 1;
    static int REQUESCODE = 1;
    Uri pickedimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        imageView = (ImageView) findViewById(R.id.img);
        gallery=(Button) findViewById(R.id.gal);
        location=(Button) findViewById(R.id.button);
        lati=(TextView) findViewById(R.id.textView3);
        longi=(TextView) findViewById(R.id.textView2);
        phno = (EditText) findViewById(R.id.ed1);
        register=(Button) findViewById(R.id.btn2);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >=22) {

                    checkAndRequestForPermission();

                }
                else
                {
                    openGallery();
                }


            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showExif(pickedimg);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadtofirebase();
            }
        });


    }



    private void showExif(Uri pickedimg) {

        if(pickedimg!=null){
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {

                parcelFileDescriptor = getContentResolver().openFileDescriptor(pickedimg, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                ExifInterface exifInterface = new ExifInterface(fileDescriptor);
                String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String longitude=  exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                lati.setText(latitude);
                longi.setText(longitude);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    private void openGallery() {
        Intent Gintent= new Intent(Intent.ACTION_GET_CONTENT);
        Gintent.setType("image/*");
        startActivityForResult(Gintent,REQUESCODE);

    }

    private void checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this,"Please accept the permission",Toast.LENGTH_LONG).show();
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Preqcode);
            }
        }
        else {
            openGallery();
        }


    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){

            pickedimg= data.getData();
            imageView.setImageURI(pickedimg);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadtofirebase() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("File Uploader");
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("image1" + new Random().nextInt(50));

        uploader.putFile(pickedimg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dialog.dismiss();
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference root = db.getReference("Complaints");
                        DbHolder obj = new DbHolder(lati.getText().toString(),longi.getText().toString(),uri.toString());
                        root.child(phno.getText().toString()).setValue(obj);

                        lati.setText("");
                        longi.setText("");
                        phno.setText("");
                        imageView.setImageResource(R.drawable.ic_launcher_background);
                        Toast.makeText(MainActivity.this,"Registered Sucessfully",Toast.LENGTH_LONG).show();
                    }
                });

            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        dialog.setMessage("Uploaded: " + (int) percent + "%");

                    }
                });

    }


}