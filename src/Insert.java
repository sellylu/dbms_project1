
public class Insert extends SQLRequest{

	public Insert(Command c) {
		super(c);
		// TODO Auto-generated constructor stub
	}
	
	public Insert(Command c, String n, String[] col, String[] v) {
		super(c);
		this.name = n;
		this.colName = col;
		this.colValue = v;
	}
	
	public void parseValue(String[] c, String[] v) throws Exception {
		if(c.length != v.length)
			throw new Exception("Syntax Error: column and value not fit");

		for(int i = 0; i < c.length; i++) {
			// TODO: 判斷''for string
			
			c[i].trim();
			v[i].trim();
		}
		this.colName = c;
		this.colValue = v;
	}
	
	public void setName(String n) { this.name = n; }

	public String name;
	public String[] colName;
	public String[] colValue;
	
}
