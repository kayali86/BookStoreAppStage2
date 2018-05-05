package com.developer.kayali.bookstoreappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kayali.bookstoreappstage2.data.BookContract.BookEntry;

public class BookDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;
    // The current book Uri
    private Uri mCurrentBookUri;
    // The bookQuantity as a global variable to modify it using increment and decrement buttons
    private String bookQuantity;
    // Declare views in BookDetailsActivity
    private TextView mBookNameText;
    private TextView mBookPriceText;
    private TextView mBookQuantityText;
    private TextView mSupplierNameText;
    private TextView mSupplierPhoneText;
    private ImageButton mCallButton;
    private ImageButton mQuantityDecrementButton;
    private ImageButton mQuantityIncrementButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        // FloatingActionButton to transport the current book uri to EditActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookDetailsActivity.this, EditActivity.class);
                intent.setData(mCurrentBookUri);
                startActivity(intent);
            }
        });
        // Get book uri from Intent
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        // Initialize views in BookDetailsActivity
        mBookNameText = (TextView) findViewById(R.id.book_name_view);
        mBookPriceText = (TextView) findViewById(R.id.book_price_view);
        mBookQuantityText = (TextView) findViewById(R.id.book_quantity_view);
        mSupplierNameText = (TextView) findViewById(R.id.supplier_name_view);
        mSupplierPhoneText = (TextView) findViewById(R.id.supplier_phone_view);
        mCallButton = (ImageButton) findViewById(R.id.call_button);
        mQuantityDecrementButton = (ImageButton) findViewById(R.id.decrement_quantity_button);
        mQuantityIncrementButton = (ImageButton) findViewById(R.id.increment_quantity_button);
        // Initialize loader
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    // Helper Method to delete book
    private void deleteBook() {
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
                        if (rowsDeleted == 0) {
                            String messageText = getString(R.string.delete_book_failed);
                            showToastMessage(messageText);
                        } else {
                            finish();
                            String messageText = getString(R.string.delete_book_successful);
                            showToastMessage(messageText);
                        }
                    }
                };
        // Show a dialog that notifies the user they have unsaved changes
        showDeleteConfirmDialog(discardButtonClickListener);
    }

    // Helper Method to show Toast messages
    private void showToastMessage(String messageText) {
        Toast.makeText(this, messageText,
                Toast.LENGTH_SHORT).show();
    }

    // Display an alert dialog
    private void showDeleteConfirmDialog(
            DialogInterface.OnClickListener deleteButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirm_dialog_msg);
        builder.setPositiveButton(R.string.delete, deleteButtonClickListener);
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Projections to get data from database
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};
        // Return a cursorLoader to get data and display book details
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // If there ara no data, no need to continue getting data
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            // Get column index for each value
            int bookNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int bookPriceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int bookQuantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            // Retrieve values from cursor using column index
            String bookName = data.getString(bookNameColumnIndex);
            String bookPrice = data.getString(bookPriceColumnIndex);
            bookQuantity = data.getString(bookQuantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            final String supplierPhoneNumber = data.getString(supplierPhoneColumnIndex);

            // Display retrieved data on activity_book_details_layout
            mBookNameText.setText(bookName);
            mBookPriceText.setText(bookPrice);
            mBookQuantityText.setText(bookQuantity);
            mSupplierNameText.setText(supplierName);
            mSupplierPhoneText.setText(supplierPhoneNumber);

            // If call imageButton is clicked, call dial action
            mCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", supplierPhoneNumber, null));
                    startActivity(intent);
                }
            });

            // Decrease Quantity by one
            mQuantityDecrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(bookQuantity)) {
                        int quantity = Integer.parseInt(bookQuantity);
                        int newQuantity = quantity - 1;
                        updateQuantity(newQuantity);
                    }
                }
            });

            // Increase Quantity by one
            mQuantityIncrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(bookQuantity)) {
                        int quantity = Integer.parseInt(bookQuantity);
                        int newQuantity = quantity + 1;
                        updateQuantity(newQuantity);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear all fields when loader no longer valid
        mBookNameText.setText("");
        mBookPriceText.setText("");
        mBookQuantityText.setText("");
        mSupplierNameText.setText("");
        mSupplierPhoneText.setText("");
    }

    // Helper Method to update quantity
    private void updateQuantity(int newQuantity) {
        // Check if quantity negative
        if (newQuantity < 0) {
            String messageText = getString(R.string.negative_quantity_alert);
            showToastMessage(messageText);
        } else {
            // Insert data in ContentValues
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, newQuantity);
            // Update quantity and return number of updated rows
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                String messageText = getString(R.string.update_quantity_failed);
                showToastMessage(messageText);
            } else {
                String messageText = getString(R.string.update_quantity_successful);
                showToastMessage(messageText);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Delete current book when action button clicked
            case R.id.action_delete_book:
                deleteBook();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(BookDetailsActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
