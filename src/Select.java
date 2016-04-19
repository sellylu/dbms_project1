import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Select extends SQLRequest{
	
	public Select(Command c) {
		super(c);
		this.colName = new ArrayList<List<String>>();
		this.tableName = new ArrayList<List<String>>();
		this.condition = new ArrayList<ConditionStruct>();
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
		constructTable(where);
		if(input.contains("where")) {
			String cond = input.split("where")[1].trim();
			parseCondition(cond);
		}
		
		String[] col = input.split(" ",3)[1].trim().split(",");
		if(input.contains("count(") || input.contains("sum(")) {
			parseAggr(col);
		} else {
			parseColTable(col);
		}
		
		System.out.println("colName");
		for(List<String> l: this.colName) {
			for(String s: l) {
				System.out.println(s);
			}
		}
		System.out.println("tableName");
		for(List<String> l: this.tableName) {
			for(String s: l) {
				System.out.println(s);
			}
		}
		System.out.println("op " + this.op);
		System.out.println("aggr " + this.aggr);

	}
	
	private void parseColTable(String[] col) throws Exception {
		// parse column and table
		int i = 0;	
		for(String c: col) {
			this.colName.add(new ArrayList<String>());
			String[] tmp_c = c.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
			String t = findTableName(tmp_c[0]);

			if(tmp_c.length == 1) {		// ambiguous variable
				this.colName.get(i).add(null);
				this.colName.get(i).add(tmp_c[0]);
			} else if(t != null) {	// Table.col or t.col
				this.colName.get(i).add(t);
				this.colName.get(i).add(tmp_c[1]);
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
			s.replaceAll(" ", "");
			String[] tmp;
			if(s.contains("<>")) {
				tmp = s.split("<>");
				this.condition.addAll(new ArrayList<ConditionStruct>(0));
			} else if(s.contains("<")) {
				tmp = s.split("<");					
				this.condition.addAll(new ArrayList<ConditionStruct>(1));
			} else if(s.contains(">")) {
				tmp = s.split(">");
				this.condition.addAll(new ArrayList<ConditionStruct>(2));
			} else if(s.contains("=")) {
				tmp = s.split("=");
				this.condition.addAll(new ArrayList<ConditionStruct>(3));
			} else {
				throw new Exception("Syntax error: around where, operator not found");
			}
			parseExpression(tmp[0].trim(), tmp[1].trim(), this.condition.get(i));
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
	/*	
		for(TableList.row_node col0 : subls0){
			for(TableList.row_node col1 : subls1){
				if(){
					
				}
			}
			
		}
		
		*/
		
	}
	
	private void constructTable(String[] where) throws Exception {
		int i = 0;
		for(String t: where) {
			String[] tmp_t = t.split(" as ");	// Table as t	tmp_t[0]=>Table	tmp_t[1]=>t
			
			// check table
			if(Main.ct.checktablename(tmp_t[0]) == null) {
				throw new Exception("table " + tmp_t[0] + " not exist.");
			} else if(tmp_t.length == 1) {
				this.tableName.add(new ArrayList<String>());
				this.tableName.get(i).set(0, null);
				this.tableName.get(i).set(1, tmp_t[0]);
			} else {
				// pass
				this.tableName.add(new ArrayList<String>());
				this.tableName.set(i, Arrays.asList(tmp_t));
			}
			i++;
		}
	}
	
	private String findTableName(String in) {
		for(List<String> l: this.tableName) {
			if(l.indexOf(in) != -1)
				return l.get(0);
		}
		return null;
	}
	
	private void parseExpression(String left, String right, ConditionStruct cs) {
		// Left
		try {
			Integer.parseInt(left);
			cs.typeLeft = 2;
			cs.valueLeft = left;
		} catch (NumberFormatException e) {
			if(left.startsWith("'") && left.endsWith("'")) {
				cs.typeLeft = 1;
				cs.valueLeft = left.substring(1, left.length()-1);
			} else {
				cs.typeLeft = 0;
				String[] tmp_c = left.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				String t = findTableName(tmp_c[0]);

				if(tmp_c.length == 1) {		// ambiguous variable
					cs.valueLeft = tmp_c[1];
				} else if(t != null) {	// Table.col or t.col
					cs.valueLeft = t.concat("." + tmp_c[1]);
				}
			}
		}
		// Right
		try {
			Integer.parseInt(right);
			cs.typeLeft = 2;
			cs.valueLeft = right;
		} catch (NumberFormatException e) {
			if(left.startsWith("'") && right.endsWith("'")) {
				cs.typeLeft = 1;
				cs.valueLeft = right.substring(1, right.length()-1);
			} else {
				cs.typeLeft = 0;
				String[] tmp_c = right.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				String t = findTableName(tmp_c[0]);

				if(tmp_c.length == 1) {		// ambiguous variable
					cs.valueLeft = tmp_c[1];
				} else if(t != null) {	// Table.col or t.col
					cs.valueLeft = t.concat("." + tmp_c[1]);
				}			}
		}
	}
	
	class ConditionStruct{
		String valueLeft;
		String valueRight;
		int typeLeft;  // 0:table.col  , 1: String 2: Int
		int typeRight;
		int operator;	// 0 for <>; 1 for <; 2 for >; 3 for =
		
		ConditionStruct(int o) {
			this.operator = o;
		}
	}
	
	
	public List<List<String>> colName;
	public List<List<String>> tableName;
	public List<ConditionStruct> condition;
	public int op = 0;	// 0 for none; 1 for and; 2 for or
	public int aggr = 0;	// 0 for none; 1 for count; 2 for sum

}
