package com.blackviking.campusrush.Plugins.SkitCenter;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Fragments.Materials;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SkitDetails extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference skitRef, skitLikesRef, userRef;
    private String currentUid, currentSkitId, currentSkitOwner, skitOwnerUid;
    private TextView title, owner, description, likes;
    private SimpleExoPlayerView videoPlayer;
    private ImageView likeBtn, downloadSkit;
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private SimpleExoPlayer exoPlayer;
    private String userType, skitState, theTitle, theDescription, theLink;
    private DownloadManager downloadManager;
    private APIService mService;

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

        setContentView(R.layout.activity_skit_details);


        /*---   LOCAL   ---*/
        currentSkitId = getIntent().getStringExtra("SkitId");
        currentSkitOwner = getIntent().getStringExtra("SkitOwner");


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        skitRef = db.getReference("Skits");
        skitLikesRef = db.getReference("SkitLikes");
        userRef = db.getReference("Users");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        title = (TextView)findViewById(R.id.currentSkitTitle);
        owner = (TextView)findViewById(R.id.currentSkitOwner);
        description = (TextView)findViewById(R.id.currentSkitDescription);
        likes = (TextView)findViewById(R.id.currentLikeCount);
        videoPlayer = (SimpleExoPlayerView)findViewById(R.id.videoPlayer);
        likeBtn = (ImageView)findViewById(R.id.currentLikeBtn);
        downloadSkit = (ImageView)findViewById(R.id.downloadSkit);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exoPlayer != null) {
                    exoPlayer.release();
                    exoPlayer.stop();
                }
                finish();
            }
        });

        activityName.setText("Skit Detail");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(SkitDetails.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT SKIT USER   ---*/
        userRef.orderByChild("username").equalTo(currentSkitOwner).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    skitOwnerUid = child.getKey();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        owner.setText("@"+currentSkitOwner);



        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                    if (skitOwnerUid != null) {

                        if (skitOwnerUid.equals(currentUid)) {

                            if (exoPlayer != null) {
                                exoPlayer.release();
                                exoPlayer.stop();
                            }
                            Intent myProfileIntent = new Intent(SkitDetails.this, MyProfile.class);
                            startActivity(myProfileIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            finish();

                        } else {

                            if (exoPlayer != null) {
                                exoPlayer.release();
                                exoPlayer.stop();
                            }
                            Intent otherUserProfileIntent = new Intent(SkitDetails.this, OtherUserProfile.class);
                            otherUserProfileIntent.putExtra("UserId", skitOwnerUid);
                            startActivity(otherUserProfileIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            finish();

                        }

                    }

                } else {

                    showErrorDialog("No Internet Access !");

                }

            }
        });


        /*---   DOWNLOAD SKIT   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {

            downloadSkit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(SkitDetails.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){


                        downloadTheSkit(theLink);

                    } else {

                        ActivityCompat.requestPermissions(SkitDetails.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, VERIFY_PERMISSIONS_REQUEST);

                    }

                }
            });

            loadCurrentSkit();

        } else {

            showErrorDialog("No Internet Access !");

        }

    }

    private void downloadTheSkit(String theLink) {

        URI uri = null;
        try {

            uri = new URI(theLink);
            String[] segments = uri.getPath().split("/");
            String theFileName = segments[segments.length-1];


            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Campus Rush/Videos/");
            File file= new File(path, theFileName);
            if (!file.exists()){

                new SkitDetails.DownloadFileFromURL().execute(theLink);

            } else {

                showErrorDialog("File Exists... Check Downloads");

            }


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /*---   INDICATE DOWNLOAD START   ---*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(SkitDetails.this, "Download Started . . .", Toast.LENGTH_SHORT).show();

        }

        /*---   DOWNLOADING IN BACKGROUND   ---*/
        @Override
        protected String doInBackground(String... f_url) {

            try {
                long date = System.currentTimeMillis();

                final String link = f_url[0];


                URI uril = null;
                try {

                    uril = new URI(link);
                    String[] segments = uril.getPath().split("/");
                    String theFileName = segments[segments.length-1];

                    if (theFileName.contains("home:"))
                        theFileName.replace("home:", "");


                    downloadManager = (DownloadManager) SkitDetails.this.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(link);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setDestinationInExternalPublicDir("/Campus Rush/Videos/", theTitle+".mp4");
                    request.allowScanningByMediaScanner();
                    Long reference = downloadManager.enqueue(request);

                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                        public void onReceive(Context ctxt, Intent intent) {

                            Toast.makeText(ctxt, "Download Finished", Toast.LENGTH_SHORT).show();

                        }
                    };
                    //register receiver for when file download is compete
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {

                Log.e("Error: ", e.getMessage());

            }

            return null;
        }

        /*---   AFTER TASK COMPLETE   ---*/
        @Override
        protected void onPostExecute(String file_url) {
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == VERIFY_PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            downloadTheSkit(theLink);

        }  else {

            Toast.makeText(SkitDetails.this, "Permissions Denied", Toast.LENGTH_SHORT).show();

        }

    }

    private void loadCurrentSkit() {

        skitRef.child(currentSkitId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                theTitle = dataSnapshot.child("title").getValue().toString();
                theDescription = dataSnapshot.child("description").getValue().toString();
                theLink = dataSnapshot.child("mediaUrl").getValue().toString();

                title.setText(theTitle);
                description.setText(theDescription);

                try {

                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    TrackSelector selector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(SkitDetails.this, selector);

                    Uri videoUri = Uri.parse(theLink);

                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("Campus Rush");
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                    MediaSource videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);

                    videoPlayer.setPlayer(exoPlayer);
                    exoPlayer.prepare(videoSource);

                    exoPlayer.setPlayWhenReady(true);

                } catch (Exception e){

                    Toast.makeText(SkitDetails.this, "Video parse error, try later", Toast.LENGTH_SHORT).show();

                }


                skitLikesRef.child(currentSkitId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*---   LIKES   ---*/
                        int countLike = (int) dataSnapshot.getChildrenCount();

                        likes.setText(String.valueOf(countLike));

                        if (dataSnapshot.child(currentUid).exists()){

                            likeBtn.setImageResource(R.drawable.liked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(currentSkitId).child(currentUid).removeValue();
                                }
                            });

                        } else {

                            likeBtn.setImageResource(R.drawable.unliked_icon);

                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    skitLikesRef.child(currentSkitId).child(currentUid).setValue("liked");

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void showApprovalDialog(){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage("Are You Sure You Want To Approve Skit?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        skitRef.child(currentSkitId)
                                .child("status")
                                .setValue("Approved").addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        sendApprovalNotification();
                                        sendNewSkitNotification();

                                        if (exoPlayer != null) {
                                            exoPlayer.release();
                                            exoPlayer.stop();
                                            finish();
                                        }
                                    }
                                }
                        );

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    private void sendNewSkitNotification() {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Skit Center");
        dataSend.put("message", "A new skit by @"+currentSkitOwner+" is now available to be streamed and downloaded.");
        dataSend.put("skit_id", currentSkitId);
        dataSend.put("skit_owner", currentSkitOwner);
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.SKIT_NOTIFICATION_TOPIC).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(SkitDetails.this, "Error sending notification", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void sendApprovalNotification() {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Skit Approved");
        dataSend.put("message", "Your Skit has been approved and is now available to be streamed and downloaded by all.");
        dataSend.put("skit_id", currentSkitId);
        dataSend.put("skit_owner", currentSkitOwner);
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(skitOwnerUid).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(SkitDetails.this, "Error sending notification", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void showDenialDialog(){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage("Are You Sure You Want To Deny This Skit?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        skitRef.child(currentSkitId)
                                .removeValue()
                                .addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (exoPlayer != null) {
                                            exoPlayer.release();
                                            exoPlayer.stop();
                                            finish();
                                        }
                                    }
                                }
                        );

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer.stop();
        }
        finish();
    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Attention !")
                .setIcon(R.drawable.ic_attention_red)
                .setMessage(theWarning)
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }
}
