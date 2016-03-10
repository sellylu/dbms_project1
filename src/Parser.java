import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceTokenList;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class Parser {
	
	public Parser(){
	}
	
	public void doParser(String command){
		TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);
        sqlparser.sqltext = command;
        int ret = sqlparser.parse();
        if (ret == 0){
              for(int i=0;i<sqlparser.sqlstatements.size();i++){
                    analyzeStmt(sqlparser.sqlstatements.get(i));
                    System.out.println("");
              }
        }else{
              System.out.println(sqlparser.getErrormessage());
        }
	}
	public void analyzeStmt(TCustomSqlStatement stmt){
		switch(stmt.sqlstatementtype){
			case sstinsert:
				analyzeInsertStmt((TInsertSqlStatement)stmt);
				
				
				break;
			case sstcreatetable:
				analyzeCreateStmt((TCreateTableSqlStatement)stmt);
				
				
				break;
		
//			
//	        case sstselect: 
//	        		analyzeSelectStmt((TSelectSqlStatement)stmt);
//	        		break;
//			case sstupdate:
//			   break;
//			case sstaltertable:
//			   break;
//			case sstcreateview:
//			   break;
//			
			default:
			   System.out.println(stmt.sqlstatementtype.toString());
		}
			
	
	}

	
	private void analyzeCreateStmt(TCreateTableSqlStatement stmt){
		String tablename = stmt.getTableName().toString();
		System.out.println("table name = " + tablename);
		
		// check table name 是否重複
		if(Main.ct.checktablename(tablename)!=null){
			
		}
		
		for(int i=0;i<stmt.getColumnList().size();i++){
			System.out.println(stmt.getColumnList().getElement(i));
		}
		
		
		
	}
	
	private void analyzeInsertStmt(TInsertSqlStatement stmt){
		
	}
	private void analyzeSelectStmt(TSelectSqlStatement stmt) {
		// TODO Auto-generated method stub
		
	}
	
	 
}
