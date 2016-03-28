package nanodegree.gemma.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import nanodegree.gemma.alexandria.api.Callback;


public class MainActivity extends AppCompatActivity implements Callback {

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if (IS_TABLET) {
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = getTitle();

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

//                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment nextFragment;



                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){


                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.list:
                        nextFragment = new ListOfBooks();
                        if(findViewById(R.id.right_container) != null){
                            findViewById(R.id.right_container).setVisibility(View.VISIBLE);
                        }
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, nextFragment)
                                .addToBackStack((String) title)
                                .commit();
//                        Toast.makeText(getApplicationContext(),"Settings Selected",Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        return true;

                    case R.id.scan:
                        nextFragment = new AddBook();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, nextFragment)
                                .addToBackStack((String) title)
                                .commit();
                        if(findViewById(R.id.right_container) != null){
                            findViewById(R.id.right_container).setVisibility(View.GONE);
                        }
                        return true;
                    case R.id.about:
                        nextFragment = new About();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, nextFragment)
                                .addToBackStack((String) title)
                                .commit();
                        if(findViewById(R.id.right_container) != null){
                            findViewById(R.id.right_container).setVisibility(View.GONE);
                        }
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Start with preferred fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mCurrentSelectedPosition = Integer.parseInt(prefs.getString("pref_startFragment", "0"));
        navigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(true);
        Fragment nextFragment;
        switch (mCurrentSelectedPosition) {
            default:
            case 0:
                nextFragment = new ListOfBooks();
                if(findViewById(R.id.right_container) != null){
                    findViewById(R.id.right_container).setVisibility(View.VISIBLE);
                }
                break;
            case 1:
                nextFragment = new AddBook();
                if(findViewById(R.id.right_container) != null){
                    findViewById(R.id.right_container).setVisibility(View.GONE);
                }
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();

    }


    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            Log.d("MainActivity", "two pane mode");
            id = R.id.right_container;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                AddBook addBookFrag = (AddBook)
                        getSupportFragmentManager().findFragmentById(R.id.container);
                addBookFrag.setError(intent.getStringExtra(MESSAGE_KEY));
                // Changed to the EditText setError, more visible
//                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

//    Removed as it was called from a custom back button
//    that has been removed to comply with Android Guidelines
//    public void goBack(View view){
//        getSupportFragmentManager().popBackStack();
//    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

//    @Override
//    public void onBackPressed() {
//
//        if(getSupportFragmentManager().getBackStackEntryCount()<2){
//            finish();
//        }
//        super.onBackPressed();
//    }

    // ERROR added code to stop detail fragment loading into list fragment on change from portrait to landscape on tablets
    @Override
    protected void onResume() {
        super.onResume();
        if(findViewById(R.id.right_container) != null){
            if(getSupportFragmentManager().getBackStackEntryCount()>2) {
                getSupportFragmentManager().popBackStack();
            }
        }
    }
}