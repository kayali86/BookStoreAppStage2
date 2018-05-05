package com.developer.kayali.bookstoreappstage2.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.developer.kayali.bookstoreappstage2.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {
    public static final String LOG_TAG = BookProvider.class.getSimpleName();
    // Declare constants to the two types of content Uri
    // BOOK statement for multiple rows
    // BOOKS_ID for one row in the Database
    private static final int BOOKS = 100;
    private static final int BOOKS_ID = 101;
    // Declare an Uri matcher
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOKS_ID);
    }
    // Declare a database helper
    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Declare a readable database using mDbHelper
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // Declare a cursor
        Cursor cursor;
        // Match the Uri
        int match = sUriMatcher.match(uri);
        switch (match) {
            // Query multiple rows from database
            case BOOKS:
                // The query returns a cursor
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            // Query one row from database
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // The query returns a cursor
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            // If Uri not match throw an IllegalArgumentException
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Match the Uri
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Helper method to insert a book in database, returns Uri
    private Uri insertBook(Uri uri, ContentValues values) {
        // Get data from ContentValues and throw an IllegalArgumentException if value = null
        String bookName = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (bookName == null) {
            throw new IllegalArgumentException("Book requires a name");
        }

        Float bookPrice = values.getAsFloat(BookEntry.COLUMN_BOOK_PRICE);
        if (bookPrice == null) {
            throw new IllegalArgumentException("Book price equals null");
        }
        if (bookPrice < 0) {
            throw new IllegalArgumentException("Book requires valid price");
        }

        Integer bookQuantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (bookQuantity == null) {
            throw new IllegalArgumentException("Book quantity equals null");
        }
        if (bookQuantity < 0) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }

        String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Book requires a supplier name");
        }

        Integer supplierPhone = values.getAsInteger(BookEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Book requires a supplier phone number");
        }
        if (supplierPhone < 0) {
            throw new IllegalArgumentException("Book requires valid supplier phone number");
        }

        // Declare a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert data to database returns the row id as a long number
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify if data changed to reload and display the updated data
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        // Match the Uri
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // Notify if data changed to reload and display the updated data
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Match the Uri
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    // Helper Method to update data
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Verify the key before updating there values and then get the values from ContentValues
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String book_name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (book_name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Float bookPrice = values.getAsFloat(BookEntry.COLUMN_BOOK_PRICE);
            if (bookPrice == null) {
                throw new IllegalArgumentException("Book price equals null");
            }
            if (bookPrice < 0) {
                throw new IllegalArgumentException("Book requires valid price");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer bookQuantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (bookQuantity == null) {
                throw new IllegalArgumentException("Book quantity equals null");
            }
            if (bookQuantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Book requires a supplier name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE)) {
            Integer supplierPhone = values.getAsInteger(BookEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Book requires a supplier phone number");
            }
            if (supplierPhone < 0) {
                throw new IllegalArgumentException("Book requires valid supplier phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Declare a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Verify if the data has been updated
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Match the Uri
        final int match = sUriMatcher.match(uri);
        // Return the type of Uri
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
