import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


public class WordleServerInstance implements Runnable {

    private final Socket socket;
    String[] credentials;
    String username;
    int guesses = 0;

    Database DB;
    Sharer multiCastSharer;


    public WordleServerInstance(Socket socket, Database DB, Sharer multiCastSharer) {
        this.socket = socket;
        this.DB = DB;
        this.multiCastSharer = multiCastSharer;
    }
    public void run() {
        try (Scanner cmd = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             String authUser = null;
             Boolean playing = false;
             Boolean fecthWord = false;
             int triesLeft = 12;
             String currentWord = null;
            System.out.println("New client connected");



            while (cmd.hasNext()) {

                {
                    String input = cmd.nextLine();
                    if(fecthWord && playing){
                        User user = DB.findUser(authUser);
                        if (user.lastHints == null){
                            user.lastHints = "";
                        }
                        String chosenWord = DB.getChosenWord();
                        if (currentWord != null && !currentWord.equals(chosenWord)) {
                            System.out.println("SW changed, resetting tries");
                            triesLeft = 12; // check if SW has changed since game started (  sw changed between sendWord() command and user  actually sending the GW)
                            currentWord = chosenWord;

                        }
                        String guessWord = input;

                        if (DB.wordExists(guessWord)) {
                            String[] hintFormats = printHints(guessWord, chosenWord);
                            if (guessWord.equals(chosenWord)) {
                                guesses++;
                                user.hasPlayed = true;
                                user.played++;
                                user.won++;
                                user.streak++;
                                if (user.streak > user.maxStreak) user.maxStreak = user.streak;
                                user.guessDistrib.add(guesses);
                                user.wonPerc = (int) (((float)user.won / user.played )* 100);
                                playing = false;

                                out.println(18); // game ends
                            } else {
                                guesses++;
                                user.hasPlayed = true;
                                triesLeft--;
                                out.println(19);
                                out.println(hintFormats[0]);

                            }
                            if (user.lastHints.length() > 1) {
                                user.lastHints = user.lastHints + "|" + hintFormats[1];
                            }
                            else user.lastHints = hintFormats[1];
                        }
                        else{
                            out.println(15);

                        }
                        DB.updateUser(user);
                        fecthWord = false; //user has to type sendWord againF
                        continue;
                    }


                    if (input.contains("register")) {
                        String passwd;
                        String username;
                        if (authUser == null) {
                            try {
                                 username = parseCredentials(input)[0];
                                 passwd = parseCredentials(input)[1];
                            }
                            catch(Exception e){
                                out.println(0);
                                continue;
                            }

                            if(passwd == null ||passwd.equals("") || passwd.equals(" ") || passwd.trim().isEmpty()) {
                                out.println(1); // typo error
                                continue;

                            }
                            if (DB.findUser(username) == null) {
                                try {
                                    User user = new User(username, getHash(passwd));
                                    DB.addUser(user);
                                    out.println(2); // successfully registered
                                    out.println(username);


                                } catch (Exception e) {
                                    out.println(e);
                                }
                            } else out.println(3); // username taken
                        }
                        else {
                            out.println(4); // already logged in
                            out.println(authUser);
                        }
                        continue;

                    }

                    if(input.contains("login")){
                        if (authUser == null) {
                            String[] creds = parseCredentials(input);
                            if(creds != null) {
                                var username = creds[0];
                                var passwd = creds[1];
                                var user = DB.findUser(username);
                                if (user == null) out.println(17);
                                else if (user.passwd.equals(getHash(passwd))) {
                                    out.println(16);
                                    authUser = user.username;
                                    out.println(authUser);
                                } else {
                                    out.println(5); // wrong password
                                }
                            }
                            else out.println(0);
                        }
                        else {
                            out.println(6); // already logged in
                            out.println(authUser);
                        }
                        continue;
                    }
                    if(input.equals("logout()")){
                        if (authUser!=null) {
                                System.out.println("Logging out " +  authUser);
                                if(playing){ // counts  as lost game
                                    playing = false;
                                    countLoss(authUser,DB);
                                }
                               authUser = null;

                                out.println(7); //logged out
                                continue;
                        }
                        else out.println(9); // not logged in
                        continue;
                    }

                    if (input.equals("playWORDLE()")){
                        if(authUser != null) {
                            String chosenWord = DB.getChosenWord();
                            currentWord = chosenWord; // setting the SW the user will try to guess
                            User user = DB.findUser(authUser);
                            if (!user.hasPlayed) {
                                    playing = true;
                                    out.println(11); // client can type guess word

                            } else {
                                out.println(12);
                            }
                        }
                        else {
                            out.println(9);
                        }
                        continue;
                    }

                    if (input.equals("sendWord()")){
                        if(playing && fecthWord == false){
                            String chosenWord = DB.getChosenWord();
                            if (currentWord != null && !currentWord.equals(chosenWord)) {
                                System.out.println("SW changed, resetting tries");
                                triesLeft = 12; // check if SW has changed since game started ( PlayWORDLE() command)
                                currentWord = chosenWord;
                            }
                            if(triesLeft >= 1) {
                                fecthWord = true;
                                out.println(13);

                            }
                            else{
                                playing = false;
                                countLoss(authUser,DB);
                                out.println(20);
                            }
                            continue;
                        }
                     else    {
                         out.println(14);
                         continue;
                        }
                    }



                        if(input.equals("share()")){
                            if(authUser != null){
                                User user = DB.findUser(authUser);
                                multiCastSharer.notifyUser(user);
                                out.println(22);

                            }
                            else {
                                out.println(9);
                            }
                            continue;
                        }


                    if(input.equals("showMeSharing()")){
                        out.println(99);
                        continue;
                    }

                    if (input.equals("sendMeStatistics()")) {
                        User user = DB.findUser(authUser);
                        out.println(21);
                        out.println(user.wonPerc);
                        out.println(user.won);
                        out.println(user.played);
                        out.println(user.streak);
                        out.println(user.maxStreak);
                        out.println(user.getAverage());
                        continue;
                    }

                    if (input.equals("exit()")){ //client wants to terminate
                        out.println(100);//confirm exit


                    }
                    else {
                        out.println(10); // invalid command
                        out.println(input);

                    }

                }
            }

            if(playing) countLoss(authUser,DB); // if user exits game while playing counts as a loss
            System.out.println("user exiting...");


        } catch (Exception e) {
            System.out.println(e);

        }


    }
    String[] parseCredentials(String input){

        try{
           credentials = input.split("\\(")[1].split("\\)")[0].split(",");
           if(credentials.length == 2) return credentials;
           else return null;
        }
        catch (Exception e){
            return null;

        }
    }


    final String ANSI_GREEN = "\u001B[32m";
    final String ANSI_YELLOW = "\033[0;33m";
    final String ANSI_RESET = "\u001B[0m";
    String[] printHints(String guessedWord, String chosenWord){
        String hint = "";
        String encodedHint = "";
        for( int i = 0; i<guessedWord.length();i++){
            var let = guessedWord.charAt(i);
            if (guessedWord.charAt(i) ==  chosenWord.charAt(i))    {
                hint =  hint + ANSI_GREEN + let + ANSI_RESET;
                encodedHint =  encodedHint + "+";
            }
            else if (chosenWord.indexOf(guessedWord.charAt(i)) != -1)    {
                hint = hint + ANSI_YELLOW + let + ANSI_RESET;
                encodedHint =  encodedHint + "?";
            }
            else {
                hint = hint + let;
                encodedHint =  encodedHint + "X";
            }
        }


        String[] hintFormats = {hint, encodedHint};
        return hintFormats;
        }
        String getHash(String passwd) throws NoSuchAlgorithmException {
            String hashed = null;
            try {
                  hashed = Hashing.sha256()
                        .hashString(passwd, StandardCharsets.UTF_8)
                        .toString();
            } catch (Exception e) {
                System.out.println("Hash error");

            }
            return hashed;
        }
        void countLoss(String authUser, Database DB) throws IOException {
            User user = DB.findUser(authUser);
            user.streak = 0;
            user.played++;
            user.wonPerc = (int)(((float) user.won / user.played )* 100);
            DB.updateUser(user);

        }

    }


