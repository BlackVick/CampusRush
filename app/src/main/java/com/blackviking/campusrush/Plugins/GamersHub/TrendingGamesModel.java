package com.blackviking.campusrush.Plugins.GamersHub;

public class TrendingGamesModel {

    private String link;
    private String rating;
    private String imageUrl;
    private String releaseDate;
    private String platforms;
    private String genre;

    public TrendingGamesModel() {
    }

    public TrendingGamesModel(String link, String rating, String imageUrl, String releaseDate, String platforms, String genre) {
        this.link = link;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.releaseDate = releaseDate;
        this.platforms = platforms;
        this.genre = genre;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
