package com.expertsight.app.lttc.model;



import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Match extends FirestoreModel{

    private String player1Id;
    private String player2Id;
    private String player1FullName;
    private String player2FullName;
    private int player1Games;
    private int player2Games;
    private long timestamp;

    public Match() {}

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getPlayer1FullName() {
        return player1FullName;
    }

    public void setPlayer1FullName(String player1FullName) {
        this.player1FullName = player1FullName;
    }

    public String getPlayer2FullName() {
        return player2FullName;
    }

    public void setPlayer2FullName(String player2FullName) {
        this.player2FullName = player2FullName;
    }

    public int getPlayer1Games() {
        return player1Games;
    }

    public void setPlayer1Games(int player1Games) {
        this.player1Games = player1Games;
    }

    public int getPlayer2Games() {
        return player2Games;
    }

    public void setPlayer2Games(int player2Games) {
        this.player2Games = player2Games;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Match{" +
                "player1Ref='" + player1Id + '\'' +
                ", player2Ref='" + player2Id + '\'' +
                ", player1Games=" + player1Games +
                ", player2Games=" + player2Games +
                ", timestamp=" + timestamp +
                '}';
    }
}
