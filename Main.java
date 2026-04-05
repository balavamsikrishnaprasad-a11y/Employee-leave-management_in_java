import util.DataStore;
import gui.LoginFrame;

public class Main {
    public static void main(String[] args) {
        DataStore.initializeDatabase();
        new LoginFrame();
    }
}
