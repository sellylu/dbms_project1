import java.util.*;

public class Main {

	public static void main(String[] argc){
		String input_command;
		
		Scanner scanner = new Scanner(System.in);
		Parser parser = new Parser();
		while(true){
			input_command = scanner.nextLine();
			parser.doParser(input_command);
			
		}
	}
	
	
}
