package com.tikinokys.dontletmeplay;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BlockActivity extends AppCompatActivity {

    Calendar c;
    int year;
    int month;
    int day;
    int hour;
    int minute1;
    EditText txtTime;
    EditText txtdate;
    EditText txtloginfriend;
    Date date;
    Boolean isBlocked = false;
    Button btn;
    int daysToUnblocked;
    int hoursToUnblocked;
    int minutesToUnblocked;

    TextView txtTimeLeftLabel;
    TextView txtTimeLeft;
    TextView txtAttention;
    TextView txtFriendName;
    String friendName;
    TextView txtLoginFr;

    SharedPreferences sPref;
    String token = "";

    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        String friendName;
        String unlockingdate;
        Boolean status = false;
        Boolean hasInternetConnection = true;
        JSONObject errors = null;
        JSONObject data = null;
        Boolean JSONBlock = false;
        Boolean JSONFriendName = false;

        @Override
        protected void onPreExecute(){
            friendName = String.valueOf(((EditText) findViewById(R.id.loginFriend)).getText());
            friendName = (friendName.equals(""))?"ttt":friendName;
            unlockingdate = String.valueOf((long) date.getTime()/1000);
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
                try {
                    URL url = new URL("https://dlmp.herokuapp.com/api/users/block/" + friendName + "/" + unlockingdate + "?token=" + token);

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

                    if(status){
                        data = dataJsonObj.getJSONObject("data");
                        daysToUnblocked = data.getInt("days");
                        hoursToUnblocked = data.getInt("hours");
                        minutesToUnblocked = data.getInt("minutes");
                    }else{
                        errors = dataJsonObj.getJSONObject("errors");
                        JSONFriendName = errors.getBoolean("not_your_name");
                        JSONBlock = errors.getBoolean("not_much_time");
                    }
                } catch (Exception e) {
                    hasInternetConnection = false;
                    e.printStackTrace();
                }
            return resultJson;
        }

        protected void onPostExecute(String result){
            if(!status){
                if(JSONBlock){
                    Toast toast = Toast.makeText(BlockActivity.this, "Минимальное время блокировки 30 минут", Toast.LENGTH_SHORT);
                    toast.show();
                }else if(JSONFriendName){
                    txtloginfriend.setError("Нельзя указать себя в качестве друга");
                }else {
                    Toast toast = Toast.makeText(BlockActivity.this, "Логин друга введён неверно", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }else {
                accBlocked();
                Toast toast = Toast.makeText(BlockActivity.this, "Успешно заблокировано", Toast.LENGTH_SHORT);
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
                    if(data.getBoolean("blocked")){
                        isBlocked = true;
                        daysToUnblocked = data.getInt("days");
                        hoursToUnblocked = data.getInt("hours");
                        minutesToUnblocked = data.getInt("minutes");
                        friendName = data.getString("friend");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        protected void onPostExecute(String result){
            if(status && isBlocked){
                accBlocked();
                txtLoginFr.setText("Логин друга: ");
                txtFriendName.setText(friendName);
            }else{
                txtAttention.setText("Внимание! \nНажимая на эту кнопку вы блокируете все игровые приложения на вашем компьютере. Они будут разблокированы по истечении указанного срока. Человек, чей логин указан в поле \"Логин друга\" получит возможность досрочно отключить блокировку.");
            }
        }
    }

    public void accBlocked(){
        btn.setEnabled(false);
        txtAttention.setText("Кнопка \"Заблокировать\" будет не доступна пока активна текущая блокировка.");
        txtTimeLeftLabel.setText("До конца блокировки осталось:");
        txtTimeLeft.setText(daysToUnblocked + "д. " + hoursToUnblocked + "ч. " + minutesToUnblocked + "м. ");
    }

    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_block, menu);
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
        setContentView(R.layout.activity_block);

        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        token = sPref.getString("user_token", "");

        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute1 = c.get(Calendar.MINUTE);
        txtTime=(EditText) findViewById(R.id.edtTime);
        txtdate=(EditText) findViewById(R.id.edtDate);
        txtloginfriend=(EditText) findViewById(R.id.loginFriend);

        txtTimeLeftLabel = (TextView) findViewById(R.id.txtTimeLeftLabel);
        txtTimeLeft = (TextView) findViewById(R.id.txtTimeLeft);
        txtAttention = (TextView) findViewById(R.id.txtAttention);

        new BlockActivity.ParseTask1().execute();

        txtTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                TimePickerDialog timepick=new TimePickerDialog(BlockActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        txtTime.setText(((hourOfDay<10)?"0":"") + hourOfDay + ":" + ((minute<10)?"0":"") + minute);
                    }
                },hour,minute1,true);
                timepick.setTitle("Выберите время");
                timepick.show();
            }
        });

        txtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datepickter = new DatePickerDialog(BlockActivity.this, new DatePickerDialog.OnDateSetListener(){

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        txtdate.setText(((dayOfMonth<10)?"0":"") + dayOfMonth + "." + ((monthOfYear+1<10)?"0":"") + (monthOfYear+1) + "." + year);
                    }
                },year,month,day);
                datepickter.setTitle("Выберите дату");
                datepickter.show();
            }
        });

        txtFriendName = (TextView) findViewById(R.id.txtFriendName);
        txtLoginFr = (TextView) findViewById(R.id.txtLoginFr);
        btn = (Button) findViewById(R.id.BlockBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(txtdate.getText()).equals("") || String.valueOf(txtTime.getText()).equals("")){
                    Toast toast = Toast.makeText(BlockActivity.this, "Выберите дату и время разблокировки", Toast.LENGTH_SHORT);
                    toast.show();
                }else {
                    try {
                        String datetime = String.valueOf(txtdate.getText()) + " " + String.valueOf(txtTime.getText());
                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        date = dateFormat.parse(datetime);
                        new BlockActivity.ParseTask().execute();

                        txtLoginFr.setText("Логин друга: ");
                        txtFriendName.setText(txtloginfriend.getText());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast toast1 = Toast.makeText(BlockActivity.this, "Введите корректные дату и время", Toast.LENGTH_SHORT);
                        toast1.show();
                    }
                }
            }
        });

    }
}
