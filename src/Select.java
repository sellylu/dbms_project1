import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
			} else if(t == null) {
				throw new Exception("Table not found.");
			}
			i++;
		}
	}
	
	private void parseAggr(String[] col) throws Exception {
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
			
			if(this.tableName.size() > 1 && tmp.equals("*"))
				throw new Exception("Syntax Error: no table name specified");
			
			
			String[] tmp_c = tmp.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
			String t = findTableName(tmp_c[0]);
			String table = new String();
			String co = new String();

			if(tmp_c.length == 1) {		// ambiguous variable
				co = tmp_c[0];
				boolean conflict = true;
				if(!co.equals("*")) {
					for(List<String> tn : this.tableName){
						if(Main.ct.ifExistCol(tn.get(0), co) == true){
							table = tn.get(0);
							conflict &= true;
						} else {
							conflict &= false;
						}
					}
					if(conflict && this.tableName.size() > 1) {
						throw new Exception("Column name conflicts.");
					}
				}
			} else if(t != null) {	// Table.col or t.col
				table = t;
				co = tmp_c[1];
				if(!co.equals("*")) {
					if(!Main.ct.ifExistCol(table,co)) {
						throw new Exception("Column name not found.");
					}
				}
			}
			
			this.colName.add(new ArrayList<String>());
			this.colName.get(i).add(table);
			this.colName.get(i).add(co);
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
		
		ConditionStruct condition0 = null;
		ConditionStruct condition1 = null;
		// 判斷 From 多少table
		
		String tablename0,tablename1 = null;
		List<TableList.row_node> tn0_allRow = null;
		List<TableList.row_node> tn1_allRow = null;
		String []table0colname = null;
		String []table1colname = null;
		String []table0datatype = null;
		List<Integer> indexoftargetcol_onetable = null;
		List<List<Integer>> indexoftargetcol_twotable = null;
		int lenofcondition = 0;
		int tmpc =0;
		int count = 0;
		int sum = 0;
		int targetindex0=0;
		int targetindex1=0;
		int targetindex2=0;
		int targetindex3=0;
		List<Integer> tmpLI;
		List<Integer> checkList0;
		List<Integer> checkList1;
		int doit;
		switch(this.tableName.size()){
			
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
					case 0:		// 沒有 where
						//select name,apple
						//from table
						count = 0;
						for(TableList.row_node tmp_lr : tn0_allRow) {
							for(int a : indexoftargetcol_onetable) {
								switch(this.aggr) {
								case 0:
									System.out.printf("%20s",tmp_lr.data[a]);
									break;
								case 1:
									if(tmp_lr.data[a] != null)
										count++;
									break;
								case 2:
									if(table0datatype[a].equals("int")) {
										count += Integer.parseInt(tmp_lr.data[a]);;
									}
									break;
								}
							}
							if(this.aggr == 0)
								System.out.println();
						}
						if(this.aggr == 1)
							System.out.println(count/indexoftargetcol_onetable.size());
						if(this.aggr == 2)
							System.out.println(count);
						break;
					case 1:		// one condition
						condition0 = this.condition.get(0);
						switch(condition0.typeRight){
							case 0:
								targetindex0 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition0.valueLeft)){
										break;
					 				}else{
					 					targetindex0++;
					 				}
								}
								targetindex1 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition0.valueRight)){
										break;
									}else{
										targetindex1++;
									}
								}
								count = 0;
								for(TableList.row_node tmp_lr : tn0_allRow){
									for(int a:indexoftargetcol_onetable){
										count = checkAggr(tmp_lr.data[targetindex0], tmp_lr.data[targetindex1], tmp_lr.data[a], table0datatype[a], count, condition0.operator, 1);
									}
									if(this.aggr == 0)
										System.out.println();
								}
								if(this.aggr == 1)
									System.out.println(count/indexoftargetcol_onetable.size());
								if(this.aggr == 2)
									System.out.println(count);
								break;
							case 1:	//右邊是字串
						 	case 2:	// 右邊是數字
								targetindex0 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition0.valueLeft)){
										break;
						 			}else{
						 				targetindex0++;
						 			}
						 		}
								count = 0;
								for(TableList.row_node tmp_lr : tn0_allRow){
						 			for(int a:indexoftargetcol_onetable){
										count = checkAggr(tmp_lr.data[targetindex0], condition0.valueRight, tmp_lr.data[a], table0datatype[a], count, condition0.operator, condition0.typeRight-1);
						 			}
									if(this.aggr == 0)
										System.out.println();
								}
								if(this.aggr == 1)
									System.out.println(count/indexoftargetcol_onetable.size());
								if(this.aggr == 2)
									System.out.println(count);
								break;
				 			}
						 						
							//one condition
							break;
						case 2:
						// two condition
							
							
						//----------------------------
							checkList0 = new ArrayList<Integer>();
							checkList1 = new ArrayList<Integer>();
							condition0 = this.condition.get(0);
							condition1 = this.condition.get(1);
							doit =0;
							switch(condition0.typeRight){
								case 0:
									targetindex0 = 0;
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
									
									targetindex1 = 0;
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueRight)){
											break;
										}else{
											targetindex1++;
										}
									}
									
									switch(condition0.operator){
										case 0:
													for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){
															if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}

							 				break;
							 			case 3:
									 				for(TableList.row_node tmp_lr : tn0_allRow){
									 					for(int a:indexoftargetcol_onetable){
									 						if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
									 					}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
						 			}
									break;
								case 1:
							 		//右邊是字串
									targetindex0 = 0;
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
							 			}else{
							 				targetindex0++;
							 			}
							 		}
							 		
							 		switch(condition0.operator){
							 			case 0:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
											 			for(int a:indexoftargetcol_onetable){
											 				if(!tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
							 			case 3:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){
															if(tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
							 		}
						 			break;
							 	case 2:
									// 右邊是數字
						 			// 取得左邊在第幾個位置
						 			
						 			targetindex0 = 0;
						 			for(String t : table0colname){
							 			if(t.equalsIgnoreCase(condition0.valueLeft)){
							 				break;
										}else{
						 					targetindex0++;
						 				}
						 			}
							 			
						 			switch(condition0.operator){
							 			case 0:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){				
															if(Integer.parseInt(tmp_lr.data[targetindex0]) != Integer.parseInt(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
										case 1:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) < Integer.parseInt(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit=1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
										case 2:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) > Integer.parseInt(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}

														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
							 				break;
										case 3:
											 		for(TableList.row_node tmp_lr : tn0_allRow){
														for(int a:indexoftargetcol_onetable){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) == Integer.parseInt(condition0.valueRight)){
																//checkList0.add(1);
																//System.out.printf("%20s", tmp_lr.data[a]);
																doit =1;
															}else{
																//checkList0.add(0);
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit =0;
														}else{
															checkList0.add(0);
														}
													}
											break;
						 			}
					 				break;
					 			}
							//----------------------------
							// end of switch lefttype
							
							
							// start of switch righttype
							switch(condition1.typeRight){
							case 0:
								targetindex2 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition1.valueLeft)){
										break;
					 				}else{
					 					targetindex2++;
					 				}
								}
								
								targetindex3 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition1.valueRight)){
										break;
									}else{
										targetindex3++;
									}
								}
								
								switch(condition1.operator){
									case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(!tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit= 1;
														}else{
															//checkList1.add(0);
														}
													}
													if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}

						 				break;
						 			case 3:
								 				for(TableList.row_node tmp_lr : tn0_allRow){
								 					for(int a:indexoftargetcol_onetable){
								 						if(tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
														}else{
															//checkList1.add(0);
														}
								 					}
								 					if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
					 			}
								break;
							case 1:
						 		//右邊是字串
								targetindex2 = 0;
								for(String t : table0colname){
									if(t.equalsIgnoreCase(condition1.valueLeft)){
										break;
						 			}else{
						 				targetindex2++;
						 			}
						 		}
						 		
						 		switch(condition1.operator){
						 			case 0:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
										 			for(int a:indexoftargetcol_onetable){
										 				if(!tmp_lr.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
														}else{
															//checkList1.add(0);
														}
													}
										 			if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
						 			case 3:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(tmp_lr.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
															
														}else{
															//checkList1.add(0);
														}
													}
													if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
						 		}
					 			break;
						 	case 2:
								// 右邊是數字
					 			// 取得左邊在第幾個位置
					 			
					 			targetindex2 = 0;
					 			for(String t : table0colname){
						 			if(t.equalsIgnoreCase(condition1.valueLeft)){
						 				break;
									}else{
					 					targetindex2++;
					 				}
					 			}
						 			
					 			switch(condition1.operator){
						 			case 0:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){				
														if(Integer.parseInt(tmp_lr.data[targetindex2]) != Integer.parseInt(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
														}else{
															//checkList1.add(0);
														}
													}
													if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
									case 1:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex2]) < Integer.parseInt(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
															
														}else{
															//checkList1.add(0);
														}
													}
													
													if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
									case 2:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex2]) > Integer.parseInt(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
															
														}else{
															//checkList1.add(0);
														}
													}
													if(doit ==1){
														checkList1.add(1);
														doit=0;
													}else{
														checkList1.add(0);
													}
												}
						 				break;
									case 3:
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex2]) == Integer.parseInt(condition1.valueRight)){
															//checkList1.add(1);
															//System.out.printf("%20s", tmp_lr.data[a]);
															doit =1;
														}else{
															//checkList1.add(0);
														}
													}
													if(doit ==1){
														checkList1.add(1);
														doit =0;
													}else{
														checkList1.add(0);
													}
												}
										break;
					 			}
				 				break;
				 			}
							// end of switch lefttype
							
							
							switch(this.op){
								case 1:
									int c =0;
									count = 0;
									sum = 0;
									for(TableList.row_node tmp_lr : tn0_allRow){
										if(checkList0.get(c) + checkList1.get(c) == 2){
											count++;
											for(int a:indexoftargetcol_onetable){
												if(this.aggr == 0) {
													System.out.printf("%20s", tmp_lr.data[a]);
												} else if(this.aggr == 2) {
													if(table0datatype[a].equals("int"))
														sum += Integer.parseInt(tmp_lr.data[a]);
												}
											}
											if(this.aggr == 0)
												System.out.println();
										}
										c++;
									}
									if(this.aggr == 1)
										System.out.println(count);
									if(this.aggr == 2)
										System.out.println(sum);
									
									break;
								case 2:
									int d =0;
									count = 0;
									sum = 0;
									for(TableList.row_node tmp_lr : tn0_allRow){
										if(checkList0.get(d) + checkList1.get(d) >0){
											count++;
											for(int a:indexoftargetcol_onetable){
												if(this.aggr == 0) {
													System.out.printf("%20s", tmp_lr.data[a]);
												} else if(this.aggr == 2) {
													if(table0datatype[a].equals("int"))
														sum += Integer.parseInt(tmp_lr.data[a]);
												}
											}
											if(this.aggr == 0)
												System.out.println();
										}
										d++;
									}
									if(this.aggr == 1)
										System.out.println(count);
									if(this.aggr == 2)
										System.out.println(sum);
									break;
							}
							
						
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
				indexoftargetcol_twotable = new ArrayList<List<Integer>>();
				
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
											System.out.printf("%20s", tmp_lr0.data[a.get(0)]);
										}else if(t==1){
											System.out.printf("%20s", tmp_lr1.data[a.get(0)]);
											
										}
									}
									System.out.println();
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
						condition0 = this.condition.get(0);
						switch(condition0.typeRight){
							case 0:
								targetindex0 = 0;
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
								
								targetindex1 = 0;
								if(condition0.valueRight.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueRight)){
											break;
						 				}else{
						 					targetindex1++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueRight)){
											break;
						 				}else{
						 					targetindex1++;
						 				}
									}
								}
							
								switch(condition0.operator){
									case 0:
										switch(this.aggr) {
											case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																	for(List<Integer> a:indexoftargetcol_twotable){
																		int t = a.get(1); // 哪個table
																		if(t==0){
																			System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																		}else if(t==1){
																			System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																			
																		}
																	}
																	System.out.println();
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	for(List<Integer> a:indexoftargetcol_twotable){
																		int t = a.get(1); // 哪個table
																		if(t==0){
																			System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																		}else if(t==1){
																			System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																			
																		}
																	}
																	System.out.println();
																}
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){                   
																	for(List<Integer> a:indexoftargetcol_twotable){
																		int t = a.get(1); // 哪個table
																		if(t==0){
																			System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																		}else if(t==1){
																			System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																			
																		}
																	}
																	System.out.println();
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	for(List<Integer> a:indexoftargetcol_twotable){
																		int t = a.get(1); // 哪個table
																		if(t==0){
																			System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																		}else if(t==1){
																			System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																			
																		}
																	}
																	System.out.println();
																}
															}
															
														}
													}
													//System.out.println();
												}
													
												
												break;
											case 1:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1]))
															if(tmp_lr.data[a] != null)
																count++;
													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
												break;
											case 2:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){

														if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
												break;
												}

						 					break;
							 			case 3:
							 				switch(this.aggr) {
								 				case 0:
								 					for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
																if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																	if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																		for(List<Integer> a:indexoftargetcol_twotable){
																			int t = a.get(1); // 哪個table
																			if(t==0){
																				System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																			}else if(t==1){
																				System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																				
																			}
																		}
																		System.out.println();
																	}
																}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																	if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																		for(List<Integer> a:indexoftargetcol_twotable){
																			int t = a.get(1); // 哪個table
																			if(t==0){
																				System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																			}else if(t==1){
																				System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																				
																			}
																		}
																		System.out.println();
																	}
																}
															}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
																if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																	if(tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){                   
																		for(List<Integer> a:indexoftargetcol_twotable){
																			int t = a.get(1); // 哪個table
																			if(t==0){
																				System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																			}else if(t==1){
																				System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																				
																			}
																		}
																		System.out.println();
																	}
																}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																	if(tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																		for(List<Integer> a:indexoftargetcol_twotable){
																			int t = a.get(1); // 哪個table
																			if(t==0){
																				System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																			}else if(t==1){
																				System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																				
																			}
																		}
																		System.out.println();
																	}
																}
																
															}
														}
														//System.out.println();
													}
						 						break;
							 				case 1:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1]))
															if(tmp_lr.data[a] != null)
																count++;
													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
												break;
							 				case 2:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
												break;
						 				}
						 				break;
					 			}
								break;
							case 1:
						 		//右邊是字串
								targetindex0 = 0;
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
								
								
						 		
						 		switch(condition0.operator){
						 			case 0:
						 				switch(this.aggr) {
						 					case 0:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(!tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
								 				break;
						 					case 1:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
										 				if(!tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight))
															if(tmp_lr.data[a] != null)
																count++;
													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
						 						break;
						 					case 2:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
										 				if(!tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
						 						break;
						 				}
						 				break;
						 			case 3:
						 				switch(this.aggr) {
						 					case 0:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(tmp_lr2.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
						 						break;
						 					case 1:
						 						count = 0;
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight))      
															if(tmp_lr.data[a] != null)
																count++;
													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
						 						break;
						 					case 2:
												count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
										 				if(tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
						 						break;
						 				}
						 				break;
						 		}
					 			break;
						 	case 2:
								// 右邊是數字
					 			// 取得左邊在第幾個位置
					 			
						 		targetindex0 = 0;
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
						 			
					 			switch(condition0.operator){
						 			case 0:
						 				switch(this.aggr) {
						 					case 0:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) != Integer.parseInt(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) != Integer.parseInt(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
						 						break;
						 					case 1:
						 						count = 0;
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){				
														if(Integer.parseInt(tmp_lr.data[targetindex0]) != Integer.parseInt(condition0.valueRight))
															if(tmp_lr.data[a] != null)
																count++;													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
						 						break;
						 					case 2:
						 						count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex0]) != Integer.parseInt(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
						 						break;
						 				}
						 				break;
									case 1:
										switch(this.aggr) {
											case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) < Integer.parseInt(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) < Integer.parseInt(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
												break;
											case 1:
						 						count = 0;
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){				
														if(Integer.parseInt(tmp_lr.data[targetindex0]) < Integer.parseInt(condition0.valueRight))
															if(tmp_lr.data[a] != null)
																count++;													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
												break;
											case 2:
						 						count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex0]) < Integer.parseInt(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
												break;
										}
						 				break;
									case 2:
										switch(this.aggr) {
											case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) > Integer.parseInt(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) > Integer.parseInt(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
												break;
											case 1:
						 						count = 0;
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){				
														if(Integer.parseInt(tmp_lr.data[targetindex0]) > Integer.parseInt(condition0.valueRight))
															if(tmp_lr.data[a] != null)
																count++;													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
												break;
											case 2:
						 						count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex0]) > Integer.parseInt(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
												break;
										}
						 				break;
									case 3:
										switch(this.aggr) {
											case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) == Integer.parseInt(condition0.valueRight)){
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) == Integer.parseInt(condition0.valueRight)){                   
																for(List<Integer> a:indexoftargetcol_twotable){
																	int t = a.get(1); // 哪個table
																	if(t==0){
																		System.out.printf("%20s", tmp_lr.data[a.get(0)]);
																	}else if(t==1){
																		System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
																		
																	}
																}
																System.out.println();
															}
														}
													}
													//System.out.println();
												}
												break;
											case 1:
						 						count = 0;
										 		for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){				
														if(Integer.parseInt(tmp_lr.data[targetindex0]) == Integer.parseInt(condition0.valueRight))
															if(tmp_lr.data[a] != null)
																count++;													}
												}
												System.out.println(count/indexoftargetcol_onetable.size());
												break;
											case 2:
						 						count = 0;
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(int a:indexoftargetcol_onetable){
														if(Integer.parseInt(tmp_lr.data[targetindex0]) == Integer.parseInt(condition0.valueRight)) {
															if(table0datatype[a].equals("int")) 
																count += Integer.parseInt(tmp_lr.data[a]);
														}
													}
												}
												System.out.println(count);
												break;
										}
										break;
					 			}
				 				break;
				 			}
						
						break;
						
						// end of case 1
						
					case 2:
					/*
						// 取出table的全部col
						tn0_allRow = ct.return_colList(tablename0);
						tn1_allRow = ct.return_colList(tablename1);
						table0colname = ct.getColName(tablename0);
						table1colname = ct.getColName(tablename1);
						indexoftargetcol_twotable= new ArrayList<List<Integer>>();
						*/
						// two condition
						doit =0;
						checkList0 = new ArrayList<Integer>();
						checkList1 = new ArrayList<Integer>();
						
						condition0 = this.condition.get(0);
						condition1 = this.condition.get(1);
						
						switch(condition0.typeRight){
							case 0:
								targetindex0 = 0;
								//System.out.println("----" + condition0.tableLeft + " -----" + tablename0);
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
								
								targetindex1 = 0;
								if(condition0.valueRight.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueRight)){
											break;
						 				}else{
						 					targetindex1++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueRight)){
											break;
						 				}else{
						 					targetindex1++;
						 				}
									}
								}
							
								switch(condition0.operator){
									case 0:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																	doit =1;
																	
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(!tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	doit =1;
																	
																}
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){                   
																	doit =1;
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	doit =1;
																}
															}
															
														}
														
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
												break;
											
							 		case 3:
								 				for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){
																	doit =1;
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(tmp_lr.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	doit =1;
																}
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															if(condition0.tableRight.equalsIgnoreCase(tablename0)){
																if(tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr.data[targetindex1])){                   
																	doit =1;
																}
															}else if(condition0.tableRight.equalsIgnoreCase(tablename1)){
																if(tmp_lr2.data[targetindex0].equalsIgnoreCase(tmp_lr2.data[targetindex1])){
																	doit =1;
																}
															}
															
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
							 				
							 		}
								break;
							case 1:
						 		//右邊是字串
								targetindex0 = 0;
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
								
								
						 		
						 		switch(condition0.operator){
						 			case 0:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(!tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																doit=1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(!tmp_lr2.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){                   
																doit =1;
															}
														}
														if(doit == 1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
								 				break;
						 			case 3:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(tmp_lr.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){
																doit =1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(tmp_lr2.data[targetindex0].equalsIgnoreCase(condition0.valueRight)){                   
																doit =1;
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
						 						break;
						 		}
					 			break;
						 	case 2:
								// 右邊是數字
					 			// 取得左邊在第幾個位置
					 			
						 		targetindex0 = 0;
								if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
									for(String t : table0colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}else{
									for(String t : table1colname){
										if(t.equalsIgnoreCase(condition0.valueLeft)){
											break;
						 				}else{
						 					targetindex0++;
						 				}
									}
								}
						 			
					 			switch(condition0.operator){
						 			case 0:
						 						for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) != Integer.parseInt(condition0.valueRight)){
																doit =1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) != Integer.parseInt(condition0.valueRight)){                   
																doit =1;
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
						 						break;
						 					
									case 1:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) < Integer.parseInt(condition0.valueRight)){
																doit=1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) < Integer.parseInt(condition0.valueRight)){                   
																doit=1;
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
												break;
									case 2:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) > Integer.parseInt(condition0.valueRight)){
																doit =1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) > Integer.parseInt(condition0.valueRight)){                   
																doit =1;
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
												break;
											
									case 3:
												for(TableList.row_node tmp_lr : tn0_allRow){
													for(TableList.row_node tmp_lr2 : tn1_allRow){
														if(condition0.tableLeft.equalsIgnoreCase(tablename0)){
															if(Integer.parseInt(tmp_lr.data[targetindex0]) == Integer.parseInt(condition0.valueRight)){
																doit =1;
															}
														}else if(condition0.tableLeft.equalsIgnoreCase(tablename1)){
															
															if(Integer.parseInt(tmp_lr2.data[targetindex0]) == Integer.parseInt(condition0.valueRight)){                   
																doit =1;
															}
														}
														if(doit ==1){
															checkList0.add(1);
															doit=0;
														}else{
															checkList0.add(0);
														}
													}
												}
												break;

					 			}
				 				break;
				 		}
								
								switch(condition1.typeRight){
								case 0:
									targetindex2 = 0;
									if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
										for(String t : table0colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}else{
										for(String t : table1colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}
									
									targetindex3 = 0;
									if(condition1.valueRight.equalsIgnoreCase(tablename0)){
										for(String t : table0colname){
											if(t.equalsIgnoreCase(condition1.valueRight)){
												break;
							 				}else{
							 					targetindex3++;
							 				}
										}
									}else{
										for(String t : table1colname){
											if(t.equalsIgnoreCase(condition1.valueRight)){
												break;
							 				}else{
							 					targetindex3++;
							 				}
										}
									}
								
									switch(condition1.operator){
										case 0:
													for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(condition1.tableRight.equalsIgnoreCase(tablename0)){
																	if(!tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){
																		doit =1;
																		
																	}
																}else if(condition1.tableRight.equalsIgnoreCase(tablename1)){
																	if(!tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr2.data[targetindex3])){
																		doit =1;
																		
																	}
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																if(condition1.tableRight.equalsIgnoreCase(tablename0)){
																	if(!tmp_lr2.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){                   
																		doit =1;
																	}
																}else if(condition1.tableRight.equalsIgnoreCase(tablename1)){
																	if(!tmp_lr2.data[targetindex2].equalsIgnoreCase(tmp_lr2.data[targetindex3])){
																		doit =1;
																	}
																}
																
															}
															
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
														
													
													break;
												
								 			case 3:
									 					for(TableList.row_node tmp_lr : tn0_allRow){
															for(TableList.row_node tmp_lr2 : tn1_allRow){
																if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																	if(condition1.tableRight.equalsIgnoreCase(tablename0)){
																		if(tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){
																			doit =1;
																		}
																	}else if(condition1.tableRight.equalsIgnoreCase(tablename1)){
																		if(tmp_lr.data[targetindex2].equalsIgnoreCase(tmp_lr2.data[targetindex3])){
																			doit =1;
																		}
																	}
																}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																	if(condition1.tableRight.equalsIgnoreCase(tablename0)){
																		if(tmp_lr2.data[targetindex2].equalsIgnoreCase(tmp_lr.data[targetindex3])){                   
																			doit =1;
																		}
																	}else if(condition1.tableRight.equalsIgnoreCase(tablename1)){
																		if(tmp_lr2.data[targetindex2].equalsIgnoreCase(tmp_lr2.data[targetindex3])){
																			doit =1;
																		}
																	}
																	
																}
																if(doit ==1){
																	checkList1.add(1);
																	doit=0;
																}else{
																	checkList1.add(0);
																}
															}
														}
							 						break;
								 				
						 			}
									break;
								case 1:
							 		//右邊是字串

									//System.out.println("wwwww =" + condition1.tableLeft);
									if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
										for(String t : table0colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}else{
										for(String t : table1colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}
									
									
							 		
							 		switch(condition1.operator){
							 			case 0:
							 						for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(!tmp_lr.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){
																	doit=1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(!tmp_lr2.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){                   
																	doit =1;
																}
															}
															if(doit == 1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
									 				break;
							 					
							 			case 3:
							 						for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(tmp_lr.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){
																	doit =1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(tmp_lr2.data[targetindex2].equalsIgnoreCase(condition1.valueRight)){                   
																	doit =1;
																}
															}
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
							 						break;
							 					
							 		}
						 			break;
							 	case 2:
									// 右邊是數字
						 			// 取得左邊在第幾個位置
						 			
							 		targetindex2 = 0;
									if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
										for(String t : table0colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}else{
										for(String t : table1colname){
											if(t.equalsIgnoreCase(condition1.valueLeft)){
												break;
							 				}else{
							 					targetindex2++;
							 				}
										}
									}
							 			
						 			switch(condition1.operator){
							 			case 0:
							 						for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(Integer.parseInt(tmp_lr.data[targetindex2]) != Integer.parseInt(condition1.valueRight)){
																	doit =1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(Integer.parseInt(tmp_lr2.data[targetindex2]) != Integer.parseInt(condition1.valueRight)){                   
																	doit =1;
																}
															}
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
							 						break;
							 					
										case 1:
													for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(Integer.parseInt(tmp_lr.data[targetindex2]) < Integer.parseInt(condition1.valueRight)){
																	doit=1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(Integer.parseInt(tmp_lr2.data[targetindex2]) < Integer.parseInt(condition1.valueRight)){                   
																	doit=1;
																}
															}
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
													break;
										case 2:
													for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(Integer.parseInt(tmp_lr.data[targetindex2]) > Integer.parseInt(condition1.valueRight)){
																	doit =1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(Integer.parseInt(tmp_lr2.data[targetindex2]) > Integer.parseInt(condition1.valueRight)){                   
																	doit =1;
																}
															}
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
													break;
										case 3:
													for(TableList.row_node tmp_lr : tn0_allRow){
														for(TableList.row_node tmp_lr2 : tn1_allRow){
															if(condition1.tableLeft.equalsIgnoreCase(tablename0)){
																if(Integer.parseInt(tmp_lr.data[targetindex2]) == Integer.parseInt(condition1.valueRight)){
																	doit =1;
																}
															}else if(condition1.tableLeft.equalsIgnoreCase(tablename1)){
																
																if(Integer.parseInt(tmp_lr2.data[targetindex2]) == Integer.parseInt(condition1.valueRight)){                   
																	doit =1;
																}
															}
															if(doit ==1){
																checkList1.add(1);
																doit=0;
															}else{
																checkList1.add(0);
															}
														}
													}
													break;
						 			}
					 				break;
								}

								
								switch(this.op){
								case 1:
									int c =0;
									for(TableList.row_node tmp_lr : tn0_allRow){
										for(TableList.row_node tmp_lr2 : tn1_allRow){
											if(checkList0.get(c) + checkList1.get(c) == 2){
												for(List<Integer> a:indexoftargetcol_twotable){
													int t = a.get(1); // 哪個table
													if(t==0){
														System.out.printf("%20s", tmp_lr.data[a.get(0)]);
													}else if(t==1){
														System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
														
													}
												}
												System.out.println();
											}
											c++;
										}
									
											
									}
									
									break;
								case 2:
									int d =0;
									for(TableList.row_node tmp_lr : tn0_allRow){
										for(TableList.row_node tmp_lr2 : tn1_allRow){
											if(checkList0.get(d) + checkList1.get(d) >0){
												for(List<Integer> a:indexoftargetcol_twotable){
													int t = a.get(1); // 哪個table
													if(t==0){
														System.out.printf("%20s", tmp_lr.data[a.get(0)]);
													}else if(t==1){
														System.out.printf("%20s", tmp_lr2.data[a.get(0)]);
														
													}
												}
												System.out.println();
											}
											d++;
										}
									
											
									}
									break;
							}
								
							
						
						// end of two condition
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
			cs.dataLeft ="int";
			cs.indexLeft = false;
		} catch (NumberFormatException e) {
			if(left.startsWith("\\'") && left.endsWith("\\'")) {
				cs.typeLeft = 1;
				cs.valueLeft = left;
				cs.dataLeft ="varchar";
				cs.indexLeft = false;
			} else {
				cs.typeLeft = 0;
				String[] tmp_c = left.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				String t = findTableName(tmp_c[0]);
				String table = new String();

				if(tmp_c.length == 1) {		// ambiguous variable
					table = null;
					String col = tmp_c[0];
					cs.tableLeft = checkCol(table, col,cs, false);
					cs.valueLeft = col;
				} else if(t != null) {	// Table.col or t.col
					table = t;
					String col = tmp_c[1];
					cs.tableLeft = checkCol(table, col,cs, false);
					cs.valueLeft = col;
				}
				cs.indexLeft = false;
				TableList.table_node tn = Main.ct.checktablename(cs.tableLeft);
				if(Main.ct.checkIndex(tn, cs.valueLeft)) {
					cs.indexLeft = true;
				}
			}
		}
		// Right
		try {
			Integer.parseInt(right);
			cs.typeRight = 2;
			cs.valueRight = right;
			cs.dataRight ="int";
			cs.indexRight = false;
		} catch (NumberFormatException e) {
			if(right.startsWith("\'") && right.endsWith("\'")) {
				cs.typeRight = 1;
				cs.valueRight = right;
				cs.dataRight = "varchar";
				cs.indexRight = false;
			} else {
				cs.typeRight = 0;
				String[] tmp_c = right.split("\\.");	// table.col	tmp_c[0]=>table		tmp_c[1]=>col
				String t = findTableName(tmp_c[0]);
				String table = new String();

				if(tmp_c.length == 1) {		// ambiguous variable
					table = null;
					String col = tmp_c[0];
					cs.tableRight = checkCol(table, col,cs, true);
					cs.valueRight = col;
				} else if(t != null) {	// Table.col or t.col
					table = t;
					String col = tmp_c[1];
					cs.tableRight = checkCol(table, col,cs, true);
					cs.valueRight = col;
				}
				cs.indexRight = false;
				TableList.table_node tn = Main.ct.checktablename(cs.tableRight);
				if(Main.ct.checkIndex(tn, cs.valueRight)) {
					cs.indexRight = true;
				}
			}
		}
		
		if(!cs.dataLeft.equalsIgnoreCase(cs.dataRight)) {
			throw new Exception("Datatype not match");
		}
	}
	
	
	private String checkCol(String table, String col, ConditionStruct cs, boolean pos) throws Exception {
		String datatype = null;
		String useToSetNonTableNameCol = null;
		boolean conflict = true;
		if(!col.equals("*") && table == null) {
			for(List<String> tmp_input_table_name : this.tableName){
				if(Main.ct.ifExistCol(tmp_input_table_name.get(0),col) == true){
					datatype = Main.ct.getDataType(tmp_input_table_name.get(0), col);
					useToSetNonTableNameCol = tmp_input_table_name.get(0);
					conflict &= true;
				} else {
					conflict &= false;
				}
			}
			if(conflict && this.tableName.size() > 1) {
				throw new Exception("Column name conflicts.");
			}
		} else if(Main.ct.ifExistCol(table,col)) {
			datatype = Main.ct.getDataType(table, col);
			useToSetNonTableNameCol = table;
		} else {
			throw new Exception("Column name not found.");
		}
		if(pos == false) { //left
			cs.dataLeft = datatype;
		} else {
			cs.dataRight = datatype;
		}
		return useToSetNonTableNameCol;
	}
	
	private int checkAggr(String le, String ri, String a, String type, int count, int op, int num) {
		if(op == 0 || op == 3 && num != 1) {
			if((!le.equalsIgnoreCase(ri) && op == 0) || (le.equalsIgnoreCase(ri) && op == 3)) 
				count = innerCheck(a, count, type);
		} else if(op == 0 || op == 3) {
			if((Integer.parseInt(le) != Integer.parseInt(ri) && op == 0) || (Integer.parseInt(le) == Integer.parseInt(ri) && op == 3))
				count = innerCheck(a, count, type);
		} else if(op == 1 || op == 2) {
			if((Integer.parseInt(le) < Integer.parseInt(ri) && op == 1) || (Integer.parseInt(le) > Integer.parseInt(ri) && op == 2))
				count = innerCheck(a, count, type);
		}
		return count;
	}
	private int innerCheck(String a, int count, String type) {
		switch(this.aggr) {
		case 0:
			System.out.printf("%20s",a);
			break;
		case 1:
			if(a != null)
				count++;
			break;
		case 2:
			if(type.equals("int")) 
				count += Integer.parseInt(a);
		}
		return count;
	}
	
	public boolean checkIndex() {
		for(ConditionStruct cs: this.condition) {
			if(cs.indexLeft || cs.indexRight) {
				return true;
			}
		}
		return false;
	}
	
	public void doIndexSelect() {
		ConditionStruct cs1 = null;
		ConditionStruct cs2 = null;
		Index in1;
		Index in2;
		List<TableList.row_node> l1 = new ArrayList<TableList.row_node>();
		List<TableList.row_node> l2 = new ArrayList<TableList.row_node>();
		String[] cn;
		
		switch(this.tableName.size()) {
			case 1:	// one table
				switch(this.condition.size()) {
					case 1:	// one condition which is index
						cs1 = this.condition.get(0);
						if(cs1.indexLeft && cs1.typeRight != 0) {
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							l1 = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
						} else if(cs1.indexLeft) {
							// TODO: where compare two column in one table
						}
						break;	// end one cond
					case 2:	// two condition
						cs1 = this.condition.get(0);
						cs2 = this.condition.get(1);
						if (cs1.indexLeft && cs1.typeRight == 0) {
							int a = Main.ct.checktablename(cs1.tableLeft).getColumnIndex(cs1.valueLeft);
							List<TableList.row_node> rn = Main.ct.return_colList(cs1.tableLeft);
							for(TableList.row_node r: rn) {
								in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
								l1 = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, r.data[a]));
							}
						} else if(cs1.valueLeft.equals(cs2.valueLeft) && cs1.tableLeft.equals(cs2.tableLeft) &&
								cs1.indexLeft) {	// two condition use same col which is index
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							l1 = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
							l2 = new ArrayList<TableList.row_node>(getCondResult(in1, cs2, null));
							uni_or_inter(l1, l2);
						} else if(cs1.indexLeft) {	// first condition use index only
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							l1 = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
							twoCond_oneIndex(l1,null, cs2);
						} else if(cs2.indexLeft) {	// second condition use index only
							in1 = Main.indexlist.getIndex(cs2.tableRight, cs2.valueRight);
							l1 = new ArrayList<TableList.row_node>(getCondResult(in1, cs2, null));
							twoCond_oneIndex(l1,null, cs1);
						}						
						break;	// end two cond
				}
				// TODO: pick assign column from List<TableList.row_node> l1
				TableList.table_node tn = Main.ct.checktablename(cs1.tableLeft);
				cn = Main.ct.getColName(cs1.tableLeft);
				String[] type= Main.ct.getDataType(cs1.tableLeft);

				//int a = Main.ct.checktablename(cs1.tableLeft).getColumnIndex(cs1.valueLeft);
				int count = 0;
				int num = this.colName.size();					
				for(TableList.row_node t : l1) {
					for(List<String> ls: this.colName) {
						if(ls.get(1).equals("*")) {
							if(count == 0) {
								num += cn.length-1;
							}
							for(int i = 0; i < cn.length; i++) {
								count = innerCheck(t.data[i], count, type[i]);
							}
						} else {
							int a = tn.getColumnIndex(ls.get(1));
							count = innerCheck(t.data[a], count, type[a]);
						}
					}
					if(this.aggr == 0)
						System.out.println();
				}
				if(this.aggr == 1)
					System.out.println(count/num);
				if(this.aggr == 2)
					System.out.println(count);
				
				break;	// end one table
			case 2:	// two table
				
				List<String> cn1 = Arrays.asList(Main.ct.getColName(this.tableName.get(0).get(0)));
				List<String> cn2 = Arrays.asList(Main.ct.getColName(this.tableName.get(1).get(0)));
				List<Integer> resultColIndex1 = new ArrayList<Integer>();
				List<Integer> resultColIndex2 = new ArrayList<Integer>();
				
				for(List<String> ls: this.colName){
					if(!ls.get(1).equals("*")) {
						if(ls.get(0).equalsIgnoreCase(this.tableName.get(0).get(0))) {
							for(int i = 0; i < cn1.size(); i++)
								if(cn1.get(i).equalsIgnoreCase(ls.get(1))) {
									resultColIndex1.add(i);
									break;
								}
						} else if(ls.get(0).equalsIgnoreCase(this.tableName.get(1).get(0))) {
							for(int i = 0; i < cn1.size(); i++)
								if(cn2.get(i).equalsIgnoreCase(ls.get(1))) {
									resultColIndex2.add(i);
									break;
								}
						}
					} else {
						if(ls.get(0).equalsIgnoreCase(this.tableName.get(0).get(0))) {
							for(int i = 0; i < cn1.size(); i++) {
								resultColIndex1.add(i);
							}
						} else if(ls.get(0).equalsIgnoreCase(this.tableName.get(1).get(0))) {
							for(int i = 0; i < cn2.size(); i++) {
								resultColIndex2.add(i);
							}
						}
					}
				}
				
				switch(this.condition.size()) {
					case 1:	// one condition which is index
						cs1 = this.condition.get(0);
						if(cs1.indexLeft && cs1.typeRight != 0) {
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							List<TableList.row_node> tmp = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
							for(TableList.row_node rn: tmp) {
								String tb = new String();
								if(this.tableName.get(0).get(0).equals(cs1.tableLeft)) {
									tb = this.tableName.get(1).get(0);
								} else {
									tb = this.tableName.get(0).get(0);
								}
								List<TableList.row_node> lr = Main.ct.return_colList(tb);
								for(int i = 0; i < lr.size(); i++)
									l1.add(rn);
								l2.addAll(lr);
							}
						} else if(cs1.indexLeft) {
							// TODO: where compare two column in one table
						}
						break;
					case 2:
						cs1 = this.condition.get(0);
						cs2 = this.condition.get(1);
						if (cs1.indexLeft && cs1.typeRight == 0) {
							int a = Main.ct.checktablename(cs1.tableLeft).getColumnIndex(cs1.valueLeft);
							List<TableList.row_node> rn = Main.ct.return_colList(cs1.tableLeft);
							l2 = new ArrayList<TableList.row_node>();
							for(TableList.row_node r: rn) {
								in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
								
								String tb = new String();
								if(this.tableName.get(0).get(0).equals(cs1.tableLeft)) {
									tb = this.tableName.get(1).get(0);
								} else {
									tb = this.tableName.get(0).get(0);
								}
								List<TableList.row_node> re = getCondResult(in1, cs1, r.data[a]);
								l1.addAll(re);
								for(int i = 0; i < re.size(); i++) {
									l2.add(r);
								}
							}
							twoCond_oneIndex(l1, l2, cs2);
						} else if(cs1.valueLeft.equals(cs2.valueLeft) && cs1.tableLeft.equals(cs2.tableLeft) &&
								cs1.indexLeft) {	// two condition use same col which is index
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							List<TableList.row_node> tmp = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
							l2 = new ArrayList<TableList.row_node>(getCondResult(in1, cs2, null));
							uni_or_inter(tmp, l2);
							l2 = new ArrayList<TableList.row_node>();
							for(TableList.row_node rn: tmp) {
								String tb = new String();
								if(this.tableName.get(0).get(0).equals(cs1.tableLeft)) {
									tb = this.tableName.get(1).get(0);
								} else {
									tb = this.tableName.get(0).get(0);
								}
								List<TableList.row_node> lr = Main.ct.return_colList(tb);
								for(int i = 0; i < lr.size(); i++)
									l1.add(rn);
								l2.addAll(lr);
							}
							
						} else if(cs1.indexLeft) {	// first condition use index only
							in1 = Main.indexlist.getIndex(cs1.tableLeft, cs1.valueLeft);
							List<TableList.row_node> tmp = new ArrayList<TableList.row_node>(getCondResult(in1, cs1, null));
							for(TableList.row_node rn: tmp) {
								String tb = new String();
								if(this.tableName.get(0).get(0).equals(cs1.tableLeft)) {
									tb = this.tableName.get(1).get(0);
								} else {
									tb = this.tableName.get(0).get(0);
								}
								List<TableList.row_node> lr = Main.ct.return_colList(tb);
								for(int i = 0; i < lr.size(); i++)
									l1.add(rn);
								l2.addAll(lr);
							}
							twoCond_oneIndex(l1, l2, cs2);
						} else if(cs2.indexLeft) {	// second condition use index only
							in1 = Main.indexlist.getIndex(cs2.tableRight, cs2.valueRight);
							List<TableList.row_node> tmp = new ArrayList<TableList.row_node>(getCondResult(in1, cs2, null));
							for(TableList.row_node rn: tmp) {
								String tb = new String();
								if(this.tableName.get(0).get(0).equals(cs1.tableLeft)) {
									tb = this.tableName.get(1).get(0);
								} else {
									tb = this.tableName.get(0).get(0);
								}
								List<TableList.row_node> lr = Main.ct.return_colList(tb);
								for(int i = 0; i < lr.size(); i++)
									l1.add(rn);
								l2.addAll(lr);
							}
							twoCond_oneIndex(l1, l2, cs1);
						}	
						
						
						break;
				}
				cn = Main.ct.getColName(cs1.tableLeft);
				String[] type1 = Main.ct.getDataType(this.tableName.get(0).get(0));
				String[] type2 = Main.ct.getDataType(this.tableName.get(1).get(0));

				//int a = Main.ct.checktablename(cs1.tableLeft).getColumnIndex(cs1.valueLeft);
				int count2 = 0;
				int j = 0;
				for(TableList.row_node t : l1) {
					for(Integer id: resultColIndex1) {
						if(cs1.tableLeft.equals(this.tableName.get(0).get(0)))
							count2 = innerCheck(t.data[id], count2, type2[id]);
						else
							count2 = innerCheck(l2.get(j).data[id], count2, type1[id]);
					}
					for(Integer id: resultColIndex2) {
						if(cs1.tableLeft.equals(this.tableName.get(0).get(0)))
							count2 = innerCheck(l2.get(j).data[id], count2, type2[id]);
						else
							count2 = innerCheck(t.data[id], count2, type1[id]);
					}
					if(this.aggr == 0)
						System.out.println();
					j++;
				}
				if(this.aggr == 1)
					System.out.println(count2/(resultColIndex1.size()+resultColIndex2.size()));
				if(this.aggr == 2)
					System.out.println(count2);
				
				break;	// end two table
			default:
		}

	}
	
	private List<TableList.row_node> getCondResult(Index i, ConditionStruct cs, String s) {
		Integer integer;
		if(cs.typeRight != 0)
			integer = new Integer(cs.valueRight);
		else
			integer = Integer.parseInt(s);
		
		switch(cs.operator) {
		
		case 0:	// not equal
			return i.btree.get_notequal(integer);
		case 1:	// smaller
			return i.btree.get_less(integer);
		case 2:	// greater
			return i.btree.get_bigger(integer);
		case 3:	// equal
			return i.btree.get(integer);
		default:
			return new ArrayList<TableList.row_node>();
		}
	}
		
	private void uni_or_inter(List l1, List l2) {
		switch(this.op) {
		case 1:	// AND
			l1.retainAll(l2);
			break;
		case 2:	// OR
			l1.addAll(l2);
			break;
		}
	}
	private void twoCond_oneIndex(List<TableList.row_node> l1,List<TableList.row_node> l2, ConditionStruct cs2) {
		int a = Main.ct.checktablename(cs2.tableLeft).getColumnIndex(cs2.valueLeft);
		int j = 0;
		int i = 0;
		switch(this.op) {
		case 1:	// AND
			List<TableList.row_node> lr = new ArrayList<TableList.row_node>(l1);
				for(Object o: lr) {
					TableList.row_node rn1 = (TableList.row_node)o;
					TableList.row_node rn2 = l2.get(j);
					TableList.row_node rn;
					if(cs2.tableLeft.equalsIgnoreCase(this.tableName.get(0).get(0))) {
						rn = rn2;
					} else {
						rn = rn1;
					}
					switch(cs2.typeRight){
					case 0:
						TableList.row_node rn3;
						if(cs2.tableRight.equalsIgnoreCase(this.tableName.get(0).get(0))) {
							rn3 = rn1;
						} else {
							rn3 = rn2;
						}
						int b = Main.ct.checktablename(cs2.tableRight).getColumnIndex(cs2.valueRight);
						String type = cs2.dataLeft;
						if(type.equals("int")){
							if(!rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 0) {
								continue;
							} else if(Integer.parseInt(rn.data[a]) < Integer.parseInt(rn3.data[b]) && cs2.operator == 1) {
								continue;
							} else if(Integer.parseInt(rn.data[a]) > Integer.parseInt(rn3.data[b]) && cs2.operator == 2) {
								continue;
							} else if(rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 3) {
								continue;
							} else {
								int ii = l1.indexOf(o);
								l1.remove(o);
								l2.remove(ii);
							}
						}else if(type.equals("varchar")){
							if(!rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 0) {
								continue;
							} else if(rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 3) {
								continue;
							} else {
								int ii = l1.indexOf(o);
								l1.remove(o);
								l2.remove(ii);
							}
						}
						break;
					case 1:
						if(!rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 0) {
							continue;
						}else if(rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 3) {
							continue;
						} else {
							int ii = l1.indexOf(o);
							l1.remove(o);
							l2.remove(ii);
						}
						break;
					case 2:
					
						if(!rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 0) {
							continue;
						} else if(Integer.parseInt(rn.data[a]) < Integer.parseInt(cs2.valueRight) && cs2.operator == 1) {
							continue;
						} else if(Integer.parseInt(rn.data[a]) > Integer.parseInt(cs2.valueRight) && cs2.operator == 2) {
							continue;
						} else if(rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 3) {
							continue;
						} else {
							int ii = l1.indexOf(o);
							l1.remove(o);
							l2.remove(ii);
						}
						break;
						
					
					}
				}
			break;
		case 2:	// OR
			//List<TableList.row_node> lr = new ArrayList<TableList.row_node>(l1);
			i = 0;
			
				List<TableList.row_node> row_list = null;
				List<TableList.row_node> row_list2 = null;
				if(cs2.tableLeft.equalsIgnoreCase(this.tableName.get(0).get(0))) {
					row_list = Main.ct.return_colList(this.tableName.get(0).get(0));
					row_list2 = Main.ct.return_colList(this.tableName.get(1).get(0));
				} else if(cs2.tableLeft.equalsIgnoreCase(this.tableName.get(1).get(0))) {
					row_list = Main.ct.return_colList(this.tableName.get(1).get(0));
					row_list2 = Main.ct.return_colList(this.tableName.get(0).get(0));
				}
				List<TableList.row_node> tmp = new ArrayList<TableList.row_node>();
				List<TableList.row_node> tmp2 = new ArrayList<TableList.row_node>();
				for(TableList.row_node rn : row_list) {	
					TableList.row_node rn3 = row_list.get(i);

					switch(cs2.typeRight){
					case 0:
						int b = Main.ct.checktablename(cs2.tableRight).getColumnIndex(cs2.valueRight);
						String type = cs2.dataLeft;
						if(type.equals("int")){
							if(!rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 0) {
								tmp.add(rn);
								tmp2.add(rn3);
							} else if(Integer.parseInt(rn.data[a]) < Integer.parseInt(rn3.data[b]) && cs2.operator == 1) {
								tmp.add(rn);
								tmp2.add(rn3);
							} else if(Integer.parseInt(rn.data[a]) > Integer.parseInt(rn3.data[b]) && cs2.operator == 2) {
								tmp.add(rn);
								tmp2.add(rn3);
							} else if(rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 3) {
								tmp.add(rn);
								tmp2.add(rn3);
							}
						} else if(type.equals("varchar")){
							if(!rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 0) {
								tmp.add(rn);
								tmp2.add(rn3);
							} else if(rn.data[a].equalsIgnoreCase(rn3.data[b]) && cs2.operator == 3) {
								tmp.add(rn);
								tmp2.add(rn3);
							}
						}
						break;
					case 1:
						if(!rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 0) {
							tmp.add(rn);
							tmp2.add(rn3);
						} else if(rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 3) {
							tmp.add(rn);
							tmp2.add(rn3);
						}
						break;
					case 2:
					
						if(!rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 0) {
							tmp.add(rn);
							tmp2.add(rn3);
						} else if(Integer.parseInt(rn.data[a]) < Integer.parseInt(cs2.valueRight) && cs2.operator == 1) {
							tmp.add(rn);
							tmp2.add(rn3);
						} else if(Integer.parseInt(rn.data[a]) > Integer.parseInt(cs2.valueRight) && cs2.operator == 2) {
							tmp.add(rn);
							tmp2.add(rn3);
						} else if(rn.data[a].equalsIgnoreCase(cs2.valueRight) && cs2.operator == 3) {
							tmp.add(rn);
							tmp2.add(rn3);
						}
						break;
					}
					i++;
					
				}
				l1.removeAll(tmp);
				l1.addAll(tmp);
				l2.remove(tmp2);
				l2.addAll(tmp2);
			
			break;
		}
	}
	
	class ConditionStruct{
		boolean indexLeft = false;
		boolean indexRight = false;
		String dataLeft;
		String dataRight; 
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
