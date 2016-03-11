import java.util.*;

public class Main {
	
	public static TableList ct;
	//public static InsertTb it;
	
	public static void main(String[] argc) {
		
		Scanner scanner = new Scanner(System.in);
		Parser parser;
		ct = new TableList();
		//it = new InsertTb();
		
		while(true){
			
			String command = "";
			String[] tmp;
			do {
				String input = scanner.nextLine();
				if(input.indexOf(";") > 0) {
					tmp = input.split(";");
					command = command.concat(tmp[0]);
					// remaining tmp[1]
					break;
				} else if(input.indexOf(";") == 0) {
					// remaining tmp[1]
					break;
				} else {
					command = command.concat(input);
				}
			} while(command.length() <= 1024);

			parser = new Parser(command);
			
			// get command type in enum
			switch(parser.r.getCommand()) {
				case CreateTable:
					CreateTable c = (CreateTable)parser.r;
					System.out.println(c.name);
					System.out.println(c.primaryKey);
					System.out.println(c.dataType.toString());
					System.out.println(c.attribute.toString());
					break;
				case Insert:
					Insert i = (Insert)parser.r;
					break;
				default:
			}
			
			
			
			
			

			
			
			/*
			
			//parser.doParser(input_command);
			
			String[] s1 = new String[3];
			s1[0] = "a";
			s1[1] = "b";
			s1[2] = "c";
			
			String[] s2 = new String[3];
			s2[0] = "d";
			s2[1] = "e";
			s2[2] = "f";
			
			ct.addTable("tablename", s1, s2,0);
			
			//ct.printtb();
			String[] s3 = new String[3];
			s3[0] = "q";
			s3[1] = "w";
			s3[2] = "e";
			String[] s4 = new String[3];
			s4[0] = "r";
			s4[1] = "t";
			s4[2] = "y";
			ct.addTable("tablename2",s3,s4,1);
			ct.printtb();
			
			break;
			
			
			*/
		}
	}
}
