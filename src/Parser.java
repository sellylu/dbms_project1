public class Parser {
	
	public Parser(){
	}
	
	public Parser(String input) throws Exception {
		this.checkCommandSyntax(input);
	}
	
	private void checkCommandSyntax(String input) throws Exception {

		String[] value, col;
		
		input = input.replaceAll("\\s+", " ").trim();
		
		String[] command = input.split(" ", 5);
		
		if(command[0].equalsIgnoreCase("create") && command[1].equalsIgnoreCase("table")) {
			String[] tmp = input.split("\\(", 2);
			if(tmp.length < 2)
				throw new Exception("Syntax Error: open bracket (");
			if(command.length > 4)
				throw new Exception("Command Not Found.");
			// TODO: close bracket not check.
			
			value = tmp[1].substring(0, tmp[1].length()-1).split(",");
				
			CreateTable ct = new CreateTable(Command.CreateTable, command[2]);
			ct.parseValue(value);
			r = ct;
		} else if(command[0].equalsIgnoreCase("insert") && command[1].equalsIgnoreCase("into")) {
			String[] tmp = input.split("\\(", 2);
			if(tmp.length < 2)
				throw new Exception("Syntax Error: open bracket (");
			command = tmp[0].split(" ");
			// TODO: close bracket not check.
			
			if(command.length == 4 && !command[3].equalsIgnoreCase("values")){
				throw new Exception("Command Not Found.");
			} else if(command.length == 4) {	// INSERT INTO table VALUES ();
				value = tmp[1].substring(0, tmp[1].length()-1).split(",");
				if(Main.ct.getColName(command[2]) == null)
					throw new Exception("Table doesn't exist");
				else
					col = Main.ct.getColName(command[2]);
			} else {	// INSERT INTO table () VALUES ();
				col = tmp[1].split("\\)", 2)[0].split(",");
				String s = tmp[1].split("\\(", 2)[1];
				value = s.substring(0, s.length()-1).split(",");
			}
			
			Insert i = new Insert(Command.Insert, command[2]);
			i.parseValue(col, value);
			r = i;
		} else if(command[0].equalsIgnoreCase("select") && input.indexOf("from") != -1) {
			
			Select s = new Select(Command.Select, command[2]);
			s.parseValue();
			r = s;
		} else if(command[0].equalsIgnoreCase("import")) {
			
			Import i = new Import(Command.Import, command[1]);
			r = i;
		} else {
			r = new SQLRequest(Command.Error);
			throw new Exception("Command Not Found.");
		}
		
		
	}

	public SQLRequest r;
	
}