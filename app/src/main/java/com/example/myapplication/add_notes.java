package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.Arrays;

public class add_notes extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;
    protected static String usr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        EditText dat = findViewById(R.id.newData);
        Button done = findViewById(R.id.done);

        SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        usr = getData.getString("user", "None");
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()){
                    Toast.makeText(add_notes.this, "This is an online data add", Toast.LENGTH_SHORT).show();
                    onlineDataAdd(usr, dat);
                }else{
                    Toast.makeText(add_notes.this, "This is an offline data add", Toast.LENGTH_SHORT).show();
                    offlineDataAdd(dat.getText().toString());
                }
            }
        });
    }

    private void offlineDataAdd(String data){
        SharedPreferences getData = getSharedPreferences("only_offline", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getData.edit();
        String members = getData.getString("members", "none");
        if ( (members.toString()).equals("none") ){
            editor.putString("members", "0");
            editor.putString("0", data);
            editor.commit();
        }else{
            int dat = Integer.parseInt(members);
            dat++;
            String dat1 = Integer.toString(dat);
            editor.putString("members", dat1);
            editor.putString(dat1, data);
            editor.commit();
            System.out.println(getData.getAll());
        }
        Intent intent = new Intent(add_notes.this, allNotes.class);
        startActivity(intent);
    }

    private void onlineDataAdd(String usr, EditText dat){
        JSONObject payload = new JSONObject();
        RequestQueue queue;
        try{
            payload.put("id", usr);
            payload.put("data", dat.getText().toString());
        }catch (Exception e){
            System.out.println("Error in payload"+e);
        }
        String url = "https://knote-app-api.herokuapp.com/insert-data";
        queue = Volley.newRequestQueue(add_notes.this);


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

    private boolean isConnected() {
        ConnectivityManager con = (ConnectivityManager) getApplication().getSystemService(this.CONNECTIVITY_SERVICE);
        return con.getActiveNetworkInfo() != null && con.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}