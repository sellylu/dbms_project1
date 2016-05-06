
public class CreateIndex extends SQLRequest{

	public CreateIndex(Command c, String n) throws Exception {
		super(c);
		this.name = n;
		for(Index in: Main.indexlist) {
			if(in.getName() == this.name) 
				throw new Exception("Index name existed");
		}
	}
	
	void parseValue(String input) throws Exception {
		
		String[] tmp = input.split("\\(");
		
		this.colName = tmp[1].split("\\)")[0];
		this.tableName = tmp[0].split(" ")[4];
		
		if(Main.ct.getColName(this.tableName) == null)
			throw new Exception("Table doesn't exist");
		if(!Main.ct.ifExistCol(this.tableName, this.colName))
			throw new Exception("Column doesn't exist");
		
	}
	
	String name;
	String tableName;
	String colName;
	
}
