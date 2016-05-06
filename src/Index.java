import java.util.ArrayList;
import java.util.List;




public class Index {
	private String tablename;
	private String colname;
	private String indexname;
	private BTree btree;
	public Index(String indexname,String tablename , String colname){
		this.tablename = tablename;
		this.colname = colname;
		this.indexname = indexname;
		btree = new BTree();
	}
	
}
