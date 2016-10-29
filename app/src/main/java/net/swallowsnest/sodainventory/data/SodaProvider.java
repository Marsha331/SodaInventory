package net.swallowsnest.sodainventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static net.swallowsnest.sodainventory.data.SodaContract.CONTENT_AUTHORITY;
import static net.swallowsnest.sodainventory.data.SodaContract.PATH_SODAS;
import static net.swallowsnest.sodainventory.data.SodaContract.SodaEntry;

/**
 * Created by marshas on 10/28/16.
 */

public class SodaProvider extends ContentProvider {
    /**
     * URI matcher code for the content URI for the sodas table
     */
    private static final int SODAS = 100;

    /**
     * URI matcher code for the content URI for a single soda in the sodas table
     */
    private static final int SODA_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_SODAS, SODAS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_SODAS + "/#", SODA_ID);
    }

    private SodaDbHelper mDbHelper;
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = SodaProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new SodaDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SODAS:
                // For the SODAS code, query the sodas table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the sodas table.
                cursor = database.query(SodaEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case SODA_ID:
                // For the SODA_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.sodas/sodas/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = SodaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the sodas table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(SodaEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SODAS:
                return insertSoda(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a soda into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSoda(Uri uri, ContentValues values) {

        //Check that the soda has a name
        String name = values.getAsString(SodaEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Soda requires a name.");
        }

        //Check that the quantity is greater than 0
        Integer quantity = values.getAsInteger(SodaEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Soda requires quantity greater than zero.");
        }

        //Check that the price is also valid.
        Integer price = values.getAsInteger(SodaEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Soda requires a price.");
        }


        //Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(SodaEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SODAS:
                return updateSoda(uri, contentValues, selection, selectionArgs);
            case SODA_ID:
                // For the SODA_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = SodaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateSoda(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update sodas in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more sodas).
     * Return the number of rows that were successfully updated.
     */
    private int updateSoda(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        /// If the {@link SodaEntry#COLUMN_SODA_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(SodaEntry.COLUMN_NAME)) {
            String name = values.getAsString(SodaEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Soda requires a name");
            }
        }

        // If the {@link SodaEntry#COLUMN_SODA_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(SodaEntry.COLUMN_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(SodaEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Soda requires valid quantity.");
            }
        }

        // If the {@link SodaEntry#COLUMN_SODA_PRICE} key is present,
        //Check that price isn't empty
        if (values.containsKey(SodaEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(SodaEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Soda requires a price.");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(SodaEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SODAS:
                // Delete all rows that match the selection and selection args
                return database.delete(SodaEntry.TABLE_NAME, selection, selectionArgs);
            case SODA_ID:
                // Delete a single row given by the ID in the URI
                selection = SodaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SodaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Returns the number of database rows affected by the update statement
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SODAS:
                return SodaEntry.CONTENT_LIST_TYPE;
            case SODA_ID:
                return SodaEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

