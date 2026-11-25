// Name                 Rishi Sahonta
// Student ID           S2533401
// Programme of Study   BSc (Hons) Software Development

package com.example.sahonta_rishi_s2533401;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SituationAdapter extends ArrayAdapter<Situation> {

    private Context context;
    private ArrayList<Situation> situations;

    public SituationAdapter(Context context, ArrayList<Situation> situations) {
        super(context, 0, situations);
        this.context = context;
        this.situations = situations;
    }

    @Override
    public View getView(int position, View listItem, ViewGroup listView) {
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.situation, listView, false);
        }

        ImageView situationIcon = listItem.findViewById(R.id.situationIcon);
        TextView situationText = listItem.findViewById(R.id.situationText);
        Situation situation = situations.get(position);
        int icon;

        if (situation.getType().equals("MaintenanceWorks")) {
            icon = R.drawable.icon_maintenance;

        } else if (situation.getType().equals("RoadOrCarriagewayOrLaneManagement")) {
            icon = R.drawable.icon_road_management;

        } else if (situation.getType().equals("AbnormalTraffic")) {
            icon = R.drawable.icon_abnormal_traffic;

        } else if (situation.getType().equals("GeneralObstruction")) {
            icon = R.drawable.icon_general_obstruction;

        } else if (situation.getType().equals("VehicleObstruction")) {
            icon = R.drawable.icon_vehicle_obstruction;

        } else {
            icon = R.drawable.icon_other;
        }

        situationIcon.setImageResource(icon);
        situationText.setText(situation.getType() + "\n" + situation.getDescription());
        return listItem;
    }
}
