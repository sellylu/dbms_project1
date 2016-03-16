
public class CreateTable extends SQLRequest{
	
	public CreateTable(Command c) {
		super(c);
	}

	public CreateTable(Command c, String n, String[] d, String[] a, int p) {
		super(c);
		// TODO Auto-generated constructor stub
		this.name = n;
		this.dataType = d;
		this.attribute = a;
		this.primaryKey = p;
	}
	
	public void parseValue(String[] in) throws Exception {

		this.attribute = new String[in.length];
		this.dataType = new String[in.length];
		this.primaryKey = -1;
		
		for(int i = 0; i < in.length; i++) {
			String[] tmp = in[i].trim().split(" ");
			attribute[i] = tmp[0];
			
			if(tmp.length > 2 && tmp[2].equals("primary") && tmp[3].equals("key"))
				primaryKey = i;	
			else if(tmp.length > 2)
				throw new Exception("Syntax Error: comma");
			
			/* WARN
			 * Suposse there are only two datatype.
			 */
			if (tmp[1].equals("int")){
				dataType[i] = tmp[1];
			} else if (tmp[1].length() >= 7) {
				if(tmp[1].substring(0,7).equals("varchar")) {
					if(tmp[1].substring(tmp[1].length()-1).equals(")") && tmp[1].substring(7,8).equals("(")) {
						dataType[i] = tmp[1].replace("(", "_").substring(0, tmp[1].length()-1);
					} else {
						throw new Exception("Syntax Error: varchar()");
					}
				} else {
					throw new Exception("Undefined datatype \"" + tmp[1] + "\"");
				}
			} else {
				throw new Exception("Undefined datatype \"" + tmp[1] + "\"");
			}

		}	
	}
	
	public void setName(String n) { this.name = n; }
	
	public String name;
	public String[] dataType;
	public String[] attribute;
	public int primaryKey = -1;

}
