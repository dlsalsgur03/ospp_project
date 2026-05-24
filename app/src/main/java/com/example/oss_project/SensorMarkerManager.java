package com.example.oss_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

public class SensorMarkerManager {

    private final Context context;
    private final KakaoMap kakaoMap;


    private final java.util.Map<String, BleDeviceData> sensorDataMap = new java.util.HashMap<>();
    //ㄴ>나중에 서버 전송 시 사용할거임

    // BLE 데이터 업데이트 메서드
    public void updateSensorData(java.util.List<BleDeviceData> dataList) {
        for (BleDeviceData data : dataList) {
            sensorDataMap.put(data.deviceAddress.toUpperCase(), data);
        }
    }
    private static final double[][] SENSOR_POSITIONS = {
            {36.6287545, 127.4579699},
            {36.62933,   127.4575},
            {36.62976,   127.4563},
            {36.63083,   127.4549},
            {36.63089,   127.4543}
    };

    public static final String[] SENSOR_NAMES = {
            "센서 1 - 도서관 뒷편",
            "센서 2 - 사회과학대 입구",
            "센서 3 - 이마트24 건너편",
            "센서 4 - 테니스장 앞",
            "센서 5 - 테니스장 건너편"
    };
    public static final String[] SENSOR_MAC_ADDRESSES = {
            "D8:3A:DD:79:8E:BF",  // 센서 1 - 도서관 뒷편
            "B8:27:EB:D3:40:06",  // 센서 2 - 사회과학대 입구
            "88:A2:9E:9B:6A",     // 센서 3 - 이마트24 건너편
            "D8:3A:DD:79:8F:80",  // 센서 4 - 테니스장 앞
            "D8:3A:DD:C1:88:BD"   // 센서 5 - 테니스장 건너편
    };

    public SensorMarkerManager(Context context, KakaoMap kakaoMap) {
        this.context = context;
        this.kakaoMap = kakaoMap;
    }

    public void addSensorMarkers() {
        Bitmap bitmap = createSensorBitmap();
        LabelStyles styles = kakaoMap.getLabelManager()
                .addLabelStyles(LabelStyles.from(LabelStyle.from(bitmap)));

        for (int i = 0; i < SENSOR_POSITIONS.length; i++) {
            LatLng pos = LatLng.from(SENSOR_POSITIONS[i][0], SENSOR_POSITIONS[i][1]);
            kakaoMap.getLabelManager().getLayer().addLabel(
                    LabelOptions.from(pos)
                            .setStyles(styles)
                            .setTag(i)
            );
        }

        // 마커 클릭 이벤트
        kakaoMap.setOnLabelClickListener((map, layer, label) -> {
            Object tag = label.getTag();
            if (tag instanceof Integer) {
                int index = (Integer) tag;
                showSensorDialog(index);
            }
        });
    }

    private Bitmap createSensorBitmap() {
        int size = 36;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#E53935"));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint);

        return bitmap;
    }

    private void showSensorDialog(int index) {
        int[] sensorImages = {
                R.drawable.sensor_1,
                R.drawable.sensor_2,
                R.drawable.sensor_3,
                R.drawable.sensor_4,
                R.drawable.sensor_5
        };

        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sensor);

        // 둥근 배경 및 크기 설정
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.88f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // 센서 사진
        de.hdodenhof.circleimageview.CircleImageView imgSensor =
                dialog.findViewById(R.id.img_sensor);
        imgSensor.setImageResource(sensorImages[index]);

        // 닫기 버튼
        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        // 획득 버튼
        dialog.findViewById(R.id.btn_collect).setOnClickListener(v -> {
            android.widget.Toast.makeText(context,
                    SENSOR_NAMES[index] + " 획득!",
                    android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}