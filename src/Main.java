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
				String input = scanner.nextLine().toLowerCase();
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
							System.out.println("已經存在此table");
						}
						
						break;
					case Insert:
						Insert i = (Insert)parser.r;
						
						TableList.table_node tn = ct.checktablename(i.name);
						if(tn != null){
							int primary_key_index = ct.returnPrimaryKeyIndex(tn);
							
							if(!i.colValue[primary_key_index].equals("null") && !i.colValue[primary_key_index].equals("") && !i.colValue[primary_key_index].equals("Null")){
								switch(ct.checkRowDatatypeAndLength(tn, i.colValue,i.colName)){
									case 0:
										
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
							}else{
								System.out.println("primary_key 不能為空");
							}
						}else{
							System.out.println("不存在此table");
						}
						
						// 判斷欄位值是否正確
						// 判斷int varchar長度是否正確
						// insert
						break;
					default:
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			
			
			
		}
	}
}
