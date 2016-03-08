import java.util.*;

public class Main {
	
	public static TableList ct;
	
	public static void main(String[] argc){
		String input_command;
		
		Scanner scanner = new Scanner(System.in);
		Parser parser = new Parser();
		ct = new TableList();
		while(true){
			input_command = scanner.nextLine();
			parser.doParser(input_command);
			
			String[] s1 = new String[3];
			s1[0] = "a";
			s1[1] = "b";
			s1[2] = "c";
			String[] s2 = new String[3];
			s2[0] = "d";
			s2[1] = "e";
			s2[2] = "f";
			
			ct.addTable("tablename", s1, s2);
			
			//ct.printtb();
			String[] s3 = new String[3];
			s3[0] = "q";
			s3[1] = "w";
			s3[2] = "e";
			String[] s4 = new String[3];
			s4[0] = "r";
			s4[1] = "t";
			s4[2] = "y";
			ct.addTable("tablename2",s3,s4);
			ct.printtb();
			
			break;
			
			
			
		}
	}
	
	
}
