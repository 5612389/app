package com.example.icyclist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.ServiceSettings;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AMapLocationListener {
    private MapView mMapView = null;
    private AMap aMap;
    private AMapLocationClient locationClient;

    private List<LatLng> routePoints = new ArrayList<>();
    private boolean isRiding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置高德地图的隐私合规配置
        MapsInitializer.updatePrivacyAgree(this, true);
        MapsInitializer.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);

        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mMapView.getMap();
            setupLocation();

            // 设置初始位置和缩放级别
            LatLng initialPosition = new LatLng(30.6586, 104.0648); // 示例坐标，可以替换为合适的默认位置
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 17)); // 初始缩放级别设置为17
        }

        Button startButton = findViewById(R.id.start_button);
        Button stopButton = findViewById(R.id.stop_button);
        Button shareButton = findViewById(R.id.share_button);

        startButton.setOnClickListener(v -> {
            isRiding = true;
            routePoints.clear();
            aMap.clear(); // 清除地图上的所有图形
            Toast.makeText(this, "骑行开始", Toast.LENGTH_SHORT).show();
        });

        stopButton.setOnClickListener(v -> {
            isRiding = false;
            Toast.makeText(this, "骑行结束", Toast.LENGTH_SHORT).show();
            // 清除已经绘制的路线
            routePoints.clear();
            aMap.clear(); // 清除地图上的所有图形，包括路线
        });

        shareButton.setOnClickListener(v -> shareRoute());
    }

    private void setupLocation() {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption locationOption = new AMapLocationClientOption();

            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setInterval(2000);
            locationOption.setNeedAddress(true);
            locationClient.setLocationOption(locationOption);
            locationClient.setLocationListener(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
            locationClient.startLocation();

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(null); // 设置自定义图标
            myLocationStyle.strokeColor(Color.TRANSPARENT);
            myLocationStyle.radiusFillColor(Color.TRANSPARENT);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "定位初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            if (location.getErrorCode() == 0) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                if (isRiding) {
                    routePoints.add(new LatLng(lat, lon));
                    drawRoute();
                }

                // 确保蓝标始终在地图上
                aMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
            } else {
                Toast.makeText(this, "定位失败: " + location.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void drawRoute() {
        if (routePoints.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .color(Color.BLUE)
                    .width(10);
            aMap.addPolyline(polylineOptions);
        }
    }

    private void shareRoute() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "我骑行的路线: ...");
        startActivity(Intent.createChooser(shareIntent, "分享骑行路线"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // 调用父类方法

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocation(); // 重新初始化定位
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
