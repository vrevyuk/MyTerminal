package com.revyuk.myterminal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.revyuk.myterminal.R;
import com.revyuk.myterminal.model.Prediction;

import java.util.List;

/**
 * Created by Vitaly on 23.05.2015.
 */
public class AutocompleteAdapter extends ArrayAdapter<Prediction> {
    Holder holder = new Holder();
    int resource;

    public AutocompleteAdapter(Context context, int resource, List<Prediction> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            holder.text1 = (TextView) view.findViewById(R.id.autocomplete_text1);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.text1.setText(getItem(position).getDescription());
        return view;
    }

    private class Holder {
        TextView text1;
    }
}
