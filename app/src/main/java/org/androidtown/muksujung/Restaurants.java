package org.androidtown.muksujung;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.List;


public class Restaurants extends AppCompatActivity implements MapView.CurrentLocationEventListener {
    MapView mapView;
    ListView listView;
    StoreAdapter mAdapter;
    EditText keywordView;
    MapPOIItem marker;
    Button currbtn; // 현재위치버튼
    Button search_btn; // 검색버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants);

        //다음이 제공하는 MapView객체 생성 및 API Key 설정
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("API_KEY");
        //xml에 선언된 map_view 레이아웃을 찾아온 후, 생성한 MapView객체 추가
        LinearLayout container = (LinearLayout) findViewById(R.id.map_view);
        container.addView(mapView);

        //지도 api 연습시작(0409)
        mapView.setCurrentLocationEventListener(this);

        // 기본마커추가
        marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        keywordView = (EditText)findViewById(R.id.restaurant_search_text);
        listView = (ListView)findViewById(R.id.restaurant_listview);
        mAdapter = new StoreAdapter();
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        currbtn = (Button)findViewById(R.id.current_location_button);

        // 현재위치로 이동 버튼
        currbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
            }
        });

        // edittext 엔터 버튼 처리
        keywordView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search_btn.performClick();
                    getCurrentFocus();
                    return true;
                }
                return false;
            }
        });

        // 검색안누르고 바로 뜨게
        mAdapter.notifyDataSetChanged();
        Intent i = getIntent();
        String keyword = i.getStringExtra("theme_keyword").toString();

        NaverStoreRequest request = new NaverStoreRequest(keyword);
        NetworkManager.getInstance().getNetworkData(request, new NetworkManager.OnResultListener<NaverStores>() {
            @Override
            public void onSuccess(NetworkRequest<NaverStores> request, NaverStores result) {
                mAdapter.addAll(result.items);
            }

            @Override
            public void onFailure(NetworkRequest<NaverStores> request, int errorCode, int responseCode, String message, Throwable exception) {
                Toast.makeText(Restaurants.this, "fail", Toast.LENGTH_SHORT).show();
                Log.i("Restaurants.java", "responseCode : " + responseCode);
            }
        });

        // 상세페이지로 이동
        final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                pos = listView.getCheckedItemPosition();
                if(pos != -1){
                    Log.d("Restaurants.java", "link= " + mAdapter.getItem(pos).link);

                    String url = mAdapter.getItem(pos).link;

                    if(url != null){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(Restaurants.this, "링크없음", Toast.LENGTH_SHORT).show();
                }
                try {
                    Log.d("SearchAll.java", "try실패");
                    List<Address> listAddr = null;
                    pos = listView.getCheckedItemPosition();
                    Geocoder geocoder = new Geocoder(Restaurants.this);
                    listAddr = geocoder.getFromLocationName(mAdapter.getItem(pos).address,10);
                    keywordView.setText(listAddr.toString());
                    Toast.makeText(Restaurants.this, listAddr.toString(), Toast.LENGTH_SHORT).show();

                    double longi =  listAddr.get(0).getLongitude();
                    double lati = listAddr.get(0).getLatitude();
                    Log.d("SearchAll.java", "lat:"+lati +"& longi:"+longi);

                }catch (IOException e) {
                    e.printStackTrace();
                    Log.d("SearchAll.java", "try실패");
                }
            }
        };        listView.setOnItemClickListener(listener);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {    }
}
