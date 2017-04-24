package com.tikinokys.dontletmeplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendListAdapter extends BaseAdapter {

    private Context context;
    ArrayList<BlockedFriend> data = new ArrayList<BlockedFriend>();

    public FriendListAdapter(Context context, ArrayList<BlockedFriend> arr){
        if(arr!=null){
            data = arr;
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View someView, ViewGroup arg2) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(someView==null){
            someView = inflater.inflate(R.layout.item, arg2, false);
        }
        TextView login = (TextView) someView.findViewById(R.id.txtLoginFriend);
        TextView unblockingDate = (TextView) someView.findViewById(R.id.txtDateUnblock);

        login.setText(data.get(i).getLogin());
        unblockingDate.setText(data.get(i).getUnBlockDate());

        return someView;
    }
}
