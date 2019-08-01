package com.blackviking.campusrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Model.AdminManageModel;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.blackviking.campusrush.Services.SubscriptionService;
import com.blackviking.campusrush.Settings.Help;
import com.blackviking.campusrush.ViewHolder.AdminManageViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AdminManagement extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private RecyclerView adminRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<AdminManageModel, AdminManageViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, adminRef, notificationRef;
    private String currentUid;
    private FloatingActionButton adminMessaging;
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

        setContentView(R.layout.activity_admin_management);


        /*---   FCM   ---*/
        mService = Common.getFCMService();

        /*---   FIREBASE   ---*/
        adminRef = db.getReference("AdminManagement");
        userRef = db.getReference("Users");
        notificationRef = db.getReference("Notifications");


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        adminRecycler = (RecyclerView)findViewById(R.id.adminRecycler);
        adminMessaging = (FloatingActionButton)findViewById(R.id.adminMessaging);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        activityName.setText("Admin Management");
        helpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(AdminManagement.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });

        adminMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMessagingDialog();
            }
        });


        if (Common.isConnectedToInternet(getBaseContext())){
            loadItems();
        } else {
           showErrorDialog("No Internet Access !");
        }
    }

    private void openMessagingDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.admin_messaging_layout,null);

        final EditText announcementUser = (EditText) viewOptions.findViewById(R.id.announcementUser);
        final EditText announcementMessage = (EditText) viewOptions.findViewById(R.id.announcementMessage);
        final RadioGroup announcementStyle = (RadioGroup) viewOptions.findViewById(R.id.announcementStyle);
        final RadioButton single = (RadioButton) viewOptions.findViewById(R.id.single);
        final RadioButton all = (RadioButton) viewOptions.findViewById(R.id.all);
        final Button sendAnnouncement = (Button) viewOptions.findViewById(R.id.sendAnnouncement);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        announcementStyle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.single) {

                    announcementUser.setVisibility(View.VISIBLE);
                    sendAnnouncement.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (Common.isConnectedToInternet(getBaseContext())) {

                                final String theAnnouncementMessage = announcementMessage.getText().toString().trim();
                                String theAnnouncementUser = announcementUser.getText().toString().trim();

                                if (!TextUtils.isEmpty(theAnnouncementMessage) || !TextUtils.isEmpty(theAnnouncementUser)) {

                                    userRef.orderByChild("username")
                                            .equalTo(theAnnouncementUser)
                                            .addListenerForSingleValueEvent(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            if (dataSnapshot.exists()) {


                                                                for (DataSnapshot child : dataSnapshot.getChildren()) {

                                                                    String key = child.getKey();

                                                                    final Map<String, Object> notificationMap = new HashMap<>();
                                                                    notificationMap.put("title", "Account");
                                                                    notificationMap.put("details", theAnnouncementMessage);
                                                                    notificationMap.put("comment", "");
                                                                    notificationMap.put("type", "Business");
                                                                    notificationMap.put("status", "Unread");
                                                                    notificationMap.put("intentPrimaryKey", "");
                                                                    notificationMap.put("intentSecondaryKey", "");
                                                                    notificationMap.put("user", key);
                                                                    notificationMap.put("timestamp", ServerValue.TIMESTAMP);

                                                                    notificationRef.child(key)
                                                                            .push()
                                                                            .setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            alertDialog.dismiss();

                                                                            Map<String, String> dataSend = new HashMap<>();
                                                                            dataSend.put("title", "Account");
                                                                            dataSend.put("message", theAnnouncementMessage);
                                                                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.ADMIN_MESSAGE).toString(), dataSend);

                                                                            mService.sendNotification(dataMessage)
                                                                                    .enqueue(new retrofit2.Callback<MyResponse>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                                                                            Toast.makeText(AdminManagement.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });

                                                                }

                                                            } else {

                                                                Toast.makeText(AdminManagement.this, "No Such User", Toast.LENGTH_SHORT).show();

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    }
                                            );

                                } else {

                                    Toast.makeText(AdminManagement.this, "Empty Fields", Toast.LENGTH_SHORT).show();

                                }

                            }

                        }
                    });

                } else if(checkedId == R.id.all) {

                    announcementUser.setVisibility(View.GONE);
                    sendAnnouncement.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (Common.isConnectedToInternet(getBaseContext())) {

                                final String theAnnouncementMessage = announcementMessage.getText().toString().trim();

                                if (!TextUtils.isEmpty(theAnnouncementMessage)) {

                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot child : dataSnapshot.getChildren()) {

                                                String key = child.getKey();

                                                final Map<String, Object> notificationMap = new HashMap<>();
                                                notificationMap.put("title", "Account");
                                                notificationMap.put("details", theAnnouncementMessage);
                                                notificationMap.put("comment", "");
                                                notificationMap.put("type", "Business");
                                                notificationMap.put("status", "Unread");
                                                notificationMap.put("intentPrimaryKey", "");
                                                notificationMap.put("intentSecondaryKey", "");
                                                notificationMap.put("user", key);
                                                notificationMap.put("timestamp", ServerValue.TIMESTAMP);

                                                notificationRef.child(key)
                                                        .push()
                                                        .setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        alertDialog.dismiss();

                                                        Map<String, String> dataSend = new HashMap<>();
                                                        dataSend.put("title", "Account");
                                                        dataSend.put("message", theAnnouncementMessage);
                                                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.ADMIN_MESSAGE).toString(), dataSend);

                                                        mService.sendNotification(dataMessage)
                                                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                                                    @Override
                                                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                                                        Toast.makeText(AdminManagement.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } else {

                                    Toast.makeText(AdminManagement.this, "Empty Message", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }
                    });

                }
            }

        });

        alertDialog.show();
    }

    private void loadItems() {

        adminRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adminRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<AdminManageModel, AdminManageViewHolder>(
                AdminManageModel.class,
                R.layout.admin_manage_item,
                AdminManageViewHolder.class,
                adminRef
        ) {
            @Override
            protected void populateViewHolder(final AdminManageViewHolder viewHolder, final AdminManageModel model, int position) {

                if (model.getAdminType().equalsIgnoreCase("Feed")){

                    viewHolder.adminItemTitle.setText(model.getAdminType());
                    if (!model.getImageThumbUrl().equalsIgnoreCase("")){

                        Picasso.with(getBaseContext())
                                .load(model.getImageThumbUrl())
                                .placeholder(R.drawable.ic_new_placeholder_icon)
                                .into(viewHolder.adminItemImage);

                    } else {

                        viewHolder.adminItemImage.setImageResource(R.drawable.campus_rush_colored_logo);

                    }

                    if (!model.getUpdate().equalsIgnoreCase("")){

                        viewHolder.adminItemDescription.setText(model.getUpdate());

                    } else {

                        viewHolder.adminItemDescription.setText("It is an Image Feed");

                    }


                } else if (model.getAdminType().equalsIgnoreCase("Skit")){

                    viewHolder.adminItemTitle.setText(model.getAdminType());
                    viewHolder.adminItemDescription.setText(model.getTitle());

                    Picasso.with(getBaseContext())
                            .load(model.getThumbnail())
                            .placeholder(R.drawable.ic_new_placeholder_icon)
                            .into(viewHolder.adminItemImage);

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent managementDetail = new Intent(AdminManagement.this, ManagementDetail.class);
                        managementDetail.putExtra("ItemId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        managementDetail.putExtra("ItemType", model.getAdminType());
                        startActivity(managementDetail);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
                    }
                });

            }
        };
        adminRecycler.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
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
