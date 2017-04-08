package com.example.ultim.radio5;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ultim.radio5.Univesity.UniversityItem;
import com.example.ultim.radio5.Univesity.UnivesityListAdapter;

import java.util.ArrayList;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ListView universityListView;
    UnivesityListAdapter univesityListAdapter;
    Switch nav_menu_switch;
    SearchView nav_menu_searchView;
    LinearLayout nav_menu_settingsLayout;

    LinearLayout onlineContent;
    LinearLayout offlineContent;
    TextView networkStatusTextBox;
    RadioGroup musicGenresGroup;

    ArrayList<String> musicGengres;  //жанры музыки


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ArrayList<UniversityItem> universityItems = new ArrayList<UniversityItem>();
        universityItems.add(new UniversityItem("Волгатех"));
        universityItems.add(new UniversityItem("МарГУ"));
        universityItems.add(new UniversityItem("МОСИ"));

        onlineContent = (LinearLayout) findViewById(R.id.online_content);
        offlineContent = (LinearLayout) findViewById(R.id.offline_content);
        networkStatusTextBox = (TextView) findViewById(R.id.menu_network_status_item);

        universityListView = (ListView) findViewById(R.id.menu_university_list_view);
        univesityListAdapter = new UnivesityListAdapter(universityItems, this);
        universityListView.setAdapter(univesityListAdapter);

        nav_menu_switch = (Switch) findViewById(R.id.nav_menu_switch);
        nav_menu_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b) {
                    onlineContent.setVisibility(View.VISIBLE);
                    offlineContent.setVisibility(View.GONE);
                    networkStatusTextBox.setText("Офлайн");
                }
                else {
                    onlineContent.setVisibility(View.GONE);
                    offlineContent.setVisibility(View.VISIBLE);
                    networkStatusTextBox.setText("Онлайн");
                }
            }
        });

        musicGengres = new ArrayList<>();
        musicGengres.add("Rock");
        musicGengres.add("Pop");
        musicGengres.add("Jazz");

        musicGenresGroup = (RadioGroup) findViewById(R.id.music_genres_group);
        for(String genre : musicGengres){
            RadioButton b = new RadioButton(this);
            b.setText(genre);
            musicGenresGroup.addView(b); //the RadioButtons are added to the radioGroup instead of the layout
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.navigation_drawer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onSwitchChange() {

    }
}
