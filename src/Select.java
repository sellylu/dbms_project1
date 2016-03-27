
public class Select extends SQLRequest{
	
	public Select(Command c, String n) {
		super(c);
		this.name = n;
	}
	
	void parseValue() {
		
	}
	
	private String name;

}
