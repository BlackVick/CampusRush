package com.blackviking.campusrush.CampusRant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.BuildConfig;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.ImageController.ImageViewer;
import com.blackviking.campusrush.Messaging.Messaging;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.blackviking.campusrush.ViewHolder.RantViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RantRoom extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private EditText rantInputBox;
    private ImageView addAttachment, sendRant;
    private RecyclerView rantRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<RantModel, RantViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, rantRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference rantImageRef;
    private static final int VERIFY_PERMISSIONS_REQUEST = 47;
    private static final int CAMERA_REQUEST_CODE = 366;
    private static final int GALLERY_REQUEST_CODE = 95;
    private Uri imageUri;
    private String currentUid, myUsername;
    private String originalImageUrl, thumbDownloadUrl;
    private String privacyState;
    private String rantTopic;
    private android.app.AlertDialog mDialog;

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

        setContentView(R.layout.activity_rant_room);


        /*---   LOCAL   ---*/
        Paper.init(this);
        rantTopic = getIntent().getStringExtra("RantTopic");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        rantRef = db.getReference("Rants");
        rantImageRef = storage.getReference("RantImages");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        rantInputBox = (EditText)findViewById(R.id.rantEditText);
        sendRant = (ImageView)findViewById(R.id.sendRant);
        addAttachment = (ImageView)findViewById(R.id.addAttachment);
        rantRecycler = (RecyclerView)findViewById(R.id.rantRecycler);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("#"+rantTopic);
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(RantRoom.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                privacyState = dataSnapshot.child("privacy").getValue().toString();
                myUsername = dataSnapshot.child("username").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*---   SEND MESSAGE   ---*/
        sendRant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext()))
                    sendTheRant();
                else
                    Common.showErrorDialog(RantRoom.this, "No Internet Access !");
            }
        });


        /*---   ADD ATTACHMENT   ---*/
        addAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RantRoom.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(RantRoom.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                    openChoiceDialog();

                } else {

                    ActivityCompat.requestPermissions(RantRoom.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, VERIFY_PERMISSIONS_REQUEST);

                }
            }
        });

        loadRants();

    }

    private void loadRants() {

        rantRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rantRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RantModel, RantViewHolder>(
                RantModel.class,
                R.layout.single_rant_item,
                RantViewHolder.class,
                rantRef.orderByChild("rantTopic").equalTo(rantTopic).limitToLast(70)
        ) {
            @Override
            protected void populateViewHolder(final RantViewHolder viewHolder, final RantModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getTimestamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                viewHolder.rantTimestamp.setVisibility(View.VISIBLE);
                viewHolder.rantTimestamp.setText(lastSeenTime);


                /*---   PRIVACY   ---*/
                if (model.getRantPrivacyState().equalsIgnoreCase("private")) {

                    viewHolder.rantUsername.setText("PROTECTED");

                } else if (model.getRantPrivacyState().equalsIgnoreCase("public")){

                    viewHolder.rantUsername.setText("@"+model.getRantUsername());

                }

                /*---   RANT TEXT   ---*/
                if (!model.getRant().equals("")){

                    viewHolder.rantText.setVisibility(View.VISIBLE);
                    viewHolder.rantText.setText(model.getRant());

                } else {

                    viewHolder.rantText.setVisibility(View.GONE);

                }


                /*---   RANT IMAGE   ---*/
                if (!model.getRantImageThumb().equals("")){

                    viewHolder.rantImage.setVisibility(View.VISIBLE);

                    Picasso.with(getBaseContext())
                            .load(model.getRantImageThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_placeholders)
                            .into(viewHolder.rantImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(model.getRantImageThumb())
                                            .placeholder(R.drawable.image_placeholders)
                                            .into(viewHolder.rantImage);
                                }
                            });


                    viewHolder.rantImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent rantImageIntent = new Intent(RantRoom.this, ImageViewer.class);
                            rantImageIntent.putExtra("ImageLink", model.getRantImage());
                            rantImageIntent.putExtra("ImageThumbLink", model.getRantImageThumb());
                            startActivity(rantImageIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                        }
                    });

                } else {

                    viewHolder.rantImage.setVisibility(View.GONE);

                }

            }
        };
        rantRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                /* If the recycler view is initially being loaded or the
                   user is at the bottom of the list, scroll to the bottom
                   of the list to show the newly added message.*/
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rantRecycler.scrollToPosition(positionStart);

                }
                layoutManager.smoothScrollToPosition(rantRecycler, null, adapter.getItemCount());
            }
        });

    }

    private void sendTheRant() {

        final String theRant = rantInputBox.getText().toString().trim();

        if (!TextUtils.isEmpty(theRant)){

            /*---   PUSH   ---*/
            final DatabaseReference pushIdRef = rantRef.push();
            final String pushId = pushIdRef.getKey();


            final Map<String, Object> rantMap = new HashMap<>();
            rantMap.put("rantUsername", myUsername);
            rantMap.put("rantUserId", currentUid);
            rantMap.put("rantPrivacyState", privacyState);
            rantMap.put("rant", theRant);
            rantMap.put("timestamp", ServerValue.TIMESTAMP);
            rantMap.put("rantImage", "");
            rantMap.put("rantImageThumb", "");
            rantMap.put("rantTopic", rantTopic);



            rantRef.child(pushId).setValue(rantMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    rantInputBox.setText("");
                }
            });

        }

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

                if (Common.isConnectedToInternet(RantRoom.this)){

                    openCamera();

                }else {

                    Common.showErrorDialog(RantRoom.this, "No Internet Access !");
                }
                alertDialog.dismiss();

            }
        });

        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(RantRoom.this)){

                    openGallery();

                }else {

                    Common.showErrorDialog(RantRoom.this, "No Internet Access !");
                }
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

        File file = getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                RantRoom.this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            //Uri theUri = imageUri;
            CropImage.activity(imageUri)
                    .start(this);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .start(this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();
                currentUid = mAuth.getCurrentUser().getUid();

                showPreviewDialog(resultUri, imgURI);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void showPreviewDialog(final Uri resultUri, String imgURI) {

        final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.DialogTheme).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.image_rant_layout,null);

        final ImageView rantImagePreview = (ImageView) viewOptions.findViewById(R.id.rantImagePreview);
        final ImageView sendRantPreview = (ImageView) viewOptions.findViewById(R.id.sendRantPreview);
        final EditText rantTextPreview = (EditText) viewOptions.findViewById(R.id.rantPreviewText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        String theRant = rantInputBox.getText().toString().trim();

        if (!TextUtils.isEmpty(theRant))
            rantTextPreview.setText(theRant);


        setImage(imgURI, rantImagePreview);


        sendRantPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog = new SpotsDialog(RantRoom.this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(RantRoom.this)
                            .setMaxWidth(350)
                            .setMaxHeight(350)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = rantImageRef.child("FullImages").child(currentUid + dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = rantImageRef.child("Thumbnails").child(currentUid + dateShitFmt + ".jpg");

                    imageRef1.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                originalImageUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                                UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = Objects.requireNonNull(thumb_task.getResult().getDownloadUrl()).toString();

                                        if (thumb_task.isSuccessful()){

                                            mDialog.dismiss();

                                            /*---   PUSH   ---*/
                                            final DatabaseReference pushIdRef = rantRef.push();
                                            final String pushId = pushIdRef.getKey();
                                            final String theFinalRantText = rantTextPreview.getText().toString().trim();


                                            /*---   MODEL FOR MESSAGE   ---*/
                                            final Map<String, Object> rantMap = new HashMap<>();
                                            rantMap.put("rantUsername", myUsername);
                                            rantMap.put("rantUserId", currentUid);
                                            rantMap.put("rantPrivacyState", privacyState);
                                            rantMap.put("rant", theFinalRantText);
                                            rantMap.put("timestamp", ServerValue.TIMESTAMP);
                                            rantMap.put("rantImage", originalImageUrl);
                                            rantMap.put("rantImageThumb", thumbDownloadUrl);
                                            rantMap.put("rantTopic", rantTopic);


                                            rantRef.child(pushId).setValue(rantMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    rantTextPreview.setText("");
                                                    rantInputBox.setText("");
                                                    alertDialog.dismiss();
                                                }
                                            });

                                        } else {
                                            mDialog.dismiss();
                                            Toast.makeText(RantRoom.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RantRoom.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();
                                        imageUri = null;
                                    }
                                });



                            } else {

                                mDialog.dismiss();
                                Toast.makeText(RantRoom.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RantRoom.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            imageUri = null;
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        alertDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0) {

            if (requestCode == VERIFY_PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openChoiceDialog();

            } else {

                Toast.makeText(RantRoom.this, "Permissions Denied", Toast.LENGTH_SHORT).show();

            }

        } else {

            Toast.makeText(this, "Permission Error", Toast.LENGTH_SHORT).show();

        }

    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Campus Rush/Images");

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

    private void setImage(String imgUrl, ImageView image){

        ImageLoader loader = ImageLoader.getInstance();

        loader.init(ImageLoaderConfiguration.createDefault(RantRoom.this));

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

    @Override
    public void onBackPressed() {
        finish();
    }
}