package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class allNotes extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    //VARIABLES
    private AppBarConfiguration appBarConfiguration;
    public static final String MyPREFERENCES = "MyPrefs";
    String link, usr;
    JSONArray jarr = new JSONArray();

    String c[] = new String[]{"#cfccfc", "#ccfccf", "#ffcffc"};
    int rot[] = new int[]{3, -3, 1, -2, 1, -2};
    //====================================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);

        //refresh
        SwipeRefreshLayout srl = findViewById(R.id.swipeToRefresh);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isConnected()){
                    LinearLayout parent = findViewById(R.id.data);
                    parent.removeAllViews();
                    getPage();
                }
                else{
                    Toast.makeText(allNotes.this, "You are still offline", Toast.LENGTH_SHORT).show();
                }
                srl.setRefreshing(false);
            }
        });
        //==========================================================

        //CREATING NOTES
        if (isConnected()) {
            getPage();

        } else {
            Map[] arr = getAllOfflineData();
            System.out.println("------------------->\n"+arr);
            for(int i=0; i < arr.length; i++){
                if(i == 0){
                    getOfflinePage(arr[i], false);
                }
                else{
                    getOfflinePage(arr[i], true);
                }
            }

        }
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
                if(isConnected()) {
                    JSONObject payload = new JSONObject();
                    String url = "https://knote-app-api.herokuapp.com/update-particular-notes";
                    String data = ((EditText) findViewById(R.id.edit_text_data)).getText().toString();
                    SharedPreferences sharedpreferences = getSharedPreferences("savedData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    String id = view.getTag().toString();
                    editor.putString(id, data);
                    editor.commit();

                    try {
                        payload.put("id", id);
                        payload.put("data", data);
                        LinearLayout container = findViewById(R.id.container);
                        container.setVisibility(view.VISIBLE);

                        LinearLayout edit_text = findViewById(R.id.edit_text);
                        edit_text.setVisibility(view.INVISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RequestQueue queue = Volley.newRequestQueue(allNotes.this);
                    JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                            TextView tb = ll.findViewWithTag("Parent" + view.getTag()).findViewWithTag("TextBox" + view.getTag());
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
                else{
                    Toast.makeText(allNotes.this, "offline data edit", Toast.LENGTH_SHORT).show();

                    String id = view.getTag().toString();
                    String data = ((EditText) findViewById(R.id.edit_text_data)).getText().toString();

                    SharedPreferences dat = getSharedPreferences("only_offline", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = dat.edit();
                    editor.putString(id, data);
                    editor.commit();

                    LinearLayout ll = findViewById(R.id.data);
                    TextView tb = ll.findViewWithTag("Parent" + view.getTag()).findViewWithTag("TextBox" + view.getTag());
                    tb.setText(data);

                    LinearLayout container = findViewById(R.id.container);
                    container.setVisibility(view.VISIBLE);

                    LinearLayout edit_text = findViewById(R.id.edit_text);
                    edit_text.setVisibility(view.INVISIBLE);
                }
            }
        });

        //==============================================================
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
    }

    private void getPage() {
        offlineDataUpload();
        View.OnClickListener edi = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout container = findViewById(R.id.container);
                container.setVisibility(view.INVISIBLE);

                LinearLayout edit_text = findViewById(R.id.edit_text);
                edit_text.setVisibility(view.VISIBLE);

                findViewById(R.id.edit_text_done).setTag(view.getTag());

                LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                TextView tb = ll.findViewWithTag("Parent" + view.getTag()).findViewWithTag("TextBox" + view.getTag());

                ((EditText) findViewById(R.id.edit_text_data)).setText(tb.getText().toString());
            }
        };
        View.OnClickListener del = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(allNotes.this);
                String data = view.getTag().toString();
                String url = "https://knote-app-api.herokuapp.com/delete-note";
                JSONObject payload = new JSONObject();
                try {
                    payload.put("id", data);
                    SharedPreferences sharedpreferences = getSharedPreferences("savedData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.remove(data);
                    editor.commit();

                } catch (Exception e) {
                    System.out.println("Error in payload");
                }

                JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(allNotes.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                        LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                        ll.removeView(ll.findViewWithTag("Parent" + view.getTag().toString()));
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
                try {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                    jarr = response.getJSONArray("data");
                    deleteAllDataFromOfflineTable();

                    for (int i = 0; i < jarr.length(); i++) {
                        String data_id = jarr.getJSONObject(i).getString("_id");
                        String data_data = jarr.getJSONObject(i).getString("data");

                        addDataToOfflineTable(data_id, data_data);

                        LinearLayout parent = new LinearLayout(allNotes.this);

                        parent.setTag("Parent" + (data_id).toString());
                        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        parent.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout controls = new LinearLayout(allNotes.this);
                        LinearLayout.LayoutParams controlsparam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        controlsparam.setMargins(30, 60, 0, 150);

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
                        deleteparams.setMargins(50, 0, 0, 0);
                        delete.setLayoutParams(deleteparams);
                        controls.addView(edit);
                        controls.addView(delete);

                        LinearLayout.LayoutParams ex = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        ex.setMargins(60, 40, 60, 0);

                        TextView value = new TextView(allNotes.this);

                        value.setText(data_data);
                        value.setTextSize(30);
                        value.setTag("TextBox" + (jarr.getJSONObject(i).getString("_id")).toString());
                        value.setPadding(10, 10, 10, 10);
                        value.setBackgroundColor(Color.parseColor(c[i % 3]));

                        Typeface tf = ResourcesCompat.getFont(allNotes.this, R.font.nothing);
                        value.setTypeface(tf);

                        value.setRotation(rot[i % 6]);
                        value.setLayoutParams(ex);
                        value.setElevation(10);
                        value.setMinHeight(800);

                        parent.addView(value);
                        parent.addView(controls);

                        ll.addView(parent);
                    }
                } catch (Exception e) {
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
    }

    private Map[] getAllOfflineData() {
        SharedPreferences sharedPreferences = getSharedPreferences("savedData", Context.MODE_PRIVATE);
        SharedPreferences dat = getSharedPreferences("only_offline", Context.MODE_PRIVATE);

        Map<String,?> map2;
        Map<String, ?> map;

        map = sharedPreferences.getAll();
        map2 = dat.getAll();

        return new Map[]{map, map2};
    }

    private void addDataToOfflineTable(String id, String data) {
        SharedPreferences sharedpreferences = getSharedPreferences("savedData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(id, data);
        editor.commit();
    }

    private void deleteAllDataFromOfflineTable() {
        SharedPreferences sharedpreferences = getSharedPreferences("savedData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }

    private boolean isConnected() {
        ConnectivityManager con = (ConnectivityManager) getApplication().getSystemService(this.CONNECTIVITY_SERVICE);
        return con.getActiveNetworkInfo() != null && con.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void getOfflinePage(Map<String, ?> dat, boolean b) {
        ArrayList<String> keys = new ArrayList<>(dat.keySet());
        String data_id, data_data;

        View.OnClickListener edi = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout container = findViewById(R.id.container);
                container.setVisibility(view.INVISIBLE);

                LinearLayout edit_text = findViewById(R.id.edit_text);
                edit_text.setVisibility(view.VISIBLE);

                findViewById(R.id.edit_text_done).setTag(view.getTag());

                LinearLayout ll = findViewById(R.id.data);
                TextView tb = ll.findViewWithTag("Parent" + view.getTag()).findViewWithTag("TextBox" + view.getTag());

                ((EditText) findViewById(R.id.edit_text_data)).setText(tb.getText().toString());
            }
        };
        View.OnClickListener del = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = view.getTag().toString();
                SharedPreferences dat = getSharedPreferences("only_offline", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = dat.edit();
                editor.remove(data);
                editor.commit();
                LinearLayout ll = (LinearLayout) findViewById(R.id.data);
                ll.removeView(ll.findViewWithTag("Parent" + view.getTag().toString()));
            }
        };

        LinearLayout ll = findViewById(R.id.data);
        for (int i=keys.size()-1; i>=0; i--) {
            data_id = keys.get(i);
            if(data_id.equals("members")){
                continue;
            }
            data_data = dat.get(keys.get(i)).toString();

            LinearLayout parent = new LinearLayout(allNotes.this);

            parent.setTag("Parent" + (data_id).toString());
            parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parent.setOrientation(LinearLayout.VERTICAL);

            LinearLayout controls = new LinearLayout(allNotes.this);
            LinearLayout.LayoutParams controlsparam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            controlsparam.setMargins(30, 60, 0, 150);

            controls.setLayoutParams(controlsparam);
            controls.setOrientation(LinearLayout.HORIZONTAL);

            if(b == true) {
                Button edit = new Button(allNotes.this);
                edit.setTag(data_id);
                edit.setOnClickListener(edi);
                edit.setBackgroundResource(R.drawable.edit);
                edit.setLayoutParams(new LinearLayout.LayoutParams(110, 110));

                Button delete = new Button(allNotes.this);
                delete.setTag(data_id);
                delete.setOnClickListener(del);
                delete.setBackgroundResource(R.drawable.wrong);
                LinearLayout.LayoutParams deleteparams = new LinearLayout.LayoutParams(110, 110);
                deleteparams.setMargins(50, 0, 0, 0);
                delete.setLayoutParams(deleteparams);
                controls.addView(edit);
                controls.addView(delete);
            }

            LinearLayout.LayoutParams ex = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ex.setMargins(60, 40, 60, 0);

            TextView value = new TextView(allNotes.this);

            value.setText(data_data);
            value.setTextSize(30);
            value.setTag("TextBox" + data_id);
            value.setPadding(10, 10, 10, 10);
            value.setBackgroundColor(Color.parseColor(c[i % 3]));

            Typeface tf = ResourcesCompat.getFont(allNotes.this, R.font.nothing);
            value.setTypeface(tf);

            value.setRotation(rot[i % 6]);
            value.setLayoutParams(ex);
            value.setElevation(10);
            value.setMinHeight(800);

            parent.addView(value);
            parent.addView(controls);

            ll.addView(parent);
        }
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
        queue = Volley.newRequestQueue(allNotes.this);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(allNotes.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
                if(response.optString("msg").equals("data added")){
                    Intent intent = new Intent(allNotes.this, allNotes.class);
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

    private void offlineDataUpload(){
        SharedPreferences dat = getSharedPreferences("only_offline", Context.MODE_PRIVATE);
        Map<String, ?> dat_get = dat.getAll();

        for (String name : dat_get.keySet()) {
            if(name.equals("members")){
                System.out.println(name);
            }
            else{
                String val = dat_get.get(name).toString();
                EditText txt = new EditText(allNotes.this);
                txt.setText(val);
                SharedPreferences getData = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                String user = getData.getString("user", "None");
                onlineDataAdd(user, txt);
            }
        }

        SharedPreferences.Editor datEditor = dat.edit();
        datEditor.clear();
        datEditor.commit();

        Toast.makeText(allNotes.this, "Offline data add complete", Toast.LENGTH_SHORT).show();
    }
}