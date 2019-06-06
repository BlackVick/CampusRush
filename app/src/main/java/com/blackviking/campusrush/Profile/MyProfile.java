package com.blackviking.campusrush.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.BuildConfig;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.Permissions;
import com.blackviking.campusrush.ImageController.BlurImage;
import com.blackviking.campusrush.ImageController.ImageViewer;
import com.blackviking.campusrush.R;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyProfile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton editProfileFab;
    private String currentUid;
    private ImageView userProfileImage, coverPhoto, changeProfilePic;
    private TextView username, fullName, status, gender, department, bio;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, businessProfileRef;
    private CoordinatorLayout rootLayout;
    private int BLUR_PRECENTAGE = 50;
    private Target target;
    private android.app.AlertDialog mDialog;
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String originalImageUrl, thumbDownloadUrl;
    private String serverUsername, serverFullName, serverGender, serverStatus,
            serverDepartment, serverBio, serverProfilePictureThumb, serverProfilePicture,
            serverUserType;

    private RelativeLayout businessLayout;
    private TextView busName, busAddress, busCategory, busDescription, busPhone, busFacebook, busInstagram, busTwitter,
            accountType;

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

        setContentView(R.layout.activity_my_profile);


        /*---   TOOLBAR   ---*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*---   LOCAL   ---*/
        Paper.init(this);


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);
        businessProfileRef = db.getReference("BusinessProfile");
        imageRef = storage.getReference("ProfileImages").child(currentUid);


        /*---   WIDGETS   ---*/
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.myCollapsing);
        editProfileFab = (FloatingActionButton)findViewById(R.id.editMyProfile);
        coverPhoto = (ImageView)findViewById(R.id.myProfilePictureBlur);
        userProfileImage = (ImageView)findViewById(R.id.myProfilePicture);
        changeProfilePic = (ImageView)findViewById(R.id.changeMyProfilePicture);
        rootLayout = (CoordinatorLayout)findViewById(R.id.myProfileRootLayout);
        username = (TextView)findViewById(R.id.myUsername);
        status = (TextView)findViewById(R.id.myStatus);
        gender = (TextView)findViewById(R.id.myGender);
        fullName = (TextView)findViewById(R.id.myFullName);
        department = (TextView)findViewById(R.id.myDepartment);
        bio = (TextView)findViewById(R.id.myBio);

        businessLayout = (RelativeLayout)findViewById(R.id.myProfileBusinessLayout);
        busName = (TextView)findViewById(R.id.myBusinessNameTxt);
        busAddress = (TextView)findViewById(R.id.myBusinessAddressTxt);
        busCategory = (TextView)findViewById(R.id.myBusinessCategoryTxt);
        busDescription = (TextView)findViewById(R.id.myBusinessDescriptionTxt);
        busPhone = (TextView)findViewById(R.id.myBusinessPhoneTxt);
        busFacebook = (TextView)findViewById(R.id.myBusinessFacebookTxt);
        busInstagram = (TextView)findViewById(R.id.myBusinessInstagramTxt);
        busTwitter = (TextView)findViewById(R.id.myBusinessTwitterTxt);
        accountType = (TextView)findViewById(R.id.myAccountType);


        /*---   TOOLBAR   ---*/
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);


        /*---   PERMISSIONS HANDLER   ---*/
        if (checkPermissionsArray(Permissions.PERMISSIONS)){

        } else {

            verifyPermissions(Permissions.PERMISSIONS);

        }


        editProfileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext()))
                    openEditProfileDialog();
                else
                    Common.showErrorDialog(MyProfile.this, "No Internet Access !");
            }
        });


        /*---   CHANGE PROFILE PICTURE   ---*/
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChoiceDialog();
            }
        });


        /*---   LOAD PROFILE   ---*/
        loadMyProfile(currentUid);

    }

    private void openChoiceDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(MyProfile.this).create();
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

                if (Common.isConnectedToInternet(MyProfile.this)){

                    openCamera();

                }else {

                    Common.showErrorDialog(MyProfile.this, "No Internet Access !");
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

    private void openEditProfileDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.edit_profile_layout,null);

        final MaterialEditText statusEdt = (MaterialEditText) viewOptions.findViewById(R.id.editYourStatus);
        final EditText editBio = (EditText) viewOptions.findViewById(R.id.changeYourBio);
        final Button saveChanges = (Button) viewOptions.findViewById(R.id.saveProfileChanges);




        /*---   TEXT   ---*/
        statusEdt.setText(serverStatus);
        editBio.setText(serverBio);



        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        /*---   SAVE BUTTON   ---*/
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog = new SpotsDialog(MyProfile.this, "Updating . . .");
                mDialog.show();


                final String newStatus = statusEdt.getText().toString().trim();
                final String newBio = editBio.getText().toString().trim();

                if (!newStatus.equalsIgnoreCase("") && !newStatus.equalsIgnoreCase(serverStatus)){

                    userRef.child("status").setValue(newStatus);

                }

                if (!newBio.equalsIgnoreCase("") && !newBio.equalsIgnoreCase(serverBio)){

                    userRef.child("bio").setValue(newBio);

                }

                mDialog.dismiss();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void loadMyProfile(final String currentUid) {

        /*---   BLUR COVER   ---*/
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                coverPhoto.setImageBitmap(BlurImage.fastblur(bitmap, 1f,
                        BLUR_PRECENTAGE));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                coverPhoto.setImageResource(R.drawable.profile);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                coverPhoto.setImageResource(R.drawable.image_placeholders);
            }
        };

        /*---   CURRENT USER   ---*/
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                serverUsername = dataSnapshot.child("username").getValue().toString();
                serverFullName = dataSnapshot.child("lastName").getValue().toString() + " " + dataSnapshot.child("firstName").getValue().toString();
                serverStatus = dataSnapshot.child("status").getValue().toString();
                serverGender = dataSnapshot.child("gender").getValue().toString();
                serverDepartment = dataSnapshot.child("department").getValue().toString();
                serverBio = dataSnapshot.child("bio").getValue().toString();
                serverProfilePictureThumb = dataSnapshot.child("profilePictureThumb").getValue().toString();
                serverProfilePicture = dataSnapshot.child("profilePicture").getValue().toString();
                serverUserType = dataSnapshot.child("userType").getValue().toString();

                /*---   DETAILS   ---*/
                collapsingToolbarLayout.setTitle("@"+serverUsername);
                username.setText("@"+serverUsername);
                fullName.setText(serverFullName);
                status.setText(serverStatus);
                department.setText(serverDepartment);
                gender.setText(serverGender);
                bio.setText(serverBio);

                if (serverUserType.equalsIgnoreCase("Admin")){
                    accountType.setText("Admin");
                } else if (serverUserType.equalsIgnoreCase("Business")){
                    accountType.setText("Business Account");
                } else {
                    accountType.setText("Regular");
                }


                /*---   ACCOUNT TYPE   ---*/
                businessProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(currentUid).exists()) {

                            businessLayout.setVisibility(View.VISIBLE);

                            String theBusName = dataSnapshot.child(currentUid).child("businessName").getValue().toString();
                            String theBusAddress = dataSnapshot.child(currentUid).child("businessAddress").getValue().toString();
                            String theBusCategory = dataSnapshot.child(currentUid).child("businessCategory").getValue().toString();
                            String theBusDescription = dataSnapshot.child(currentUid).child("businessDescription").getValue().toString();
                            String theBusPhone = dataSnapshot.child(currentUid).child("businessPhone").getValue().toString();
                            String theBusFacebook = dataSnapshot.child(currentUid).child("businessFacebook").getValue().toString();
                            String theBusInstagram = dataSnapshot.child(currentUid).child("businessInstagram").getValue().toString();
                            String theBusTwitter = dataSnapshot.child(currentUid).child("businessTwitter").getValue().toString();


                            busName.setText(theBusName);
                            busAddress.setText(theBusAddress);
                            busCategory.setText(theBusCategory);
                            busDescription.setText(theBusDescription);
                            busPhone.setText(theBusPhone);
                            busFacebook.setText(theBusFacebook);
                            busInstagram.setText(theBusInstagram);
                            busTwitter.setText(theBusTwitter);

                        } else {

                            businessLayout.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                /*---   IMAGE   ---*/
                if (!serverProfilePictureThumb.equals("")){

                    /*---   PROFILE IMAGE   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile)
                            .into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getBaseContext())
                                            .load(serverProfilePictureThumb)
                                            .placeholder(R.drawable.profile)
                                            .into(userProfileImage);
                                }
                            });


                    /*---   COVER PHOTO   ---*/
                    Picasso.with(getBaseContext())
                            .load(serverProfilePictureThumb)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_placeholders)
                            .into(target);


                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileImgIntent = new Intent(MyProfile.this, ImageViewer.class);
                            profileImgIntent.putExtra("ImageLink", serverProfilePicture);
                            profileImgIntent.putExtra("ImageThumbLink", serverProfilePictureThumb);
                            startActivity(profileImgIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
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

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    private void openCamera() {

        final long date = System.currentTimeMillis();

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file=getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                MyProfile.this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                MyProfile.this,
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

        int permissionRequest = ActivityCompat.checkSelfPermission(MyProfile.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            Uri theUri = imageUri;
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(this);
            }

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mDialog = new SpotsDialog(MyProfile.this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                Uri resultUri = result.getUri();
                String imgURI = resultUri.toString();

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());


                try {
                    Bitmap thumb_bitmap = new Compressor(MyProfile.this)
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

                    originalUpload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                originalImageUrl = task.getResult().getDownloadUrl().toString();
                                final UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()){

                                            final Map<String, Object> newProfilePictureMap = new HashMap<>();
                                            newProfilePictureMap.put("profilePicture", originalImageUrl);
                                            newProfilePictureMap.put("profilePictureThumb", thumbDownloadUrl);

                                            userRef.updateChildren(newProfilePictureMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    Common.showErrorDialog(MyProfile.this, "Profile Picture Changed Successfully");
                                                    mDialog.dismiss();
                                                }
                                            });

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

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
