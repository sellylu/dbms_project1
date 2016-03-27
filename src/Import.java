
public class Import extends SQLRequest{
	
	public Import(Command c, String n) {
		super(c);
		this.name = n;
	}

	private String name;

}
