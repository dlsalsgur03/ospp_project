package com.example.oss_project;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

public class MyLocationManager {

    private final Context context;
    private final KakaoMap kakaoMap;
    private Label myLocationLabel;

    public MyLocationManager(Context context, KakaoMap kakaoMap) {
        this.context = context;
        this.kakaoMap = kakaoMap;
    }

    public void addMyLocationMarker(double lat, double lon) {
        LabelLayer layer = kakaoMap.getLabelManager().getLayer();
        if (myLocationLabel != null) layer.remove(myLocationLabel);

        Bitmap bitmap = createMyLocationBitmap();
        LabelStyles styles = kakaoMap.getLabelManager()
                .addLabelStyles(LabelStyles.from(LabelStyle.from(bitmap)));

        myLocationLabel = layer.addLabel(
                LabelOptions.from(LatLng.from(lat, lon)).setStyles(styles));
    }

    public void updateLocation(double lat, double lon) {
        LatLng newPos = LatLng.from(lat, lon);
        if (myLocationLabel != null) {
            myLocationLabel.moveTo(newPos);
        } else {
            addMyLocationMarker(lat, lon);
        }
        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(newPos));
    }

    public void startGps(LocationUpdateListener listener) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        // 마지막 위치 즉시 반영
        Location last = locationManager.getLastKnownLocation(
                LocationManager.NETWORK_PROVIDER);
        if (last != null) listener.onLocationUpdate(last.getLatitude(), last.getLongitude());

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 3000L, 2f,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        listener.onLocationUpdate(
                                location.getLatitude(), location.getLongitude());
                    }
                });

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 3000L, 2f,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        listener.onLocationUpdate(
                                location.getLatitude(), location.getLongitude());
                    }
                });
    }

    private Bitmap createMyLocationBitmap() {
        int size = 30;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#4A90E2"));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint);

        return bitmap;
    }

    // 위치 업데이트 콜백 인터페이스
    public interface LocationUpdateListener {
        void onLocationUpdate(double lat, double lon);
    }
}