package com.example.dankwansere.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dankwansere.popularmovies.Database.DBHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity {

    public Movie movieDetail = new Movie();
    private Bitmap mainbitmap;
    private String movieID;
    private FloatingActionButton fab;
    private Bitmap mainBackdropBitmap;
    protected String[] movieTrailerUrl; // = "";
    protected String youtubeBaseUrl = "https://www.youtube.com/watch?v=";
    protected String movieReview;
    private ProgressDialog progress;
    private MovieTrailer[] movieTrailers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDatabase();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //Retrieved the serialized object that was passed in the intent, and store it in the local Movie object
        Intent detailIntent = this.getIntent();
        if(detailIntent != null && detailIntent.hasExtra("Serialized_Movie_Object")) {
            movieDetail =  (Movie) detailIntent.getSerializableExtra("Serialized_Movie_Object");
        }
        //Set movie ID to use to retrieve trailer;
        this.movieID = Integer.toString(movieDetail.getId());

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();


        this.updateMovieDetailView();
        this.fetchTrailerAndReview(this.movieID);

    }

    public void addToDatabase()
    {

        if(movieDetail.isFavourite()) { //If movie is already in the favourite list, then remove it.
            this.deleteData();
        }
        else { //If movie is not in favourite list, then add it to favourite list
            this.addData();
        }
    }

    //Description: To call async task to fetch trailer
    private void fetchTrailerAndReview(String id) {
        FetchMovieTrailer fetchMovieTrailer = new FetchMovieTrailer();
        FetchMovieReview fetchMovieReview = new FetchMovieReview();
        fetchMovieTrailer.execute(id);
        fetchMovieReview.execute(id);
    }

    //Update each view according to the movie object
    private void updateMovieDetailView()
    {

        //Setting title of App abar Layout to movie title
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(movieDetail.getTitle());
        }

        //Set favourite star icon to true if movie is already in favourite list
        if(movieDetail.isFavourite()) {

            Drawable starFilled = getResources().getDrawable(R.drawable.star_filled);
            fab.setImageDrawable(starFilled);

        }

        //Retrieve id of ImageView to store the movie poster and backdrop pictures
        ImageView posterPicture = (ImageView)findViewById(R.id.detail_movie_poster);
        ImageView collapseImage = (ImageView)findViewById(R.id.imageCollapse);
        ImageView play_Button = (ImageView)findViewById(R.id.detail_play_button_collapse);

        /*If poster path is null, then it means the input received is from the database
         * Images in database were stored as blob files. To convert them back to images
         * must convert byte to bitmap and store in image view
         */

        if(movieDetail.getPosterPath() == null ) {
            byte[] data = movieDetail.getBytes();
            byte[] backDropData = movieDetail.getBackDropImageBytes();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap backdropBitmap = BitmapFactory.decodeByteArray(backDropData, 0, backDropData.length);
            posterPicture.setImageBitmap(bitmap);
            collapseImage.setImageBitmap(backdropBitmap);
            Drawable playButton = getResources().getDrawable(R.drawable.play_button_overlay);
            play_Button.setImageDrawable(playButton);
        }
        else {
            Picasso.with(this).load(movieDetail.getPosterPath()).fit().into(posterPicture);
            Picasso.with(this).load(movieDetail.getBackDrop()).fit().into(collapseImage);
            Drawable playButton = getResources().getDrawable(R.drawable.play_button_overlay);
            play_Button.setImageDrawable(playButton);

        }

        String rating = "Ratings: ";
        String releaseDate = movieDetail.getReleaseDate();
        String fullRatings;
        TextView movieRatingsCount = (TextView)findViewById(R.id.detail_ratings);
        String ratingAverage = "";
        ratingAverage = Float.toString(movieDetail.getVote_average());

        //Arranging ratings and release date into one string, and making specific part of the string bold
        fullRatings = rating + "<b>" + ratingAverage + "</b>" + "\n" + "Release Date: " + "<b>" + releaseDate + "</b>";
        //Setting the movie ratings and using HTML.formHtml method to properly handle the "<b>" html tags to make specific parts of the string bold
        movieRatingsCount.setText(Html.fromHtml(fullRatings));

        //Retrieve Id of movie description textview to populate with Movie description of selected movie
        TextView movieDescription = (TextView)findViewById(R.id.detail_movie_description);
        if(movieDetail.getOverView().equals("null")) {
            movieDescription.setText("Movie plot unavailable");}
        else {
            movieDescription.setText(movieDetail.getOverView());
        }
    }

    //Open movie trailer in Youtube or Web browser
    //A maximum of 3 trailers will be played if available
    //1st trailer will always be in the Collapse layout bar
    public void fetchTrailer(View view) {
        int trailerNum = 0;
        switch (view.getId()) {
            case R.id.detail_play_button_collapse:
                trailerNum = 0;
                break;
            case R.id.detail_trailer2_text:
                trailerNum = 1;
                break;
            case R.id.detail_trailer_2_image:
                trailerNum = 1;
                break;
            case R.id.detail_trailer3_text:
                trailerNum = 2;
                break;
            case R.id.detail_trailer_3_image:
                trailerNum = 2;
                break;
        }

        Context context = getApplicationContext();
        CharSequence text = "Now Playing " + movieDetail.getTitle() + " trailer";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(movieTrailerUrl[trailerNum]));
        if(intent.resolveActivity(this.getPackageManager()) != null) {

            toast.show();
            startActivity(intent);
        }
        else {
            System.out.println("Could not start intent");
        }
    }

    //Add movie to database
    public void addData() {
        DBHandler db = new DBHandler(this, null, null, 1);
        this.movieDetail.setIsFavourite(true);

        //Before saving an image to a SQLite database, image must be converted to byte data
        byte[] data = getBitmapAsByteArray(mainbitmap);
        byte[] backDropData = getBitmapAsByteArray(mainBackdropBitmap);
        if(data == null) {
        }

        this.movieDetail.setReview(this.movieReview);

        try {
            db.addMovie(this.movieDetail, this.movieTrailerUrl[0], data,backDropData );

            //Set floating action button to filled star so user will know movie has been added to database
            Drawable starFilled = getResources().getDrawable(R.drawable.star_filled);
            fab.setImageDrawable(starFilled);

            Context context = getApplicationContext();
            CharSequence text = movieDetail.getTitle() + " has been added to favourites";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }
        catch(Exception ex) {
            System.out.println("Could not add to favourite");
        }
    }

    public void deleteData() {
        DBHandler db = new DBHandler(this, null, null, 1);

        try {
            //Delete Movie from favourite list and set favourite star to blank
            db.deleteMovie(this.movieDetail.getId());

            //Set floating action button to blank star so user will know movie is no longer in favourite list
            Drawable starFilled = getResources().getDrawable(R.drawable.star_blank);
            fab.setImageDrawable(starFilled);

            Context context = getApplicationContext();
            CharSequence text = movieDetail.getTitle() + " has been removed from favourites";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        catch(Exception ex) {
            System.out.println("Could not delete movie..Error: " + ex.toString());
        }
    }

    //To convert Bitmap image to bytes
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, MovieListActivity.class));
            return true;
        }

        else if(id == R.id.menu_item_share){
            sendToSMS();
        }

        return super.onOptionsItemSelected(item);
    }

    //Send frst trailer of movie via text msg
    public void sendToSMS() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("sms:"));
        String messageToSend = "Hey check out " + movieDetail.getTitle() + " trailer: " + movieTrailerUrl[0].toString();

        intent.putExtra("sms_body", messageToSend);
        if(intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);

        }
        else {
            System.out.println("Couldn't not call sms intent");
        }

    }

    //Parse movie trailer Json String
    protected MovieTrailer[] parseTrailerJson(String jsonString) {

        try {
            JSONObject rootObject = new JSONObject(jsonString);
            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = rootObject.optJSONArray("results");
            movieTrailers = new MovieTrailer[jsonArray.length()];

            for(int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                movieTrailers[i] = new MovieTrailer(jsonObject.getString("name"), jsonObject.getString("key"));

            }

        }
        catch(Exception e) {
            System.out.println("Parsing Movie trailer json error: " + e.toString());
        }

        return movieTrailers;
    }

    //Parse movie review Json String
    protected String paraseReviewJson(String jsonString){

        String localMovieReview = "";

        try {
            JSONObject rootObject = new JSONObject(jsonString);
            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = rootObject.optJSONArray("results");

            // for(int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            localMovieReview = jsonObject.getString("content");
            //  }

        }
        catch(Exception e) {
            System.out.println("Parsing Movie review json error: " + e.toString());
        }
        return localMovieReview;
    }

    public class FetchMovieTrailer extends AsyncTask<String, Void, String> {

        String trailerJsonStr = null;
        Uri.Builder urlBuilder = new Uri.Builder();
        String apikey = getString(R.string.ApiKey);
        Bitmap bitmap;
        Bitmap backDropBitmap;

        //Default Constructor
        public FetchMovieTrailer() {}

        protected void onPostExecute(String strings) {

            mainbitmap = bitmap;
            mainBackdropBitmap = backDropBitmap;

            //parsing movie trailer json string to MovieTrailer object(s)
            MovieTrailer[] trailerRetrieved = parseTrailerJson(trailerJsonStr);
            movieTrailerUrl = new  String[trailerRetrieved.length];

            //If only 1 trailer is available, the trailer 2 and trailer 3 views will be set to invisible
            View Trailer1 = findViewById(R.id.detail_linear_trailer2);
            View Trailer2 = findViewById(R.id.detail_linear_trailer3);

            switch (movieTrailerUrl.length) {
                case 1:
                    Trailer1.setVisibility(View.INVISIBLE);
                    Trailer2.setVisibility(View.INVISIBLE);
                    break;

                case 2:
                    Trailer2.setVisibility(View.INVISIBLE);
                    break;
            }

            try {
                for(int i = 0; i < trailerRetrieved.length; i++) {
                    movieTrailerUrl[i] = youtubeBaseUrl + trailerRetrieved[i].getTrailerKey();
                }
            }
            catch (Exception ex) {
                System.out.println("Error loading trailer url: " + ex.toString());
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            //Converting movie image from url to bitmap, to be capable to store in database
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(movieDetail.getPosterPath()).getContent());
                backDropBitmap = BitmapFactory.decodeStream((InputStream) new URL(movieDetail.getBackDrop()).getContent());
            } catch (MalformedURLException e) {
                System.out.println("Error: " + e.toString());
            } catch (IOException e) {
                System.out.println("Error: " + e.toString());
            }

            // Will contain the raw JSON response as a string.

            try {


                urlBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(params[0])
                        .appendPath("videos")
                        .appendQueryParameter("api_key", apikey);


                URL url = new URL(urlBuilder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    //return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    //return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("MovieFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                //return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieFragment", "Error closing stream", e);
                    }
                }
            }
            return trailerJsonStr;
        }
    }

    public class FetchMovieReview extends AsyncTask<String, Void, String> {

        String trailerJsonStr = null;
        Uri.Builder urlBuilder = new Uri.Builder();
        String apikey = getString(R.string.ApiKey); //insert API key here
        TextView sample = (TextView)findViewById(R.id.detail_movie_review);


        //Default Constructor
        public FetchMovieReview() {}

        protected void onPostExecute(String strings) {

            //parsing movie trailer json string to MovieTrailer object(s)
            String parsedMovieReview = paraseReviewJson(trailerJsonStr);
            movieReview = parsedMovieReview;

            TextView movieReview_textView = (TextView)findViewById(R.id.detail_movie_review);
            try {
                if(movieDetail.getReview() == null) {
                    if(movieReview.equals("")) {
                        movieReview_textView.setText("NO REVIEW AVAILABLE AT THE MOMENT");
                    }
                    else {
                        movieReview_textView.setText(movieReview);
                    }

                }
                else {
                    if(movieReview.equals("")) {
                        movieReview_textView.setText("NO REVIEW AVAILABLE AT THE MOMENT");
                    }
                    else {
                        movieReview_textView.setText(movieDetail.getReview());
                    }
                }
            }
            catch(Exception ex) {
                System.out.println("Movie Review Error: " + ex.toString());
            }
            finally {
                progress.dismiss();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            try {
                urlBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(params[0])
                        .appendPath("reviews")
                        .appendQueryParameter("api_key", apikey);


                URL url = new URL(urlBuilder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    //return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    //return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("Movie Review", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                //return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Movie Review", "Error closing stream", e);
                    }
                }
            }
            return trailerJsonStr;
        }
    }
}
