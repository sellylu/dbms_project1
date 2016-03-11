
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
	
	public void setName(String n) { this.name = n; }
	
	public String name;
	public String[] dataType;
	public String[] attribute;
	public int primaryKey;

}
