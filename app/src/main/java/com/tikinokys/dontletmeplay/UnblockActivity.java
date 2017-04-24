package com.tikinokys.dontletmeplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;

public class UnblockActivity extends AppCompatActivity {
    final String USER_TOKEN = "user_token";

    SharedPreferences sPref;
    String token = "";
    String loginFriend;

    TextView txtBeginBlock;
    TextView txtEndBlock;
    TextView txtTimeLeft;

    String blockExp;
    String blockStr;

    long blockExpires;
    long blockStarts;

    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;

        long timeDim;


        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/getFriendInfo/" + loginFriend + "?token=" + token);

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

                    timeDim = data.getLong("time_dim");
                    blockExpires = data.getLong("block_expires")*1000;
                    blockStarts = data.getLong("block_starts")*1000;

                    Timestamp stamp1 = new Timestamp(blockExpires);
                    Date date1 = new Date(stamp1.getTime());
                    blockExp = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date1));

                    Timestamp stamp2 = new Timestamp(blockStarts);
                    Date date2 = new Date(stamp2.getTime());
                    blockStr = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date2));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
            if(status){
                txtBeginBlock.setText(blockStr);
                txtEndBlock.setText(blockExp);

                long minsUb = (timeDim/60)%60;
                long hoursUb = (timeDim/3600)%24;
                long daysUb = (timeDim/86400);

                txtTimeLeft.setText(String.valueOf(daysUb) + "д. " + String.valueOf(hoursUb) + "ч. " + String.valueOf(minsUb) + "м.");

            }else{
                Toast toast = Toast.makeText(UnblockActivity.this,"Не удалось получить информацию о пользователе, обновите страницу", Toast.LENGTH_LONG);
                toast.show();
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

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/unblock/" + loginFriend + "?token=" + token);

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


            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status) {
                Toast toast = Toast.makeText(UnblockActivity.this, "Пользователь разблокирован", Toast.LENGTH_SHORT);
                toast.show();
                toMainActivity();
            }
        }
    }

    private void toMainActivity(){
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
    }

    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_unblock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unblock);

        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        token = sPref.getString(USER_TOKEN, "");

        Bundle bundle = getIntent().getExtras();
        loginFriend = bundle.getString("friendName");

        TextView txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtLogin.setText(loginFriend);

        txtBeginBlock = (TextView) findViewById(R.id.txtBeginBlock);
        txtEndBlock = (TextView) findViewById(R.id.txtEndBlock);
        txtTimeLeft = (TextView) findViewById(R.id.txtTimeLeft);

        new ParseTask().execute();

        Button btn = (Button) findViewById(R.id.btnUnblock);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ParseTask1().execute();
            }
        });

    }
}
