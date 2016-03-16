import java.util.*;


public class Main {
	
	public static TableList ct;
	//public static InsertTb it;
	
	public static void main(String[] argc) {
		
		Scanner scanner = new Scanner(System.in);
		Parser parser;
		ct = new TableList();
		//it = new InsertTb();
		
		String command;
		String[] tmp;
		String remain = "";
		while(true){
			command = "";
			/*
			if(!remain.isEmpty()) {
				command = command.concat(remain);
				remain = "";
			}*/
			
			do {
				/*
				if(command.indexOf(";") > 0) {
					tmp = command.split(";", 2);
					command = command.concat(tmp[0]);
					remain = tmp[1];
				}*/
					
				
				String input = scanner.nextLine().toLowerCase();
				
				if(input.indexOf(";") > 0) {
					tmp = input.split(";", 2);
					if(!command.isEmpty())
						command = command.concat(" " + tmp[0]);
					else
						command = command.concat(tmp[0]);
					remain = tmp[1];
					// remaining tmp[1]
					break;
				} else if(input.indexOf(";") == 0) {
					tmp = input.split(";", 2);
					remain = tmp[1];
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
							throw new Exception("Table " + c.name + "has been existed.");
							//System.out.println("已經存在此table");
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
										throw new Exception("duplicated primary key.");
									}
								}else{
									throw new Exception("primary key cannot be null.");
								}
							}
							switch(ct.checkRowDatatypeAndLength(tn, i.colValue,i.colName)){
								case 0:
									ct.insertRow(tn, i.colValue);
									break;
								case 1:
									System.out.println("Overflow!");
									break;
								case 2:
									System.out.println("varchar size too long");
									break;
								case 3:
									System.out.println("Datatype wrong");
									break;
								default:
									System.out.println("Something wrong");
							}
						} else {
							throw new Exception("Table " + i.name + "doesn't exist.");
							//System.out.println("不存在此table");
						}
					
						
						break;
					default:
				}
				
				ct.printtb();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("[Error]  " + e.getMessage());
				//e.printStackTrace();
			}	
	
		}
	}
}
