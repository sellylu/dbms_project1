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
										
		String from = input.split("from")[1].trim();
		String[] where;
		if(from.contains("where")) {
			where = from.split("where")[0].trim().split(",");
		} else {
			where = from.split(",");
		}
		tableLength = where.length;
		if(input.contains("where")) {
			String cond = input.split("where")[1].trim();
			parseCondition(cond);
		}
		
		String[] col = input.split(" ",3)[1].trim().split(",");
		if(input.contains("count(") || input.contains("sum(")) {
			parseAggr(col);
		} else {
			parseColTable(where, col);
		}
		
		for(List<String> l: this.colName) {
			for(String s: l) {
				System.out.println(s);
			}
		}

	}
	
	private void parseColTable(String[] where, String[] col) throws Exception {
		// parse column and table
		int i = 0;	
		for(String c: col) {
			this.colName.add(new ArrayList<String>());
			int j = 0;
			for(String t: where) {
				String[] tmp_c = c.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				List<String> tmp_t = Arrays.asList(t.split(" as "));	// Table as t	tmp_t[0]=>Table	tmp_t[1]=>t
			
				// check table
				if(i == 0 && Main.ct.checktablename(tmp_t.get(0)) == null) {
					throw new Exception("table " + tmp_t.get(0) + " not exist.");
				} else if(i == 0) {
					// pass
					this.tableName.add(new ArrayList<String>());
					this.tableName.set(j, tmp_t);
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
	}
	
	private void parseCondition(String cond) throws Exception {
		String[] expr;
		if(cond.contains(" and ")) {
			op = 1;
			expr = cond.split(" and ");
		} else if(cond.contains(" or ")) {
			this.op = 2;
			expr = cond.split(" or ");
		} else {
			this.op = 0;
			expr = new String[1];
			expr[0] = cond;
		}
		int i = 0;
		// let parse by space
		for(String s : expr) {
			this.condition.add(new ArrayList<String>());
			s.replaceAll(" ", "");
			String[] tmp;
			if(s.contains("<>")) {
				tmp = s.split("<>");
				this.operator.add(0);
			} else if(s.contains("<")) {
				tmp = s.split("<");					
				this.operator.add(1);
			} else if(s.contains(">")) {
				tmp = s.split(">");
				this.operator.add(2);
			} else if(s.contains("=")) {
				tmp = s.split("=");
				this.operator.add(3);
			} else {
				throw new Exception("Syntax error: around where, operator not found");
			}
			parseExpression(tmp);

			i++;
		}
	}
	
	private void parseAggr(String[] col) {
		int i = 0;
		for(String c: col) {
			String tmp = new String();
			if(c.contains("count(")) {
				aggr = 1;
				tmp = c.split("count\\(")[1].split("\\)")[0];
			} else if(c.contains("sum(")) {
				aggr = 2;
				tmp = c.split("sum\\(")[1].split("\\)")[0];
			}
			this.colName.add(new ArrayList<String>());
			this.colName.get(i).add(this.tableName.get(0).get(0));
			this.colName.get(i).add(tmp);
			i++;
		}
	}
	
	public void doSelectFunction(TableList ct){
		
		int col_index0 = 0;
		int col_index1 = 0;
		List<String> ls0 = colName.get(0);
		List<String> ls1 = colName.get(1);
		
		String[] ls0_table_colname = ct.getColName(ls0.get(0));
		String[] ls1_table_colname = ct.getColName(ls1.get(0));
		
		List<TableList.row_node> subls0 = ct.return_colList(ls0.get(0), ls0.get(1));
		List<TableList.row_node> subls1 = ct.return_colList(ls1.get(0), ls1.get(1));
	
		
		// 找尋目標的col 是第幾個
		for(String tmp:ls0_table_colname){
			if(tmp.equals(ls0.get(1))){
				
			}else{
				col_index0++;
			}
		}
		
		for(String tmp:ls1_table_colname){
			if(tmp.equals(ls1.get(1))){
				
			}else{
				col_index1++;
			}
		}
		
		for(TableList.row_node col0 : subls0){
			for(TableList.row_node col1 : subls1){
				if(){
					
				}
			}
			
		}
		
		
		
	}
	
	private String findTableName(String in) {
		for(List<String> l: this.tableName) {
			for(String s: l) {
				if(s.equals(in)) {
					return s;
				}
			}
		}
		return null;
	}
	
	private void parseExpression(String[] tmp) {
		int j = 0;
		for(String ex: tmp) {
			String[] tmp_c = ex.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
			if(tmp_c.length == 1) {		// ambiguous variable
				this.condition.get(j).add(null);
				this.condition.get(j).add(tmp_c[0]);
			} else if(this.tableName.contains(tmp_c[0])) {	// Table.col or t.col
				String t = findTableName(tmp_c[0]);
				this.condition.get(j).add(t);
				this.condition.get(j).add(tmp_c[1]);
			}
		}
	}
	
	public List<List<String>> colName;
	public List<List<String>> tableName;
	public List<List<String>> condition;
	public List<Integer> operator;	// 0 for <>; 1 for <; 2 for >; 3 for =
	public int tableLength;
	public int op = 0;	// 0 for none; 1 for and; 2 for or
	public int aggr = 0;	// 0 for none; 1 for count; 2 for sum

}
