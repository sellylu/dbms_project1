import java.util.Arrays;
import java.util.List;


public class Select extends SQLRequest{
	
	public Select(Command c) {
		super(c);
	}
	
	void parseValue(String input) {

		input = input.replaceAll(", ", ",").toLowerCase();
										
		this.colName = input.split(" ",2)[1].split("from")[0].trim().split(",");
		String from = input.split("from")[1].trim();
		if(from.contains("where")) {
			this.tableName = from.split("where")[0].trim().split(",");
		} else {
			this.tableName = from.split(",");
		}
	
		if(input.contains("where"))
			this.condition = input.split("where")[1].trim();
		
		for(String s: colName) 
			System.out.println(s);
		for(String s: tableName)
			System.out.println(s);
		System.out.println(condition);
		
	}
	
	
	
	public String[] colName;
	public String[] tableName;
	public String condition;
	

}
