import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;

public class WordleClientMain {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);  // Create a Scanner object
         List<JSONObject> msgs = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Connecting to server...");
        Scanner scanner;
        String userLog = null;
        ClientConfig params = loadConfig();
        try (Socket socket = new Socket("localhost", params.port)) {
            System.out.println("Welcome to WORDLE");
            scanner = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Thread MulticastServer= new Thread(new MultiCastReceiver(params.multicastPort,params.multicastAddress, msgs)); //start listen foe notifications
            MulticastServer.start();

            while (true ) {
                String authUser = null;
                String line = in.nextLine();
                if (line.equals("")) continue;
                if(line.equals("help()")){
                    System.out.println("This is a list of the available commands:\n");
                    System.out.println("register(username,password)\n");
                    System.out.println("login(username,password)\n");
                    System.out.println("PlayWORDLE(): Starts the game\n");
                    System.out.println("sendWord(): After this command you can send your guess\n");
                    System.out.println("logout()\n");
                    System.out.println("share(): Shares your latest game results and your stats with other players\n");
                    System.out.println("sendMeStatistics(): Shows your statistics\n");
                    System.out.println("showMeSharing(): Shows all the notifications received from other players\n");
                    System.out.println("sendMeStatistics(): Shows your statistics\n");
                    System.out.println("exit(): Exits the game\n");
                    continue;

                }
                out.println(line);
                int resCode = Integer.parseInt(scanner.nextLine());
                switch(resCode) {
                    case 0:
                        System.out.println("There seems to be a typo in the command");
                        break;
                    case 1:
                        System.out.println("Choose a different password");
                        break;
                    case 2:
                        String username = scanner.nextLine();
                        System.out.println(username + ", you successfully registered to WORDLE!" );
                        userLog = username;
                        break;
                    case 3:
                        System.out.println("This username is already taken! Choose another one ");                        // code block
                        break;
                    case 4:

                    case 6:
                        authUser = scanner.nextLine();
                         System.out.println("You are already logged is as " + authUser);
                        break;

                    case 5:
                        System.out.println("Wrong password");
                        break;
                    case 7:
                        System.out.println("Logged out");
                        break;

                    case 8:
                        authUser = scanner.nextLine();
                        System.out.println("You are  logged is as " + authUser);
                        break;
                    case 9:
                        System.out.println("You are not logged in");
                        break;
                    case 10:
                        String input = scanner.nextLine();
                        System.out.println(input + " is an invalid command");
                        break;
                    case 11:
                        System.out.println("Game started! Type sendWord() to make your guess\nNote: if you exit the game while playing you will lose!");
                        break;
                    case 12:
                        System.out.println("You already played for this word! Come back later");
                        break;

                    case 13:
                        System.out.println("Hints given after each guess:");
                         String ANSI_GREEN = "\u001B[32m";
                         String ANSI_YELLOW = "\033[0;33m";
                         String ANSI_RESET = "\u001B[0m";

                        System.out.println(ANSI_GREEN + "Green letter: The letter is the right spot! " + ANSI_RESET +" ") ;
                        System.out.println(ANSI_YELLOW + "Yellow letter: The letter is contained in the secret word but not there... " + ANSI_RESET +"") ;
                        System.out.println("\nYou can now type in your guess!\n");
                        System.out.println("My guess is: ");
                        break;
                    case 14:
                        System.out.println("Before making your guess, start the game by typing playWORDLE()");
                        break;
                    case 15:
                        System.out.println("Mh... That word is not in the vocabulary... Try something different");
                        break;

                    case 16:
                        authUser = scanner.nextLine();
                        System.out.println("Now logged is as " + authUser);
                        userLog = authUser;
                        break;
                    case 17:
                        System.out.println("User not found");
                        break;
                    case 18:
                        String ANSI_RED = "\u001B[31m";
                        ANSI_RESET = "\u001B[0m";

                        System.out.println(ANSI_RED+  "You found the secret word! Well played!"+ ANSI_RESET);
                        System.out.println("You can play again as soon as the new SW is chosen!");
                        break;
                    case 19:
                        String hint = scanner.nextLine();
                        System.out.println("Your guess is similar to the secret one, here is a hint!\n");
                        System.out.println(hint);
                        System.out.println("\nType sendWord() again to guess again!\n");

                        break;
                    case 20:
                        System.out.println("You have used up all your guesses for now, come back later!!");
                        break;

                    case 21:
                        int wonPerc = Integer.parseInt(scanner.nextLine());
                        int won = Integer.parseInt(scanner.nextLine());
                        int played = Integer.parseInt(scanner.nextLine());
                        int streak = Integer.parseInt(scanner.nextLine());
                        int maxStreak = Integer.parseInt(scanner.nextLine());
                        int averageGuess = Integer.parseInt(scanner.nextLine());
                        System.out.println("--------------------");
                        System.out.println("Here are your stats\n");
                        if (wonPerc == -1) {
                            System.out.println("Percentage of wins: You never played");
                        }
                        else  System.out.println("Percentage of wins: " + wonPerc + "%");

                        System.out.println("Games played: " + played);
                        System.out.println("Games won: " + won);
                        System.out.println("Streak: " + streak);
                        System.out.println("maxStreak: " + maxStreak);
                        System.out.println("Average guesses number: " + averageGuess);



                        break;

                    case 22:
                        System.out.println("Sharing your stats...");
                        break;

                    case 99: // shows multicast messages

                            ArrayList<String> oldMsgs = new ArrayList<String>();
                            synchronized (msgs) {
                                if(!(msgs.size() == 0 || (msgs.size() == 1 && msgs.get(0).getString("username").equals(userLog)))) { //nobody is sharing or the user is the oly one sharing
                                    for (int i = msgs.size(); i-- > 0; ) {
                                       JSONObject user = msgs.get(i);
                                        String usernameShared = user.getString("username");
                                          if (!oldMsgs.contains(usernameShared) && !usernameShared.equals(userLog)) {
                                             oldMsgs.add(usernameShared);
                                             printMsgs(user,usernameShared);
                                        }
                                     else msgs.remove(msgs.indexOf(user));

                                    }

                                }
                                else System.out.println("Nobody is sharing their stats...");
                            }
                            break;
                    case 100: // termination of client and server thread
                     System.out.println("Thank you for playing WORDLE");
                        System.out.println("Exiting game...");
                        System.exit(0);
                        break;


                    default:
                        // code block
                }

            }

        }
        catch(ConnectException e){
            System.out.println("Could not connect to WORDLE...");

        }
    }


    public static ClientConfig loadConfig() throws IOException {
        int port = 0;
        String multicastAddress = null;
        int multicastPort = 0;
        int n = 1;
        try {
            var reader = new BufferedReader(new FileReader("./resources/clientConfig.conf"));
            String line = reader.readLine();
            while (line != null) {
                switch(n) {

                    case 1:
                        port = Integer.parseInt(line.split(": ")[1]);
                        break;
                    case 2:
                        multicastAddress = line.split(": ")[1];
                        break;
                    case 3:
                        multicastPort = Integer.parseInt(line.split(": ")[1]);
                        break;
                    default:
                }
                n++;
                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ClientConfig(port, multicastAddress,multicastPort);

    }
    static  public void printMsgs(JSONObject user, String username){
        String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        int wonPercShared = user.getInt("wonPerc");
        int wonShared = user.getInt("won");
        int streakShared = user.getInt("streak");
        int maxStreakShared = user.getInt("maxStreak");
        int guessDistribShared = user.getInt("passwd");
        int playedShared = user.getInt("played");
        String encodedHints = user.getString("lastHints");

    if(encodedHints.length() > 1) {
        System.out.println(ANSI_RED+ "Here are " + username + "'s " + "latest game results"+ ANSI_RESET+"\n");

        System.out.println(decodeHints(encodedHints));

    }
    else   System.out.println(username +" still has to play for the current SW!");

        System.out.println(ANSI_RED+ "Here are " + username + "'s " + "stats"+ ANSI_RESET+"\n");


        if (wonPercShared == -1) System.out.println("Percentage of wins: " + " User has never played");
        else System.out.println("Percentage of wins: " + wonPercShared+"%");
        System.out.println("Games won: " + wonShared);
        System.out.println("Games played: " + playedShared);
        System.out.println("Streak: " + streakShared);
        System.out.println("Guess distribution: " + guessDistribShared);
        System.out.println("maxStreak: " + maxStreakShared);

    }

    static  public String  decodeHints(String hints){
        String ANSI_GREEN_BACKGROUND = "\u001B[42m";
        String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
        String ANSI_BLACK_BACKGROUND = "\u001B[40m";


        final String ANSI_RESET = "\u001B[0m";
        String decoded = "";

        for (int i = 0 ; i<hints.length();i++){
            String c = null;
            switch(hints.charAt(i)) {
                case '+':
                    c =  ANSI_GREEN_BACKGROUND +  " " + ANSI_RESET;
                    break;
                case '?':
                    c =  ANSI_YELLOW_BACKGROUND  +  " " + ANSI_RESET;
                    break;
                case 'X':
                    c =  ANSI_BLACK_BACKGROUND + " " + ANSI_RESET;
                    break;
                case '|':
                    c =  "\n----------\n";
                    break;
                default:

            }
            decoded = decoded + c;
        }
        return decoded;
    }

}







