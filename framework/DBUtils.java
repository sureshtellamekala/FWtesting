package net.boigroup.bdd.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtils {
	private static final String COL_DELIM = "|";
    private static final String CSV_COL_DELIM = "\t,";
	public static String getDBTableReportFormat(List<Map<String, Object>> table){
		DBUtils util = new DBUtils();
		List<Map<String, String>> res = DBUtils.toStringMap(table);
		return util.createTable(res);
	}

	private static List<Map<String, String>> toStringMap(List<Map<String, Object>> m) {
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();


		for (Map<String, Object> row : m) {
            Map<String, String> copy = new HashMap<String, String>();;
			for(Map.Entry m1:row.entrySet()){
			    String key = m1.getKey().toString();
				String value = "";
				if (m1.getValue()!=null){
				    value = m1.getValue().toString().replace(",","");

				}else{
					value = "";
				}

				copy.put(key,value);
			}

			res.add(copy);
		}
		return res;
	}

	public static String getDBTableReportFormatCol(List<Map<String, Object>> table){
		if(table.size()!=1){
			return getDBTableReportFormat(table);
		}else {
			DBUtils util = new DBUtils();
			List<Map<String, String>> res1 = new ArrayList<Map<String, String>>();
			Map<String, String> copy = new HashMap<String, String>();

			for (Map<String, Object> row : table) {
				for (Map.Entry m1 : row.entrySet()) {
					String key = m1.getKey().toString();
					String value = "";
					if (m1.getValue() != null) {
						value = m1.getValue().toString();
					} else {
						value = "";
					}

					copy.put(key, value);
				}
				res1.add(copy);
			}

			StringBuilder res = new StringBuilder();
			int finalLen = 0;
			int finalValLen = 0;
			for (Map.Entry m : copy.entrySet()) {
				int len = m.getKey().toString().length();
				if (len > finalLen) {
					finalLen = len;
				}

				len = m.getValue().toString().length();
				if (len > finalValLen) {
					finalValLen = len;
				}
			}

			for (Map.Entry m : copy.entrySet()) {
				res.append(m.getKey().toString());
				res.append(util.getSpaces(finalLen - m.getKey().toString().length()));
				res.append(COL_DELIM);
				res.append(m.getValue().toString());
				res.append("\n");
			}

			return res.toString();
		}
	}

	public String createTable(List<Map<String, String>> table) {
		if (table.isEmpty() || table.get(0).isEmpty()) {
			return "";
		}
		List<Integer> maxLengths = findLength(table);
		StringBuilder sb = new StringBuilder();
		String tableHeaders = createRow(maxLengths, new ArrayList<Object>(table.get(0).keySet()),COL_DELIM);
		sb.append(tableHeaders);
		for (Map<String, String> row : table) {
			sb.append(createRow(maxLengths, new ArrayList<Object>(row.values()),COL_DELIM));
		}
		return sb.toString();
	}

	private static String createRow(List<Integer> colMaxLengths, List<Object> elements,String delim) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < elements.size(); i++) {
			String val = elements.get(i).toString();
			if (val.length() > colMaxLengths.get(i)) {
				val = val.substring(0, colMaxLengths.get(i) - 2) + "...";
				res.append(val);
			} else {
				res.append(val);
				res.append(getSpaces(colMaxLengths.get(i) - val.length()));
			}
			res.append(delim);
		}
		res.append("\n");
		return res.toString();
	}

	private static String getSpaces(int len) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i <= len; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}

	private static final int ROW_MAX_LENGTH = 100;
	public static List<Integer> findLength(List<Map<String, String>> table) {
		List<Integer> maxLengths = new ArrayList<Integer>();
		for (String key : table.get(0).keySet()) {
			int len = key.length();
			if (len > ROW_MAX_LENGTH) {
				len = ROW_MAX_LENGTH;
			}
			maxLengths.add(len);
		}
		int cnt = 0;
		for (Map<String, String> row : table) {
			cnt = 0;
			for (String value : row.values()) {
				Integer curr = maxLengths.get(cnt);
				if (curr == ROW_MAX_LENGTH) {
					continue;
				}
				int len = value.length();
				if (len > ROW_MAX_LENGTH) {
					maxLengths.set(cnt, ROW_MAX_LENGTH);
				} else if (len > curr) {
					maxLengths.set(cnt, len);
				}
				cnt++;
			}
		}
		return maxLengths;
	}



    public static String getCSVFormat(List<Map<String, Object>> result) {
        DBUtils util = new DBUtils();
        List<Map<String, String>> res = DBUtils.toStringMap(result);

        return createCSVTable(res);
    }

    public static String createCSVTable(List<Map<String, String>> table) {
        if (table.isEmpty() || table.get(0).isEmpty()) {
            return "";
        }
        List<Integer> maxLengths = findLength(table);
        String tableHeaders = createRow(maxLengths,new ArrayList<Object>(table.get(0).keySet()),CSV_COL_DELIM);
        StringBuilder sb = new StringBuilder();
        sb.append(tableHeaders);
        for (Map<String, String> row : table) {
            sb.append(createRow(maxLengths,new ArrayList<Object>(row.values()),CSV_COL_DELIM));
        }
        return sb.toString();
    }
}
