package ph.coreproc.android.procex;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Response;

import ph.coreproc.android.procex.libs.HttpClient.HttpClient;
import ph.coreproc.android.procex.libs.HttpClient.HttpClientCallback;
import ph.coreproc.android.procex.libs.HttpClient.HttpClientError;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private ArrayAdapter<String> mCategories = null;
    private MapsFragment mMapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait ...");
        mContext = this;

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mMapsFragment = MapsFragment.newInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mMapsFragment)
                .commit();

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position) {
            case 0:

                break;
            case 1:
                Intent intent = new Intent(mContext, AboutActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            getCategoriesDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCategoriesDialog() {

        if (mCategories != null) {
            showCategoriesDialog();
            return;
        }

        HttpClient httpClient = new HttpClient(mContext);
        httpClient.get("https://procex.coreproc.ph/api/categories", null, new HttpClientCallback() {
            @Override
            public void onStart() {
                mProgressDialog.show();
            }

            @Override
            public void onFinish() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(HttpClientError httpClientError) {

            }

            @Override
            public void onSuccess(Response<JsonObject> response) {
                mCategories = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_singlechoice);
                JsonObject jsonResponse = response.getResult();
                JsonArray data = jsonResponse.get("data").getAsJsonArray();
                Log.i("api", data.toString());
                mCategories.add("All categories");
                for (int i = 0; i < data.size(); i++) {
                    mCategories.add(data.get(i).getAsString());
                }
                showCategoriesDialog();
            }
        });


    }

    private void showCategoriesDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                mContext);
        builderSingle.setTitle("Select a category");
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(mCategories,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mMapsFragment.removeCategory();
                        } else {
                            mMapsFragment.setCategory(mCategories.getItem(which));
                        }

                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

}
