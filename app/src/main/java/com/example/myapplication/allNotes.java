package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.databinding.ActivityAllNotesBinding;

import org.json.JSONArray;
import org.json.JSONObject;

public class allNotes extends AppCompatActivity {

    //VARIABLES
    private AppBarConfiguration appBarConfiguration;
    public static final String MyPREFERENCES = "MyPrefs" ;
    Button logout, add_notes;
    String link, usr;
    JSONArray jarr = new JSONArray();
    String c[] = new String[]{"#cfccfc", "#ccfccf", "#ffcffc"};
    int rot[] = new int[]{3,-3,1,-2,1,-2};
    //====================================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);

        //GETTING NOTES
        link = "https://knote-app-api.herokuapp.com/get-notes";
        SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        usr = getData.getString("user", "None");

        JSONObject payload = new JSONObject();
        try {
            payload.put("id", usr);
        } catch (Exception e) {
            System.out.println(e);
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, link, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                    jarr = response.getJSONArray("data");



                    for(int i=0;i<jarr.length();i++){
                        ScrollView sv = new ScrollView(allNotes.this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
                        sv.setLayoutParams(layoutParams);

                        LinearLayout.LayoutParams ex = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        ex.setMargins(60, 40,60, 0);

                        TextView value = new TextView(allNotes.this);

                        value.setText(jarr.getJSONObject(i).getString("data"));
                        value.setTextSize(30);

                        value.setPadding(10,10,10,10);
                        value.setBackgroundColor(Color.parseColor(c[i%3]));

                        Typeface tf =  ResourcesCompat.getFont(allNotes.this, R.font.nothing);
                        value.setTypeface(tf);

                        value.setRotation(rot[i%6]);
                        value.setLayoutParams(ex);
                        value.setElevation(10);
                        value.setMinHeight(800);
                        sv.addView(value);
                        ll.addView(sv);
                    }
                }
                catch (Exception e){
                    System.out.println(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
        //====================================================


        //LOGOUT LOGIC
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REMOVING THE user key
                SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = getData.edit();
                editor.remove("user");
                editor.apply();

                //Redirecting to login page.
                Intent intent = new Intent(allNotes.this, MainActivity.class);
                startActivity(intent);
            }
        });
        //==============================================================

        //ADD_NOTES
        add_notes = findViewById(R.id.add_notes);
        add_notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(allNotes.this, add_notes.class);
                startActivity(intent);
            }
        });
        //==============================================================
    }
}