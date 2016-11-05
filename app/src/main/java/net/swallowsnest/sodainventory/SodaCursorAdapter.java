package net.swallowsnest.sodainventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import net.swallowsnest.sodainventory.data.SodaContract.SodaEntry;

/**
 * {@link SodaCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of soda data as its data source. This adapter knows
 * how to create list items for each row of soda data in the {@link Cursor}.
 */
public class SodaCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link SodaCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public SodaCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the soda data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current soda can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        final TextView nameTextView = (TextView) view.findViewById(R.id.soda_name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.soda_quantity);
        final TextView priceTextView = (TextView) view.findViewById(R.id.soda_price);
        Button sellButton = (Button) view.findViewById(R.id.list_sell_soda);

        // Find the columns of soda attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(SodaEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(SodaEntry.COLUMN_PRICE);

        // Read the soda attributes from the Cursor for the current soda
        int rowId = cursor.getInt(idColumnIndex);
        final String sodaName = cursor.getString(nameColumnIndex);
        final String sodaQuantity = cursor.getString(quantityColumnIndex);
        String sodaPrice = cursor.getString(priceColumnIndex);

        // If the soda price is empty string or null, then use some default text
        // that says "Unknown price", so the TextView isn't blank.
        if (TextUtils.isEmpty(sodaPrice)) {
            sodaPrice = context.getString(R.string.unknown_price);
        }

        // Update the TextViews with the attributes for the current soda
        nameTextView.setText(sodaName);
        nameTextView.setTag(rowId);
        quantityTextView.setText(sodaQuantity);
        priceTextView.setText(sodaPrice);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                int sellOne = Integer.valueOf(quantityTextView.getText().toString());

                if (sellOne == 0) {
                    return;
                }
                sellOne = --sellOne;

                values.put(SodaEntry.COLUMN_QUANTITY, sellOne);
                int rowid = (Integer) nameTextView.getTag();

                Uri currentSodaUri = ContentUris.withAppendedId(SodaEntry.CONTENT_URI, rowid);
                int rowAffected = context.getContentResolver().update(currentSodaUri, values, null, null);
            }

        });

    }

}


