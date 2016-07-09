package com.roger.tinychief.activity;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.roger.tinychief.R;
import com.roger.tinychief.ar.ImageTargets;

public class DetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle=this.getIntent().getExtras();
        setTitle("食譜詳情"+bundle.getString("DATA"));
        setToolbar();
        setNavigationView();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void setNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.detail_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(!menuItem.isChecked()) menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()){
                    case R.id.nav_item_hot:
                        Toast.makeText(getApplicationContext(),"nav_item_hot",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_love:
                        Toast.makeText(getApplicationContext(),"nav_item_love",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_create:
                        Toast.makeText(getApplicationContext(),"nav_item_create",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_calendar:
                        Toast.makeText(getApplicationContext(),"nav_item_calendar",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_history:
                        Toast.makeText(getApplicationContext(),"nav_item_history",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_setting:
                        Toast.makeText(getApplicationContext(),"nav_item_setting",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
    }

    public void letCook(View view)
    {
        Intent intent=new Intent(view.getContext(),CookActivity.class);
        startActivity(intent);
    }

    public void openAR(View view)
    {
        Intent i=new Intent(view.getContext(), ImageTargets.class);
        startActivity(i);
    }
}
