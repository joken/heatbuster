package io.github.joken.heatbuster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

public class CheckboxListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<CheckBoxItem> checkBoxItemsList;

    public CheckboxListAdapter(Context context, ArrayList<CheckBoxItem>checkBoxItemsList){
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.checkBoxItemsList=checkBoxItemsList;
    }

    @Override
    public int getCount(){
        return checkBoxItemsList.size();
    }

    @Override
    public Object getItem(int position){
        return checkBoxItemsList.get(position);
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        convertView=layoutInflater.inflate(R.layout.checkbox_listview_cell,parent,false);
        TextView serialView = (TextView)convertView.findViewById(R.id.serialTextView);
        serialView.setText(checkBoxItemsList.get(position).getSerial());
        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBoxView);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                //buttonView.setChecked(!buttonView.isChecked());
                checkBoxItemsList.get(position).setChecked(buttonView.isChecked());
        }
        });

        return convertView;
    }


}
