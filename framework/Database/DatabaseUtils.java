package net.boigroup.bdd.framework.Database;

import net.boigroup.bdd.framework.DBUtils;
import net.boigroup.bdd.framework.LogUtil;
import net.boigroup.bdd.framework.DbActions;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static net.boigroup.bdd.framework.ConfigLoader.config;
import static java.lang.String.format;

public class DatabaseUtils {
	final static Logger LOG = Logger.getLogger(DatabaseUtils.class);

	private static final String CONNECTION_STRING_TEMPLATE = "jdbc:oracle:thin:@%s:1521:%s";

	private static DatabaseUtils instance = new DatabaseUtils();

	public static DatabaseUtils getInstance() {
		return instance;
	}

	public List<Map<String, Object>> runSelectQueryImpl(String dataSource, String username, String password, String sql) {
		List<Map<String, Object>> result = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection connection = null;
			connection = DriverManager.getConnection(dataSource, config().getString(username),
					config().getString(password));
			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			result = DbActions.QueryExecutor.asMaps(rs);
			if(result.isEmpty()){
				LogUtil.logAttachment(sql,"No Results found for the Query \n");
			}else {
                if (result.size() > 1) {
                    LogUtil.logCSVAttachment(sql, DBUtils.getCSVFormat(result));
                } else {
                    LogUtil.logAttachment(sql, "\n Results : \n" + DBUtils.getDBTableReportFormatCol(result));
                }
            }
			connection.close();
		} catch (SQLException | ClassNotFoundException e) {
			LogUtil.log("Error running query: \n" + sql + "\n" + e);
		}

		return result;
	}

	public void runQueryImpl(String dataSource, String username, String password, String sql) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection connection = null;
			connection = DriverManager.getConnection(dataSource,config().getString(username),
					config().getString(password));
			Statement stat = connection.createStatement();
			LogUtil.log("SQL Query : " + sql);
			stat.executeUpdate(sql);
			connection.close();
		} catch (SQLException | ClassNotFoundException e) {
			LogUtil.log("Error running query: \n" + sql + "\n" + e);
		}
	}

	public void runBulkUpdateQueryImpl(String dataSource, String username, String password, List<String> sql) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection connection = null;
			connection = DriverManager.getConnection(dataSource,config().getString(username),
					config().getString(password));
			Statement statement = connection.createStatement();

			for (String query : sql) {
				statement.addBatch(query);
			}
			statement.executeBatch();
			statement.close();
			connection.close();
		} catch (SQLException | ClassNotFoundException e) {
			LogUtil.log("Error running query: \n" + sql + "\n" + e);
		}
	}
	private String connString(String host, String sessionId) {
		return format(CONNECTION_STRING_TEMPLATE, config().getString(host), config().getString(sessionId));
	}

}