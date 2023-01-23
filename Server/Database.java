import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;

public class Database {
    static final String DatabaseFile = "./resources/database.json";

    static void addUser(User user) {
        Gson gson = new Gson();
        String json = "\n" + gson.toJson(user)+"\n]";


        try {
            File dbfile = new File("./resources/database.json");
            if (dbfile.length() == 0 ) initializeDB();
            else {
                json = ","+json;
            }
            RandomAccessFile file = new RandomAccessFile(dbfile, "rwd");
            synchronized (file) {
                file.seek(dbfile.length() - 1);
                file.setLength(dbfile.length() - 3);

                FileWriter writeToDB = new FileWriter(DatabaseFile, true);
                BufferedWriter userData = new BufferedWriter(writeToDB);
                userData.write(json);
                userData.newLine();
                userData.close();
            }
        } catch (IOException e) {
            System.out.println("Datase base initialization error");

        }
    }

    public static User findUser(String username) throws IOException {

        try {
            var reader = new BufferedReader(new FileReader(DatabaseFile));
            String line = reader.readLine();
            Gson gson = new Gson();
            boolean jsonOffset=  true;

            while (line != null) {
                if(line.equals("]")){
                    break;
                }
                if (jsonOffset ) {
                    jsonOffset = false;
                    line = reader.readLine();
                    continue;
                }
                if(line.equals(",")){
                    line = reader.readLine();
                    continue;
                }

                User user = gson.fromJson(line, User.class);
                if (username.equals(user.username)) {
                    return user;

                }
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateCurrentWord() {
        String chosenWordPath = "./resources/chosenWord.txt";
        var File = new File(chosenWordPath);
        synchronized (File) {
            try {
                FileWriter writeToDB = new FileWriter(chosenWordPath ,false);
                BufferedWriter writeWord = new BufferedWriter(writeToDB);
                String chosenWord = (ChooseRandomWord("./resources/words.txt"));
                writeWord.write(chosenWord);
                writeWord.newLine();
                writeWord.close();
            } catch (Exception e) {
                System.out.println("Error while updating SW word");

            }
        }
    }

    public static String ChooseRandomWord(String filePathWithFileName) throws Exception {

        File file = new File(filePathWithFileName);
        final RandomAccessFile f = new RandomAccessFile(file, "r");
        final long randomLocation = (long) (Math.random() * f.length());
        f.seek(randomLocation);
        f.readLine();
        String randomLine = f.readLine();
        f.close();
        return randomLine;
    }

    public static String getChosenWord() {
        String line = " ";
        try {
            var reader = new BufferedReader(new FileReader("./resources/chosenWord.txt"));
            line = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error while getting chosenWord");

        }
        return line;
    }

    public static Boolean wordExists(String guessedWord) {
        boolean exists = false;
        try {
            var reader = new BufferedReader(new FileReader("./resources/words.txt"));
            String line = reader.readLine();

            while (line != null) {
                if (line.equals(guessedWord)) {
                    exists = true;

                }
                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public static void resetUserStatus(){
        Gson gson = new Gson();
        try {
            Path DBpath = Path.of(DatabaseFile);
            List<String> lines = new ArrayList<>(Files.readAllLines(DBpath, StandardCharsets.UTF_8));
            for (int i = 1; i < lines.size()-1; i=i+2) {
                User User = gson.fromJson(lines.get(i), User.class);
                User.hasPlayed = false;
                User.lastHints = "";
                lines.set(i, gson.toJson(User));
            }

            synchronized (DatabaseFile){
                Files.write(DBpath, lines, StandardCharsets.UTF_8);
            }
        }
        catch (Exception e){
            System.out.println("Error while updating played status");

        }
    }


    public  static void updateUser(User newUser) throws IOException {
        Gson gson = new Gson();
        try {
            Path DBpath = Path.of(DatabaseFile);
            List<String> lines = new ArrayList<>(Files.readAllLines(DBpath, StandardCharsets.UTF_8));
            for (int i = 1; i < lines.size()-1; i=i+2) {
                User oldUser = gson.fromJson(lines.get(i), User.class);
                if (oldUser.username.equals(newUser.username)) {
                    lines.set(i, gson.toJson(newUser));
                    break;
                }
            }

            Files.write(DBpath, lines, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            System.out.println("Error while updating user");

        }
    }


    public static void initializeDB() throws IOException {
        try {
                Files.writeString(Path.of("./resources/database.json"), "[\n]\n");
        }
        catch (Exception e){
            System.out.println("Error while updating user");

        }
    }

    public static ServerConfig loadConfig()  {
        int timeoutMinutes = 0;
        int port = 0;
        String multicastAddress = null;
        int multicastPort = 0;
        int n = 1;
        try {
            var reader = new BufferedReader(new FileReader("./resources/serverConfig.conf"));
            String line = reader.readLine();
            while (line != null) {
                switch (n) {
                    case 1 -> timeoutMinutes = Integer.parseInt(line.split(": ")[1]);
                    case 2 -> port = Integer.parseInt(line.split(": ")[1]);
                    case 3 -> multicastAddress = line.split(": ")[1];
                    case 4 -> multicastPort = Integer.parseInt(line.split(": ")[1]);
                    default -> {}
                }
                n++;
                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServerConfig(timeoutMinutes,port, multicastAddress,multicastPort);

    }


}
