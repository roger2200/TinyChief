package com.roger.tinychief.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.ar.ImageTargets;
import com.roger.tinychief.volleymgr.NetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Color;
//import com.vuforia.ImageTarget;

public class DetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Response.Listener<String> mResponseListener = new Response.Listener<String>() {
        public void onResponse(String string) {
            int j = 1;
            int k = 1;
            TextView text1 = (TextView) findViewById(R.id.material);
            StringBuilder materials = new StringBuilder();
            TextView text2 = (TextView) findViewById(R.id.step);
            StringBuilder steps = new StringBuilder();
            try {
                JSONArray ary = new JSONArray(string);
                for (int i = 0; i < ary.length(); i++) {
                    JSONObject json = ary.getJSONObject(i);
                    try {
                        while (json.getString("material_" + j) != null) {
                            String material = json.getString("material_" + j);
                            materials.append(j+"."+material + "\r\n");
                            j++;
                        }
                    }
                    catch(Exception e){
                        Log.d("error:",e.getMessage());
                    }
                    finally {
                        k =1;
                        text1.setText(materials.toString());
                    }
                    while(json.getString("step_"+j)!=null) {
                        String step = json.getString("step_"+k);
                        steps.append(k+"."+"\r\n"+step + "\r\n");
                        k++;
                    }
                }
            }
            catch (Exception e)
            {
                Log.d("error:",e.getMessage());
            }
            finally {
                j = 1;
                text2.setText(steps.toString());
            }
        }
    };
    private String path;

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("Error", error.toString());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        TextView cookBookName = (TextView)findViewById(R.id.cookbookName);
        StringRequest request = new StringRequest(Request.Method.GET, "https://intense-oasis-69003.herokuapp.com/", mResponseListener, mErrorListener);
        NetworkManager.getInstance(this).request(null, request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Bundle bundle = this.getIntent().getExtras();
        setTitle("食譜詳情" + bundle.getString("DATA"));
        cookBookName.setText("食譜的名稱："+bundle.getString("DATA"));
        setToolbar();
        setNavigationView();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.detail_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (!menuItem.isChecked()) menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_hot:
                        Toast.makeText(getApplicationContext(), "nav_item_hot", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_love:
                        Toast.makeText(getApplicationContext(), "nav_item_love", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_create:
                        Toast.makeText(getApplicationContext(), "nav_item_create", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_calendar:
                        Toast.makeText(getApplicationContext(), "nav_item_calendar", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_history:
                        Toast.makeText(getApplicationContext(), "nav_item_history", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_setting:
                        Toast.makeText(getApplicationContext(), "nav_item_setting", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
    }

    public void letCook(View view) {
        Intent intent = new Intent(view.getContext(), CookActivity.class);
        startActivity(intent);
    }

    public void openAR(View view) {

        Intent i = new Intent(view.getContext(), ImageTargets.class);
        i.putExtra("IMAGE_PATH", path);
        startActivity(i);
    }

    public void loadImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        Intent destIntent = Intent.createChooser(intent, "選擇檔案");
        startActivityForResult(destIntent, 0);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = managedQuery(contentUri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        return img_path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // 有選擇檔案
        if (resultCode == RESULT_OK) {
            // 取得檔案的 Uri
            Uri uri = data.getData();
            if (uri != null) {
                path = getRealPathFromURI(uri);
            }
        }
    }
}
