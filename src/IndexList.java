import java.util.ArrayList;
import java.util.List;



public class IndexList {
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
	
	public void checkName(String name) throws Exception {
		for(Index i: list) {
			if(i.getName() == name)
				throw new Exception("Index name existed");
		}
	}
	
}