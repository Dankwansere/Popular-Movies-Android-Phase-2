package com.example.dankwansere.popularmovies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    public Movie movieDetail = new Movie();
    private String movieID;
    protected String youtubeBaseUrl = "https://www.youtube.com/watch?v=";
    private Bitmap mainbitmap;
    private Bitmap mainBackdropBitmap;
    protected String movieReview;
    private FloatingActionButton floatingActionButton;
    private ProgressDialog progress;
    private boolean dirtyFlag = false;
    protected String[] movieTrailerUrl;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            int protrait = Configuration.ORIENTATION_PORTRAIT;
            //If dirty flag is set to true, means device is a tablet and currently in landscape mode
            if(getActivity().getResources().getConfiguration().orientation == protrait) {
                this.dirtyFlag = false;
            }
            else {
                this.dirtyFlag = true;
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getArguments().containsKey("Serialized_Movie_Object") && this.dirtyFlag) {
            Bundle bundle = getArguments();
            movieDetail = (Movie) bundle.getSerializable("Serialized_Movie_Object");
            floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            Drawable starBlank = getResources().getDrawable(R.drawable.star_blank);
            floatingActionButton.setImageDrawable(starBlank);
            this.movieID = Integer.toString(movieDetail.getId());

            progress = new ProgressDialog(this.getActivity());
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading...");
            progress.show();

            this.fetchTrailerAndReview(this.movieID);
            this.updateMovieDetailView();
        }
    }

    private void updateMovieDetailView() {

        Activity activity = this.getActivity();

        //Set favourite star icon to true if movie is already in favourite list
        if(movieDetail.isFavourite()) {
            Drawable starFilled = getResources().getDrawable(R.drawable.star_filled);
            floatingActionButton.setImageDrawable(starFilled);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addToDatabase();
            }
        });



        //Retrieve id of ImageView to store the movie poster pictures
        ImageView posterPicture = (ImageView)activity.findViewById(R.id.detail_movie_poster);
        ImageView backdropPicture = (ImageView)activity.findViewById(R.id.detail_movie_backdrop);
        ImageView play_Button = (ImageView)activity.findViewById(R.id.detail_play_button);

        //Setting listeners for 3 trailer buttons
        TextView trailer2 = (TextView)activity.findViewById(R.id.detail_trailer2_text);
        TextView trailer3 = (TextView)activity.findViewById(R.id.detail_trailer3_text);
        trailer2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchTrailerWide(v);

            }
        });
        trailer3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchTrailerWide(v);

            }
        });
        play_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchTrailerWide(v);

            }
        });


        if(movieDetail.getPosterPath() == null ) {
            byte[] data = movieDetail.getBytes();
            byte[] backDropData = movieDetail.getBackDropImageBytes();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap backdropBitmap = BitmapFactory.decodeByteArray(backDropData, 0, backDropData.length);
            posterPicture.setImageBitmap(bitmap);
            backdropPicture.setImageBitmap(backdropBitmap);
            Drawable playButton = getResources().getDrawable(R.drawable.play_button_filled);
            play_Button.setImageDrawable(playButton);
        }
        else {
            Picasso.with(activity).load(movieDetail.getPosterPath()).fit().into(posterPicture);
            Picasso.with(activity).load(movieDetail.getBackDrop()).fit().into(backdropPicture);
            Drawable playButton = getResources().getDrawable(R.drawable.play_button_filled);
            play_Button.setImageDrawable(playButton);

        }

        String rating = "Ratings: ";
        String releaseDate = movieDetail.getReleaseDate();
        String fullRatings;
        TextView movieRatingsCount = (TextView)activity.findViewById(R.id.detail_ratings);
        String ratingAverage = "";
        ratingAverage = Float.toString(movieDetail.getVote_average());

        //Arranging ratings and release date into one string, and making specific part of the string bold
        fullRatings = rating + "<b>" + ratingAverage + "</b>" + "\n" + "Release Date: " + "<b>" + releaseDate + "</b>";
        //Setting the movie ratings and using HTML.formHtml method to properly handle the "<b>" html tags to make specific parts of the string bold
        movieRatingsCount.setText(Html.fromHtml(fullRatings));

        //Retrieve Id of movie description textview to populate with Movie description of selected movie
        TextView movieDescription = (TextView)activity.findViewById(R.id.detail_movie_description);
        if(movieDetail.getOverView().equals("null")) {
            movieDescription.setText("Movie plot unavailable");}
        else {
            movieDescription.setText(movieDetail.getOverView());
        }
    }


    //To add/remove movie from database
    public void addToDatabase() {

        //If movie is already in the favourite list, then remove it.
        if(movieDetail.isFavourite()) {
            this.deleteData();
        }
        //If movie is not in favourite list, then add it to favourite list
        else {
            this.addData();
        }
    }


    //Save movie to database
    public void addData() {
        DBHandler db = new DBHandler(this.getActivity(), null, null, 1);
        this.movieDetail.setIsFavourite(true);

        byte[] data = getBitmapAsByteArray(mainbitmap);
        byte[] backDropData = getBitmapAsByteArray(mainBackdropBitmap);
        if(data == null) {
        }

        this.movieDetail.setReview(this.movieReview);

        try {
            db.addMovie(this.movieDetail, this.movieTrailerUrl[0], data,backDropData );

            Drawable starFilled = getResources().getDrawable(R.drawable.star_filled);
            floatingActionButton.setImageDrawable(starFilled);

            Context context = this.getContext();
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
        DBHandler db = new DBHandler(this.getActivity(), null, null, 1);

        try {
            //Delete Movie from favourite list and set favourite star to blank
            db.deleteMovie(this.movieDetail.getId());

            Drawable starBlank = getResources().getDrawable(R.drawable.star_blank);
            floatingActionButton.setImageDrawable(starBlank);

            Context context = this.getContext();
            CharSequence text = movieDetail.getTitle() + " has been removed from favourites";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        catch(Exception ex) {
            System.out.println("Could not delete movie..Error: " + ex.toString());
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    //Description: To call async task to fetch trailer
    private void fetchTrailerAndReview(String id) {
        FetchMovieTrailer fetchMovieTrailer = new FetchMovieTrailer();
        FetchMovieReview fetchMovieReview = new FetchMovieReview();
        fetchMovieTrailer.execute(id);
        fetchMovieReview.execute(id);
    }

    public void fetchTrailerWide(View view) {

        int trailerNum = 0;
        switch (view.getId()) {
            case R.id.detail_trailer2_text:
                trailerNum = 1;
                break;
            case R.id.detail_trailer3_text:
                trailerNum = 2;
                break;
        }

        Context context = getActivity().getApplicationContext();
        CharSequence text = "Now Playing " + movieDetail.getTitle() + " trailer";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(movieTrailerUrl[trailerNum]));
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {

            toast.show();
            startActivity(intent);
        }
        else {
            System.out.println("Could not start intent");
        }
    }

    protected MovieTrailer[] parseTrailerJson(String jsonString) {
        MovieTrailer[] movieTrailers = null;

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

//---------------------------------------------------------------
public class FetchMovieTrailer extends AsyncTask<String, Void, String> {

    String trailerJsonStr = null;
    Uri.Builder urlBuilder = new Uri.Builder();
    String apikey = "252196683e135d60eaf32a4940cd7162"; //insert API key here
    Bitmap bitmap;
    Bitmap backDropBitmap;

    //Default Constructor
    public FetchMovieTrailer() {}

    protected void onPostExecute(String strings) {
        //MovieDetail movieDetail = new MovieDetail();


        mainbitmap = bitmap;
        mainBackdropBitmap = backDropBitmap;

        //parsing movie trailer json string to MovieTrailer object(s)
        MovieTrailer[] trailerRetrieved = parseTrailerJson(trailerJsonStr);
        movieTrailerUrl = new  String[trailerRetrieved.length];

        View Trailer2 = (LinearLayout)getActivity().findViewById(R.id.detail_linear_trailer2);
        View Trailer3 = (LinearLayout)getActivity().findViewById(R.id.detail_linear_trailer3);

        switch (movieTrailerUrl.length) {
            case 1:
                Trailer2.setVisibility(View.INVISIBLE);
                Trailer3.setVisibility(View.INVISIBLE);
                break;

            case 2:
                Trailer3.setVisibility(View.INVISIBLE);
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
    //-----------------------------------------------------------

    public class FetchMovieReview extends AsyncTask<String, Void, String> {

        String trailerJsonStr = null;
        Uri.Builder urlBuilder = new Uri.Builder();
        String apikey = "252196683e135d60eaf32a4940cd7162"; //insert API key here
        TextView sample = (TextView)getActivity().findViewById(R.id.detail_movie_review);


        //Default Constructor
        public FetchMovieReview() {}

        protected void onPostExecute(String strings) {

            //parsing movie trailer json string to MovieTrailer object(s)
            String parsedMovieReview = paraseReviewJson(trailerJsonStr);
            movieReview = parsedMovieReview;

            TextView movieReview_textView = (TextView)getActivity().findViewById(R.id.detail_movie_review);
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



            // sample.setText(parsedMovieReview);
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
