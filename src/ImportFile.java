
public class ImportFile extends SQLRequest{
	
	public ImportFile(Command c, String n) {
		super(c);
		this.name = n;
	}

	String name;

}
