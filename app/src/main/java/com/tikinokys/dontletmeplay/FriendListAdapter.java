package com.tikinokys.dontletmeplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter<BlockedFriend> {

    private final Context context;
    private final ArrayList<BlockedFriend> itemsArrayList;

    public FriendListAdapter(Context context, ArrayList<BlockedFriend> itemsArrayList) {

        super(context, R.layout.item, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.item, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.txtLoginFriend);
        TextView valueView = (TextView) rowView.findViewById(R.id.txtDateUnblock);

        // 4. Set the text for textView
        labelView.setText(itemsArrayList.get(position).getLogin());
        valueView.setText(itemsArrayList.get(position).getUnBlockDate());

        // 5. retrn rowView
        return rowView;
    }
}
