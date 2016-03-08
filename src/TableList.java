import java.util.HashMap;
 
public class TableList {
	class table_node{
		
		String tablename;
		table_node next_table;
		String []colname;
		String []datatype;
		row row_node;
		
		public table_node(String tablename, String[] colname, String[] datatype) {
			// TODO Auto-generated constructor stub
			this.tablename = tablename;
			this.colname = colname;
			this.datatype = datatype;
			next_table = null;
			row_node = null;
		}

		
	}
	
	class row{
		HashMap col; 
		row next_row;
		
		public row(String[] index, String[] datatype){
			// do insert to hash
			next_row = null;
		}
	}
	
	
	public table_node head_tb;
	public table_node now_tb;
	public TableList(){
		head_tb = null;
		now_tb = null;
	}
	
	public void addTable(String tablename,String []colname,String []datatype){
		if(head_tb == null){
			head_tb = new table_node(tablename,colname,datatype);
			now_tb = head_tb;
			System.out.println("qq");
		}else{
			table_node tb2 = new table_node(tablename,colname,datatype);
			now_tb.next_table = tb2;
			now_tb = tb2;
		}
	}
	
	public void printtb(){
		table_node keke = head_tb;
		while(keke != null){
			System.out.println("tablename = " + keke.tablename);
			String []haha = keke.datatype;
			String []lolo = keke.colname;
			for(int i=0 ; i<haha.length;i++){
				System.out.println("type&col " + haha[i] + " " + lolo[i]);
			}
			keke = keke.next_table;
		}
	}
}
