package com.revyuk.myterminal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.revyuk.myterminal.R;
import com.revyuk.myterminal.model.ResultList;

/**
 * Created by Notebook on 10.04.2015.
 */
public class TerminalAdapter extends ArrayAdapter<ResultList> {
    Context context;
    int resource;

    public TerminalAdapter(Context context, int resource, ResultList[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if(v == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resource, parent, false);
            holder.provider = (TextView) v.findViewById(R.id.provider);
            holder.city = (TextView) v.findViewById(R.id.city);
            holder.address = (TextView) v.findViewById(R.id.address);
            holder.location = (TextView) v.findViewById(R.id.location);
            v.setTag(holder);
        } else { holder = (ViewHolder) v.getTag(); }
        holder.provider.setText(getItem(position).provider);
        holder.city.setText(getItem(position).city);
        holder.location.setText(getItem(position).location);
        holder.address.setText(getItem(position).street + " " + getItem(position).build);
        return v;
    }

    class ViewHolder {
        TextView provider;
        TextView city;
        TextView address;
        TextView location;
    }
}
