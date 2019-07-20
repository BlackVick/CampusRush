package com.blackviking.campusrush.Plugins.SkitCenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.AddFeed;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.Permissions;
import com.blackviking.campusrush.Login;
import com.blackviking.campusrush.Model.MaterialModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Plugins.GamersHub.AddGameFeed;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddNewSkit extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, userRef, theAdminRef, adminRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ImageView uploadSkit;
    private EditText skitDescription, skitTitle;
    private Button updateShare;
    private static final int VERIFY_PERMISSIONS_REQUEST = 27;
    private static final int GALLERY_REQUEST_CODE = 431;
    private Uri videoUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference skitVideoRef;
    private String skitDownloadUrl = "", currentUid, skitThumbUrl = "", currentUserName;
    private ProgressDialog progressDialog;
    private APIService mService;
    private android.app.AlertDialog mDialog;

    private Intent intent;
    private String action, type;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_add_new_skit);

        if (mAuth.getCurrentUser() == null){
            Intent goLogin = new Intent(this, Login.class);
            startActivity(goLogin);
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            /*---   FCM   ---*/
            mService = Common.getFCMService();


            /*---   FIREBASE   ---*/
            if (mAuth.getCurrentUser() != null)
                currentUid = mAuth.getCurrentUser().getUid();
            userRef = db.getReference("Users").child(currentUid);
            theAdminRef = db.getReference("Users");
            skitRef = db.getReference("Skits");
            skitVideoRef = storage.getReference("Skits");
            adminRef = db.getReference("AdminManagement");


            /*---   WIDGETS   ---*/
            activityName = (TextView)findViewById(R.id.activityName);
            exitActivity = (ImageView)findViewById(R.id.exitActivity);
            helpActivity = (ImageView)findViewById(R.id.helpIcon);
            uploadSkit = (ImageView)findViewById(R.id.uploadSkit);
            skitDescription = (EditText)findViewById(R.id.updateDetails);
            skitTitle = (EditText)findViewById(R.id.updateTitle);
            updateShare = (Button)findViewById(R.id.updateShare);


            /*---   LOCAL SHARE   ---*/
            intent = getIntent();
            action = intent.getAction();
            type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null){
                if (Common.isConnectedToInternet(getBaseContext())) {

                    uploadLocalVideo();

                } else {

                    Common.showErrorDialog(this, "No Internet Access !");

                }

            }


            /*---   ACTIVITY BAR FUNCTIONS   ---*/
            exitActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            activityName.setText("Add Skit");
            helpActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent helpIntent = new Intent(AddNewSkit.this, Help.class);
                    startActivity(helpIntent);
                    overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                }
            });


            /*---   PERMISSIONS HANDLER   ---*/
            if (checkPermissionsArray(Permissions.PERMISSIONS)){


            } else {

                verifyPermissions(Permissions.PERMISSIONS);

            }


            /*---   CURRENT USER   ---*/
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    currentUserName = dataSnapshot.child("username").getValue().toString();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            /*---   UPLOAD VIDEO   ---*/
            uploadSkit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGallery();
                }
            });


            /*---   SHARE SKIT   ---*/
            updateShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareUpdate();
                }
            });

        }

    }

    private void uploadLocalVideo() {
        videoUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        /*---   FILE NAME   ---*/
        String theFileName = videoUri.getLastPathSegment();


        progressDialog = new ProgressDialog(AddNewSkit.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Upload In Progress. . .");
        progressDialog.setProgress(0);
        progressDialog.show();


        final StorageReference fileRefUp = skitVideoRef.child("Videos/"+theFileName);
        final StorageReference fileThumbRef = skitVideoRef.child("Thumbnail/"+theFileName);

        fileRefUp.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        skitDownloadUrl = taskSnapshot.getDownloadUrl().toString();

                        progressDialog.dismiss();

                        getThumbNail(fileThumbRef);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });
    }

    private void shareUpdate() {

        final String theDescription = skitDescription.getText().toString().trim();
        final String theTitle = skitTitle.getText().toString().trim();

        if (Common.isConnectedToInternet(getBaseContext())){

            if (!skitDownloadUrl.equalsIgnoreCase("") && !skitThumbUrl.equalsIgnoreCase("") && !TextUtils.isEmpty(theTitle)){

                final Map<String, Object> newSkitMap = new HashMap<>();
                newSkitMap.put("title", theTitle);
                newSkitMap.put("owner", currentUserName);
                newSkitMap.put("mediaUrl", skitDownloadUrl);
                newSkitMap.put("thumbnail", skitThumbUrl);
                newSkitMap.put("description", theDescription);

                newSkitMap.put("sourceType", "");
                newSkitMap.put("update", "");

                newSkitMap.put("sender", "");
                newSkitMap.put("realSender", "");
                newSkitMap.put("imageUrl", "");
                newSkitMap.put("imageThumbUrl", "");
                newSkitMap.put("timestamp", "");
                newSkitMap.put("updateType", "");
                newSkitMap.put("adminType", "Skit");


                adminRef.push().setValue(newSkitMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        sendNotificationToAdmin();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            } else {

                Common.showErrorDialog(AddNewSkit.this, "Skit Has To Contain A Valid File . . .");

            }

        } else {

            Common.showErrorDialog(AddNewSkit.this, "No Internet Access !");

        }

    }

    private void sendNotificationToAdmin() {

        theAdminRef.orderByChild("userType").equalTo("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){

                    String key = child.getKey();

                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "Admin");
                    dataSend.put("message", "There are new items to sort out. Lets Go");
                    DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(key).toString(), dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new retrofit2.Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(AddNewSkit.this, "Error sending notification", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                AddNewSkit.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    private boolean checkPermissionsArray(String[] permissions) {

        for (int i = 0; i < permissions.length; i++){

            String check = permissions[i];
            if (!checkPermissions(check)){
                return false;
            }

        }
        return true;
    }

    private boolean checkPermissions(String permission) {

        int permissionRequest = ActivityCompat.checkSelfPermission(AddNewSkit.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            videoUri = data.getData();

            /*---   FILE NAME   ---*/
            String theFileName = videoUri.getLastPathSegment();


            progressDialog = new ProgressDialog(AddNewSkit.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("Upload In Progress. . .");
            progressDialog.setProgress(0);
            progressDialog.show();


            final StorageReference fileRefUp = skitVideoRef.child("Videos/"+theFileName);
            final StorageReference fileThumbRef = skitVideoRef.child("Thumbnail/"+theFileName);

            fileRefUp.putFile(videoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            skitDownloadUrl = taskSnapshot.getDownloadUrl().toString();

                            progressDialog.dismiss();

                            getThumbNail(fileThumbRef);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    int currentProgress = (int) (100*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setProgress(currentProgress);

                }
            });

        }

    }

    private void getThumbNail(StorageReference fileThumbRef) {

        mDialog = new SpotsDialog(this, "Fetching Thumbnail . . .");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(skitDownloadUrl, new HashMap<String, String>());
        Bitmap image = retriever.getFrameAtTime(2000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        uploadSkit.setImageBitmap(image);

        fileThumbRef.putFile(getImageUri(image))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        skitThumbUrl = taskSnapshot.getDownloadUrl().toString();
                        mDialog.dismiss();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }


    public Uri getImageUri(Bitmap theBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        theBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), theBitmap, "Bit_Map", null);
        return Uri.parse(path);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
