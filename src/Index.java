import java.util.ArrayList;
import java.util.List;



public class Index {
	public String tablename;
	public  String colname;
	public String indexname;
	public BTree<Integer, TableList.row_node> btree;
	
	
	public Index(String indexname,String tablename , String colname){
		
		this.tablename = tablename;
		this.colname = colname;
		this.indexname = indexname;
		btree = new BTree<Integer, TableList.row_node>();
		
	}
	
	public String getName() { return this.indexname;}
	
}
