package net.boigroup.bdd.framework;


import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class DbActions {

	final static Logger LOG = Logger.getLogger(DbActions.class);
	public static class QueryExecutor {
		private final String hostname;
		private final String user;
		private final int port;
		private final String password;
		private final String database;
		public QueryExecutor(String hostname, int port,String database, String user, String password){
			String driver = ConfigLoader.config().getString("jdbc.driver","oracle.jdbc.driver.OracleDriver");
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				LOG.error("Cannot load driver",e);
				throw new IllegalStateException("No JDBC driver", e);			
			}
			this.hostname = hostname;
			this.port=port;
			this.user=user;
			this.password = password;
			this.database = database;
		}
		private Connection connection;
		private Connection getConnection() throws SQLException{
			if (connection == null || connection.isClosed()){
				String driver = ConfigLoader.config().getString("jdbc.driver","oracle.jdbc.driver.OracleDriver"); 
				String jdbConnectionString = ConfigLoader.config().getString("jdbc.conn",null);
				if (jdbConnectionString == null && driver.contains("OracleDriver")){
					jdbConnectionString = String.format("jdbc:oracle:thin:@%s:%s:%s", hostname,port,database);
				} else if (jdbConnectionString == null && driver.contains("TeraDriver")){
					jdbConnectionString = String.format("jdbc:teradata://%s/database=%s,tmode=ANSI,charset=UTF8", hostname,database);
				}
				
				LOG.info("Connecting to { " + jdbConnectionString + " }");
				connection = DriverManager.getConnection(jdbConnectionString, user,password);
			}
			return connection;
		}

		public void close(){
			try {
				if (connection != null && ! connection.isClosed() ){
					connection.close();
				}
			} catch (SQLException ignore) {
			}
		}

		public String runSqlQuery(String sqlQuery) throws SQLException{
			String result = "";
			try {
				ResultSet resultData = runSqlQueryToSet(sqlQuery);
				result = Joiner.on("\n").join(asMaps(resultData));
				LOG.debug("Result of query { "+ sqlQuery+ " }: {" + result + "}");
			} catch (SQLException e){
				LOG.error("Execution of query { "+ sqlQuery+ " }: {" + e + "}");
				Throwables.propagate(e);
			}
			return result;
		}

		public ResultSet runSqlQueryToSet(String sqlQuery) throws SQLException{
			Statement statement = getConnection().createStatement();
			LOG.info("Running query {" + sqlQuery + "} on database {" + database + "}");
			ResultSet resultData = statement.executeQuery(sqlQuery);
			return resultData;
		}

		public static List<Map<String,Object>> asMaps(ResultSet queryData) throws SQLException{
			BasicRowProcessor processor = new BasicRowProcessor();
			List<Map<String,Object>> result = Lists.newArrayList();
			while (queryData.next()){
				Map<String,Object> res = processor.toMap(queryData);
				result.add(res);
			}
			return result;
		}

		public List<Map<String,Object>> runSqlQueryToMap(String query) throws SQLException{
			return asMaps(runSqlQueryToSet(query));
		}
	}


	public static QueryExecutor onDatabase(String hostname, String database, String user, String password){
		return new QueryExecutor(hostname, 1521, database,user, password);
	}


	public static QueryExecutor onDatabase(String hostname, int port, String database,String user, String password){
		return new QueryExecutor(hostname, port, database,user, password);
	}
}



