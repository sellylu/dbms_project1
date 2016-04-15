import java.util.HashMap;
import java.util.List;
 
public class TableList {
	class table_node{
		
		String tablename;
		table_node next_table;
		String []colname;
		String []datatype;
		row rowlist;
		int primary_key;
		HashMap<String, Integer> varchar_map;
		
		
		public table_node(String tablename, String[] colname, String[] datatype, int primary_key) {
			// TODO Auto-generated constructor stub
			this.tablename = tablename;
			this.datatype = new String[datatype.length];
			this.colname = new String[colname.length];
			varchar_map = new HashMap<String, Integer>();
			
			for(int i=0;i<datatype.length;i++){
				if(datatype[i].contains("_")){
					String []tmp = datatype[i].split("_");
					
					this.datatype[i] = tmp[0];
					this.colname[i] = colname[i];
					
					this.varchar_map.put(colname[i],Integer.parseInt(tmp[1]));
				}else{
					this.datatype[i] = datatype[i];
					this.colname[i] = colname[i];
				}
			}
			
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
			this.next_row = null;
			this.data = new String[input.length];
			this.data = input.clone();
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
		
		while(tmp != null){
			if(tmp.tablename.equalsIgnoreCase(name)){
				return tmp;
			}
			tmp = tmp.next_table;
		}
		
		return null;
		
	}
	
	public String checktabledatatype(String[] datatype){
		String re = "";
		int ifwrongdatatype = 0;
		for(String data : datatype){
			if(data.contains("_")){
				data = data.split("_")[0];
				
			}
			if(data.contains("int")&&data.length()==3||data.contains("varchar")&&data.length()==7){
				continue;
			}else{
				
				ifwrongdatatype = 1;
				re = re + data + " ";
			}
		}
		if(ifwrongdatatype == 1)
			return re;
		else
			return null;
	}
	
	public int checkRowDatatypeAndLength(table_node now_table, String[] data,String []colname){
		int [] datatype = new int [data.length];
		
		int i=0;
		for(String d : data){
			
			try{
				if(d.equals("null")){
					datatype[i] = 3;
					i++;
					continue;
				}
				int tmp = Integer.parseInt(d);
				datatype[i] = 1;
				i++;
			}catch(NumberFormatException e){
				datatype[i] = 2;
				i++;
			}
		}
		
		for(int a=0;a<now_table.datatype.length ;a++){
			if(!data[a].equals("")){ //表示此insert的col有值
				if(datatype[a] == 1 && now_table.datatype[a].equals("int")){
					if(Integer.parseInt(data[a]) > Integer.MAX_VALUE){
						return 1;
					}else{
						continue;
					}
				}else if(datatype[a] == 2 && now_table.datatype[a].equals("varchar")){
					if(now_table.varchar_map.get(colname[a]) < data[a].length()-2){
						
						return 2;
					}else{
						continue;
					}
				}else if(datatype[a] == 3){
					continue;
				}else {
					return 3;
				}
			}
			
		}
		return 0;
	}
	
	
	
	public int returnPrimaryKeyIndex(table_node now_table){
		return now_table.primary_key;
	}
	
	public boolean checkPrimaryKeyComflict(table_node now_table,String key,int index){
		row_node a = now_table.rowlist.head_row;
		while(a != null){
			if(a.data[index].equals(key)){
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
		table_node tn = head_tb;
		while(tn != null){
			System.out.println(tn.tablename);
			for(String a : tn.colname){
				System.out.printf("%15s",a);
			}
			System.out.println("");
			row_node rn = tn.rowlist.head_row;
			while(rn != null){
				for(String a:rn.data){
					System.out.printf("%15s",a);
				}
				System.out.println("");
				rn = rn.next_row;
			}
			tn = tn.next_table;
		}
	}
	
	public String[] getColName(String name){
		table_node tn;
		if((tn = this.checktablename(name))!=null){
			return tn.colname;
		}else{
			return null;
		}
	}
	
	public boolean ifExistCol(String tablename,String colname){
		String [] colnames = getColName(tablename);
		for(String tmp : colnames){
			if(tmp.equals(colname)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkColInTable(String[] input_table_name,List<List<String>> input_colname){
		
		// 判斷是否都存在其table
		
		for(List<String> a : input_colname){
			
			// 假如沒有宣告tablename
			// 判斷有沒有conflict
			// a => {tableName, colName} 
			String tablename = a.get(0);
			String colname = a.get(1);
			
			
			if(colname.equals("*")) continue;
			
			
			if(tablename == null){
				int count = 0;
				int site = 0;
				String useToSetNonTableNameCol = null;
				for(String tmp_input_table_name : input_table_name){
					if(ifExistCol(tmp_input_table_name,colname) == true){
						site = count;
						useToSetNonTableNameCol = tmp_input_table_name;
						count++;
						
					}else{
						System.out.println("沒有table存在此col");
						return false;
					}
					
					if(count ==2){
						System.out.println("conflict了～～～");
						return false;
					}
				}
				a.set(site, useToSetNonTableNameCol);
				
				
			}
			
			// 拿到a_node 的 col   
			else{
				if(!ifExistCol(tablename,colname)){
					System.out.println("沒這col name");
					return false;
				};
			}
			
			
		}
		return true;
		
	}
}
