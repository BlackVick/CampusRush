package com.blackviking.campusrush.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.BuildConfig;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.Permissions;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Model.FeedModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
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

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedUpdate extends Fragment {

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
    private DatabaseReference updateRef, pushRef, userRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String originalImageUrl, thumbDownloadUrl;
    private String privacyState, userType;
    private APIService mService;

    public FeedUpdate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed_update, container, false);

        /*---   PAPER DB   ---*/
        Paper.init(getContext());


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        updateRef = db.getReference("Feed");
        imageRef = storage.getReference("FeedImages");
        userRef = db.getReference("Users").child(currentUid);


        /*---   WIDGETS   ---*/
        updateImage = (ImageView)v.findViewById(R.id.updateImage);
        updateText = (EditText)v.findViewById(R.id.updateDetails);
        updateShare = (Button)v.findViewById(R.id.updateShare);


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

        return v;
    }

    private void shareUpdate() {

        final String theUpdate = updateText.getText().toString().trim();

        final long date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy HH:mm");
        final String dateString = sdf.format(date);

        if (imageUri != null || originalImageUrl != null || thumbDownloadUrl != null || !theUpdate.isEmpty()){

            if (Common.isConnectedToInternet(getContext())){

                if (imageUri != null || originalImageUrl != null || thumbDownloadUrl != null){

                    final Map<String, Object> newFeedMap = new HashMap<>();
                    /*---   USER TYPE   ---*/
                    if (userType.equalsIgnoreCase("User"))
                        newFeedMap.put("sourceType", "User");
                    else if (userType.equalsIgnoreCase("Admin"))
                        newFeedMap.put("sourceType", "Admin");

                    newFeedMap.put("update", theUpdate);

                    /*---   PRIVACY   ---*/
                    if (privacyState.equalsIgnoreCase("public"))
                        newFeedMap.put("sender", currentUid);
                    else if (privacyState.equalsIgnoreCase("classified"))
                        newFeedMap.put("sender", "");

                    newFeedMap.put("realSender", currentUid);
                    newFeedMap.put("imageUrl", originalImageUrl);
                    newFeedMap.put("imageThumbUrl", thumbDownloadUrl);
                    newFeedMap.put("videoUrl", "");
                    newFeedMap.put("timestamp", dateString);
                    newFeedMap.put("updateType", "Image");

                    pushRef = updateRef.push();
                    final String pushId = pushRef.getKey();
                    updateRef.child(pushId).setValue(newFeedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                Intent i = new Intent(getContext(), Home.class);
                                startActivity(i);
                                getActivity().finish();

                            }

                        }
                    });



                } else {

                    final Map<String, Object> newFeedMap = new HashMap<>();
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
                    newFeedMap.put("videoUrl", "");
                    newFeedMap.put("timestamp", dateString);
                    newFeedMap.put("updateType", "Text");

                    pushRef = updateRef.push();
                    final String pushId = pushRef.getKey();
                    updateRef.child(pushId).setValue(newFeedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendNewFeedNotification(pushId);

                                Intent i = new Intent(getContext(), Home.class);
                                startActivity(i);
                                getActivity().finish();

                            }

                        }
                    });


                }

            } else {

                Common.showErrorDialog(getContext(), "No Internet Access !");

            }

        } else {

            Common.showErrorDialog(getContext(), "Update Has To Contain Valid Stuff . . .");

        }

    }

    private void sendNewFeedNotification(final String pushId) {

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Toast.makeText(getContext(), ""+pushId, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Error sending notification", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openChoiceDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
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

                if (Common.isConnectedToInternet(getContext())){

                    openCamera();

                }else {

                    Common.showErrorDialog(getContext(), "No Internet Access !");
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
                getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                getActivity(),
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

        int permissionRequest = ActivityCompat.checkSelfPermission(getContext(), permission);

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

            Uri theUri = imageUri;
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(getContext(), FeedUpdate.this);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(getContext(), FeedUpdate.this);
            }

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mDialog = new SpotsDialog(getContext(), "Upload In Progress . . .");
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
                    Bitmap thumb_bitmap = new Compressor(getContext())
                            .setMaxWidth(400)
                            .setMaxHeight(400)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
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
                                            mDialog.dismiss();
                                            imageUri = null;
                                        }
                                    }
                                });

                            } else {

                                mDialog.dismiss();
                                imageUri = null;

                            }
                        }
                    });
                    imageRef1.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

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

        loader.init(ImageLoaderConfiguration.createDefault(getContext()));

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

}
