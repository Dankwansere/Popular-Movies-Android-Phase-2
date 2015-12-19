package com.example.dankwansere.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dankwansere.popularmovies.Provider.MovieContentProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Name: Eric Sarpong
 * Title: Popular Movie app Stage 2
 * Description: An application to retrieve list of popular movies,
 * read reviews and watch trailers.
 *
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private boolean fromDatabaseFlag = false;
    private static ArrayList<Movie> movieObject = new ArrayList<Movie>();
    ArrayList<Movie> moviesFromDatabase = new ArrayList<Movie>();

    private ProgressDialog progress;
    public static String sortOrder = "popularity.desc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.retrieveMovieList();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Will be implemented in the future", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Setting progess dialog to display while background activity is running
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();



        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.most_popular) {
            progress.show();
            item.setChecked(true);
            setSortOrder("popularity.desc");
            this.retrieveMovieList();
        }
        else if( id == R.id.highest_rated) {
            progress.show();
            item.setChecked(true);
            setSortOrder("vote_count.desc");
            this.retrieveMovieList();
        }
        else if(id == R.id.highest_grossings) {
            progress.show();
            item.setChecked(true);
            setSortOrder("revenue.desc");
            this.retrieveMovieList();

        }

        else if(id == R.id.favourites_list) {
            progress.show();
            this.retrieveDatabaseList();
            item.setChecked(true);


        }

        return super.onOptionsItemSelected(item);
    }

    //Retrieve list of movies from the database(Requires no network call)
    public void retrieveDatabaseList() {
        try {
            getSupportLoaderManager().initLoader(1, null, this);
        }
        catch(Exception ex) {
            System.out.println("Error: " + ex.toString());
        }
    }

    //Retrieve list from API(requires network call)
    public void retrieveMovieList() {
        this.fromDatabaseFlag = false;
        FetchMovieTask fetchMovieTask = new FetchMovieTask();
        fetchMovieTask.execute(getSortOrder());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri CONTENT_URI = MovieContentProvider.CONTENT_URI;
        return new CursorLoader(this, CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        this.moviesFromDatabase.clear();
        try {
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {

                    Movie favouriteMovies = new Movie();
                    favouriteMovies.setId(cursor.getInt(0));
                    favouriteMovies.setTitle(cursor.getString(1));
                    favouriteMovies.setOverView(cursor.getString(2));
                    favouriteMovies.setTrailerURL(cursor.getString(3));
                    favouriteMovies.setReleaseDate(cursor.getString(4));
                    favouriteMovies.setVote_average(Float.parseFloat(cursor.getString(5)));
                    favouriteMovies.setReview(cursor.getString(6));
                    favouriteMovies.setIsFavourite(Boolean.valueOf(cursor.getString(7)));
                    favouriteMovies.setBackDropImageBytes(cursor.getBlob(8));
                    favouriteMovies.setBytes(cursor.getBlob(9));
                    moviesFromDatabase.add(favouriteMovies);
                    cursor.moveToNext();
                }
            }

            if(moviesFromDatabase.size() == 0) {
                Context context = getApplicationContext();
                CharSequence text = "You have no movies in your favourite list";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                this.fromDatabaseFlag = false;
            }
            else {
                this.fromDatabaseFlag = true;
            }

            View recyclerView = findViewById(R.id.movie_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);
            progress.dismiss();

        }
        catch(Exception e) {
            System.out.println("Error binding data to Movie Objects: " + e.toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public static String getSortOrder() {
        return sortOrder;
    }

    public static void setSortOrder(String sortOrder) {
        MovieListActivity.sortOrder = sortOrder;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        if(fromDatabaseFlag) {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this.moviesFromDatabase, false, null));
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            if(progress.isShowing()){
                progress.dismiss();
            }
        }
        else {
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(movieObject, false, null));
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }

//-----------------------------------------------------------------------------------
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private ArrayList<String> movieDescription = new ArrayList<String>();
        private ArrayList<String> imageUrls = new ArrayList<String>();
        private ArrayList<Bitmap> imageFromDatabase = new ArrayList<Bitmap>();

        private ArrayList<Movie> recycleMovieObject = new ArrayList<Movie>();

        public SimpleItemRecyclerViewAdapter(ArrayList<Movie> movie, boolean fromDatabase, Bitmap bitmap ) {

            this.recycleMovieObject = movie;
        }


    @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);

        ViewHolder vh = new ViewHolder(view);

        return vh;

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.mTextView.setText(recycleMovieObject.get(position).getTitle());
            if(fromDatabaseFlag) {
                byte[] data = recycleMovieObject.get(position).getBytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.mImageView.setImageBitmap(bitmap);
            }
            else {
                Picasso.with(getBaseContext()).load(recycleMovieObject.get(position).getPosterPath()).fit().into(holder.mImageView);
            }


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        //arguments.putString(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        arguments.putSerializable("Serialized_Movie_Object", recycleMovieObject.get(position));
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra("Serialized_Movie_Object", recycleMovieObject.get(position));
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {

            return recycleMovieObject.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;
            public final View mView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.moviePoster);
                mTextView = (TextView) view.findViewById(R.id.item_text);

            }

            @Override
            public String toString() {
               return super.toString() + " '" + "'";
            }
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String> {

        String movieJsonStr = null;
        public boolean update = false;
        Uri.Builder urlBuilder = new Uri.Builder();
        String apikey = getString(R.string.ApiKey); //insert API key here
        ArrayList<Movie> retrievedMovieList = new ArrayList<Movie>();

        public FetchMovieTask() {
        }



        @Override
        protected void onPostExecute(String strings) {

            //Calling the jSonParser method with the raw json string as the parameter, so each movie result will be mapped to a Movie object
            try {
                retrievedMovieList = new ArrayList<Movie>(Arrays.asList(this.jSonParser(this.returnJsonString())));
            }
            catch(NullPointerException ex) {
                System.out.println("Error retrieving Json String: " + ex.toString());
            }
            catch(Exception ex) {
                System.out.println("Unexpected Error: " + ex.toString());
            }

            movieObject = retrievedMovieList;

            View recyclerView = findViewById(R.id.movie_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);
            progress.dismiss();

        }

        protected String returnJsonString()
        {
            return this.movieJsonStr;
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
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", params[0])
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
                movieJsonStr = buffer.toString();
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
            return movieJsonStr;
        }

        //--------------------------------------------------------------------
        protected Movie[] jSonParser(String jsonString)
        {
            Movie[] movieObject = null;
            try {
                JSONObject rootObject = new JSONObject(jsonString);

                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = rootObject.optJSONArray("results");
                movieObject = new Movie[jsonArray.length()];

                //Iterate the jsonArray and store value into object
                for(int i = 0; i < jsonArray.length(); i++)
                {
                    movieObject[i] = new Movie();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    movieObject[i].setTitle(jsonObject.getString("title"));
                    movieObject[i].setBackDrop(jsonObject.getString("backdrop_path"));
                    movieObject[i].setId(jsonObject.getInt("id"));
                    movieObject[i].setLanguage(jsonObject.getString("original_language"));
                    movieObject[i].setOverView(jsonObject.getString("overview"));
                    movieObject[i].setReleaseDate(jsonObject.getString("release_date"));
                    movieObject[i].setPopularity(Float.parseFloat(jsonObject.getString("popularity")));
                    movieObject[i].setVote_average(Float.parseFloat(jsonObject.getString("vote_average")));
                    movieObject[i].setVote_count(jsonObject.getInt("vote_count"));
                    movieObject[i].setPosterPath(jsonObject.getString("poster_path"));
                }
            }
            catch(Exception ex) {

            }
            return movieObject;
        }
    }

}
