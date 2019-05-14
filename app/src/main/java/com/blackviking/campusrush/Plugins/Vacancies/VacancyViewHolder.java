package com.blackviking.campusrush.Plugins.Vacancies;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.campusrush.Interface.ItemClickListener;
import com.blackviking.campusrush.R;

public class VacancyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    TextView title, company, location;

    public VacancyViewHolder(@NonNull View itemView) {
        super(itemView);

        title = (TextView)itemView.findViewById(R.id.vacancyPost);
        company = (TextView)itemView.findViewById(R.id.vacancyCompany);
        location = (TextView)itemView.findViewById(R.id.vacancyLocation);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
