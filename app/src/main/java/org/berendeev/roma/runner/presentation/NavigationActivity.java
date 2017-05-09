package org.berendeev.roma.runner.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.presentation.fragment.GoogleMapFragment;
import org.berendeev.roma.runner.presentation.fragment.LocationApiFragment;
import org.berendeev.roma.runner.presentation.fragment.LocationFragment;
import org.berendeev.roma.runner.presentation.fragment.LocationRepositoryFragment;
import org.berendeev.roma.runner.presentation.fragment.MainFragment;
import org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment;
import org.berendeev.roma.runner.presentation.fragment.YandexMapKitFragment;


public class NavigationActivity extends AppCompatActivity {

    public static final String YANDEX_MAP = "yandex";
    public static final String GOOGLE_MAP = "google";
    public static final String MAIN = "favourite";
    public static final String LOCATION = "history";

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = navigation.getSelectedItemId();
            int from = navigation.getMenu().findItem(id).getOrder();

            switch (item.getItemId()) {
                case R.id.yandex_map:
                    showFragment(YANDEX_MAP);
                    return true;
                case R.id.main:
                    showFragment(MAIN);
                    return true;
                case R.id.location:
                    showFragment(LOCATION);
                    return true;
                case R.id.google_map:
                    showFragment(GOOGLE_MAP);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null){
//            beginTransaction();
//            showFirstFragment(LOCATION);
            navigation.setSelectedItemId(R.id.location);
//            commitTransaction();
        }

    }

    private void showFragment(String tag){
        beginTransaction();
        showEnterFragment(tag);
        commitTransaction();
    }

    private void beginTransaction(){
        transaction = fragmentManager.beginTransaction();
    }

    private void commitTransaction(){
        transaction.commit();
    }

    private void showEnterFragment(String tag) {
        transaction.replace(R.id.container, getFragment(tag), tag);
    }

    private void showFirstFragment(String tag){
        Fragment fragment = getFragment(tag);
        transaction.add(R.id.container, fragment, tag);

    }

    @NonNull private Fragment getFragment(String tag) {
        switch (tag){
            case YANDEX_MAP:
                return new YandexMapKitFragment();
            case LOCATION:
                return new ServiceControlFragment();
            case MAIN:
                return new LocationFragment();
            case GOOGLE_MAP:
                return new GoogleMapFragment();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("myTag", "onRequestPermissionsResult");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
