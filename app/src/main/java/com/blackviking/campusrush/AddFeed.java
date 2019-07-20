package com.blackviking.campusrush;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.Permissions;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Plugins.SkitCenter.AddNewSkit;
import com.blackviking.campusrush.Settings.Help;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddFeed extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private ImageView updateImage;
    private EditText updateText;
    private Button updateShare;
    private android.app.AlertDialog mDialog;
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private String currentUid;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference updateRef, justUserRef, pushRef, userRef, adminRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String originalImageUrl, thumbDownloadUrl;
    private String privacyState, userType;
    private APIService mService;

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

        setContentView(R.layout.activity_add_feed);


        /*---   PAPER DB   ---*/
        Paper.init(this);

        if (mAuth.getCurrentUser() == null){
            Intent goLogin = new Intent(this, Login.class);
            startActivity(goLogin);
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            /*---   LOCAL SHARE   ---*/
            intent = getIntent();
            action = intent.getAction();
            type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null){
                if (Common.isConnectedToInternet(getBaseContext())) {
                    imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    CropImage.activity(imageUri)
                            .setAspectRatio(1, 1)
                            .start(AddFeed.this);
                } else {
                    Common.showErrorDialog(this, "No Internet Access !");
                }

            }


            /*---   FCM   ---*/
            mService = Common.getFCMService();


            /*---   FIREBASE   ---*/
            currentUid = mAuth.getCurrentUser().getUid();
            updateRef = db.getReference("Feed");
            imageRef = storage.getReference("FeedImages");
            userRef = db.getReference("Users").child(currentUid);
            adminRef = db.getReference("AdminManagement");
            justUserRef = db.getReference("Users");


            /*---   WIDGETS   ---*/
            activityName = (TextView)findViewById(R.id.activityName);
            exitActivity = (ImageView)findViewById(R.id.exitActivity);
            helpActivity = (ImageView)findViewById(R.id.helpIcon);
            updateImage = (ImageView)findViewById(R.id.updateImage);
            updateText = (EditText)findViewById(R.id.updateDetails);
            updateShare = (Button)findViewById(R.id.updateShare);


            /*---   ACTIVITY BAR FUNCTIONS   ---*/
            exitActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            activityName.setText("Add Feed");
            helpActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent helpIntent = new Intent(AddFeed.this, Help.class);
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

                    privacyState = dataSnapshot.child("privacy").getValue().toString();
                    userType = dataSnapshot.child("userType").getValue().toString();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            /*---   UPDATE IMAGE   ---*/
            updateImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openChoiceDialog();

                }
            });


            /*---   SHARE UPDATE   ---*/
            updateShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareUpdate();
                }
            });

        }




    }

    private void shareUpdate() {

        final String theUpdate = updateText.getText().toString().trim();

        final long date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
        final String dateString = sdf.format(date);

        if (imageUri != null || originalImageUrl != null || thumbDownloadUrl != null || !theUpdate.isEmpty()){

            if (Common.isConnectedToInternet(getBaseContext())){

                if (imageUri != null && !TextUtils.isEmpty(originalImageUrl) && !TextUtils.isEmpty(thumbDownloadUrl)){

                    final Map<String, Object> newFeedMap = new HashMap<>();

                    newFeedMap.put("title", "");
                    newFeedMap.put("owner", "");
                    newFeedMap.put("mediaUrl", "");
                    newFeedMap.put("thumbnail", "");
                    newFeedMap.put("description", "");

                    /*---   USER TYPE   ---*/
                    if (userType.equalsIgnoreCase("User"))
                        newFeedMap.put("sourceType", "User");
                    else if (userType.equalsIgnoreCase("Admin"))
                        newFeedMap.put("sourceType", "Admin");

                    newFeedMap.put("update", theUpdate);

                    /*---   PRIVACY   ---*/
                    if (privacyState.equalsIgnoreCase("public"))
                        newFeedMap.put("sender", currentUid);
                    else if (privacyState.equalsIgnoreCase("private"))
                        newFeedMap.put("sender", "");

                    newFeedMap.put("realSender", currentUid);
                    newFeedMap.put("imageUrl", originalImageUrl);
                    newFeedMap.put("imageThumbUrl", thumbDownloadUrl);
                    newFeedMap.put("timestamp", dateString);
                    newFeedMap.put("updateType", "Image");
                    newFeedMap.put("adminType", "Feed");

                    pushRef = adminRef.push();
                    final String pushId = pushRef.getKey();
                    adminRef.child(pushId).setValue(newFeedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendNotificationToAdmin();
                                finish();

                            }
                        }
                    });

                } else {

                    final Map<String, Object> newFeedMap = new HashMap<>();

                    newFeedMap.put("title", "");
                    newFeedMap.put("owner", "");
                    newFeedMap.put("mediaUrl", "");
                    newFeedMap.put("thumbnail", "");
                    newFeedMap.put("description", "");

                    /*---   USER TYPE   ---*/
                    if (userType.equalsIgnoreCase("User"))
                        newFeedMap.put("sourceType", "User");
                    else if (userType.equalsIgnoreCase("Admin"))
                        newFeedMap.put("sourceType", "Admin");

                    newFeedMap.put("update", theUpdate);

                    /*---   PRIVACY   ---*/
                    if (privacyState.equalsIgnoreCase("public"))
                        newFeedMap.put("sender", currentUid);
                    else if (privacyState.equalsIgnoreCase("private"))
                        newFeedMap.put("sender", "");

                    newFeedMap.put("realSender", currentUid);
                    newFeedMap.put("imageUrl", "");
                    newFeedMap.put("imageThumbUrl", "");
                    newFeedMap.put("timestamp", dateString);
                    newFeedMap.put("updateType", "Text");
                    newFeedMap.put("adminType", "Feed");

                    pushRef = adminRef.push();
                    final String pushId = pushRef.getKey();
                    adminRef.child(pushId).setValue(newFeedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendNotificationToAdmin();
                                finish();

                            }

                        }
                    });


                }

            } else {

                Common.showErrorDialog(this, "No Internet Access !");

            }

        } else {

            Common.showErrorDialog(this, "Update Has To Contain Valid Stuff . . .");

        }

    }

    private void sendNotificationToAdmin() {

        justUserRef.orderByChild("userType").equalTo("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    Toast.makeText(AddFeed.this, "Error sending notification", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendNewFeedNotification(final String pushId) {

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("username").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Campus Feed");
                dataSend.put("message", "@"+userName+" Just dropped a new post check it out");
                dataSend.put("feed_id", pushId);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.FEED_NOTIFICATION_TOPIC).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(AddFeed.this, "Error sending notification", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openChoiceDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.image_source_choice,null);

        final ImageView cameraPick = (ImageView) viewOptions.findViewById(R.id.cameraPick);
        final ImageView galleryPick = (ImageView) viewOptions.findViewById(R.id.galleryPick);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        cameraPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())){

                    openCamera();

                }else {

                    Common.showErrorDialog(AddFeed.this, "No Internet Access !");
                }
                alertDialog.dismiss();

            }
        });

        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void openGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    private void openCamera() {

        final long date = System.currentTimeMillis();

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file=getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                this,
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

        int permissionRequest = ActivityCompat.checkSelfPermission(this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(AddFeed.this);


        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(AddFeed.this);
            }

        }




        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mDialog = new SpotsDialog(this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();
                setImage(imgURI, updateImage);

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());


                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(450)
                            .setMaxHeight(450)
                            .setQuality(70)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = imageRef.child("FullImages").child(dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = imageRef.child("Thumbnails").child(dateShitFmt + ".jpg");

                    final UploadTask originalUpload = imageRef1.putFile(resultUri);

                    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            imageUri = null;
                            originalUpload.cancel();
                        }
                    });

                    originalUpload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                originalImageUrl = task.getResult().getDownloadUrl().toString();
                                final UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        imageUri = null;
                                        uploadTask.cancel();
                                    }
                                });

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()){

                                            mDialog.dismiss();

                                        } else {
                                            Toast.makeText(AddFeed.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                            imageUri = null;
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });

                            } else {

                                Toast.makeText(AddFeed.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                                imageUri = null;

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void setImage(String imgUrl, ImageView image){

        ImageLoader loader = ImageLoader.getInstance();

        loader.init(ImageLoaderConfiguration.createDefault(this));

        loader.displayImage(imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Campus Rush");

        /**Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
