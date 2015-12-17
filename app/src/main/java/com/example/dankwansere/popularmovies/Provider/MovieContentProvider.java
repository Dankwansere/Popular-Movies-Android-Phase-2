package com.example.dankwansere.popularmovies.Provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.dankwansere.popularmovies.Database.DBHandler;

public class MovieContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.dankwansere.popularmovies.Provider.MovieContentProvider";
    private static final String MOVIES_TABLE = "movies";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MOVIES_TABLE);

    public static final int MOVIES = 1;
    public static final int MOVIES_ID = 2;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DBHandler myDB;

    static {
        sURIMatcher.addURI(AUTHORITY, MOVIES_TABLE, MOVIES);
        sURIMatcher.addURI(AUTHORITY, MOVIES_TABLE + "/#", MOVIES_ID);
    }

    public MovieContentProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case MOVIES:
                rowsDeleted = sqlDB.delete(DBHandler.TABLE_MOVIES, selection, selectionArgs);
                break;

            case MOVIES_ID:
                String movieTitle = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DBHandler.TABLE_MOVIES, DBHandler.COLUMN_MOVIE_TITLE + "=" + movieTitle, null);
                }

                else {
                    rowsDeleted = sqlDB.delete(DBHandler.TABLE_MOVIES, DBHandler.COLUMN_MOVIE_TITLE + "=" + movieTitle + " and " + selection, selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
        public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case MOVIES:
                try {
                    id = sqlDB.insert(DBHandler.TABLE_MOVIES, null, values);
                }
                catch (Exception ex) {
                    System.out.println("Content Provider could not insert data into database: " + ex.toString());
                }
                finally {
                    sqlDB.close();
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(MOVIES_TABLE + "/" + id);
    }

    @Override
    public boolean onCreate() {
        myDB = new DBHandler(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHandler.TABLE_MOVIES);

        Cursor cursor = null;

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case MOVIES_ID:
                break;

            case MOVIES:
                break;

            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        try {
            cursor = queryBuilder.query(myDB.getReadableDatabase(), null, null, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        catch (Exception e) {
            System.out.println("Could not read data from database: " + e.toString());
        }



        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        /*Not Required to be implemented. No rows would be required to be updated*/

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
