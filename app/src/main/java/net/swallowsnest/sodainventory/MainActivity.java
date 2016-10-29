package net.swallowsnest.sodainventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import net.swallowsnest.sodainventory.data.SodaContract.SodaEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the soda data loader
     */
    private static final int SODA_LOADER = 0;

    int soldValue = 0;
    int getValue = 0;

    /**
     * Adapter for the ListView
     */
    SodaCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup button to open EditorActivity
        Button plus = (Button) findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the soda data
        ListView sodaListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        sodaListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of soda data in the Cursor.
        // There is no soda data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new SodaCursorAdapter(this, null);
        sodaListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        sodaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific soda that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link SodaEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.sodas/sodas/2"
                // if the soda with ID 2 was clicked on.
                Uri currentSodaUri = ContentUris.withAppendedId(SodaEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentSodaUri);

                // Launch the {@link EditorActivity} to display the data for the current soda.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(SODA_LOADER, null, this);
    }


    /**
     * Helper method to insert hardcoded soda data into the database. For debugging purposes only.
     */
    private void insertSoda() {
        // Create a ContentValues object where column names are the keys,
        // and soda attributes are the values.
        ContentValues values = new ContentValues();
        values.put(SodaEntry.COLUMN_NAME, "Pepsi");
        values.put(SodaEntry.COLUMN_QUANTITY, 24);
        values.put(SodaEntry.COLUMN_PRICE, 1.5);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link SodaEntry#CONTENT_URI} to indicate that we want to insert
        // into the sodas database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(SodaEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all sodas in the database.
     */
    private void deleteAllSodas() {
        int rowsDeleted = getContentResolver().delete(SodaEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from soda database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        deleteAllSodas();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                SodaEntry._ID,
                SodaEntry.COLUMN_NAME,
                SodaEntry.COLUMN_QUANTITY,
                SodaEntry.COLUMN_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                SodaEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link SodaCursorAdapter} with this new cursor containing updated soda data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

}