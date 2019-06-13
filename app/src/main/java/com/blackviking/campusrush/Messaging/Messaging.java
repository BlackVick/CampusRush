package com.blackviking.campusrush.Messaging;

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
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.BuildConfig;
import com.blackviking.campusrush.CampusRant.RantRoom;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.GetTimeAgo;
import com.blackviking.campusrush.FeedDetails;
import com.blackviking.campusrush.ImageController.ImageViewer;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Profile.OtherUserProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.Settings.Help;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Messaging extends AppCompatActivity {

    private ImageView exitActivity, helpActivity;
    private TextView username;
    private CircleImageView userImage;
    private ImageView attachment, sendMessage;
    private EditText messageBox;
    private RecyclerView messageRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<MessageModel, MessagingViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, messagesRef, userMessageRef, messageListRef, userMessageListRef;
    private String currentUid, userId;
    private static final int GALLERY_REQUEST_CODE = 686;
    private static final int CAMERA_REQUEST_CODE = 456;
    private static final int VERIFY_PERMISSIONS_REQUEST = 17;
    private Uri imageUri;
    private String originalImageUrl, thumbDownloadUrl;
    private android.app.AlertDialog mDialog;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private APIService mService;
    private String userUsername, userImageLink;

    private LinearLayout myQuoteLayout;
    private TextView myQuoter, myQuoteText;
    private ImageView myQuoteImage, cancelQuote;
    private boolean isQuoting = false;
    private String currentQuote;

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

        setContentView(R.layout.activity_messaging);


        /*---   LOCAL   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users");
        messagesRef = db.getReference("Messages")
                .child(currentUid);
        userMessageRef = db.getReference("Messages")
                .child(userId);
        messageListRef = db.getReference("MessageList").child(currentUid);
        userMessageListRef = db.getReference("MessageList").child(userId);
        imageRef = storage.getReference("MessageImages");


        /*---   WIDGETS   ---*/
        username = (TextView)findViewById(R.id.msgUsernameView);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        userImage = (CircleImageView)findViewById(R.id.msgUserImageView);
        attachment = (ImageView)findViewById(R.id.addAttachment);
        sendMessage = (ImageView)findViewById(R.id.sendMessage);
        messageBox = (EditText)findViewById(R.id.messageEditText);
        messageRecycler = (RecyclerView)findViewById(R.id.messageRecycler);

        myQuoteLayout = (LinearLayout)findViewById(R.id.quoteLayout);
        myQuoter = (TextView)findViewById(R.id.quoter);
        myQuoteText = (TextView)findViewById(R.id.quoteText);
        myQuoteImage = (ImageView) findViewById(R.id.quoteImage);
        cancelQuote = (ImageView) findViewById(R.id.cancelQuote);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Messaging.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   CURRENT FRIEND   ---*/
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userUsername = dataSnapshot.child("username").getValue().toString();
                userImageLink = dataSnapshot.child("profilePictureThumb").getValue().toString();

                username.setText("@"+userUsername);

                if (!userImageLink.equalsIgnoreCase("")){

                    Picasso.with(getBaseContext())
                            .load(userImageLink)
                            .placeholder(R.drawable.profile)
                            .into(userImage);

                }

                if (!userId.equalsIgnoreCase("")){
                    userImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent viewUserIntent = new Intent(Messaging.this, OtherUserProfile.class);
                            viewUserIntent.putExtra("UserId", userId);
                            startActivity(viewUserIntent);
                            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---   SEND MESSAGE   ---*/
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTheMessage();
            }
        });


        /*---   ADD ATTACHMENT   ---*/
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Messaging.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(Messaging.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                    openChoiceDialog();

                } else {

                    ActivityCompat.requestPermissions(Messaging.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, VERIFY_PERMISSIONS_REQUEST);

                }
            }
        });


        loadTheMessages();
    }

    private void sendTheMessage() {

        final String theMessage = messageBox.getText().toString().trim();

        if (!TextUtils.isEmpty(theMessage)){

            /*---   PUSH   ---*/
            final DatabaseReference pushIdRef = messagesRef.child(userId).push();
            final String pushId = pushIdRef.getKey();


            /*---   YOUR MAP   ---*/
            final Map<String, Object> myMessageMap = new HashMap<>();
            myMessageMap.put("message", theMessage);
            myMessageMap.put("imageUrl", "");
            myMessageMap.put("imageThumbUrl", "");
            myMessageMap.put("messageRead", "read");
            myMessageMap.put("messageFrom", currentUid);
            myMessageMap.put("messageTimestamp", ServerValue.TIMESTAMP);

            if (isQuoting) {

                myMessageMap.put("isQuote", "true");
                myMessageMap.put("quotedId", currentQuote);

            } else {

                myMessageMap.put("isQuote", "false");
                myMessageMap.put("quotedId", "");

            }


            /*---   FRIEND MAP   ---*/
            final Map<String, Object> friendMessageMap = new HashMap<>();
            friendMessageMap.put("message", theMessage);
            friendMessageMap.put("imageUrl", "");
            friendMessageMap.put("imageThumbUrl", "");
            friendMessageMap.put("messageRead", "unread");
            friendMessageMap.put("messageFrom", currentUid);
            friendMessageMap.put("messageTimestamp", ServerValue.TIMESTAMP);

            if (isQuoting) {

                friendMessageMap.put("isQuote", "true");
                friendMessageMap.put("quotedId", currentQuote);

            } else {

                friendMessageMap.put("isQuote", "false");
                friendMessageMap.put("quotedId", "");

            }


            /*---   MY LIST MAP   ---*/
            final Map<String, Object> myLastMessageTimeMap = new HashMap<>();
            myLastMessageTimeMap.put("lastMessageTimestamp", ServerValue.TIMESTAMP);

            /*---   FRIEND LIST MAP   ---*/
            final Map<String, Object> userLastMessageTimeMap = new HashMap<>();
            userLastMessageTimeMap.put("lastMessageTimestamp", ServerValue.TIMESTAMP);


            isQuoting = false;
            currentQuote = "";
            myQuoteLayout.setVisibility(View.GONE);

            messageBox.setText("");
            messagesRef
                    .child(userId)
                    .child(pushId)
                    .setValue(myMessageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sendLikeNotification(theMessage);
                    messageListRef.child(userId)
                            .updateChildren(myLastMessageTimeMap);
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    userMessageRef
                            .child(currentUid)
                            .child(pushId)
                            .setValue(friendMessageMap);

                    userMessageListRef.child(currentUid)
                            .updateChildren(userLastMessageTimeMap);
                }
            });

        }

    }

    private void sendLikeNotification(final String theMessage) {

        userRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String myUsername = dataSnapshot.child("username").getValue().toString();

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Messaging");
                dataSend.put("message", "@"+myUsername+" Messaged : "+theMessage);
                dataSend.put("user_id", currentUid);
                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

                mService.sendNotification(dataMessage)
                        .enqueue(new retrofit2.Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(Messaging.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadTheMessages() {

        messageRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        messageRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<MessageModel, MessagingViewHolder>(
                MessageModel.class,
                R.layout.single_message_item,
                MessagingViewHolder.class,
                messagesRef.child(userId)
        ) {
            @Override
            protected void populateViewHolder(final MessagingViewHolder viewHolder, final MessageModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getMessageTimestamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());


                if (model.getMessageFrom().equalsIgnoreCase(currentUid)){

                    viewHolder.yourMsgLayout.setVisibility(View.VISIBLE);
                    viewHolder.otherMsgLayout.setVisibility(View.GONE);
                    viewHolder.myTextTimeStamp.setText(lastSeenTime);


                    /*---   QUOTED MESSAGES   ---*/
                    if (model.getIsQuote().equalsIgnoreCase("true")){

                        viewHolder.myQuoteLayout.setVisibility(View.VISIBLE);
                        viewHolder.myQuoter.setVisibility(View.VISIBLE);

                        messagesRef.child(userId)
                                .child(model.getQuotedId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String theQuote = dataSnapshot.child("message").getValue().toString();
                                        String theImage = dataSnapshot.child("imageThumbUrl").getValue().toString();
                                        String theSenderOfQuote = dataSnapshot.child("messageFrom").getValue().toString();

                                        if (theSenderOfQuote.equalsIgnoreCase(userId)){

                                            viewHolder.myQuoter.setText("@"+userUsername);

                                        } else {

                                            viewHolder.myQuoter.setText("You");

                                        }

                                        if (!theImage.equalsIgnoreCase("")){

                                            viewHolder.myQuoteImage.setVisibility(View.VISIBLE);
                                            Picasso.with(getBaseContext())
                                                    .load(theImage)
                                                    .placeholder(R.drawable.image_placeholders)
                                                    .into(viewHolder.myQuoteImage);

                                        } else {

                                            viewHolder.myQuoteImage.setVisibility(View.GONE);
                                        }

                                        if (!theQuote.equalsIgnoreCase("")){

                                            viewHolder.myQuoteText.setVisibility(View.VISIBLE);
                                            viewHolder.myQuoteText.setText(theQuote);
                                        } else {

                                            viewHolder.myQuoteText.setVisibility(View.GONE);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    } else {

                        viewHolder.myQuoteLayout.setVisibility(View.GONE);

                    }


                    /*---   MESSAGE   ---*/
                    if (!model.getMessage().equalsIgnoreCase("")){

                        viewHolder.myText.setVisibility(View.VISIBLE);
                        viewHolder.myText.setText(model.getMessage());

                    } else {

                        viewHolder.myText.setVisibility(View.GONE);

                    }

                    /*---   IMAGE   ---*/
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        viewHolder.yourMsgImage.setVisibility(View.VISIBLE);

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.image_placeholders)
                                .into(viewHolder.yourMsgImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getBaseContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.yourMsgImage);
                                    }
                                });

                        viewHolder.yourMsgImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent viewImageIntent = new Intent(Messaging.this, ImageViewer.class);
                                viewImageIntent.putExtra("ImageLink", model.getImageUrl());
                                viewImageIntent.putExtra("ImageThumbLink", model.getImageThumbUrl());
                                startActivity(viewImageIntent);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            }
                        });

                    } else {

                        viewHolder.yourMsgImage.setVisibility(View.GONE);

                    }

                } else {

                    /*---   READ ALL   ---*/
                    if (!model.getMessageRead().equalsIgnoreCase("read")){

                        messagesRef
                                .child(userId)
                                .child(adapter.getRef(viewHolder.getAdapterPosition()).getKey())
                                .child("messageRead")
                                .setValue("read");

                    }


                    viewHolder.yourMsgLayout.setVisibility(View.GONE);
                    viewHolder.otherMsgLayout.setVisibility(View.VISIBLE);
                    viewHolder.otherTextTimeStamp.setText(lastSeenTime);


                    /*---   QUOTED MESSAGES   ---*/
                    if (model.getIsQuote().equalsIgnoreCase("true")){

                        viewHolder.otherQuoteLayout.setVisibility(View.VISIBLE);
                        viewHolder.otherQuoter.setVisibility(View.VISIBLE);

                        messagesRef.child(userId)
                                .child(model.getQuotedId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String theQuote = dataSnapshot.child("message").getValue().toString();
                                        String theImage = dataSnapshot.child("imageThumbUrl").getValue().toString();
                                        String theSenderOfQuote = dataSnapshot.child("messageFrom").getValue().toString();

                                        if (theSenderOfQuote.equalsIgnoreCase(userId)){

                                            viewHolder.otherQuoter.setText("@"+userUsername);

                                        } else {

                                            viewHolder.otherQuoter.setText("You");

                                        }

                                        if (!theImage.equalsIgnoreCase("")){

                                            viewHolder.otherQuoteImage.setVisibility(View.VISIBLE);
                                            Picasso.with(getBaseContext())
                                                    .load(theImage)
                                                    .placeholder(R.drawable.image_placeholders)
                                                    .into(viewHolder.otherQuoteImage);

                                        } else {

                                            viewHolder.otherQuoteImage.setVisibility(View.GONE);
                                        }

                                        if (!theQuote.equalsIgnoreCase("")){

                                            viewHolder.otherQuoteText.setVisibility(View.VISIBLE);
                                            viewHolder.otherQuoteText.setText(theQuote);
                                        } else {

                                            viewHolder.otherQuoteText.setVisibility(View.GONE);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    } else {

                        viewHolder.otherQuoteLayout.setVisibility(View.GONE);

                    }


                    /*---   MESSAGE   ---*/
                    if (!model.getMessage().equalsIgnoreCase("")){

                        viewHolder.otherText.setVisibility(View.VISIBLE);
                        viewHolder.otherText.setText(model.getMessage());

                    } else {

                        viewHolder.otherText.setVisibility(View.GONE);

                    }

                    /*---   IMAGE   ---*/
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        viewHolder.otherMsgImage.setVisibility(View.VISIBLE);

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.image_placeholders)
                                .into(viewHolder.otherMsgImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getBaseContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.otherMsgImage);
                                    }
                                });

                        viewHolder.otherMsgImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent viewImageIntent = new Intent(Messaging.this, ImageViewer.class);
                                viewImageIntent.putExtra("ImageLink", model.getImageUrl());
                                viewImageIntent.putExtra("ImageThumbLink", model.getImageThumbUrl());
                                startActivity(viewImageIntent);
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                            }
                        });

                    } else {

                        viewHolder.otherMsgImage.setVisibility(View.GONE);

                    }

                }

                viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        quoteMessage(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                    }
                });

                /*---   RECYCLER OPTIONS   ---*/

            }
        };
        messageRecycler.setAdapter(adapter);
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
                    messageRecycler.scrollToPosition(positionStart);

                }
                layoutManager.smoothScrollToPosition(messageRecycler, null, adapter.getItemCount());
            }
        });

    }

    private void quoteMessage(String key) {

        isQuoting = true;
        currentQuote = key;

        messagesRef.child(userId)
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theQuote = dataSnapshot.child("message").getValue().toString();
                        String theImage = dataSnapshot.child("imageThumbUrl").getValue().toString();
                        String theSenderOfQuote = dataSnapshot.child("messageFrom").getValue().toString();

                        myQuoteLayout.setVisibility(View.VISIBLE);
                        myQuoter.setVisibility(View.VISIBLE);

                        if (theSenderOfQuote.equalsIgnoreCase(userId)){

                            myQuoter.setText("@"+userUsername);

                        } else {

                            myQuoter.setText("You");

                        }

                        if (!theImage.equalsIgnoreCase("")){

                            myQuoteImage.setVisibility(View.VISIBLE);
                            Picasso.with(getBaseContext())
                                    .load(theImage)
                                    .placeholder(R.drawable.image_placeholders)
                                    .into(myQuoteImage);

                        } else {

                            myQuoteImage.setVisibility(View.GONE);
                        }

                        if (!theQuote.equalsIgnoreCase("")){

                            myQuoteText.setVisibility(View.VISIBLE);
                            myQuoteText.setText(theQuote);
                        } else {

                            myQuoteText.setVisibility(View.GONE);
                        }

                        cancelQuote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isQuoting = false;
                                currentQuote = "";
                                myQuoteLayout.setVisibility(View.GONE);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void serviceLoadMessages() {

        messageRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        messageRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<MessageModel, MessagingViewHolder>(
                MessageModel.class,
                R.layout.single_message_item,
                MessagingViewHolder.class,
                messagesRef.child(userId)
        ) {
            @Override
            protected void populateViewHolder(final MessagingViewHolder viewHolder, final MessageModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getMessageTimestamp();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());


                if (model.getMessageFrom().equalsIgnoreCase(currentUid)){

                    viewHolder.yourMsgLayout.setVisibility(View.VISIBLE);
                    viewHolder.otherMsgLayout.setVisibility(View.GONE);
                    viewHolder.myTextTimeStamp.setText(lastSeenTime);

                    /*---   MESSAGE   ---*/
                    if (!model.getMessage().equalsIgnoreCase("")){

                        viewHolder.myText.setVisibility(View.VISIBLE);
                        viewHolder.myText.setText(model.getMessage());

                    } else {

                        viewHolder.myText.setVisibility(View.GONE);

                    }

                    /*---   IMAGE   ---*/
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        viewHolder.yourMsgImage.setVisibility(View.VISIBLE);

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.image_placeholders)
                                .into(viewHolder.yourMsgImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getBaseContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.yourMsgImage);
                                    }
                                });

                    } else {

                        viewHolder.yourMsgImage.setVisibility(View.GONE);

                    }

                } else {

                    viewHolder.yourMsgLayout.setVisibility(View.GONE);
                    viewHolder.otherMsgLayout.setVisibility(View.VISIBLE);
                    viewHolder.otherTextTimeStamp.setText(lastSeenTime);


                    /*---   MESSAGE   ---*/
                    if (!model.getMessage().equalsIgnoreCase("")){

                        viewHolder.otherText.setVisibility(View.VISIBLE);
                        viewHolder.otherText.setText(model.getMessage());

                    } else {

                        viewHolder.otherText.setVisibility(View.GONE);

                    }

                    /*---   IMAGE   ---*/
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        viewHolder.otherMsgImage.setVisibility(View.VISIBLE);

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.image_placeholders)
                                .into(viewHolder.otherMsgImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getBaseContext())
                                                .load(model.getImageThumbUrl())
                                                .placeholder(R.drawable.campus_rush_feed_placeholder)
                                                .into(viewHolder.otherMsgImage);
                                    }
                                });

                    } else {

                        viewHolder.otherMsgImage.setVisibility(View.GONE);

                    }

                }

                /*---   RECYCLER OPTIONS   ---*/

            }
        };
        messageRecycler.setAdapter(adapter);
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
                    messageRecycler.scrollToPosition(positionStart);

                }
                layoutManager.smoothScrollToPosition(messageRecycler, null, adapter.getItemCount());
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

                if (Common.isConnectedToInternet(Messaging.this)){

                    openCamera();

                }else {

                    Common.showErrorDialog(Messaging.this, "No Internet Access !");
                }
                alertDialog.dismiss();

            }
        });

        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(Messaging.this)){

                    openGallery();

                }else {

                    Common.showErrorDialog(Messaging.this, "No Internet Access !");
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
                Messaging.this,
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
        View viewOptions = inflater.inflate(R.layout.image_message_layout,null);

        final ImageView rantImagePreview = (ImageView) viewOptions.findViewById(R.id.rantImagePreview);
        final ImageView sendRantPreview = (ImageView) viewOptions.findViewById(R.id.sendRantPreview);
        final EditText rantTextPreview = (EditText) viewOptions.findViewById(R.id.rantPreviewText);

        alertDialog.setView(viewOptions);

        //alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;


        String theMessage = messageBox.getText().toString().trim();

        if (!TextUtils.isEmpty(theMessage))
            rantTextPreview.setText(theMessage);


        setImage(imgURI, rantImagePreview);


        sendRantPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog = new SpotsDialog(Messaging.this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                final long date = System.currentTimeMillis();
                final String dateShitFmt = String.valueOf(date);

                File thumb_filepath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(Messaging.this)
                            .setMaxWidth(350)
                            .setMaxHeight(350)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    final StorageReference imageRef1 = imageRef.child("FullImages").child(currentUid + dateShitFmt + ".jpg");

                    final StorageReference imageThumbRef1 = imageRef.child("Thumbnails").child(currentUid + dateShitFmt + ".jpg");

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
                                            final DatabaseReference pushIdRef = messagesRef.child(userId).push();
                                            final String pushId = pushIdRef.getKey();
                                            final String theFinalMessageText = rantTextPreview.getText().toString().trim();


                                            /*---   YOUR MAP   ---*/
                                            final Map<String, Object> myMessageMap = new HashMap<>();
                                            myMessageMap.put("message", theFinalMessageText);
                                            myMessageMap.put("imageUrl", originalImageUrl);
                                            myMessageMap.put("imageThumbUrl", thumbDownloadUrl);
                                            myMessageMap.put("messageRead", "read");
                                            myMessageMap.put("messageFrom", currentUid);
                                            myMessageMap.put("messageTimestamp", ServerValue.TIMESTAMP);

                                            if (isQuoting) {

                                                myMessageMap.put("isQuote", "true");
                                                myMessageMap.put("quotedId", currentQuote);

                                            } else {

                                                myMessageMap.put("isQuote", "false");
                                                myMessageMap.put("quotedId", "");

                                            }


                                            /*---   FRIEND MAP   ---*/
                                            final Map<String, Object> friendMessageMap = new HashMap<>();
                                            friendMessageMap.put("message", theFinalMessageText);
                                            friendMessageMap.put("imageUrl", originalImageUrl);
                                            friendMessageMap.put("imageThumbUrl", thumbDownloadUrl);
                                            friendMessageMap.put("messageRead", "unread");
                                            friendMessageMap.put("messageFrom", currentUid);
                                            friendMessageMap.put("messageTimestamp", ServerValue.TIMESTAMP);

                                            if (isQuoting) {

                                                friendMessageMap.put("isQuote", "true");
                                                friendMessageMap.put("quotedId", currentQuote);

                                            } else {

                                                friendMessageMap.put("isQuote", "false");
                                                friendMessageMap.put("quotedId", "");

                                            }

                                            /*---   MY LIST MAP   ---*/
                                            final Map<String, Object> myLastMessageTimeMap = new HashMap<>();
                                            myLastMessageTimeMap.put("lastMessageTimestamp", ServerValue.TIMESTAMP);

                                            /*---   FRIEND LIST MAP   ---*/
                                            final Map<String, Object> userLastMessageTimeMap = new HashMap<>();
                                            userLastMessageTimeMap.put("lastMessageTimestamp", ServerValue.TIMESTAMP);

                                            isQuoting = false;
                                            currentQuote = "";
                                            myQuoteLayout.setVisibility(View.GONE);

                                            messagesRef
                                                    .child(userId)
                                                    .child(pushId)
                                                    .setValue(myMessageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendLikeNotification(theFinalMessageText);
                                                    rantTextPreview.setText("");
                                                    messageBox.setText("");
                                                    alertDialog.dismiss();
                                                    messageListRef.child(userId)
                                                            .updateChildren(myLastMessageTimeMap);
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    userMessageRef
                                                            .child(currentUid)
                                                            .child(pushId)
                                                            .setValue(friendMessageMap);

                                                    userMessageListRef.child(currentUid)
                                                            .updateChildren(userLastMessageTimeMap);
                                                }
                                            });

                                        } else {
                                            mDialog.dismiss();
                                            Common.showErrorDialog(Messaging.this, "Error Occurred While Uploading");
                                        }
                                    }
                                });



                            } else {

                                mDialog.dismiss();
                                Common.showErrorDialog(Messaging.this, "Error Occurred While Uploading");

                            }
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

        if (requestCode == VERIFY_PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            openChoiceDialog();

        } else {

            Toast.makeText(Messaging.this, "Permissions Denied", Toast.LENGTH_SHORT).show();

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

        loader.init(ImageLoaderConfiguration.createDefault(Messaging.this));

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
