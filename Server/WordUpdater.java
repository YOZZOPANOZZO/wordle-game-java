public class WordUpdater implements Runnable {
     Database DB;
    public WordUpdater(Database DB){
        this.DB = DB;
    }
    public void run(){
        DB.updateCurrentWord();
        DB.resetUserStatus(); //reset played status and latest hints;

    }
}
