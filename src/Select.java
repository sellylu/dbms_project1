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
			if(from.split("where")[0].trim().isEmpty())
				throw new Exception("Syntax Error: no table name found.");
		} else {
			where = from.split(",");
			if(from.isEmpty())
				throw new Exception("Syntax Error: no table name found.");
		}
		constructTable(where);
		if(input.contains("where")) {
			String cond = input.split("where")[1].trim();
			parseCondition(cond);
		}
		
		if(input.split("select")[1].trim().split("from")[0].trim().isEmpty())
			throw new Exception("Syntax Error: no column name found.");
		
		String[] col = input.split(" ",3)[1].trim().split(",");
		if(input.contains("count(") || input.contains("sum(")) {
			parseAggr(col);
		} else {
			parseColTable(col);
		}

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
	
	private void parseAggr(String[] col) {
		int i = 0;
		for(String c: col) {
			String tmp = new String();
			if(c.contains("count(")) {
				aggr = 1;
				tmp = c.split("\\(")[1].split("\\)")[0];
			} else if(c.contains("sum(")) {
				aggr = 2;
				tmp = c.split("\\(")[1].split("\\)")[0];
			}
			this.colName.add(new ArrayList<String>());
			this.colName.get(i).add(this.tableName.get(0).get(0));
			this.colName.get(i).add(tmp);
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
				this.condition.add(new ConditionStruct(0));
			} else if(s.contains("<")) {
				tmp = s.split("<");					
				this.condition.add(new ConditionStruct(1));
			} else if(s.contains(">")) {
				tmp = s.split(">");
				this.condition.add(new ConditionStruct(2));
			} else if(s.contains("=")) {
				tmp = s.split("=");
				this.condition.add(new ConditionStruct(3));
			} else {
				throw new Exception("Syntax error: around where, operator not found");
			}
			parseExpression(tmp[0].trim(), tmp[1].trim(), this.condition.get(i));
			i++;
		}
	}
	
	public void doSelectFunction(TableList ct){
		
		ConditionStruct tmp0 = null;
		ConditionStruct tmp1 = null;
		ConditionStruct tmp2 = null;
		ConditionStruct tmp3 = null;
		// 判斷 From 多少table
		
		int table_count = this.tableName.size();
		String tablename0,tablename1 = null;
		List<TableList.row_node> tn0_allRow = null;
		List<TableList.row_node> tn1_allRow = null;
		String []table0colname = null;
		String []table1colname = null;
		String []table0datatype = null;
		String []table1datatype = null;
		List<Integer> indexoftargetcol_onetable = null;
		List<List<Integer>> indexoftargetcol_twotable = null;
		int lenofcondition = 0;
		int tmpc =0;
		int count = 0;
		List<Integer> tmpLI;
		switch(table_count){
			
			case 1:
				tablename0 = this.tableName.get(0).get(0);
				// 取出table的全部col
				tn0_allRow = ct.return_colList(tablename0);
				table0colname = ct.getColName(tablename0);
				table0datatype = ct.getDataType(tablename0);
				indexoftargetcol_onetable = new ArrayList<Integer>();
				
				// 把要拿出來的col 在原本table裡的位置存出來
				for(List<String> targetcol: this.colName){
					String col = targetcol.get(1);
					tmpc = 0;
					for(String c : table0colname){
						if(col.equals("*") || col.equalsIgnoreCase(c)) {
							indexoftargetcol_onetable.add(tmpc);
							if(col.equalsIgnoreCase(c)) {
								break;
							}
						}
						tmpc++;
					}
				}
				
				//得到condition
				lenofcondition = this.condition.size();
				switch(lenofcondition){
					case 0:
						// 沒有 where
						//select name,apple
						//from table
						switch(this.aggr) {
							case 0:
								for(TableList.row_node tmp_lr : tn0_allRow){
									for(int a : indexoftargetcol_onetable){
										System.out.print(tmp_lr.data[a] + "  ");
									}
									System.out.print("\n");
								}
								break;
							case 1:
								count = 0;
								for(TableList.row_node tmp_lr : tn0_allRow) {
									for(int a : indexoftargetcol_onetable) {
										if(tmp_lr.data[a] != null)
											count++;
									}
								}
								System.out.println(count);
								break;
							case 2:
								count = 0;
								for(TableList.row_node tmp_lr : tn0_allRow) {
									int i = 0;
									for(int a : indexoftargetcol_onetable) {
										int tmp = Integer.parseInt(tmp_lr.data[a]);
										if(table0datatype[i].equals("int")) {
											count += tmp;
										}
										i++;
									}
								}
								System.out.println(count);
								break;
							default:
						}
						

						break;
					case 1:
						
						//one condition
						break;
					case 2:
						// two condition
						
						break;
					default:
						System.out.println("wrong");
						break;
				}
				break;
			case 2:
				tablename0 = this.tableName.get(0).get(0);
				tablename1 = this.tableName.get(1).get(0);
				
				// 取出table的全部col
				tn0_allRow = ct.return_colList(tablename0);
				tn1_allRow = ct.return_colList(tablename1);
				table0colname = ct.getColName(tablename0);
				table1colname = ct.getColName(tablename1);
				indexoftargetcol_twotable= new ArrayList<List<Integer>>();
				
				for(List<String> targetcol : this.colName){
					tmpc = 0;
					if(targetcol.get(0).equals(tablename0)){
						for(String c : table0colname){
							String col = targetcol.get(1);
							if(col.equals("*") || col.equalsIgnoreCase(c)) {
								tmpLI = new ArrayList<Integer>();
								tmpLI.add(tmpc);
								tmpLI.add(0);
								indexoftargetcol_twotable.add(tmpLI);
								if (col.equalsIgnoreCase(c))
									break;
							}
							tmpc++;
						}
					} else if(targetcol.get(0).equals(tablename1)){
						for(String c : table1colname){
							String col = targetcol.get(1);
							if(col.equals("*") || col.equalsIgnoreCase(c)) {
								tmpLI = new ArrayList<Integer>();
								tmpLI.add(tmpc);
								tmpLI.add(1);
								indexoftargetcol_twotable.add(tmpLI);
								if(col.equalsIgnoreCase(c))
									break;
							}
							tmpc++;
						}
					}
				}
				
				lenofcondition = this.condition.size();
				switch(lenofcondition){
					case 0:
						// 沒有 where
						//select name,apple
						//from table1 table2
						
						switch(this.aggr) {
						case 0:
							for(TableList.row_node tmp_lr0 : tn0_allRow){
								for(TableList.row_node tmp_lr1 : tn1_allRow){
									for(List<Integer> a:indexoftargetcol_twotable){
										int t = a.get(1); // 哪個table
										if(t==0){
											System.out.print(tmp_lr0.data[a.get(0)] + "  ");
										}else if(t==1){
											System.out.print(tmp_lr1.data[a.get(0)] + "  ");
											
										}
									}
									System.out.print("\n");
								}
							}
							break;
						case 1:
							count = tn0_allRow.size() * tn1_allRow.size();
							System.out.println(count);
							break;
						case 2:
							break;
						default:
					}
						
						break;
					case 1:
						
						//one condition
						break;
					case 2:
						// two condition
						
						break;
					default:
						System.out.println("wrong");
						break;
				}
					
				
				break;
			default:
				break;
		}
		
		
		
		
		
		
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
				this.tableName.get(i).add(tmp_t[0]);
				this.tableName.get(i).add(null);
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
	
	private void parseExpression(String left, String right, ConditionStruct cs) throws Exception {
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
					String table = null;
					String col = tmp_c[0];
					checkCol(table, col);
					cs.tableLeft = table;
					cs.valueLeft = col;
				} else if(t != null) {	// Table.col or t.col
					String table = t;
					String col = tmp_c[1];
					checkCol(table, col);
					cs.tableLeft = table;
					cs.valueLeft = col;
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
					String table = t;
					String col = tmp_c[1];
					checkCol(table, col);
					cs.tableLeft = table;
					cs.valueLeft = col;
				} else if(t != null) {	// Table.col or t.col
					String table = t;
					String col = tmp_c[1];
					checkCol(table, col);
					cs.tableLeft = table;
					cs.valueLeft = col;
				}			
			}
		}
	}
	
	
	private void checkCol(String table, String col) throws Exception {
		
		if(!col.equals("*")){
			String useToSetNonTableNameCol = null;
			boolean conflict = true;
			for(List<String> tmp_input_table_name : this.tableName){
				if(Main.ct.ifExistCol(tmp_input_table_name.get(0),col) == true){
					useToSetNonTableNameCol = tmp_input_table_name.get(0);
					conflict &= true;
				} else {
					conflict &= false;
				}
			}
			if(conflict) {
				throw new Exception("Column name conflicts.");
			} else {
				table = useToSetNonTableNameCol;
			}
		} else if(!Main.ct.ifExistCol(table,col)) {
			throw new Exception("Column name not found.");
		}

	}
	
	class ConditionStruct{
		String tableLeft = null;
		String tableRight = null;
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
