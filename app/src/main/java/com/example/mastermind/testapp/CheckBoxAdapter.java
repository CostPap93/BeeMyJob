package com.example.mastermind.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mastermind on 19/4/2018.
 */

public class CheckBoxAdapter extends BaseAdapter {
    Context context;
    ArrayList<OfferCategory> categories = new ArrayList<>();
    SharedPreferences settingsPreferences;
    ViewHolder viewHolder = new ViewHolder();


    public CheckBoxAdapter(Context context, ArrayList<OfferCategory> categories) {
        this.context = context;
        this.categories = categories;
        this.settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }



    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return categories.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final OfferCategory offerCategory = categories.get(i);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.checkbox_list_item, null);

            viewHolder.checkBox = view.findViewById(R.id.chbox_category);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.checkBox.setText(offerCategory.getTitle());
        viewHolder.checkBox.setChecked(false);
        for (int j = 0; j < settingsPreferences.getInt("numberOfCheckedCategories",0); j++) {
            if (viewHolder.checkBox.getText().equals(settingsPreferences.getString("checkedCategoryTitle " + j, ""))) {
                viewHolder.checkBox.setChecked(true);
                break;
            }
        }


        return view;
    }

    private class ViewHolder{
        CheckBox checkBox;
    }
}

