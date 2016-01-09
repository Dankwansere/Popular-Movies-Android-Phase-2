package com.example.dankwansere.popularmovies;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Dankwansere on 1/9/2016.
 */
public class MovieJsonParser {


    //Parse Movie result String
    public static Movie[] jSonParser(String jsonString)
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

    //Parsing Movie Traler Json results
    public static MovieTrailer[] parseTrailerJson(String jsonString) {
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

    //Parse Movie reviews Json String
    public static String paraseReviewJson(String jsonString){

        String localMovieReview = "";

        try {
            JSONObject rootObject = new JSONObject(jsonString);
            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = rootObject.optJSONArray("results");
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            localMovieReview = jsonObject.getString("content");
        }
        catch(Exception e) {
            System.out.println("Parsing Movie review json error: " + e.toString());
        }
        return localMovieReview;
    }
}
