import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Main {
	
	public static TableList ct;
	//public static InsertTb it;
	
	public static String[] getFileContent(String fileName) {
		
		FileReader file;
		BufferedReader br;
		String line;
		String filecontent = new String();
		try {
			file = new FileReader(fileName);
		    br=new BufferedReader(file);
			while ((line=br.readLine()) != null) {
				filecontent = filecontent + line;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.printf("File no exist");
		} catch (IOException e){
		}
		
		int n = filecontent.codePointCount(0, filecontent.length());
		String []output = new String[n];
		output = filecontent.split(";");
		
		return output;
	}
	
	public static void main(String[] argc) {
		
		Scanner scanner = new Scanner(System.in);
		Parser parser;
		ct = new TableList();
		//it = new InsertTb();
		
		String command;
		String[] tmp;
		while(true){
			command = "";

			do {
				String input = scanner.nextLine();
				
				if(input.indexOf(";") > 0) {
					tmp = input.split(";", 2);
					if(!command.isEmpty())
						command = command.concat(" " + tmp[0]);
					else
						command = command.concat(tmp[0]);
					// remaining tmp[1]
					break;
				} else if(input.indexOf(";") == 0) {
					tmp = input.split(";", 2);
					// remaining tmp[1]
					break;
				} else {
					if(!command.isEmpty())
						command = command.concat(" " + input);
					else
						command = command.concat(input);
				}
			} while(command.length() <= 1024);
			
			
			
			try {
				parser = new Parser(command);
				// get command type in enum
				switch(parser.r.getCommand()) {
					case CreateTable:
						CreateTable c = (CreateTable)parser.r;
						if(ct.checktablename(c.name) == null){
							String chdatatype;
							if((chdatatype = ct.checktabledatatype(c.dataType)) == null){
								ct.addTable(c.name,c.attribute, c.dataType,  c.primaryKey);
								
							}else{
								System.out.println(chdatatype + " is wrong datatype");
							}
							
						}else{
							System.out.println("[Error]  Table " + c.name + " exists.");
							break;
						}
						
						break;
					case Insert:
						Insert i = (Insert)parser.r;
//						for(String a:ct.getColName(i.name)){
//							System.out.println(a);
//						}
//						
						TableList.table_node tn = ct.checktablename(i.name);
						if(tn != null){
							int primary_key_index = ct.returnPrimaryKeyIndex(tn);
							
							if(primary_key_index != -1){

								if(!i.colValue[primary_key_index].equals("null")){
									if(ct.checkPrimaryKeyComflict(tn, i.colValue[primary_key_index], primary_key_index)){
										System.out.println("[Error]  Duplicated primary key.");
										break;
									}
								}else{
									System.out.println("[Error]  Primary key cannot be null.");
									break;
								}
							}
							switch(ct.checkRowDatatypeAndLength(tn, i.colValue,i.colName)){
								case 0:
									ct.insertRow(tn, i.colValue);
									break;
								case 1:
									System.out.println("[Error]  Overflow!");
									break;
								case 2:
									System.out.println("[Error]  varchar size too long");
									break;
								case 3:
									System.out.println("[Error]  Datatype wrong");
									break;
								default:
									System.out.println("[Error]  Something wrong");
							}
						} else {
							System.out.println("Table " + i.name + " doesn't exist.");
						}
					
						
						break;
					case Select:
						Select s = (Select)parser.r;
						ct.checkColInTable(s.tableName,s.colName);
						
						break;
					case ImportFile:
						ImportFile im = (ImportFile)parser.r;
						Main.getFileContent(im.name);
						break;
					default:
				}
				
				ct.printtb();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("[Error]  " + e.getMessage());
				e.printStackTrace();
			}	
	
		}
	}
}
