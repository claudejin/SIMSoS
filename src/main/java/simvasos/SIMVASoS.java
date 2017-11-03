package simvasos;

import mci.Main;

import java.io.IOException;

/**
 * Created by mgjin on 2017-06-12.
 */
public class SIMVASoS {
    public static void main(String[] args) {
        try {
            Main.experimentMain(args);
        } catch (IOException e) {
            System.out.println("Error: Old version is not runnable");
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
