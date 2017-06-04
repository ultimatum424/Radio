package com.example.ultim.radio5;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Switch;
import android.widget.TextView;

import com.example.ultim.radio5.Fragment.FragmentGreetings;
import com.example.ultim.radio5.Fragment.FragmentRadio;
import com.example.ultim.radio5.Fragment.FragmentGenre;
import com.example.ultim.radio5.Genres.GenreData;
import com.example.ultim.radio5.Genres.GenreItem;
import com.example.ultim.radio5.Genres.GenreListAdapter;
import com.example.ultim.radio5.Radio.TitleRadio;
import com.example.ultim.radio5.Univesity.UniversityData;
import com.example.ultim.radio5.Univesity.UniversityItem;
import com.example.ultim.radio5.Univesity.UniversityListAdapter;

import java.util.ArrayList;
import java.util.Random;

import static com.example.ultim.radio5.AppConstant.FragmentType.GREETINGS;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentRadio.OnFragmentInteractionListener, FragmentGenre.OnFragmentInteractionListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    ListView universityListView;

    Switch nav_menu_switch;
    SearchView nav_menu_searchView;
    LinearLayout onlineContent;
    LinearLayout offlineContent;

    TextView networkStatusTextBox;
    ListView genreListView;

    GenreListAdapter genreListAdapter;
    UniversityListAdapter universityListAdapter;
    UniversityData universityData;
    //ArrayList<GenreItem> musicGenres;  //жанры музыки
    GenreData genreData;

    Toolbar initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        return toolbar;
    }

    void initNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        networkStatusTextBox = (TextView) findViewById(R.id.menu_network_status_item);
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
        ImageView settings = (ImageView) findViewById(R.id.settingsImageView);
    }

    void initOnlineContent(){
        onlineContent = (LinearLayout) findViewById(R.id.online_content);
        universityData = new UniversityData(this);
        universityListView = (ListView) findViewById(R.id.menu_university_list_view);
        universityListAdapter = new UniversityListAdapter(universityData.getItems(), this);
        universityListView.setAdapter(universityListAdapter);
        universityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onUniversitySelect(position);
            }
        });
    }

    void initOfflineContent(){
        offlineContent = (LinearLayout) findViewById(R.id.offline_content);
        genreData = new GenreData(this.getApplicationContext());
        genreListView = (ListView) findViewById(R.id.genre_list_view);
        genreListAdapter = new GenreListAdapter(genreData.getItems(), this);
        genreListView.setAdapter(genreListAdapter);
        genreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGenreSelect(position);
            }
        });
    }

    void initSearchView(){
        nav_menu_searchView = (SearchView) findViewById(R.id.nav_menu_search);
        nav_menu_searchView.setIconified(false);
        nav_menu_searchView.setVisibility(universityData.isSearchViewNeeded() ? View.VISIBLE : View.GONE);

        nav_menu_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                universityListAdapter.onSearchChange(newText);
                return false;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = initToolbar();

        //Navigation
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Igniting elements
        initNavigationView();
        initOnlineContent();
        initOfflineContent();
        initSearchView();
        initFragment(GREETINGS);
        // TODO : GO TO initFragment or else SM
        initRadioIfStreaming();
        // Test Intent
        //startService(new Intent(getApplicationContext(), RadioPlayerService.class).setAction(RadioPlayerService.ACTION_PLAY));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //universityData.loadData();
    }


    @Override
    protected void onStop() {
        super.onStop();
        universityData.saveData();

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

        Fragment fragment = FragmentGenre.newInstance(genreData.getItems().get(position).getName());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment);
        ft.commit();

        genreListAdapter.notifyDataSetChanged();
        drawer.closeDrawers();
    }

    private void onUniversitySelect(int position) {

       if(universityListAdapter.onSelect(position)) { //false if first time selected

           UniversityItem current;

           if(!isServiceRunning() || !TitleRadio.getInstance().getTitle().equals(universityListAdapter.getCurrent())) {
               current = universityListAdapter.getCurrent();
           }
           else if (isServiceRunning()) {
               current = universityData.findItemByTitle(TitleRadio.getInstance().getTitle());
               if(current == null) {
                   //ERROR
                   throw new IllegalArgumentException("title != title not found in arraylist");
               }
           }
           else {
               drawer.closeDrawers();
               return;
           }

           Fragment fragment = FragmentRadio.newInstance(current.getName(), current.getStream());
           FragmentManager fm = getSupportFragmentManager();
           FragmentTransaction ft = fm.beginTransaction();
           ft.replace(R.id.content_container, fragment);
           ft.commit();
       }
       drawer.closeDrawers();
    }

    private void initRadioIfStreaming() {
        if(isServiceRunning()) {
           // UniversityItem current = universityData.findItemByTitle(RadioService.title);
            UniversityItem current = universityData.findItemByTitle(TitleRadio.getInstance().getTitle());
            Fragment fragment = FragmentRadio.newInstance(current.getName(), current.getStream());
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_container, fragment);
            ft.commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //empty body
    }

    private void initFragment(AppConstant.FragmentType eFragmentType) {
        Fragment fragment;
        switch (eFragmentType){

            case GREETINGS:
                fragment = new FragmentGreetings();
                break;
            case RADIO:
                fragment = new FragmentRadio();
                break;
            case MUSIC: default:
                fragment = new FragmentGenre();
                break;

        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment);
        ft.commit();
        drawer.closeDrawers();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(RadioPlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class OKHttpRequest extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            FileManager fileManager = new FileManager(getApplication());
            String request = fileManager.doGetRequest(AppConstant.ULR_STATIONS);
            fileManager.saveFile(request, AppConstant.FILE_STATIONS);
            //universityItems = fileManager.getListUniversity();
            return(null);
        }
    }
}