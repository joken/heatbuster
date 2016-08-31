package io.github.joken.heatbuster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;

import java.util.ArrayList;

public class ClubmonitorAdapter extends BaseAdapter{
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Clubmonitor> clubmonitorsList;

    public ClubmonitorAdapter(Context context, ArrayList<Clubmonitor>clubmonitorsList){
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clubmonitorsList=clubmonitorsList;
    }

    @Override
    public int getCount(){
        return clubmonitorsList.size();
    }

    @Override
    public Object getItem(int position){
        return clubmonitorsList.get(position);
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        convertView = layoutInflater.inflate(R.layout.club_listview_cell, parent, false);
        TextView nameView = (TextView)convertView.findViewById(R.id.clubnameTextView);
        nameView.setText(clubmonitorsList.get(position).getName());

        TextView hobbyView = (TextView)convertView.findViewById(R.id.templaTextView);
        hobbyView.setText(clubmonitorsList.get(position).getClubTemp());

        TextView templaRateView = (TextView)convertView.findViewById(R.id.templarateTextView);
        templaRateView.setText(clubmonitorsList.get(position).getTempIncreaseRate());

        ImageView clubStatusImage = (ImageView) convertView.findViewById(R.id.item_image);
        clubStatusImage.setImageResource(clubmonitorsList.get(position).getSelfStatus().getImageID());

        AwesomeTextView statusText = (AwesomeTextView)convertView.findViewById(R.id.tempStatusText);
        statusText.setBootstrapBrand(clubmonitorsList.get(position).getSelfStatus().getBrand());
        statusText.setText(clubmonitorsList.get(position).getSelfStatus().getStatusText());

        return convertView;
    }

}
