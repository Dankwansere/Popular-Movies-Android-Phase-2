package com.example.dankwansere.popularmovies;

/**
 * Created by Eric Sarpong on 10/20/2015.
 */
import java.io.Serializable;

public class Movie implements Serializable{

    private String  backDrop, language, title, overView, releaseDate, posterPath, genre, review, trailerURL;
    private float vote_average, popularity;
    private int id, vote_count;
    private String returnGenre[];
    private Boolean isFavourite = false;

    private byte[] bytes;
    private byte[] backDropImageBytes;

    public Movie() {

    }

    public String getBackDrop() {
        return backDrop;
    }

    public void setBackDrop(String backDrop)
    {
        String baseUrl = "http://image.tmdb.org/t/p/w185/";
        this.backDrop = baseUrl + backDrop;
    }

    public void setGenre(int[] genreId)
    {
        this.returnGenre = new String[genreId.length];

        for(int i = 0; i < genreId.length; i++)
        {
            switch(i)
            {
                case 28:
                    this.returnGenre[i] = "Action";
                    break;
                case 12:
                    this.returnGenre[i] = "Adventure";
                    break;
                case 16:
                    this.returnGenre[i] = "Animation";
                    break;
                case 35:
                    this.returnGenre[i] = "Comedy";
                    break;
                case 80:
                    this.returnGenre[i] = "Crime";
                    break;
                case 99:
                    this.returnGenre[i] = "Documentary";
                    break;
                case 18:
                    this.returnGenre[i] = "Drama";
                    break;
                case 10751:
                    this.returnGenre[i] = "Family";
                    break;
                case 14:
                    this.returnGenre[i] = "Fantasy";
                    break;
                case 10769:
                    this.returnGenre[i] = "Foreign";
                    break;
                case 36:
                    this.returnGenre[i] = "History";
                    break;
                case 27:
                    this.returnGenre[i] = "Horror";
                    break;
                case 10402:
                    this.returnGenre[i] = "Music";
                    break;
                case 9648:
                    this.returnGenre[i] = "Mystery";
                    break;
                case 10749:
                    this.returnGenre[i] = "Romance";
                    break;
                case 878:
                    this.returnGenre[i] = "Science Fiction";
                    break;
                case 10770:
                    this.returnGenre[i] = "TV Movie";
                    break;
                case 53:
                    this.returnGenre[i] = "Thriller";
                    break;
                case 10752:
                    this.returnGenre[i] = "War";
                    break;
                case 37:
                    this.returnGenre[i] = "Western";
                    break;
            }
        }
    }

    public String[] getGenreId()
    {
        if(this.returnGenre == null)
        {
            this.returnGenre = new String[0];
        }

        return this.returnGenre;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverView() {
        return overView;
    }

    public void setOverView(String overView) {
        this.overView = overView;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        String baseUrl = "http://image.tmdb.org/t/p/w185/";
        this.posterPath = baseUrl + posterPath;
    }

    public float getVote_average() {
        return vote_average;
    }

    public void setVote_average(float vote_average) {
        this.vote_average = vote_average;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public String getTrailerURL() {
        return trailerURL;
    }

    public void setTrailerURL(String trailerURL) {
        this.trailerURL = trailerURL;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBackDropImageBytes() {
        return backDropImageBytes;
    }

    public void setBackDropImageBytes(byte[] backDropImageBytes) {
        this.backDropImageBytes = backDropImageBytes;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "backDrop='" + backDrop + '\'' +
                ", language='" + language + '\'' +
                ", title='" + title + '\'' +
                ", overView='" + overView + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", vote_average=" + vote_average +
                ", popularity=" + popularity +
                ", id=" + id +
                ", vote_count=" + vote_count +
                '}';
    }
}
