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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dankwansere.popularmovies.Database.DBHandler;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    public static Movie movieDetail = new Movie();
    private String movieID;
    protected static String youtubeBaseUrl = "https://www.youtube.com/watch?v=";
    private static Bitmap mainbitmap;
    private static Bitmap mainBackdropBitmap;
    protected static String movieReview;
    private FloatingActionButton floatingActionButton;
    protected static ProgressDialog progress;
    private boolean dirtyFlag = false;
    protected static String[] movieTrailerUrl;
    private static TextView movieReview_textView;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            int protrait = Configuration.ORIENTATION_PORTRAIT;
            //If dirty flag is set to true, means device is a tablet and currently in landscape mode
        this.dirtyFlag = getActivity().getResources().getConfiguration().orientation != protrait;
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

            this.updateMovieDetailView();
            this.fetchTrailerAndReview(this.movieID);
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

    public void setMainbitmap(Bitmap mainbitmap) {
        this.mainbitmap = mainbitmap;
    }


    public void setMainBackdropBitmap(Bitmap mainBackdropBitmap) {
        this.mainBackdropBitmap = mainBackdropBitmap;
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
        Activity activity = this.getActivity();

        MovieListActivity movieListActivity = new MovieListActivity();
        movieListActivity.fetchTrailersAndReviews(id, 2, activity, movieDetail);
        movieReview_textView = (TextView)activity.findViewById(R.id.detail_movie_review);

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

    public static void trailerAsyncResults(String[] movieTrailerUrlResult, Bitmap... bitmaps) {
        movieTrailerUrl = movieTrailerUrlResult;
        mainbitmap = bitmaps[0];
        mainBackdropBitmap = bitmaps[1];
    }

    public static void reviewAsyncResult(String reviewResult){
        movieReview = reviewResult;
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
            //    progress.dismiss();
        }
    }
}
