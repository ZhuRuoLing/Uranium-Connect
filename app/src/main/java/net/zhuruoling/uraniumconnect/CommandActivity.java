package net.zhuruoling.uraniumconnect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.CacheMemoryUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import net.zhuruoling.uraniumconnect.databinding.ActivityCommandBinding;

public class CommandActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CacheDiskUtils cache = CacheDiskUtils.getInstance();
        cache.clear();
        super.onCreate(savedInstanceState);

        net.zhuruoling.uraniumconnect.databinding.ActivityCommandBinding binding = ActivityCommandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarCommand.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_command);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Log.i("BUNDLE","Nothing passed.");
            ToastUtils.showShort("Nothing passed.");
        }

        /*
                                        options.putString("server_ip",ip);
                                        options.putInt("server_port", finalPort);
                                        options.putString("key",key);
                                        options.putString("crypto_key", cryptoKey);
                                        options.putString("server_name",message.getLoad()[0]);
         */
        cache.put("server_ip", (String) bundle.get("server_ip"));
        cache.put("server_port", Integer.toString((int) bundle.get("server_port")));
        cache.put("key", (String) bundle.get("key"));
        cache.put("crypto_key", (String) bundle.get("crypto_key"));
        cache.put("server_name",(String) bundle.get("server_name"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.command, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_command);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}