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
    private HashMap<String, String> mParams;
    private HttpClient mHttpClient;
    private TextView mCategoryTextView;
    private String mCategory;

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
        mParams = new HashMap<String, String>();
        mHttpClient = new HttpClient(mContext);

        // Layouts
        mProjectsTextView = (TextView) rootView.findViewById(R.id.projectsTextView);
        mBudgetAmountTextView = (TextView) rootView.findViewById(R.id.budgetAmountTextView);
        mApprovedProjectsTextView = (TextView) rootView.findViewById(R.id.approvedProjectsTextView);
        mSpentAmountTextView = (TextView) rootView.findViewById(R.id.spentAmountTextView);
        mProvinceTextView = (TextView) rootView.findViewById(R.id.provinceTextView);
        mCategoryTextView = (TextView) rootView.findViewById(R.id.categoryTextView);
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
        //setUpMapIfNeeded();
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
        geocodeLatLng(target);
        executeApiCall();
    }

    private void executeApiCall() {
        try {
            URI uri = new URI("https", "procex.coreproc.ph", "/api/search/chris-max-special", null);

            mHttpClient.get(uri.toString(), mParams, new HttpClientCallback() {
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
                    noLocationFound();
                }

                @Override
                public void onSuccess(Response<JsonObject> response) {
                    // we populate the data
                    JsonObject jsonResponse = response.getResult();
                    JsonObject meta = jsonResponse.getAsJsonObject("meta");

                    double budgetAmount = meta.get("total_budget_amount").getAsDouble();
                    int projects = meta.get("total_projects").getAsInt();
                    int approvedProjects = meta.get("total_approved_projects").getAsInt();
                    double spentAmount = meta.get("total_spent_amount").getAsDouble();

                    mBudgetAmountTextView.setText("" + numberToMoney(budgetAmount));
                    mSpentAmountTextView.setText("" + numberToMoney(spentAmount));
                    mProjectsTextView.setText("" + numberToFormat(projects));
                    mApprovedProjectsTextView.setText("" + numberToFormat(approvedProjects));

                    mProvinceTextView.setText(mProvince);

                    if (mCategory != null) {
                        mCategoryTextView.setText(mCategory);
                    } else {
                        mCategoryTextView.setText("");
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void noLocationFound() {
        mBudgetAmountTextView.setText("-");
        mSpentAmountTextView.setText("-");
        mProjectsTextView.setText("-");
        mApprovedProjectsTextView.setText("-");

        mProvinceTextView.setText("Select another location");
        mCategoryTextView.setText("");
    }

    private void geocodeLatLng(LatLng target) {
        try {

            //Place your latitude and longitude
            List<Address> addresses = mGeocoder.getFromLocation(target.latitude, target.longitude, 1);

            if (addresses != null) {

                Address fetchedAddress = addresses.get(0);

                mProvince = fetchedAddress.getAddressLine(fetchedAddress.getMaxAddressLineIndex() - 1);
                Log.i("procex", "I am at: " + mProvince);

                mParams.put("province", mProvince);
                return;

            } else {
                Log.e("procex", "no location found");
                mParams.put("province", "Metro Manila");
                return;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getActivity(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }

        mParams.put("province", "Metro Manila");
        return;
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

    public void setCategory(String item) {
        mParams.put("category", item);
        mCategory = item;
        executeApiCall();
    }

    public void removeCategory() {
        mParams.remove("category");
        mCategory = null;
        executeApiCall();
    }
}
