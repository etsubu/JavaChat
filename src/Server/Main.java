package Server;

import java.io.IOException;

public class Main {
	public static void main(String[] args){
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
