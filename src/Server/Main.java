package Server;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    
    private static void loadKeystore() {
        Path storage = Paths.get(System.getProperty("user.dir"), "keystore");
        if(storage.toFile().exists()) {
            Console console = System.console();
            char passwordArray[];
            if(console == null) {
                System.out.print("Input keystore password: ");
                Scanner reader = new Scanner(System.in);
                passwordArray = reader.nextLine().toCharArray();
                reader.close();
            }
            else {
                passwordArray = console.readPassword("Input keystore password: ");
            }
            System.setProperty("javax.net.ssl.keyStore", "keystore");
            System.setProperty("javax.net.ssl.keyStorePassword", new String(passwordArray));
        }
        else {
            System.out.println("You need to create keystore with elliptic curce key for the server to use! You can utilize keytool.exe");
            System.exit(0);
        }
    }
	public static void main(String[] args){
	    loadKeystore();
		Server server = new Server(7777);
		System.out.println("Hosting the server on port " + 7777 + "...");
		try {
			server.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server closed");
	}
}
