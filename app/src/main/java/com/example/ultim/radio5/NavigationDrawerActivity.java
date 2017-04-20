package com.example.ultim.radio5;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ultim.radio5.Fragment.FragmentGreetings;
import com.example.ultim.radio5.Fragment.FragmentRadio;
import com.example.ultim.radio5.Fragment.FragmentGenre;
import com.example.ultim.radio5.Genres.GenreItem;
import com.example.ultim.radio5.Genres.GenreListAdapter;
import com.example.ultim.radio5.Univesity.UniversityItem;
import com.example.ultim.radio5.Univesity.UnivesityListAdapter;

import java.util.ArrayList;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentRadio.OnFragmentInteractionListener, FragmentGenre.OnFragmentInteractionListener {
    DrawerLayout drawer;
    NavigationView navigationView;
    ListView universityListView;
    UnivesityListAdapter univesityListAdapter;
    Switch nav_menu_switch;
    SearchView nav_menu_searchView;
    LinearLayout nav_menu_settingsLayout;

    LinearLayout onlineContent;
    LinearLayout offlineContent;
    TextView networkStatusTextBox;
    RadioGroup musicGenresGroup;
    ListView genreListView;
    GenreListAdapter genreListAdapter;

    ArrayList<UniversityItem> universityItems;
    ArrayList<GenreItem> musicGengres;  //жанры музыки



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Волгатех");


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        universityItems = new ArrayList<UniversityItem>();
//        universityItems.add(new GenreItem("Волгатех"));
//        universityItems.add(new GenreItem("МарГУ"));
//        universityItems.add(new GenreItem("МОСИ"));
        universityItems.add(new UniversityItem("Volgatech"));
        universityItems.add(new UniversityItem("MarSU"));
        universityItems.add(new UniversityItem("MOSI"));

        onlineContent = (LinearLayout) findViewById(R.id.online_content);
        offlineContent = (LinearLayout) findViewById(R.id.offline_content);
        networkStatusTextBox = (TextView) findViewById(R.id.menu_network_status_item);

        universityListView = (ListView) findViewById(R.id.menu_university_list_view);
        univesityListAdapter = new UnivesityListAdapter(universityItems, this);
        universityListView.setAdapter(univesityListAdapter);
        universityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onUniversitySelect(position);
            }
        });

        musicGengres = new ArrayList<GenreItem>();
        musicGengres.add(new GenreItem("Rock"));
        musicGengres.add(new GenreItem("Pop"));
        musicGengres.add(new GenreItem("Jazz"));

        genreListView = (ListView) findViewById(R.id.genre_list_view);
        genreListAdapter = new GenreListAdapter(musicGengres, this);
        genreListView.setAdapter(genreListAdapter);
        genreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGenreSelect(position);
            }
        });

        nav_menu_searchView = (SearchView) findViewById(R.id.nav_menu_search);
        nav_menu_searchView.setIconified(false);
        nav_menu_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                univesityListAdapter.onSearchChange(newText);
                return false;
            }
        });

        nav_menu_switch = (Switch) findViewById(R.id.nav_menu_switch);
        nav_menu_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    onlineContent.setVisibility(View.GONE);
                    offlineContent.setVisibility(View.VISIBLE);
                    networkStatusTextBox.setText("Офлайн");
                }
                else {
                    onlineContent.setVisibility(View.VISIBLE);
                    offlineContent.setVisibility(View.GONE);
                    networkStatusTextBox.setText("Онлайн");
                }
            }
        });

//        musicGenresGroup = (RadioGroup) findViewById(R.id.music_genres_group);
//        for(String genre : musicGengres){
//            RadioButton b = new RadioButton(this);
//            b.setText(genre);
//            musicGenresGroup.addView(b); //the RadioButtons are added to the radioGroup instead of the layout
//        }

        ImageView settings = (ImageView) findViewById(R.id.settingsImageView);
        settings.setColorFilter(Color.WHITE);


        initContent("init");
    }

    @Override
    public void onBackPressed() {
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
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onGenreSelect(int position) {
        genreListAdapter.onItemSelect(position);

        Fragment fragment = new FragmentGenre();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment);
        ft.commit();

        genreListAdapter.notifyDataSetChanged();
    }

    private void onUniversitySelect(int position) {
        //univesityListAdapter.onIconClick(position);
       if(univesityListAdapter.onSelect(position)) {

           Fragment fragment = new FragmentRadio();
           FragmentManager fm = getSupportFragmentManager();
           FragmentTransaction ft = fm.beginTransaction();
           ft.replace(R.id.content_container, fragment);
           ft.commit();
       }
       drawer.closeDrawers();

        //univesityListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //empty body
    }

    private void initContent(String param) {
        Fragment fragment;
        if(param == "init") {
            fragment = new FragmentGreetings();
        }
        else if (param == "other") {
            fragment = new FragmentRadio();
        }
        else {
            fragment = new FragmentGenre();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment);
        ft.commit();
        drawer.closeDrawers();
    }
}