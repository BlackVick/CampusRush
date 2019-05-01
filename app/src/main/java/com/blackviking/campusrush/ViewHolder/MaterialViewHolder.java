package com.blackviking.campusrush.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.campusrush.R;

public class MaterialViewHolder extends RecyclerView.ViewHolder{

    public TextView courseTitle, materialName, materialInfo;
    public ImageView downloadMaterial;

    public MaterialViewHolder(@NonNull View itemView) {
        super(itemView);

        courseTitle = (TextView)itemView.findViewById(R.id.courseNameTxt);
        materialName = (TextView)itemView.findViewById(R.id.materialNameTxt);
        materialInfo = (TextView)itemView.findViewById(R.id.materialInfoTxt);
        downloadMaterial = (ImageView) itemView.findViewById(R.id.downloadMaterial);
    }
}
