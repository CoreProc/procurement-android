package ph.coreproc.android.procex;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ph.coreproc.android.procex.libs.HttpClient.HttpClient;
import ph.coreproc.android.procex.libs.HttpClient.HttpClientCallback;
import ph.coreproc.android.procex.libs.HttpClient.HttpClientError;

public class MapsFragment extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private Geocoder mGeocoder;
    private Context mContext;
    private TextView mProjectsTextView;
    private TextView mBudgetAmountTextView;
    private TextView mApprovedProjectsTextView;
    private TextView mSpentAmountTextView;
    private TextView mProvinceTextView;
    private String mProvince;
    private ProgressBar mLocationProgressBar;
    private Button mLocationButton;

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);

        mContext = getActivity();

        mLocationClient = new LocationClient(mContext, this, this);
        mGeocoder = new Geocoder(mContext, Locale.ENGLISH);

        // Layouts
        mProjectsTextView = (TextView) rootView.findViewById(R.id.projectsTextView);
        mBudgetAmountTextView = (TextView) rootView.findViewById(R.id.budgetAmountTextView);
        mApprovedProjectsTextView = (TextView) rootView.findViewById(R.id.approvedProjectsTextView);
        mSpentAmountTextView = (TextView) rootView.findViewById(R.id.spentAmountTextView);
        mProvinceTextView = (TextView) rootView.findViewById(R.id.provinceTextView);
        mLocationProgressBar = (ProgressBar) rootView.findViewById(R.id.locationProgressBar);

        setUpMapIfNeeded();

        mLocationButton = (Button) rootView.findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("procex", "Getting location");
                LatLng target = mMap.getCameraPosition().target;
                getData(target);
            }
        });

        return rootView;
    }

    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // nothing?
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        LatLng location = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        getData(location);
    }

    private void getData(LatLng target) {
        Log.i("procex", "Getting location");
        mProvince = geocodeLatLng(target);

        try {
            URI uri = new URI("https", "procex.coreproc.ph", "/api/search/from-location", null);

            HttpClient httpClient = new HttpClient(mContext);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("province", mProvince);
            httpClient.get(uri.toString(), params, new HttpClientCallback() {
                @Override
                public void onStart() {
                    Log.i("procex", "starting http call");
                    mLocationButton.setVisibility(View.GONE);
                    mLocationProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    mLocationButton.setVisibility(View.VISIBLE);
                    mLocationProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(HttpClientError httpClientError) {
                    Toast.makeText(mContext, "Error: " + httpClientError.getCode(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Response<JsonObject> response) {
                    // we populate the data
                    JsonObject jsonResponse = response.getResult();
                    JsonObject meta = jsonResponse.getAsJsonObject("meta");

                    double budgetAmount = meta.get("total_budget_amount").getAsDouble();
                    int projects = meta.get("total_projects").getAsInt();
                    int approvedProjects = meta.get("total_approved_projects").getAsInt();

                    mBudgetAmountTextView.setText("" + numberToMoney(budgetAmount));
                    mProjectsTextView.setText("" + numberToFormat(projects));
                    mApprovedProjectsTextView.setText("" + numberToFormat(approvedProjects));

                    mProvinceTextView.setText(mProvince);
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    private String geocodeLatLng(LatLng target) {
        try {

            //Place your latitude and longitude
            List<Address> addresses = mGeocoder.getFromLocation(target.latitude, target.longitude, 1);

            if (addresses != null) {

                Address fetchedAddress = addresses.get(0);

                String address = fetchedAddress.getAddressLine(fetchedAddress.getMaxAddressLineIndex() - 1);
                Log.i("procex", "I am at: " + address);

                return address;

            } else {
                Log.e("procex", "no location found");
                return "Metro Manila";

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getActivity(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }

        return "Metro Manila";
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private String numberToMoney(double amount) {
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        return df.format(amount);
    }

    private String numberToFormat(int number) {
        DecimalFormat df = new DecimalFormat("#,###,###");
        return df.format(number);
    }
}
