package com.blackviking.campusrush.ImageController;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImageViewer extends AppCompatActivity {

    private ImageView exitActivity, imageViewer;
    private String currentImageThumb, currentImage;

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

        setContentView(R.layout.activity_image_viewer);


        /*---   INTENT   ---*/
        currentImage = getIntent().getStringExtra("ImageLink");
        currentImageThumb = getIntent().getStringExtra("ImageThumbLink");


        /*---   WIDGETS   ---*/
        exitActivity = (ImageView)findViewById(R.id.imageBackButton);
        imageViewer = (ImageView)findViewById(R.id.viewImage);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   LOAD IMAGE   ---*/
        if (!currentImageThumb.equalsIgnoreCase("")){

            Picasso.with(getBaseContext())
                    .load(currentImageThumb) // thumbnail url goes here
                    .into(imageViewer, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(getBaseContext())
                                    .load(currentImage) // image url goes here
                                    .placeholder(imageViewer.getDrawable())
                                    .into(imageViewer);
                        }
                        @Override
                        public void onError() {

                        }
                    });

        }
    }
}
