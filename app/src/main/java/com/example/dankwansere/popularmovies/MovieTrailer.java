package com.example.dankwansere.popularmovies;

/**
 * Created by Dankwansere on 11/28/2015.
 */
public class MovieTrailer extends Movie {

    private String trailerName;
    private String trailerKey;

    public MovieTrailer(){}

    public MovieTrailer(String trailerName, String TrailerKey) {
        super.setTitle(trailerName);
        this.trailerKey = TrailerKey;
    }


    public String getTrailerKey() {
        return trailerKey;
    }

    public void setTrailerKey(String trailerKey) {
        this.trailerKey = trailerKey;
    }

    public String getTrailerName() {
        return trailerName;
    }

    public void setTrailerName(String trailerName) {
        this.trailerName = trailerName;
    }

    @Override
    public String toString() {
        return "MovieTrailer{" +
                "trailerName='" + trailerName + '\'' +
                ", trailerKey='" + trailerKey + '\'' +
                '}';
    }
}
