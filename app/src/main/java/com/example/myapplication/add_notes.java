package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class add_notes extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        EditText dat = findViewById(R.id.newData);
        Button done = findViewById(R.id.done);

        SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String usr = getData.getString("user", "None");

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject payload = new JSONObject();
                try{
                    payload.put("id", usr);
                    payload.put("data", dat.getText().toString());
                }catch (Exception e){
                    System.out.println("Error in payload"+e);
                }
                String url = "https://knote-app-api.herokuapp.com/insert-data";

                RequestQueue queue = Volley.newRequestQueue(add_notes.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(add_notes.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                        if(response.optString("msg").equals("data added")){
                            Intent intent = new Intent(add_notes.this, allNotes.class);
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
            }
        });

    }
}