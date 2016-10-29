package net.swallowsnest.sodainventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.swallowsnest.sodainventory.data.SodaContract;
import net.swallowsnest.sodainventory.data.SodaContract.SodaEntry;

import static android.R.attr.name;
import static net.swallowsnest.sodainventory.R.id.plus;

/**
 * Created by marshas on 10/28/16.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the soda data loader
     */
    private static final int EXISTING_SODA_LOADER = 0;

    /**
     * Content URI for the existing soda (null if it's a new soda)
     */
    private Uri mCurrentSodaUri;

    /**
     * EditText field to enter the soda's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the soda's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the soda's price
     */
    private EditText mPriceEditText;

    private TextView mGotTextView;

    private TextView mSoldTextView;

    /**
     * Boolean flag that keeps track of whether the soda has been edited (true) or not (false)
     */
    private boolean mSodaHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mSodaHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSodaHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new soda or editing an existing one.
        Intent intent = getIntent();
        mCurrentSodaUri = intent.getData();

        // If the intent DOES NOT contain a soda content URI, then we know that we are
        // creating a new soda.
        if (mCurrentSodaUri == null) {
            // This is a new soda, so change the app bar to say "Add a Soda"
            setTitle(getString(R.string.editor_activity_title_new_soda));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a soda that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing soda, so change app bar to say "Edit Soda"
            setTitle(getString(R.string.editor_activity_title_edit_soda));

            // Initialize a loader to read the soda data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_SODA_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_soda_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_soda_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_soda_price);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

    }

    /**
     * Get user input from editor and save soda into database.
     */
    public void saveSoda() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new soda
        // and check if all the fields in the editor are blank
        if (mCurrentSodaUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString)) {
            // Since no fields were modified, we can return early without creating a new soda.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and soda attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(SodaEntry.COLUMN_NAME, nameString);
        values.put(SodaEntry.COLUMN_QUANTITY, quantityString);
        values.put(SodaEntry.COLUMN_PRICE, priceString);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(SodaEntry.COLUMN_PRICE, price);

        // Determine if this is a new or existing soda by checking if mCurrentSodaUri is null or not
        if (mCurrentSodaUri == null) {
            // This is a NEW soda, so insert a new soda into the provider,
            // returning the content URI for the new soda.
            Uri newUri = getContentResolver().insert(SodaEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_soda_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_soda_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING soda, so update the soda with content URI: mCurrentSodaUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentSodaUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentSodaUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_soda_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_soda_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sellSoda() {

        ContentValues values = new ContentValues();

        int sell = Integer.valueOf(mQuantityEditText.getText().toString());


        if (sell == 0) {
            return;
        } else {
            sell = sell - 1;
        }

        values.put(SodaEntry.COLUMN_QUANTITY, sell);

        getContentResolver().update(mCurrentSodaUri, values, null, null);

        Toast.makeText(this, "successfully sold 1", Toast.LENGTH_SHORT).show();
    }

    public void getSoda() {

        ContentValues values = new ContentValues();

        int got = Integer.valueOf(mQuantityEditText.getText().toString());


        if (got == 0) {
            return;
        } else {
            got = got + 1;
        }

        values.put(SodaEntry.COLUMN_QUANTITY, got);

        getContentResolver().update(mCurrentSodaUri, values, null, null);

        Toast.makeText(this, "successfully got 1", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new soda, hide the "Delete" menu item.
        if (mCurrentSodaUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save soda to database
                saveSoda();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_sell:
                sellSoda();
                return true;
            case R.id.action_get:
                getSoda();
                ;
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the soda hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mSodaHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the soda hasn't changed, continue with handling back button press
        if (!mSodaHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all soda attributes, define a projection that contains
        // all columns from the soda table
        String[] projection = {
                SodaEntry._ID,
                SodaEntry.COLUMN_NAME,
                SodaEntry.COLUMN_QUANTITY,
                SodaEntry.COLUMN_PRICE,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentSodaUri,         // Query the content URI for the current soda
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of soda attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Float.toString(price));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the soda.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this soda.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the soda.
                deleteSoda();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the soda.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the soda in the database.
     */
    private void deleteSoda() {
        // Only perform the delete if this is an existing soda.
        if (mCurrentSodaUri != null) {
            // Call the ContentResolver to delete the soda at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentSodaUri
            // content URI already identifies the soda that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentSodaUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_soda_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_soda_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * This method is called when the order button is clicked.
     */
    public void submitOrder(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.soda_order_for));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}