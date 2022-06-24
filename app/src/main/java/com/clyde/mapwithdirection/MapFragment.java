package com.clyde.mapwithdirection;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private View mapView;
    private String directionURL, direction_ORI, direction_DEST;
    private Button btn_direction;
    private Handler mainHandler = new Handler();
    private List<LatLng> decodePolyPath;
    private Polyline polyline;
    private HashMap<String, Marker> tempMarker = new HashMap<>();
    private Marker marker_ori, marker_dest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        btn_direction = view.findViewById(R.id.btn_direction);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        mapView = supportMapFragment.getView();
        supportMapFragment.getMapAsync(this);

        String api_key = getString(R.string.api_key);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), api_key);
        }

        AutocompleteSupportFragment autocompleteSupportFragment_ori = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.fragment_autocomplete_ori);
        AutocompleteSupportFragment autocompleteSupportFragment_dest = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.fragment_autocomplete_dest);
        autocompleteSupportFragment_ori.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragment_dest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteSupportFragment_ori.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(), "Try to Fetch the Original Place, but occures error.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLatLng() != null) {
                    LatLng placeLatLng_ori = place.getLatLng();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            if (marker_ori != null) {
                                marker_ori.remove();
                            }

                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng_ori));
                            marker_ori = googleMap.addMarker(new MarkerOptions().position(placeLatLng_ori).title(place.getName()));
                        }
                    });
                    direction_ORI = place.getId();
                    Log.d("direction_ORI", place.getId());
                }
            }
        });

        autocompleteSupportFragment_dest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(), "Try to Fetch the Destination Place, but occures error.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLatLng() != null) {
                    LatLng placeLatLng_dest = place.getLatLng();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            if (marker_dest != null) {
                                marker_dest.remove();
                            }

                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng_dest));
                            marker_dest = googleMap.addMarker(new MarkerOptions().position(placeLatLng_dest).title(place.getName()));
                        }
                    });
                    direction_DEST = place.getId();
                    Log.d("direction_DEST", place.getId());
                }

            }
        });

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

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 30);

        final LatLng[] myLocation = new LatLng[1];
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation[0], 7));
                        } else {
                            myLocation[0] = new LatLng(25.033964, 121.564468);
                        }
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(getActivity(), "Failed to get currnt last location.", Toast.LENGTH_SHORT).show();
                    }
                });

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                try {
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.TRADITIONAL_CHINESE);
                    List<Address> myAddress = geocoder.getFromLocation(myLocation[0].latitude, myLocation[0].longitude, 1);
                    Toast.makeText(getActivity(), myAddress.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private String Poly(String ori, String dest) {
        String URL = null;
        try {
            URL = "https://maps.googleapis.com/maps/api/directions/json?";
            URL += "origin=place_id" + URLEncoder.encode(":", "utf-8") + ori + "&";
            URL += "destination=place_id" + URLEncoder.encode(":", "utf-8") + dest + "&";
            URL += "key=" + getString(R.string.api_key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return URL;
    }
}