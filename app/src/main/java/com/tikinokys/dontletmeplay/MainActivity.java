package com.tikinokys.dontletmeplay;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sPref;
    String token = "";

    FriendListAdapter<BlockedFriend> adapter = new FriendListAdapter<BlockedFriend>();
    BlockedFriend[] arrayOfFriends;


    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;
        String json_login;
        String json_email;

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

        int friendsLength;

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
                    arrayOfFriends = new BlockedFriend[friendsLength];
                    JSONArray arrayOfFriendsJSON = data.getJSONArray("friends");

                    for (int i=0;i<friendsLength;i++){
                        JSONObject currentFriendJSON = (JSONObject) arrayOfFriendsJSON.get(i);
                        arrayOfFriends[i] = new BlockedFriend(currentFriendJSON.getLong("block_expires"), currentFriendJSON.getLong("block_starts"), currentFriendJSON.getString("username"));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        protected void onPostExecute(String result){
            if(status){

            }else{
            }
        }
    }

    private void fillIn(){
        adapter = new FriendListAdapter<BlockedFriend>(this, BlockedFriend.class, R.layout.item, arrayOfFriends.length());
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

        if(sPref.getString("user_login", "").equals("")){
            new ParseTask().execute();
        }else{
            setTitle(sPref.getString("user_login", ""));
        }
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
