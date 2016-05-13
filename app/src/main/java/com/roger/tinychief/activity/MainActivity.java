package com.roger.tinychief.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.Toast;

import com.roger.tinychief.R;
import com.roger.tinychief.adapter.RecyclerViewAdapter;
import com.roger.tinychief.adapter.RecyclerViewAdapter.OnRecyclerViewItemClickListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("熱門食譜");
        setToolbar();
        setRecycleView();
        setNavigationView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void setRecycleView()
    {
        ArrayList<String> myDataset = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            myDataset.add(i + "");
        }
        recyclerView = (RecyclerView) findViewById(R.id.mian_recy_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerViewAdapter(myDataset);
        mAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener(){
            @Override
            public void onItemClick(View view , String data){
                Toast.makeText(MainActivity.this, data,Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(view.getContext(),DetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("DATA",data);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
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
}
