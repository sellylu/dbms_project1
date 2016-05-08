import java.util.ArrayList;
import java.util.List;



public class Index {
	public String tablename;
	public  String colname;
	public String indexname;
	
	public BTree btree;
	public Index(String indexname,String tablename , String colname){
		
		this.tablename = tablename;
		this.colname = colname;
		this.indexname = indexname;
		btree = new BTree();
	}
	
	public String getName() { return this.indexname;}
	
}
