package com.tikinokys.dontletmeplay;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sPref;
    String token = "";

    FriendListAdapter friendListAdapter;
    BlockedFriend[] blockedFriendsArray;

    ListView friendList;

    int friendsLength;

    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;
        String json_login;
        String json_email;


        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/getInfo?token=" + token);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

                    dataJsonObj = new JSONObject(resultJson);
                    status = dataJsonObj.getBoolean("status");
                    if(status) {
                    JSONObject data = dataJsonObj.getJSONObject("data");
                    json_login = data.getString("username");
                    json_email = data.getString("email");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
            if(status){
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("user_login", json_login);
                ed.putString("user_email", json_email);
                ed.commit();
                setTitle(json_login);
            }else{
                toLoginActivity();
            }
        }
    }

    private class ParseTask1 extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/getBlockedFriends?token=" + token);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

                dataJsonObj = new JSONObject(resultJson);
                status = dataJsonObj.getBoolean("status");
                if(status) {
                    JSONObject data = dataJsonObj.getJSONObject("data");
                    friendsLength = data.getInt("friendsLength");
                    JSONArray arrayOfFriendsJSON = data.getJSONArray("friends");
                    blockedFriendsArray = new BlockedFriend[friendsLength];
                    if(friendsLength>0) {
                        for (int i = 0; i < friendsLength; i++) {
                            JSONObject currentFriendJSON = (JSONObject) arrayOfFriendsJSON.get(i);
                            long a = currentFriendJSON.getLong("block_expires")*1000;
                            Timestamp stamp = new Timestamp(a);
                            Date date = new Date(stamp.getTime());
                            String s = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date));
                            blockedFriendsArray[i] = new BlockedFriend(currentFriendJSON.getString("username"), s);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
            if(status){
                initFillIn();
            }
        }
    }

    public ArrayList<BlockedFriend> fillIn(){
        ArrayList<BlockedFriend> items = new ArrayList<BlockedFriend>();
        for(int i =0; i<blockedFriendsArray.length;i++){
            items.add(blockedFriendsArray[i]);
        }
        return items;
    }
    public void initFillIn(){
        friendListAdapter = new FriendListAdapter(this, fillIn());
        friendList.setAdapter(friendListAdapter);
    }

    private void toLoginActivity(){
        Intent intent = new Intent (this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
        finish();
        System.runFinalizersOnExit(true);
        System.exit(0);
    }

    public void onDestroy() {
        moveTaskToBack(true);

        super.onDestroy();

        System.runFinalizersOnExit(true);
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        token = sPref.getString("user_token", "");
        friendList = (ListView) findViewById(R.id.friendList);

        if(sPref.getString("user_login", "").equals("")){
            new ParseTask().execute();
        }else{
            setTitle(sPref.getString("user_login", ""));
        }

        friendList.setAdapter(friendListAdapter);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, UnblockActivity.class);
                intent.putExtra("friendName", blockedFriendsArray[i].getLogin());
                startActivity(intent);
            }
        });

        new ParseTask1().execute();
    }

    public boolean onCreateOptionsMenu (Menu menu){

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.action_block:
                Intent intent1 = new Intent (this, BlockActivity.class);
                startActivity(intent1);
                break;
            case R.id.action_logout:
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("user_token", "");
                ed.putString("user_login", "");
                ed.putString("user_email", "");
                ed.commit();
                Intent intent = new Intent (this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
    private final NetworkStateReceiver stateReceiver = new NetworkStateReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(stateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stateReceiver);
    }
}
