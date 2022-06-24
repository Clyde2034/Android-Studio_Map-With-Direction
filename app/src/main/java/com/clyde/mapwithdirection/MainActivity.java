package com.clyde.mapwithdirection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment_map;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GET_PERMISSION();

        if (flag) {
            MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, mapFragment).commit();
        }

    }

    private void GET_PERMISSION() {
        if (checkSelfPermission(permission[0]) == PackageManager.PERMISSION_DENIED || checkSelfPermission(permission[1]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permission, 100);
        } else {
            flag = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                flag = true;
                MapFragment mapFragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, mapFragment).commit();
            }
        }

    }
}