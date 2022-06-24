# Android-Studio_MapWithDirection
This project is draw the polyline path between two points in Google Map. 

>API Require
>
>>okhttp  
>>Maps SDK for Android  
>>Distance Matrix API  
>>Place API  

>UI and Java Code  
>>UI：activity_main.xml、fragment_map.xml  
>>Java：MainActivity.java、MapFragment.java  

<p align="center">
  <img align="left" src="https://user-images.githubusercontent.com/41913354/175551626-ed83a2a3-4da6-4c1a-a25e-f388ba2eb8e4.png" width="250"/>
  <img align="center" src="https://user-images.githubusercontent.com/41913354/175551789-6ad2dff6-6b2a-4fcc-9760-80df43579227.png" width="250"/>
  <img align="right" src="https://user-images.githubusercontent.com/41913354/175552041-20c1d4b0-3f70-4069-a01f-9adc0531da47.png" width="250"/>
</p>

## AndroidManifest.xml and other Environment
```
    //...
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    //...
-------------------------------------------------------------------------------------------
dependencies {
    //...
    implementation 'com.google.android.gms:play-services-maps:18.0.2' //mapView
    
    implementation 'com.google.android.gms:play-services-location:19.0.1'//FusedLocationProviderClient
    
    implementation 'com.google.maps.android:android-maps-utils:2.2.3'//Convert json format to polyline
    
    implementation 'com.google.android.libraries.places:places:2.5.0' //Autocomplete_fragment
    
    implementation 'com.squareup.okhttp3:okhttp:4.7.2'//okhttp
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:4.7.2'//log connenting status
    implementation 'com.squareup.okhttp3:logging-interceptor:4.7.2'//log connenting status
    //...
}  
-------------------------------------------------------------------------------------------
build.gradle(Project:(Project Name)) {
    plugins {
    //..
    id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin' version '2.0.1' apply false
    //..
    }
}
//Used to Support Maps SDK for Android on the API Level 28 and higher.
-------------------------------------------------------------------------------------------
build.gradle(Module:(Project Name.app)) {
    plugins {
    //..
    id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin'
    //..
    }
}
//Used to Support Maps SDK for Android on the API Level 28 and higher.
```

## MapFragment.java
```
        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                directionURL = Poly(direction_ORI, direction_DEST);
                if (directionURL != null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                            .build();

                    Request request = new Request.Builder()
                            .url(directionURL)
                            .method("GET", null)
                            .build();

                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Failed to get Direction by JSON format", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                JSONObject jsonObject_overview_polyline = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline");
                                String overview_polyline = jsonObject_overview_polyline.getString("points");
                                decodePolyPath = PolyUtil.decode(overview_polyline);
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                            @Override
                                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                                if (polyline != null) {
                                                    polyline.remove();
                                                }
                                                PolylineOptions polylineOptions = new PolylineOptions().addAll(decodePolyPath);
                                                polylineOptions.width(8);
                                                polylineOptions.color(Color.RED);
                                                polyline = googleMap.addPolyline(polylineOptions);
                                                Toast.makeText(getActivity(), "路徑繪製完成.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Failed to get Direction by JSON format", Toast.LENGTH_SHORT).show();
                }
            }
        });

```

## The project is complex than normal small project, so decide to using text to dexcription what the project is doing and every file purpose.
# Map API_KEY must be replace.
>UI
>>activity_main.xml:This activity is first create when start the application, and check the permission.  
>>fragment_map.xml:Embeded the map to the fragment and some ui element.  

>Java  
>>MainActivity.java:Permission request and transaction to maps.  
>>MapFragment.java:Kernel Tech, like api data parse、two point of distance calculate by Distance Matrix from Google,drawing the polyline in map...  

