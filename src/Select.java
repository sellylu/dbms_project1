import java.util.Arrays;
import java.util.List;


public class Select extends SQLRequest{
	
	public Select(Command c) {
		super(c);
	}
	
	void parseValue(String input) {

		input = input.replaceAll(", ", ",").toLowerCase();
										
		this.colName = input.split(" ",2)[1].split("from")[0].trim().split(",");
		String from = input.split("from")[1];
		if(from.contains("inner join")) {
			this.tableName = from.split("inner join")[0].trim().split(",");
		} else if(from.contains("where")) {
			this.tableName = from.split("where")[0].trim().split(",");
		}
		if(input.contains("inner join")) {
			this.join = input.split("join")[1].trim();
			if(this.join.contains("where"))
				this.join = this.join.split("where")[0].trim();
		}
		
		if(input.contains("where"))
			this.condition = input.split("where")[1].trim();
		

		for(String s:this.colName)
			System.out.println(s);
		for(String s:this.tableName)
			System.out.println(s);
		System.out.println(this.condition);
		System.out.println(this.join);
		
		
		
	}
	
	public String[] colName;
	public String[] tableName;
	public String condition;
	public String join;

}
