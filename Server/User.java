import java.util.ArrayList;
import java.util.List;

public class User {
    String username;
     String passwd;


    int won;
    int wonPerc;
    int played;
    int streak;
    int maxStreak;
    String lastHints;
     List<Integer> guessDistrib;

     Boolean hasPlayed;

    public User(String username, String passwd) {
        this.username = username.toString();
        this.passwd = passwd.toString();
        initializeUser();


    }

    void initializeUser() {
        this.won = 0;
        this.streak = 0;
        this.maxStreak = 0;
        this.guessDistrib = new ArrayList<>();
        this.hasPlayed =  false;
        this.played = 0;
        this.lastHints = "";


        if ( played > 0) {
            this.wonPerc = (won/played)* 100;
        }
        else this.wonPerc = -1; // player has never played

    }
    int getAverage(){
        int avg = 0;
        if (this.guessDistrib.size() == 0 ) return 0;
        for ( int i = 0; i<guessDistrib.size();i++){
            avg=+ avg + guessDistrib.get(i);
        }
         return (int) avg/guessDistrib.size();
    }
}

