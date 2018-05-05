package com.developer.kayali.bookstoreappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
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

public class AddBookActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // Identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;
    // Declare the views in AddBookActivity
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
        setContentView(R.layout.activity_add_book);
        // Initialize the views in AddBookActivity
        mBookNameText = findViewById(R.id.book_name_edit);
        mBookPriceText = findViewById(R.id.book_price_edit);
        mBookQuantityText = findViewById(R.id.book_quantity_edit);
        mSupplierNameText = findViewById(R.id.supplier_name_edit);
        mSupplierPhoneText = findViewById(R.id.supplier_phone_edit);
        // Set onTouchListener to each EditText view
        mBookNameText.setOnTouchListener(mTouchListener);
        mBookPriceText.setOnTouchListener(mTouchListener);
        mBookQuantityText.setOnTouchListener(mTouchListener);
        mSupplierNameText.setOnTouchListener(mTouchListener);
        mSupplierPhoneText.setOnTouchListener(mTouchListener);
        // Initialize a loader
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    // Helper method to add book to database
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
            // Insert data to Database and return Uri
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.insert_book_successful),
                        Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        } else {
            // All data fields are required
            Toast.makeText(this, getString(R.string.fill_all_data_message),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Set all fields to null when loader no longer valid
        mBookNameText.setText("");
        mBookPriceText.setText("");
        mBookQuantityText.setText("");
        mSupplierNameText.setText("");
        mSupplierPhoneText.setText("");
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
    // If back button pressed
    public void onBackPressed() {
        // No need to show Alert dialog if data has not been changed
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // If data has not been changed, show Alert dialog
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_add_book.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_add_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save_book:
                // Save book to database
                saveBook();
                return true;
            // Respond to a click on the "Home" menu option
            case android.R.id.home:
                // No need to show Alert dialog if data has not been changed
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddBookActivity.this);
                    return true;
                }
                // If data has been changed, show Alert dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddBookActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
