import java.util.ArrayList;
import java.util.List;

public class Parser {
	
	public Parser(){
	}
	
	public Parser(String input) {
		this.checkCommandSyntax(input);
	}
	
	private void checkCommandSyntax(String input) {
		/* WARN
		 * Onlu suitable for Create Table and Insert command,
		 * because the manner of checking command is parsing input with (.
		 */
		
		String[] tmp = input.split("\\(", 2);
		
		String[] command = tmp[0].split(" ");
		String[] value = tmp[1].substring(0, tmp[1].length()-2).split(",");
		if(command[0].equalsIgnoreCase("create") && command[1].equalsIgnoreCase("table")) {
			this.command = Command.CreateTable;
			CreateTable ct = new CreateTable(this.command);
			ct.setName(command[2]);
			parseValue(ct, value);
			r = ct;			
		} else if(command[0].equalsIgnoreCase("insert") && command[1].equalsIgnoreCase("into")) {
			this.command = Command.Insert;
			Insert i = new Insert(this.command);
			

		} else
			this.command = Command.Error;
		
	}
	
	
	private void parseValue(CreateTable ct, String[] in) {

		String[] attribute = new String[in.length];
		String[] dataType = new String[in.length];
		int primaryKey = -1;
		
		for(int i = 0; i < in.length; i++) {
			String[] tmp = in[i].trim().split(" ");
			attribute[i] = tmp[0];
			dataType[i] = tmp[1];
			if(tmp.length > 2)
				primaryKey = i;	
		}
		ct.attribute = attribute;
		ct.dataType = dataType;
		ct.primaryKey = primaryKey;
		
	}

	
	private Command command;
	public SQLRequest r;

	
	public Command getCommand() { return command; }

	
}