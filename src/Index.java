import java.util.ArrayList;
import java.util.List;



class IndexList {
	List<Index> list;
	public IndexList(){
		list = new ArrayList<Index>();
		
	}
	
	public Boolean checkIndex(String tablename,String colname){
		for(Index i : list){
			if(i.tablename.equals(tablename) && i.colname.equals(colname)){
				return true;
			}
		}
		return false;
	}
	
	public Index getIndex(String tablename,String colname){
		for(Index i : list){
			if(i.tablename.equals(tablename) && i.colname.equals(colname)){
				return i;
			}
		}
		return null;
	}
	
	public void addIndex(Index index){
		list.add(index);
	}
}
public class Index {
	protected String tablename;
	protected String colname;
	protected String indexname;
	
	private BTree btree;
	public Index(String indexname,String tablename , String colname){
		
		this.tablename = tablename;
		this.colname = colname;
		this.indexname = indexname;
		btree = new BTree();
	}
	public void addPointer(){
		
	}
	
}
