package net.boigroup.bdd.framework.Jira;

import org.apache.commons.collections4.list.TreeList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class JiraUtil {

    private static JiraEntity jiraEntity = new JiraEntity();

    public static LinkedHashMap<String, LinkedHashMap> moveJiraListToMap(List<LinkedHashMap<String, String>> list) {
        LinkedHashMap<String, LinkedHashMap> jiraCycleMap = new LinkedHashMap<>();

        for(LinkedHashMap<String, String> map : list){
            map.put("PROJECT_ID", "");
            map.put("VERSION_ID", "");
            map.put("CYCLE_ID", "");
            map.put("FOLDER_ID", "");
            map.put("EXECUTION_ID", "");
            jiraCycleMap.put(map.get("TESTCASE_NAME"), map);
        }
        return jiraCycleMap;
    }

    public static LinkedHashMap<String, LinkedHashMap> updateJiraCycleMap(LinkedHashMap<String, LinkedHashMap> jiraCycleMap) {

        for( Map.Entry<String,LinkedHashMap> entry : jiraCycleMap.entrySet()){
            String key = entry.getKey();
            LinkedHashMap value = entry.getValue();
            value.put("PROJECT_NAME", jiraEntity.getJiraProject());
            value.put("CYCLE_NAME", jiraEntity.getJiraCycleName());
            value.put("VERSION", jiraEntity.getJiraVersion());
            value.put("FOLDER_NAME", jiraEntity.getJiraFolderName());
            jiraCycleMap.put(key, value);
        }
        return jiraCycleMap;
    }

    private static LinkedHashMap addNonEmptyValuesIntoMap(LinkedHashMap map, String key, String value) {
        if(!value.equals("") && null != value){
            map.put(key, value);
        }
        return map;
    }

    public static LinkedHashMap<String, LinkedHashMap> jiraJsonObjectToMap(String jiraMasterJson) {
        LinkedHashMap<String, LinkedHashMap> jiraCycleMap = new LinkedHashMap<>();
        JSONObject jsonObj = new JSONObject(jiraMasterJson);
        JSONArray jsonArray = jsonObj.getJSONArray("Jira");

        for (int i = 0, size = jsonArray.length(); i < size; i++)
        {
            LinkedHashMap tempMap = new LinkedHashMap();
            JSONObject objectInArray = jsonArray.getJSONObject(i);
            String[] elementNames = JSONObject.getNames(objectInArray);
            for (String elementName : elementNames) {
                tempMap.put(elementName, objectInArray.getString(elementName));
            }
            tempMap.put("PROJECT_ID", "");
            tempMap.put("VERSION_ID", "");
            tempMap.put("CYCLE_ID", "");
            tempMap.put("FOLDER_ID", "");
            tempMap.put("EXECUTION_ID", "");
            jiraCycleMap.put(objectInArray.getString("TESTCASE_NAME"), tempMap);
        }

        return jiraCycleMap;
    }

    public static String readTheFileFromAbsPath(String fileName){
        String message = "";
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(fileName.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                message = message + line + "\n";
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error: "+ e);
        }
        return message;
    }

    public static List<LinkedHashMap<String, String>> readAllLinesInDataSheet(String SheetName) {
        List<LinkedHashMap<String, String>> getDataList = new TreeList<>();
        List<LinkedHashMap<String, String>> finalList = new TreeList<>();
        ArrayList<LinkedHashMap<String, String>> data = new ArrayList<>();

        String fileLocations = new File(jiraEntity.getJiraInputLocation()).getAbsoluteFile().toString();
        String value;
        int startRow = 1;
        int test = 0;
        int dataMapCounter = 0;
        try {
            File file = new File(fileLocations + "/" +SheetName + ".xlsx");
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet("data");

            for (int row = 1; row <= sheet.getLastRowNum(); row++) {
                LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
                LinkedHashMap<String, String> dataMap1 = new LinkedHashMap<>();

                String getKeyName = sheet.getRow(row).getCell(0).toString();

                for (int col = 0; col < sheet.getRow(0).getLastCellNum(); col++) {
                    String key = sheet.getRow(0).getCell(col).toString();
                    try {
                        Cell cell = sheet.getRow(row).getCell(col);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        value = sheet.getRow(row).getCell(col).toString().replaceAll("\n", "").trim();
                        value = replaceSpaceAndTabs(value);
                    } catch (Exception e) {
                        value = "";
                    }

                    dataMap.put(key, value);
                }

                getDataList.add(dataMapCounter, dataMap);
                startRow = row + 1;
                data.add(dataMapCounter++, dataMap1);
                test = test + 1;
            }

            finalList.add(getDataList.get(0));
            for (int row = startRow; row <= sheet.getLastRowNum(); row++) {
                LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();

                String getKeyName = sheet.getRow(row).getCell(0).toString();
                if (Objects.equals(getKeyName, "")) {
                    for (int col = 0; col < sheet.getRow(0).getLastCellNum(); col++) {
                        String key = sheet.getRow(0).getCell(col).toString();
                        try {
                            Cell cell = sheet.getRow(row).getCell(col);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            value = sheet.getRow(row).getCell(col).toString().trim();
                            value = replaceSpaceAndTabs(value);
                        } catch (Exception e) {
                            value = "";
                        }

                        dataMap.put(key, value);

                    }
                    getDataList.add(dataMapCounter, dataMap);
                    test = test + 1;
                } else {
                    break;
                }
            }
            getDataList.remove(0);
            Collections.reverse(getDataList);

            finalList.addAll(getDataList);

        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }

        return finalList;
    }

    public static String replaceSpaceAndTabs(String inString) {
        int takeNum;
        for (int j = 0; j < inString.length(); j++) {
            if (inString.contains("SPACE>") || (inString.contains("TAB>"))) {
                int spacePosition = inString.indexOf("SPACE>");
                int tabPosition = inString.indexOf("TAB>");
                if (spacePosition < 0) {
                    spacePosition = tabPosition + tabPosition;
                } else if (tabPosition < 0) {
                    tabPosition = spacePosition + spacePosition;
                }
                if (spacePosition < tabPosition) {
                    String shortString = inString.substring(0, spacePosition);
                    String startString = shortString.substring(shortString.lastIndexOf("<")).replaceFirst("<", "");
                    takeNum = Integer.parseInt(startString);
                    String replaceSpace = "<" + takeNum + "SPACE>";
                    String spaces = String.format("%1$-" + takeNum + "s", "");
                    inString = inString.replaceAll(replaceSpace, spaces);
                } else {
                    String shortString = inString.substring(0, tabPosition);
                    String startString = shortString.substring(shortString.lastIndexOf("<")).replaceFirst("<", "");
                    takeNum = Integer.parseInt(startString);
                    String replaceTab = "<" + takeNum + "TAB>";
                    String tabs = "";
                    for (int num = 1; num <= takeNum; num++) {
                        tabs = tabs + "\t";
                    }
                    inString = inString.replaceAll(replaceTab, tabs);
                }
            } else {
                break;
            }
        }

        inString = inString.replaceAll("<CARRIAGERETURN>", "\r");
        inString = inString.replaceAll("<NEWLINE>", "\n");
        return inString;
    }
}
