package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    //SAVING LOCALLY
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    //
    Button authenticate;
    EditText username, password;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GETTING SHARED DATA
        SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String Value = getData.getString("user", "None");

        if(Value == "None") {
            authenticate = findViewById(R.id.authenticate);
            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
            queue = Volley.newRequestQueue(MainActivity.this);

            authenticate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                    String username_value = username.getText().toString();
                    String password_value = password.getText().toString();
                    Toast.makeText(MainActivity.this, username_value, Toast.LENGTH_SHORT).show();

                    //POST REQUEST
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    String url = "https://knote-app-api.herokuapp.com/authenticate";

                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("id", username_value);
                        payload.put("pass", password_value);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(response.optString("msg").equals("Login successfully")){
                                //SAVING THE USERDATA
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("user", response.optString("id"));
                                editor.apply();
                                Intent intent = new Intent(MainActivity.this, allNotes.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(MainActivity.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                    queue.add(jsonObjectRequest);
                }
            });
        }else{
            Intent intent = new Intent(this, allNotes.class);
            startActivity(intent);
        }


        TextView createaccount = findViewById(R.id.create_account_link);
        createaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
            }
        });
    }
}