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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.kayali.bookstoreappstage2.data.BookContract.BookEntry;

public class EditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // Identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;
    // The current book Uri
    private Uri mCurrentBookUri;
    // Declare views in EditActivity
    private EditText mBookNameText;
    private EditText mBookPriceText;
    private EditText mBookQuantityText;
    private EditText mSupplierNameText;
    private EditText mSupplierPhoneText;
    // Boolean flag that keeps track of whether the book has been edited (true) or not (false)
    private boolean mBookHasChanged = false;
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        // Get book uri from Intent
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        // Initialize the views in EditActivity
        mBookNameText = (EditText) findViewById(R.id.book_name_edit);
        mBookPriceText = (EditText) findViewById(R.id.book_price_edit);
        mBookQuantityText = (EditText) findViewById(R.id.book_quantity_edit);
        mSupplierNameText = (EditText) findViewById(R.id.supplier_name_edit);
        mSupplierPhoneText = (EditText) findViewById(R.id.supplier_phone_edit);
        // Set onTouchListener to each EditText view
        mBookNameText.setOnTouchListener(mTouchListener);
        mBookPriceText.setOnTouchListener(mTouchListener);
        mBookQuantityText.setOnTouchListener(mTouchListener);
        mSupplierNameText.setOnTouchListener(mTouchListener);
        mSupplierPhoneText.setOnTouchListener(mTouchListener);
        // Initialize a loader
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    // Helper method to update book data to database
    private void saveBook() {
        // Get data from editText views
        String bookNameString = mBookNameText.getText().toString().trim();
        String bookPriceString = mBookPriceText.getText().toString().trim();
        String bookQuantityString = mBookQuantityText.getText().toString().trim();
        String supplierNameString = mSupplierNameText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneText.getText().toString().trim();
        // Check data validation - No need to insert data to database if all values are empty
        if (TextUtils.isEmpty(bookNameString) && TextUtils.isEmpty(bookPriceString) &&
                TextUtils.isEmpty(bookQuantityString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString)) {
            return;
        }
        //  Check data validation before parsing it to integer to avoid exceptions
        float bookPrice = -1;
        if (!TextUtils.isEmpty(bookPriceString)) {
            bookPrice = Float.parseFloat(bookPriceString);
        }

        int bookQuantity = -1;
        if (!TextUtils.isEmpty(bookQuantityString)) {
            bookQuantity = Integer.parseInt(bookQuantityString);
        }

        long supplierPhoneNumber = -1;
        if (!TextUtils.isEmpty(supplierPhoneString)) {
            supplierPhoneNumber = Long.parseLong(supplierPhoneString);
        }
        // If all data are valid, add it to ContentValues
        if (!TextUtils.isEmpty(bookNameString) &&
                bookPrice >= 0 &&
                bookQuantity >= 0 &&
                !TextUtils.isEmpty(supplierNameString) &&
                supplierPhoneNumber >= 0) {
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_BOOK_NAME, bookNameString);
            values.put(BookEntry.COLUMN_BOOK_PRICE, bookPrice);
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneNumber);
            // Update book data and return number of updated rows as Integer
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                String messageText = getString(R.string.update_book_failed);
                showToastMessage(messageText);
            } else {
                // Otherwise, the update was successful and we can display a toast.
                String messageText = getString(R.string.update_book_successful);
                showToastMessage(messageText);
                // Exit activity
                finish();
            }
        } else {
            String messageText = getString(R.string.fill_all_data_message);
            showToastMessage(messageText);
        }
    }

    // Helper method to delete book
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
                            Intent intent = new Intent(EditActivity.this, CatalogActivity.class);
                            startActivity(intent);
                            String messageText = getString(R.string.delete_book_successful);
                            showToastMessage(messageText);
                        }
                    }
                };
        // Show a dialog that notifies the user they have unsaved changes
        showDeleteConfirmDialog(discardButtonClickListener);
    }

    // Display an alert dialog to confirm book delete
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
            String bookQuantity = data.getString(bookQuantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = data.getString(supplierPhoneColumnIndex);

            // Display retrieved data on activity_book_details_layout
            mBookNameText.setText(bookName);
            mBookPriceText.setText(bookPrice);
            mBookQuantityText.setText(bookQuantity);
            mSupplierNameText.setText(supplierName);
            mSupplierPhoneText.setText(supplierPhoneNumber);
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

    // Helper Method to show Toast messages
    private void showToastMessage(String messageText) {
        Toast.makeText(this, messageText,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    // If back button pressed
    public void onBackPressed() {
        // No need to show Alert dialog if data has not been changed
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // Display an alert dialog when there are unsaved changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save_book:
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete_book:
                deleteBook();
                return true;
            // Respond to a click on the "Home" menu option
            case android.R.id.home:
                // No need to show Alert dialog if data has not been changed
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                // If data has been changed, show Alert dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
