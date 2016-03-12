public class Parser {
	
	public Parser(){
	}
	
	public Parser(String input) throws Exception {
		this.checkCommandSyntax(input);
	}
	
	private void checkCommandSyntax(String input) throws Exception {
		/* WARN
		 * Only suitable for Create Table and Insert command,
		 * because the manner of checking command is parsing input with (.
		 */
		
		String[] tmp = input.split("\\(", 2);
		if(tmp.length < 2)
			throw new Exception("Syntax Error: open bracket (");
		
		String[] command = tmp[0].split(" ");
		if(command.length < 3)
			throw new Exception("Command Not Found.");
		
		if(command[0].equals("create") && command[1].equals("table")) {
			if(command.length > 4)
				throw new Exception("Command Not Found.");
			// TODO: close bracket not check.
			
			this.command = Command.CreateTable;
			String[] value = tmp[1].substring(0, tmp[1].length()-1).split(",");
			
			CreateTable ct = new CreateTable(this.command);
			ct.setName(command[2]);
			ct.parseValue(value);
			r = ct;
		} else if(command[0].equals("insert") && command[1].equals("into")) {
			if(command.length == 4 && !command[3].equals("values"))
				throw new Exception("Command Not Found.");
			this.command = Command.Insert;
			// TODO: close bracket not check.
			
			String[] col = tmp[1].split("\\)", 2)[0].split(",");
			
			
			String s = tmp[1].split("\\(", 2)[1];
			String[] value = s.substring(0, s.length()-1).split(",");
			
			Insert i = new Insert(this.command);
			i.setName(command[2]);
			i.parseValue(col, value);
			r = i;
		} else
			this.command = Command.Error;
		
	}

	
	private Command command;
	public SQLRequest r;

	public Command getCommand() { return command; }
	
}