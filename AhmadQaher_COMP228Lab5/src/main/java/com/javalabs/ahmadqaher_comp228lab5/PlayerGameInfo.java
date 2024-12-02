package com.javalabs.ahmadqaher_comp228lab5;

import java.sql.Date;

public class PlayerGameInfo {
    private String firstName;
    private String lastName;
    private String gameTitle;
    private Date playingDate;
    private int score;

    public PlayerGameInfo(String firstName, String lastName, String gameTitle, Date playingDate, int score) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gameTitle = gameTitle;
        this.playingDate = playingDate;
        this.score = score;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }
    public Date getPlayingDate() { return playingDate; }
    public void setPlayingDate(Date playingDate) { this.playingDate = playingDate; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}