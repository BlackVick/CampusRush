package com.blackviking.campusrush.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Common.Permissions;
import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.Model.MaterialModel;
import com.blackviking.campusrush.Model.MaterialParentModel;
import com.blackviking.campusrush.Profile.MyProfile;
import com.blackviking.campusrush.R;
import com.blackviking.campusrush.ViewHolder.MaterialParentViewHolder;
import com.blackviking.campusrush.ViewHolder.MaterialViewHolder;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import io.paperdb.Paper;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Materials extends Fragment {

    private FloatingActionButton addMaterial;
    private ImageView previousListBtn;
    private TextView currentListName;
    private RecyclerView materialRecycler;
    private RelativeLayout emptyMaterialLayout;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<MaterialModel, MaterialViewHolder> materialAdapter;
    private FirebaseRecyclerAdapter<MaterialParentModel, MaterialParentViewHolder> materialParentAdapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference fileRef;
    private DatabaseReference userRef, materialRef;
    private String currentUid;
    private Boolean onFaculty, onDepartment, onLevels, onMaterial;
    private android.app.AlertDialog mDialog;
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int STORAGE_PERMISSION_CODE = 987;
    private static final int FILE_REQUEST_CODE = 37;
    private Uri fileUri;
    private ProgressDialog progressDialog;
    private Button selectFile;
    private String currentFaculty = "";
    private String currentDepartment = "";
    private String currentLevel = "";
    private DownloadManager downloadManager;

    public Materials() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_materials, container, false);


        /*---   BOOLEAN CHOICES   ---*/
        onFaculty = true;
        onDepartment = false;
        onLevels = false;
        onMaterial = false;


        /*---   FIREBASE   ---*/
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        userRef = db.getReference("Users").child(currentUid);
        materialRef = db.getReference("Materials");
        fileRef = storage.getReference("Materials");


        /*---   WIDGETS   ---*/
        addMaterial = (FloatingActionButton)v.findViewById(R.id.addMaterial);
        materialRecycler = (RecyclerView)v.findViewById(R.id.materialRecycler);
        emptyMaterialLayout = (RelativeLayout)v.findViewById(R.id.emptyMaterialLayout);
        previousListBtn = (ImageView)v.findViewById(R.id.previousList);
        currentListName = (TextView)v.findViewById(R.id.currentListName);

        addMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getContext())){

                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        if (onFaculty)
                            openAddMaterialForFacultyDialog();
                        else if (onDepartment)
                            openAddMaterialForDepartmentDialog();
                        else if (onLevels)
                            openAddMaterialForLevelDialog();
                        else if (onMaterial)
                            openAddMaterialDialog();

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, VERIFY_PERMISSIONS_REQUEST);

                    }

                } else {

                    showErrorDialog("No Internet Access !");
                }
            }
        });


        /*---   EMPTY CHECK   ---*/
        materialRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    emptyMaterialLayout.setVisibility(View.GONE);

                } else {

                    emptyMaterialLayout.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        loadFaculty();

        return v;
    }

    private void loadFaculty() {

        onFaculty = true;
        onDepartment = false;
        onLevels = false;
        onMaterial = false;

        currentFaculty = "";
        currentDepartment = "";
        currentLevel = "";

        materialRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        materialRecycler.setLayoutManager(layoutManager);


        previousListBtn.setVisibility(View.GONE);
        currentListName.setText("FACULTIES");

        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        materialRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE) {

                    theNavLayout.setVisibility(View.VISIBLE);
                    addMaterial.show();
                }
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE) {
                    theNavLayout.setVisibility(View.GONE);
                    addMaterial.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        materialParentAdapter = new FirebaseRecyclerAdapter<MaterialParentModel, MaterialParentViewHolder>(
                MaterialParentModel.class,
                R.layout.material_parent_item,
                MaterialParentViewHolder.class,
                materialRef
        ) {
            @Override
            protected void populateViewHolder(MaterialParentViewHolder viewHolder, MaterialParentModel model, int position) {

                final String serverName = materialParentAdapter.getRef(viewHolder.getAdapterPosition()).getKey();

                viewHolder.name.setText(serverName);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        loadDepartments(serverName);
                    }
                });

            }
        };
        materialRecycler.setAdapter(materialParentAdapter);
    }

    private void loadDepartments(String currentFacultyString) {

        onFaculty = false;
        onDepartment = true;
        onLevels = false;
        onMaterial = false;

        currentFaculty = currentFacultyString;
        currentDepartment = "";
        currentLevel = "";


        materialRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        materialRecycler.setLayoutManager(layoutManager);


        previousListBtn.setVisibility(View.VISIBLE);
        previousListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFaculty();
            }
        });
        currentListName.setText("DEPARTMENTS");


        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        materialRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE) {

                    theNavLayout.setVisibility(View.VISIBLE);
                    addMaterial.show();
                }
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE) {
                    theNavLayout.setVisibility(View.GONE);
                    addMaterial.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        materialParentAdapter = new FirebaseRecyclerAdapter<MaterialParentModel, MaterialParentViewHolder>(
                MaterialParentModel.class,
                R.layout.material_parent_item,
                MaterialParentViewHolder.class,
                materialRef.child(currentFaculty)
        ) {
            @Override
            protected void populateViewHolder(MaterialParentViewHolder viewHolder, MaterialParentModel model, int position) {

                final String serverName = materialParentAdapter.getRef(viewHolder.getAdapterPosition()).getKey();

                viewHolder.name.setText(serverName);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        loadLevels(currentFaculty, serverName);
                    }
                });

            }
        };
        materialRecycler.setAdapter(materialParentAdapter);

    }

    private void loadLevels(String currentFacultyString, String currentDepartmentString) {

        onFaculty = false;
        onDepartment = false;
        onLevels = true;
        onMaterial = false;

        currentFaculty = currentFacultyString;
        currentDepartment = currentDepartmentString;
        currentLevel = "";

        materialRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        materialRecycler.setLayoutManager(layoutManager);


        previousListBtn.setVisibility(View.VISIBLE);
        previousListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDepartments(currentFaculty);
            }
        });
        currentListName.setText("LEVELS");

        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        materialRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE) {

                    theNavLayout.setVisibility(View.VISIBLE);
                    addMaterial.show();
                }
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE) {
                    theNavLayout.setVisibility(View.GONE);
                    addMaterial.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        materialParentAdapter = new FirebaseRecyclerAdapter<MaterialParentModel, MaterialParentViewHolder>(
                MaterialParentModel.class,
                R.layout.material_parent_item,
                MaterialParentViewHolder.class,
                materialRef.child(currentFaculty).child(currentDepartment)
        ) {
            @Override
            protected void populateViewHolder(MaterialParentViewHolder viewHolder, MaterialParentModel model, int position) {

                final String serverName = materialParentAdapter.getRef(viewHolder.getAdapterPosition()).getKey();

                viewHolder.name.setText(serverName);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        loadMaterials(currentFaculty, currentDepartment, serverName);
                    }
                });

            }
        };
        materialRecycler.setAdapter(materialParentAdapter);

    }

    private void loadMaterials(String currentFacultyString, String currentDepartmentString, String currentLevelString) {

        onFaculty = false;
        onDepartment = false;
        onLevels = false;
        onMaterial = true;

        currentFaculty = currentFacultyString;
        currentDepartment = currentDepartmentString;
        currentLevel = currentLevelString;

        materialRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        materialRecycler.setLayoutManager(layoutManager);


        previousListBtn.setVisibility(View.VISIBLE);
        previousListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLevels(currentFaculty, currentDepartment);
            }
        });
        currentListName.setText(currentLevel+" LEVEL");

        final RelativeLayout theNavLayout = (RelativeLayout)getActivity().findViewById(R.id.navLayout);


        materialRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy < 0 && theNavLayout.getVisibility() == View.GONE) {

                    theNavLayout.setVisibility(View.VISIBLE);
                    addMaterial.show();
                }
                else if(dy > 0 && theNavLayout.getVisibility() == View.VISIBLE) {
                    theNavLayout.setVisibility(View.GONE);
                    addMaterial.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    theNavLayout.setVisibility(View.VISIBLE);*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        materialAdapter = new FirebaseRecyclerAdapter<MaterialModel, MaterialViewHolder>(
                MaterialModel.class,
                R.layout.material_list_item,
                MaterialViewHolder.class,
                materialRef.child(currentFaculty).child(currentDepartment).child(currentLevel)
        ) {
            @Override
            protected void populateViewHolder(MaterialViewHolder viewHolder, final MaterialModel model, int position) {

                viewHolder.materialName.setText(model.getMaterialName());
                viewHolder.courseTitle.setText(model.getCourseName());
                viewHolder.materialInfo.setText(model.getMaterialInfo());

                viewHolder.downloadMaterial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                            downloadFile(model.getDownloadLink());

                        } else {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                        }

                    }
                });

            }
        };
        materialRecycler.setAdapter(materialAdapter);

    }

    private void downloadFile(String downloadLink) {

        if (Common.isConnectedToInternet(getContext())) {

            URI uri = null;
            try {

                uri = new URI(downloadLink);
                String[] segments = uri.getPath().split("/");
                String theFileName = segments[segments.length-1];


                File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Campus Rush/Documents/");
                File file= new File(path, theFileName);
                if (!file.exists()){

                    new DownloadFileFromURL().execute(downloadLink);

                } else {

                    showErrorDialog("File Exists... Check Downloads");

                }


            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


        } else {

            showErrorDialog("No Internet Access !");

        }

    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /*---   INDICATE DOWNLOAD START   ---*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getContext(), "Download Started . . .", Toast.LENGTH_SHORT).show();

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


                    downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(link);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setDestinationInExternalPublicDir("/Campus Rush/Documents/", theFileName);
                    request.allowScanningByMediaScanner();
                    Long reference = downloadManager.enqueue(request);

                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                        public void onReceive(Context ctxt, Intent intent) {

                            Toast.makeText(ctxt, "Download Finished", Toast.LENGTH_SHORT).show();

                        }
                    };
                    //register receiver for when file download is compete
                    getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


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

        if (grantResults.length > 0) {

            if (requestCode == VERIFY_PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (onFaculty)
                    openAddMaterialForFacultyDialog();
                else if (onDepartment)
                    openAddMaterialForDepartmentDialog();
                else if (onLevels)
                    openAddMaterialForLevelDialog();
                else if (onMaterial)
                    openAddMaterialDialog();

            } else if (requestCode == STORAGE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "You Can Now Download Files", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(getContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();

            }

        } else {

            Toast.makeText(getContext(), "Permissions Error", Toast.LENGTH_SHORT).show();

        }

    }

    private void openAddMaterialForFacultyDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_material_faculty_layout,null);

        final MaterialEditText facultyNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialFacultyNameFac);
        facultyNameEdt.setText("Faculty Of ");
        facultyNameEdt.setSelection(facultyNameEdt.getText().length());
        final MaterialEditText departmentNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialDepartmentNameFac);
        final MaterialEditText levelNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialLevelNameFac);
        final MaterialEditText materialNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialNameFac);
        final MaterialEditText courseCodeEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialCourseCodeFac);
        final EditText materialInfo = (EditText) viewOptions.findViewById(R.id.materialInfoFac);
        selectFile = (Button) viewOptions.findViewById(R.id.selectMaterialFileFac);
        final Button uploadFile = (Button) viewOptions.findViewById(R.id.uploadMaterialFileFac);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);



        /*---   SELECT FILE LOGIC   ---*/
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });


        /*---   UPLOAD FILE LOGIC   ---*/
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*---   STRINGS   ---*/
                final String theFaculty = facultyNameEdt.getText().toString().trim();
                final String theDepartment = departmentNameEdt.getText().toString().trim();
                final String theLevel = levelNameEdt.getText().toString().trim();
                final String theMaterialName = materialNameEdt.getText().toString().trim();
                final String theCourseCode = courseCodeEdt.getText().toString().trim();
                final String theMaterialInfo = materialInfo.getText().toString().trim();

                if (Common.isConnectedToInternet(getContext())){

                    if (fileUri != null){

                        if (!TextUtils.isEmpty(theFaculty) || !TextUtils.isEmpty(theDepartment) || !TextUtils.isEmpty(theLevel)
                            || !TextUtils.isEmpty(theMaterialName) || !TextUtils.isEmpty(theCourseCode) || !TextUtils.isEmpty(theMaterialInfo)
                            || theFaculty.length() > 12){


                            progressDialog = new ProgressDialog(getContext());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("Upload In Progress. . .");
                            progressDialog.setProgress(0);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            /*---   FILE NAME   ---*/
                            String theFileName = fileUri.getLastPathSegment();

                            theFileName.replace("home:", "");



                            final StorageReference fileRefUp = fileRef.child(theFaculty+"/"+theDepartment+"/"+theLevel+"/"+theFileName);

                            fileRefUp.putFile(fileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                            MaterialModel newMaterial = new MaterialModel(theMaterialName, theCourseCode, downloadUrl, theMaterialInfo, currentUid);
                                            materialRef.child(theFaculty).child(theDepartment).child(theLevel).push().setValue(newMaterial)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                alertDialog.dismiss();
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();


                                                            }
                                                        }
                                                    });

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

                        } else {

                            Toast.makeText(getContext(), "Enter Valid Details !", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(getContext(), "No File Selected !", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    alertDialog.dismiss();
                    showErrorDialog("No Internet Connection !");

                }

            }
        });


        alertDialog.show();

    }

    private void openAddMaterialForDepartmentDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_material_department_layout,null);

        final MaterialEditText departmentNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialDepartmentNameDept);
        final MaterialEditText levelNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialLevelNameDept);
        final MaterialEditText materialNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialNameDept);
        final MaterialEditText courseCodeEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialCourseCodeDept);
        final EditText materialInfo = (EditText) viewOptions.findViewById(R.id.materialInfoDept);
        selectFile = (Button) viewOptions.findViewById(R.id.selectMaterialFileDept);
        final Button uploadFile = (Button) viewOptions.findViewById(R.id.uploadMaterialFileDept);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        /*---   SELECT FILE LOGIC   ---*/
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });


        /*---   UPLOAD FILE LOGIC   ---*/
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*---   STRINGS   ---*/
                final String theDepartment = departmentNameEdt.getText().toString().trim();
                final String theLevel = levelNameEdt.getText().toString().trim();
                final String theMaterialName = materialNameEdt.getText().toString().trim();
                final String theCourseCode = courseCodeEdt.getText().toString().trim();
                final String theMaterialInfo = materialInfo.getText().toString().trim();

                if (Common.isConnectedToInternet(getContext())){

                    if (fileUri != null){

                        if (!TextUtils.isEmpty(theDepartment) || !TextUtils.isEmpty(theLevel)
                                || !TextUtils.isEmpty(theMaterialName) || !TextUtils.isEmpty(theCourseCode) || !TextUtils.isEmpty(theMaterialInfo)){


                            progressDialog = new ProgressDialog(getContext());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("Upload In Progress. . .");
                            progressDialog.setProgress(0);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            /*---   FILE NAME   ---*/
                            String theFileName = fileUri.getLastPathSegment();

                            theFileName.replace("home:", "");



                            final StorageReference fileRefUp = fileRef.child(currentFaculty+"/"+theDepartment+"/"+theLevel+"/"+theFileName);

                            fileRefUp.putFile(fileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                            MaterialModel newMaterial = new MaterialModel(theMaterialName, theCourseCode, downloadUrl, theMaterialInfo, currentUid);
                                            materialRef.child(currentFaculty).child(theDepartment).child(theLevel).push().setValue(newMaterial)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                alertDialog.dismiss();
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();


                                                            }
                                                        }
                                                    });

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

                        } else {

                            Toast.makeText(getContext(), "Enter Valid Details !", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(getContext(), "No File Selected !", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    alertDialog.dismiss();
                    showErrorDialog("No Internet Connection !");

                }

            }
        });

        alertDialog.show();

    }

    private void openAddMaterialForLevelDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_material_level_layout,null);

        final MaterialEditText levelNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialLevelNameLevel);
        final MaterialEditText materialNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialNameLevel);
        final MaterialEditText courseCodeEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialCourseCodeLevel);
        final EditText materialInfo = (EditText) viewOptions.findViewById(R.id.materialInfoLevel);
        selectFile = (Button) viewOptions.findViewById(R.id.selectMaterialFileLevel);
        final Button uploadFile = (Button) viewOptions.findViewById(R.id.uploadMaterialFileLevel);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        /*---   SELECT FILE LOGIC   ---*/
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });


        /*---   UPLOAD FILE LOGIC   ---*/
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*---   STRINGS   ---*/
                final String theLevel = levelNameEdt.getText().toString().trim();
                final String theMaterialName = materialNameEdt.getText().toString().trim();
                final String theCourseCode = courseCodeEdt.getText().toString().trim();
                final String theMaterialInfo = materialInfo.getText().toString().trim();

                if (Common.isConnectedToInternet(getContext())){

                    if (fileUri != null){

                        if (!TextUtils.isEmpty(theLevel) || !TextUtils.isEmpty(theMaterialName) || !TextUtils.isEmpty(theCourseCode) || !TextUtils.isEmpty(theMaterialInfo)){


                            progressDialog = new ProgressDialog(getContext());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("Upload In Progress. . .");
                            progressDialog.setProgress(0);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            /*---   FILE NAME   ---*/
                            String theFileName = fileUri.getLastPathSegment();

                            theFileName.replace("home:", "");



                            final StorageReference fileRefUp = fileRef.child(currentFaculty+"/"+currentDepartment+"/"+theLevel+"/"+theFileName);

                            fileRefUp.putFile(fileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                            MaterialModel newMaterial = new MaterialModel(theMaterialName, theCourseCode, downloadUrl, theMaterialInfo, currentUid);
                                            materialRef.child(currentFaculty).child(currentDepartment).child(theLevel).push().setValue(newMaterial)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                alertDialog.dismiss();
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();


                                                            }
                                                        }
                                                    });

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

                        } else {

                            Toast.makeText(getContext(), "Enter Valid Details !", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(getContext(), "No File Selected !", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    alertDialog.dismiss();
                    showErrorDialog("No Internet Connection !");

                }

            }
        });

        alertDialog.show();

    }

    private void openAddMaterialDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_material_layout,null);

        final MaterialEditText materialNameEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialNameMat);
        final MaterialEditText courseCodeEdt = (MaterialEditText) viewOptions.findViewById(R.id.materialCourseCodeMat);
        final EditText materialInfo = (EditText) viewOptions.findViewById(R.id.materialInfoMat);
        selectFile = (Button) viewOptions.findViewById(R.id.selectMaterialFileMat);
        final Button uploadFile = (Button) viewOptions.findViewById(R.id.uploadMaterialFileMat);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);


        /*---   SELECT FILE LOGIC   ---*/
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });


        /*---   CHECK URI   ---*/
        if (fileUri != null){

            selectFile.setText("SELECTED");

        } else {

            selectFile.setText("SELECT FILE");

        }


        /*---   UPLOAD FILE LOGIC   ---*/
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*---   STRINGS   ---*/
                final String theMaterialName = materialNameEdt.getText().toString().trim();
                final String theCourseCode = courseCodeEdt.getText().toString().trim();
                final String theMaterialInfo = materialInfo.getText().toString().trim();

                if (Common.isConnectedToInternet(getContext())){

                    if (fileUri != null){

                        if (!TextUtils.isEmpty(theMaterialName) || !TextUtils.isEmpty(theCourseCode) || !TextUtils.isEmpty(theMaterialInfo)){


                            progressDialog = new ProgressDialog(getContext());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("Upload In Progress. . .");
                            progressDialog.setProgress(0);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            /*---   FILE NAME   ---*/
                            String theFileName = fileUri.getLastPathSegment();

                            theFileName.replace("home:", "");



                            final StorageReference fileRefUp = fileRef.child(currentFaculty+"/"+currentDepartment+"/"+currentLevel+"/"+theFileName);

                            fileRefUp.putFile(fileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                            MaterialModel newMaterial = new MaterialModel(theMaterialName, theCourseCode, downloadUrl, theMaterialInfo, currentUid);
                                            materialRef.child(currentFaculty).child(currentDepartment).child(currentLevel).push().setValue(newMaterial)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                alertDialog.dismiss();
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();


                                                            }
                                                        }
                                                    });

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

                        } else {

                            Toast.makeText(getContext(), "Enter Valid Details !", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(getContext(), "No File Selected !", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    alertDialog.dismiss();
                    showErrorDialog("No Internet Connection !");

                }

            }
        });


        alertDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            fileUri = data.getData();
            selectFile.setText("SELECTED");

        } else {

            Toast.makeText(getContext(), "Select A File !", Toast.LENGTH_SHORT).show();
            selectFile.setText("SELECT FILE");

        }

    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext())
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