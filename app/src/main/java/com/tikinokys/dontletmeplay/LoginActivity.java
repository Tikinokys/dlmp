package com.tikinokys.dontletmeplay;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sPref;
    String token = "";

    final String USER_TOKEN = "user_token";

    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        String logintxt;
        String passwordtxt;
        Boolean status = false;
        Boolean isEmpty = false;
        String json_token;
        Boolean hasInternetConnection = true;


        @Override
        protected void onPreExecute(){
            logintxt = String.valueOf(((EditText) findViewById(R.id.LoginTxt)).getText());
            passwordtxt = String.valueOf(((EditText) findViewById(R.id.PasswordTxt)).getText());
        }
        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            if(logintxt.length()!=0 && passwordtxt.length()!=0) {
                try {
                    URL url = new URL("https://dlmp.herokuapp.com/api/users/getToken/" + logintxt + "/" + passwordtxt);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();

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
                        json_token = data.getString("token");
                    }

                } catch (Exception e) {
                    hasInternetConnection = false;
                    e.printStackTrace();
                }
            }else{
                isEmpty = true;
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
/*            textview.setText(USER_LOGIN);*/

            sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            if(!hasInternetConnection){
                Toast toast = Toast.makeText(LoginActivity.this,"Отсутствует подключение к интернету", Toast.LENGTH_SHORT);
                toast.show();
            }else if(isEmpty){
                Toast toast = Toast.makeText(LoginActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT);
                toast.show();
                ed.putString("user_token", "");
                token="";
            }else if (!status){
                Toast toast = Toast.makeText(LoginActivity.this, "Неправильные логин или пароль", Toast.LENGTH_SHORT);
                toast.show();
                ed.putString("user_token", "");
                token="";
            }else{
                ed.putString("user_token", json_token);
                token = json_token;
                toMainActivity();
            }
            ed.commit();

        }

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

    private void toMainActivity(){
        if(token.length()!=0) {
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
        }
    }
    private void toRegActivity(){
            Intent intent2 = new Intent (this, RegActivity.class);
            startActivity(intent2);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        token = sPref.getString(USER_TOKEN, "");

        toMainActivity();

        Button btn1 = (Button) findViewById(R.id.SignInBtn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ParseTask().execute();
            }
        });

        Button btn2 = (Button) findViewById(R.id.RegBtn);
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                toRegActivity();
            }
        });
    }

}
