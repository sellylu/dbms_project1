import java.util.HashMap;
 
public class TableList {
	class table_node{
		
		String tablename;
		table_node next_table;
		String []colname;
		String []datatype;
		row rowlist;
		int primary_key;
		
		
		public table_node(String tablename, String[] colname, String[] datatype, int primary_key) {
			// TODO Auto-generated constructor stub
			this.tablename = tablename;
			this.colname = colname;
			this.datatype = datatype;
			next_table = null;
			this.primary_key = primary_key;
			rowlist = new row();
		}

		
	}
	
	class row{
		public row_node head_row;
		public row_node now_row;
		
		public row(){
			// do insert to hash
			head_row = null;
			now_row = null;
		}
	}
	
	class row_node{
		public row_node next_row;
		public String []data;
		public row_node(String[] input){
			next_row = null;
			data = input;
		}
	}
	
	
	public table_node head_tb;
	public table_node now_tb;
	public TableList(){
		head_tb = null;
		now_tb = null;
	}
	
	
	public void addTable(String tablename,String []colname,String []datatype,int primary_key){
		if(head_tb == null){
			head_tb = new table_node(tablename,colname,datatype,primary_key);
			now_tb = head_tb;
			
		}else{
			table_node tb2 = new table_node(tablename,colname,datatype,primary_key);
			now_tb.next_table = tb2;
			now_tb = tb2;
		}
	}
	
	
	//  return false when hit
	public table_node checktablename(String name){
		table_node tmp = head_tb;
		System.out.println("haha");
		
		while(tmp != null){
			if(tmp.tablename.equals(name)){
				return tmp;
			}
			tmp = tmp.next_table;
		}
		
		return null;
		
	}
	
	
	
	public int returnPrimaryKeyIndex(table_node now_table){
		return now_table.primary_key;
	}
	
	public boolean checkPrimaryKeyComflict(table_node now_table,int key,int index){
		row_node a = now_table.rowlist.head_row;
		while(a != null){
			if(Integer.parseInt(a.data[index]) == key){
				return true;
			}
			a = a.next_row;
		}
		return false;
	}
	
	public void insertRow(table_node now_table, String[] input){
		if(now_table.rowlist.head_row == null){
			now_table.rowlist.head_row = new row_node(input);
			now_table.rowlist.now_row = now_table.rowlist.head_row;
			
		}else{
			row_node row2 = new row_node(input);
			now_table.rowlist.now_row.next_row = row2;
			now_table.rowlist.now_row = 	now_table.rowlist.now_row.next_row;
		
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
