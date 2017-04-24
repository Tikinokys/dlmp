package com.tikinokys.dontletmeplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegActivity extends AppCompatActivity {
    SharedPreferences sPref;
    String token = "";

    final String USER_TOKEN = "user_token";

    EditText pass1;
    EditText pass2;
    EditText login1;
    EditText email1;


    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        Boolean status = false;

        String logintext;
        String passwordtext;
        String passwordrepeattext;
        String emailtext;
        String json_token = "";
        String resultJson = "";
        String emailError = null;
        String loginError = null;

        @Override
        protected void onPreExecute(){
            logintext = String.valueOf(((EditText) findViewById(R.id.LoginText)).getText());
            passwordtext = String.valueOf(((EditText) findViewById(R.id.PasswordText)).getText());
            passwordrepeattext = String.valueOf(((EditText) findViewById(R.id.PasswordRepeatText)).getText());
            emailtext = String.valueOf(((EditText) findViewById(R.id.EmailText)).getText());
        }
        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
                try{
                    URL url = new URL("https://dlmp.herokuapp.com/api/users/new/" + logintext + "/" + passwordtext + "/" + emailtext);

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
                    }else{
                        JSONArray loginErrorArray = dataJsonObj.getJSONObject("errors").getJSONArray("username");
                        if(loginErrorArray!=null) loginError = loginErrorArray.getString(0);
                        JSONArray emailErrorArray = dataJsonObj.getJSONObject("errors").getJSONArray("email");
                        if(emailErrorArray!=null) emailError = emailErrorArray.getString(0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            return resultJson;
        }
        protected void onPostExecute(String result){
            SharedPreferences.Editor ed = sPref.edit();

            if(loginError!=null) {
                login1.setError("Логин уже занят");
            }
            if(emailError!=null){
                if(emailError.equals("is not an email")){
                    email1.setError("Введите корректный Email");
                }else{
                    email1.setError("Email уже занят");
                }
            }

            ed.putString("user_token", json_token);
            token=json_token;
            toMainActivity();
        }
    }
    private void toMainActivity(){
        if(token.length()!=0) {
            Toast toast3 = Toast.makeText(RegActivity.this, "Учётная запись зарегистрирована", Toast.LENGTH_SHORT);
            toast3.show();
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("user_token", token);
            ed.commit();
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
        }
    }
    protected Boolean isNotEmpty(){
        if((((EditText) findViewById(R.id.LoginText)).getText()).length()!=0 && (((EditText) findViewById(R.id.PasswordText)).getText()).length()!=0 && (((EditText) findViewById(R.id.PasswordRepeatText)).getText()).length()!=0 && (((EditText) findViewById(R.id.EmailText)).getText()).length()!=0) {
            return true;
        }else{
            Toast toast = Toast.makeText(RegActivity.this, "Поля не должны быть пустыми", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
    }
    public boolean onCreateOptionsMenu (Menu menu){

        getMenuInflater().inflate(R.menu.menu_reg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("user_token", "");
        ed.commit();
        Intent intent = new Intent (this, LoginActivity.class);
        startActivity(intent);
        return true;
    }

    protected Boolean passwordMatch(){

        if(String.valueOf(pass1.getText()).equals(String.valueOf(pass2.getText()))){
            return true;
        }else{
            if(isNotEmpty()){
                pass1.setError("Пароли не совпадают");
                pass2.setError("Пароли не совпадают");
            }
            return false;
        }
    }

    protected Boolean passwordLength(){
        if((String.valueOf(pass1.getText()).length()>=3 &&(String.valueOf(pass1.getText()).length()<=32))){
            return true;
        }else{
            pass1.setError("Длина пароля от 3 до 32 символов");
            return false;
        }
    }

    protected Boolean loginLength(){
        if((String.valueOf(login1.getText()).length()>=3 &&(String.valueOf(login1.getText()).length()<=12))){
            return true;
        }else{
            login1.setError("Длина логина от 3 до 12 символов");
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        sPref = getSharedPreferences("MyPref", MODE_PRIVATE);

        Button btn1 = (Button) findViewById(R.id.RegButton);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass1 = (EditText) findViewById(R.id.PasswordText);
                pass2 = (EditText) findViewById(R.id.PasswordRepeatText);
                login1 = (EditText) findViewById(R.id.LoginText);
                email1 = (EditText) findViewById(R.id.EmailText);
                if (isNotEmpty() && passwordMatch() && loginLength()&& passwordLength()){
                    new ParseTask().execute();
                }
            }
        });
    }
}