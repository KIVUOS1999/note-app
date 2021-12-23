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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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

        //edit notes
        View.OnClickListener edi = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout container = findViewById(R.id.container);
                container.setVisibility(view.INVISIBLE);

                LinearLayout edit_text = findViewById(R.id.edit_text);
                edit_text.setVisibility(view.VISIBLE);

                findViewById(R.id.edit_text_done).setTag(view.getTag());

                LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                TextView tb = ll.findViewWithTag("Parent"+view.getTag()).findViewWithTag("TextBox"+view.getTag());

                ((EditText)findViewById(R.id.edit_text_data)).setText(tb.getText().toString());
            }
        };
        //=============================================================


        //delete notes
        View.OnClickListener del = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(allNotes.this);
                String data = view.getTag().toString();
                String url = "https://knote-app-api.herokuapp.com/delete-note";
                JSONObject payload = new JSONObject();
                try{
                    payload.put("id", data);
                }catch(Exception e){
                    System.out.println("Error in payload");
                }

                JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(allNotes.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                        LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                        ll.removeView(ll.findViewWithTag("Parent"+view.getTag().toString()));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                queue.add(jor);
            }
        };
        //==========================================================


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
                        LinearLayout parent = new LinearLayout(allNotes.this);
                        parent.setTag("Parent"+(jarr.getJSONObject(i).getString("_id")).toString());
                        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        parent.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout controls = new LinearLayout(allNotes.this);
                        LinearLayout.LayoutParams controlsparam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        controlsparam.setMargins(30,60,0,150);



                        controls.setLayoutParams(controlsparam);
                        controls.setOrientation(LinearLayout.HORIZONTAL);

                        Button edit = new Button(allNotes.this);
                        edit.setTag(jarr.getJSONObject(i).getString("_id"));
                        edit.setOnClickListener(edi);
                        edit.setBackgroundResource(R.drawable.edit);
                        edit.setLayoutParams(new LinearLayout.LayoutParams(110, 110));

                        Button delete = new Button(allNotes.this);
                        delete.setTag(jarr.getJSONObject(i).getString("_id"));
                        delete.setOnClickListener(del);
                        delete.setBackgroundResource(R.drawable.wrong);
                        LinearLayout.LayoutParams deleteparams = new LinearLayout.LayoutParams(110, 110);
                        deleteparams.setMargins(50, 0,0,0);
                        delete.setLayoutParams(deleteparams);
                        controls.addView(edit);
                        controls.addView(delete);

                        LinearLayout.LayoutParams ex = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        ex.setMargins(60, 40,60, 0);

                        TextView value = new TextView(allNotes.this);

                        value.setText(jarr.getJSONObject(i).getString("data"));
                        value.setTextSize(30);
                        value.setTag("TextBox"+(jarr.getJSONObject(i).getString("_id")).toString());
                        value.setPadding(10,10,10,10);
                        value.setBackgroundColor(Color.parseColor(c[i%3]));

                        Typeface tf =  ResourcesCompat.getFont(allNotes.this, R.font.nothing);
                        value.setTypeface(tf);

                        value.setRotation(rot[i%6]);
                        value.setLayoutParams(ex);
                        value.setElevation(10);
                        value.setMinHeight(800);

                        parent.addView(value);
                        parent.addView(controls);

                        ll.addView(parent);
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
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.add_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(allNotes.this, add_notes.class);
                startActivity(intent);
            }
        });
        //==============================================================

        //edit_text_cancel
        findViewById(R.id.edit_text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout container = findViewById(R.id.container);
                container.setVisibility(view.VISIBLE);

                LinearLayout edit_text = findViewById(R.id.edit_text);
                edit_text.setVisibility(view.INVISIBLE);
            }
        });
        //==============================================================

        //edit_text_done
        findViewById(R.id.edit_text_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject payload = new JSONObject();
                String url = "https://knote-app-api.herokuapp.com/update-particular-notes";
                String data = ((EditText)findViewById(R.id.edit_text_data)).getText().toString();
                String id = view.getTag().toString();

                try{
                    payload.put("id", id);
                    payload.put("data", data);
                    LinearLayout container = findViewById(R.id.container);
                    container.setVisibility(view.VISIBLE);

                    LinearLayout edit_text = findViewById(R.id.edit_text);
                    edit_text.setVisibility(view.INVISIBLE);
                }catch(Exception e){
                    e.printStackTrace();
                }

                RequestQueue queue = Volley.newRequestQueue(allNotes.this);
                JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                        TextView tb = ll.findViewWithTag("Parent"+view.getTag()).findViewWithTag("TextBox"+view.getTag());
                        tb.setText(data);
                        Toast.makeText(allNotes.this, response.optString("data"), Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(allNotes.this, "some error occoured", Toast.LENGTH_SHORT).show();
                        System.out.println(error);
                    }
                });
                queue.add(jor);
            }
        });

        //==============================================================
    }
}