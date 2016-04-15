import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Select extends SQLRequest{
	
	public Select(Command c) {
		super(c);
		colName = new ArrayList<List<String>>();
	}
	
	void parseValue(String input) throws Exception {

		input = input.replaceAll(", ", ",").toLowerCase();
										
		String[] col = input.split(" ",2)[1].split("from")[0].trim().split(",");
		String from = input.split("from")[1].trim();
		String cond = new String();
		if(from.contains("where")) {
			this.tableName = from.split("where")[0].trim().split(",");
		} else {
			this.tableName = from.split(",");
		}
		if(input.contains("where"))
			cond = input.split("where")[1].trim();
		
		// parse column and table
		int i = 0;	
		for(String c: col) {
			this.colName.add(new ArrayList<String>());
			int j = 0;
			for(String t: this.tableName) {
				String[] tmp_c = c.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				List<String> tmp_t = Arrays.asList(t.split(" as "));	// Table as t	tmp_t[0]=>Table	tmp_t[1]=>t
				
				// check table
				if(i == 0 && Main.ct.checktablename(tmp_t.get(0)) == null) {
					throw new Exception("table " + tmp_t.get(0) + " not exist.");
				} else {
					// pass
					this.tableName[j] = tmp_t.get(0);
				}
				
				if(tmp_c.length == 1) {		// ambiguous variable
					this.colName.get(i).add(null);
					this.colName.get(i).add(tmp_c[0]);
					break;
				} else if(tmp_t.contains(tmp_c[0])) {	// Table.col or t.col
					if(tmp_c[0].equals(tmp_t.get(0))) {
						this.colName.get(i).add(tmp_c[0]);
					} else {
						this.colName.get(i).add(tmp_t.get(0));
					}
					this.colName.get(i).add(tmp_c[1]);
					break;
				}
				j++;
			}
			i++;
		}
		
		if(!cond.isEmpty()) {
			if(cond.contains(" and ")) {
				op = 1;
				condition = cond.split(" and ");
			} else if(cond.contains(" or ")) {
				op = 2;
				condition = cond.split(" or ");
			}
			// let parse by space
			for(String s : this.condition) {
				s.replaceAll(" ", "");
				if(s.contains("<>")) {
					String[] tmp = s.split("<>");
					s = tmp[0] + " <> " + tmp[1];
				} else if(s.contains("<")) {
					String[] tmp = s.split("<");
					s = tmp[0] + " < " + tmp[1];					
				} else if(s.contains(">")) {
					String[] tmp = s.split(">");
					s = tmp[0] + " > " + tmp[1];
				} else if(s.contains("==")) {
					String[] tmp = s.split("==");
					s = tmp[0] + " == " + tmp[1];
				} else {
					throw new Exception("Syntax error: around where, operator not found");
				}
			}
		}
		

	}
	
	public void doSelectFunction(TableList ct){
		
		List<String> ls0 = colName.get(0);
		List<String> ls1 = colName.get(1);
		
		String[] ls0_table_colname = ct.getColName(ls0.get(0));
		String[] ls1_table_colname = ct.getColName(ls1.get(0));
		
		List<TableList.row_node> subls0 = ct.return_colList(ls0.get(0), ls0.get(1));
		List<TableList.row_node> subls1 = ct.return_colList(ls1.get(0), ls1.get(1));
		
		
		
	}
	
	public List<List<String>> colName;
	public String[] tableName;
	public String[] condition;
	public int op = 0;	// 0 for none; 1 for and; 2 for or
	

}
