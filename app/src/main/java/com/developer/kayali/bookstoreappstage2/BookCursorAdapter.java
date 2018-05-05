package com.developer.kayali.bookstoreappstage2;

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
import android.widget.Toast;

import com.developer.kayali.bookstoreappstage2.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {
    // Constructor
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    // Inflate list_item layout
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Get current book id from cursor to modify the quantity
        final String id = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry._ID));
        // Initialize TextViews
        TextView bookNameView = (TextView) view.findViewById(R.id.book_name);
        TextView bookPriceView = (TextView) view.findViewById(R.id.book_price);
        TextView bookQuantityView = (TextView) view.findViewById(R.id.book_quantity);
        // Get column index for each value
        int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        // Retrieve values from cursor using column index
        String bookName = cursor.getString(bookNameColumnIndex);
        String bookPrice = cursor.getString(bookPriceColumnIndex);
        final String bookQuantity = cursor.getString(bookQuantityColumnIndex);
        // Display retrieved data on list_item layout
        bookNameView.setText(bookName);
        bookPriceView.setText(bookPrice);
        bookQuantityView.setText(bookQuantity);

        // Sale Button to reduce books quantity by one
        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int newQuantity;
                // Parse the quantity to integer if it is not empty
                if (!TextUtils.isEmpty(bookQuantity)) {
                    newQuantity = Integer.parseInt(bookQuantity);
                    // To avoid negative quantity
                    if (newQuantity == 0) {
                        Toast.makeText(context, context.getString(R.string.negative_quantity_alert), Toast.LENGTH_SHORT).show();
                    } else {
                        // Decrease quantity by one and insert it to ContentValues
                        newQuantity--;
                        ContentValues values = new ContentValues();
                        values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);
                        // Declare an Uri for the current item
                        Uri uri = Uri.withAppendedPath(BookEntry.CONTENT_URI, id);
                        // Insert a new row for product in the database, returning the Uri
                        int rowsAffected = context.getContentResolver().update(uri, values, null, null);
                        //Check if the quantity is successfully updated
                        if (rowsAffected == 0) {
                            Toast.makeText(context, context.getString(R.string.update_quantity_failed),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getString(R.string.update_quantity_successful),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        });
    }
}
