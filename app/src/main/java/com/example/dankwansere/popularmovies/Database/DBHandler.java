package com.example.dankwansere.popularmovies.Database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dankwansere.popularmovies.Movie;
import com.example.dankwansere.popularmovies.Provider.MovieContentProvider;

/**
 * Created by Dankwansere on 11/30/2015.
 */
public class DBHandler extends SQLiteOpenHelper {

    private ContentResolver myCR;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movieDB.db";
    public static final String TABLE_MOVIES = "movies";

    //Initializing column names for movie Database
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MOVIE_TITLE = "movieTitle";
    public static final String COLUMN_PLOT_SUMMARY = "plotSummary";
    public static final String COLUMN_USER_REVIEWS = "userReviews";
    public static final String COLUMN_RELEASE_DATE = "movieReleaseDate";
    public static final String COLUMN_RATINGS = "ratings";
    public static final String COLUMN_TRAILER_URL = "trailerURL";
    public static final String COLUMN_BACKDROP_IMAGE = "backdropimage";
    public static final String COLUMN_FAVOURITE = "favourite";
    public static final String COLUMN_IMAGE = "image";
    //public static final String COLUMN_FAVOURITE = "favourites";

    String CREATE_MOVIES_TABLE;

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        myCR = context.getContentResolver();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
         CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_MOVIE_TITLE + " TEXT, " + COLUMN_PLOT_SUMMARY + " TEXT, "  +
                COLUMN_TRAILER_URL + " TEXT, " + COLUMN_RELEASE_DATE + " TEXT, " + COLUMN_RATINGS + " TEXT, " +
                COLUMN_USER_REVIEWS + " TEXT, " + COLUMN_FAVOURITE + " TEXT, " + COLUMN_BACKDROP_IMAGE + " BLOB, " + COLUMN_IMAGE + " BLOB" + ")";

        try {
            db.execSQL(CREATE_MOVIES_TABLE);
        }
        catch(Exception e) {
            System.out.println("GEGE ERROR: Could not create table" );
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        onCreate(db);

    }

    public void addMovie(Movie movie, String trailerUrl, byte[] bytes, byte[] backdropBytes) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_MOVIE_TITLE, movie.getTitle());
        values.put(COLUMN_PLOT_SUMMARY, movie.getOverView());
        values.put(COLUMN_TRAILER_URL, trailerUrl);
        values.put(COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(COLUMN_RATINGS, movie.getVote_average());
        values.put(COLUMN_USER_REVIEWS, movie.getReview());
        values.put(COLUMN_FAVOURITE, Boolean.toString(movie.isFavourite()));
        values.put(COLUMN_BACKDROP_IMAGE, backdropBytes);
        values.put(COLUMN_IMAGE, bytes);


        try {
            myCR.insert(MovieContentProvider.CONTENT_URI, values);
        }
        catch (Exception err){
            System.out.println("Error calling contentProvider: " + err.toString());
        }

      /*  SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.insert(TABLE_MOVIES, null, values);
        }
        catch(Exception e) {

            System.out.println("GEGE: Could not insert into table: " + e.toString());
        }
        finally {
            db.close();
        } */

    }


    public Movie[] findMovie() {
        //String query = "SELECT * FROM " + TABLE_MOVIES;
        String query = "SELECT * FROM " + TABLE_MOVIES;

        Cursor cursor = myCR.query(MovieContentProvider.CONTENT_URI, null, null, null, null);


        int index = 0;
        Movie[] favouriteMovies = new Movie[cursor.getCount()];

        try {
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {

                    favouriteMovies[index] = new Movie();
                    favouriteMovies[index].setTitle(cursor.getString(1));
                    favouriteMovies[index].setOverView(cursor.getString(2));
                    favouriteMovies[index].setTrailerURL(cursor.getString(3));
                    favouriteMovies[index].setReleaseDate(cursor.getString(4));
                    favouriteMovies[index].setVote_average(Float.parseFloat(cursor.getString(5)));
                    favouriteMovies[index].setReview(cursor.getString(6));
                    favouriteMovies[index].setBytes(cursor.getBlob(7));

                    index++;
                    cursor.moveToNext();
                }
            }
        }
        catch(Exception e) {
            System.out.println("Error binding data to Movie Objects: " + e.toString());
        }


        return favouriteMovies;
    }

    public boolean deleteMovie(int movieID) {
        boolean result = false;

        String selection = "_id = \"" + movieID + "\"";
        int rowsDeleted = myCR.delete(MovieContentProvider.CONTENT_URI, selection, null);

        if(rowsDeleted > 0) {
            result = true;
        }

       /* try {

            if (cursor.moveToFirst()) {
                int id = (Integer.parseInt(cursor.getString(0)));
                String name = cursor.getString(1);
                db.delete(TABLE_MOVIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
                cursor.close();
                result = true;
            }
        }

        catch(Exception e) {
            System.out.println("GEGE: Could not delete database row: " + e.toString());
        }

        finally {
            db.close();
        } */

        return result;
    }
}
