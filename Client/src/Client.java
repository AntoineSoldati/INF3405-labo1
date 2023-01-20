import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;
public class Client {
	private static Socket socket;
	public static void main(String[] args) throws Exception {
		String serverAddress;
		int port;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		do {
			System.out.println("Enter the server's IP address");
		    serverAddress = reader.readLine();
		    System.out.println("Enter the server's port");
		    port = Integer.parseInt(reader.readLine());
		} while (!verifyIP(serverAddress) || !verifyPort(port));
		
		socket = new Socket(serverAddress, port);
		System.out.format("Serveur lancé sur [%s:%d]", serverAddress, port);
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		String command = "";
		
		while (!(command.equals("exit"))) {
		    String input = reader.readLine();
		    
		    String[] inputArr = input.split("\\s+");
			command = inputArr[0];
			String arg = "";
			if(inputArr.length > 1)
				arg = inputArr[1];
			
			switch (command) {
				case "cd" :
					break;
				case "ls" :
					out.writeUTF(input);
					System.out.println(in.readUTF());
					break;
				case "mkdir" : 
					out.writeUTF(input);
					System.out.println(in.readUTF());
					break;
				case "upload" : 
					out.writeUTF(inputArr[1]);
					break;
				case "download" :
					out.writeUTF(inputArr[1]);
					break;
				case "exit" :
					out.writeUTF(command);
					System.out.println(in.readUTF());
					break;
				 default :
					 break;
			}
		}
		socket.close();
	}
	
	public static boolean verifyIP(String IP) {
		String regex = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])"; //Source : https://www.geeksforgeeks.org/how-to-validate-an-ip-address-using-regex/
		Pattern p = Pattern.compile(regex);
		return p.matcher(IP).matches();
	}
	
	public static boolean verifyPort(int port) {
		return 5000 <= port && 5050 >= port;
	}
}