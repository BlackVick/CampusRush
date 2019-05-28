package com.blackviking.campusrush.Model;

/**
 * Created by Scarecrow on 2/8/2019.
 */

public class CommentModel {

    private String comment;
    private String commenter;
    private String realCommenter;
    private long commentTime;

    public CommentModel() {
    }

    public CommentModel(String comment, String commenter, String realCommenter, long commentTime) {
        this.comment = comment;
        this.commenter = commenter;
        this.realCommenter = realCommenter;
        this.commentTime = commentTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenter() {
        return commenter;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public String getRealCommenter() {
        return realCommenter;
    }

    public void setRealCommenter(String realCommenter) {
        this.realCommenter = realCommenter;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(long commentTime) {
        this.commentTime = commentTime;
    }
}
