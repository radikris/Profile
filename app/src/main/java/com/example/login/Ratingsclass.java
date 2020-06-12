package com.example.login;

public class Ratingsclass {
    public String ratingfromwho;
    public int num_of_stars;
    public String ratingid;
    public float rating_sum;

    public Ratingsclass(String name, int howmuch){
        this.ratingfromwho=name;
        this.num_of_stars=howmuch;
    }

    public Ratingsclass(){}

    public int getNum_of_stars() {
        return num_of_stars;
    }

    public String getRatingfromwho() {
        return ratingfromwho;
    }

    public void setNum_of_stars(int num_of_stars) {
        this.num_of_stars = num_of_stars;
    }

    public void setRatingfromwho(String ratingfromwho) {
        this.ratingfromwho = ratingfromwho;
    }

    public String getRatingid(){return ratingid;}

    public void setRatingid(String ratingid) {
        this.ratingid = ratingid;
    }

    public float getRating_sum() {
        return rating_sum;
    }

    public void setRating_sum(float rating_sum) {
        this.rating_sum = rating_sum;
    }
}
