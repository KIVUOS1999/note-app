package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

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

public class CreateAccount extends AppCompatActivity {

    Button create_account;
    EditText username, password, confirm_password;
    TextView logingo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        create_account = findViewById(R.id.create_account);

        String url = "https://knote-app-api.herokuapp.com/add-user";
        RequestQueue queue = Volley.newRequestQueue(CreateAccount.this);

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username_value = username.getText().toString();
                String password_value = password.getText().toString();
                String confirm_password_value = confirm_password.getText().toString();

                if(password_value.equals(confirm_password_value)){
                    JSONObject payload = new JSONObject();
                    try{
                        payload.put("id", username_value);
                        payload.put("pass", password_value);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(CreateAccount.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                            if(response.optString("msg").equals("User added")){
                                Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                    queue.add(jsonObjectRequest);
                }else{
                    Toast.makeText(CreateAccount.this, "password and confirm password does not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
        logingo = findViewById(R.id.login_account_link);
        logingo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}