
public class Insert extends SQLRequest{
	
	public Insert(Command c, String n) {
		super(c);
		this.name = n;
	}
	
	public Insert(Command c, String n, String[] col, String[] v) {
		super(c);
		this.name = n;
		this.colName = col;
		this.colValue = v;
		
	}
	
	void parseValue(String[] c, String[] v) throws Exception {
		if(c.length != v.length)
			throw new Exception("Syntax Error: column and value not fit");
		
		if(Main.ct.getColName(this.name) == null)
			throw new Exception("Table doesn't exist");
		else
			this.colName = Main.ct.getColName(this.name);
		this.colValue = new String[this.colName.length];
		for(int i = 0; i < this.colName.length; i++) {
			for(int j = 0; j < c.length; j++) {
				if(this.colName[i].equals(c[j].trim())) {
					this.colName[i] = c[j].trim();
					this.colValue[i] = v[j].trim();
					break;
				} else {
					this.colValue[i] = "null";
				}
			}
		}
	}
	
	public String name;
	public String[] colName;
	public String[] colValue;
	
}
