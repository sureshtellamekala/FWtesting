package net.boigroup.bdd.framework.Jira;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JiraZephyrTestCaseManagement {

    private static final Logger LOG = LoggerFactory.getLogger(JiraZephyrTestCaseManagement.class);

    private static LinkedHashMap<String, LinkedHashMap> jiraCycleMap = new LinkedHashMap<>();
    public static LinkedHashMap<String, String> jiraProjectIDMap = new LinkedHashMap<>();
    public static LinkedHashMap<String, String> jiraversionIDMap = new LinkedHashMap<>();
    public static LinkedHashMap cyclesList = new LinkedHashMap<>();
    public static LinkedHashMap<String, String> cyclePresenceMap = new LinkedHashMap<>();
    public static LinkedHashMap<String, String> folderPresenceMap = new LinkedHashMap<>();
    public static List<HashMap<String, Object>> executionResults = new ArrayList<>();

    private static JiraEntity jiraEntity = new JiraEntity();
    public static void main(String args[]){
        jiraEntity.setJiraUrl("https://jira.boigroup.net/");
        jiraEntity.setJiraUsername("");
        jiraEntity.setJiraPassword("");
        jiraEntity.setHeaderAo7deabf("SVCKPQubXYVrYssfeih5UrUMbLCoeBwuMxWG12O/LSceMp4R82bp8nfloiAS6mSW9xv9mhOEwgTTjunCO17uYw==");
        jiraEntity.setJiraUpdate("false");

        cyclesList.put("TESTCASE_NAME","IT2_SIT_OUT_Camt.029_MT196_TS01_TC01");
        cyclesList.put("PROJECT_NAME","TASFE");
        cyclesList.put("CYCLE_NAME","Test Cycle 1");
        cyclesList.put("FOLDER_NAME","Test Folder 4");
        cyclesList.put("VERSION","POC for updating Jira");
        cyclesList.put("TESTCASE_KEY","TASFE-18");
        jiraCycleMap.put("IT2_SIT_OUT_Camt.029_MT196_TS01_TC01", cyclesList);


        String projectId = "33401", versionId="32007", cycleName="Test Cycle 1", cycleId="", folderName="Test Folder 4", folderId="", issueKey="TASFE-18", testcaseName="IT2_SIT_OUT_Camt.029_MT196_TS01_TC01";

        cycleId = createTestCycles(cycleName, projectId, versionId);
        folderId = createNewFolderIntoTestCycles(folderName, projectId, versionId, cycleId);
        String execId = addTestsIntoTestCycles(issueKey, projectId, versionId, cycleId, folderId);
    }

    /* To be called from Before Scenario Hook*/
    public static void readJiraCycleMap(){
        LOG.info("Accessing method readJiraCycleMap()");
        if(jiraCycleMap.size()==0 && jiraEntity.getJiraUpdate().equalsIgnoreCase("true")){
            if(jiraEntity.getJiraInputFormat().equals("JSON")){
                String fileLocation = new File(jiraEntity.getJiraInputLocation()).getAbsoluteFile().toString();
                String jiraMasterJson = JiraUtil.readTheFileFromAbsPath(fileLocation + "/Jira_Master_Details.json");
                jiraCycleMap = JiraUtil.jiraJsonObjectToMap(jiraMasterJson);
            } else if(jiraEntity.getJiraInputFormat().equals("EXCEL")){
                jiraCycleMap = JiraUtil.moveJiraListToMap(JiraUtil.readAllLinesInDataSheet("Jira_Master_Details"));
            }

            if(!jiraEntity.getJiraProject().equals("") || !jiraEntity.getJiraCycleName().equals("") || !jiraEntity.getJiraFolderName().equals("") || !jiraEntity.getJiraVersion().equals("")){
                jiraCycleMap = JiraUtil.updateJiraCycleMap(jiraCycleMap);
            }

            String reportPath = "";
            if(JiraEntity.getJiraReportType().equals("ALLURE")){
                reportPath = System.getProperty("JIRA.ALLURE_DIR");
            } else if(JiraEntity.getJiraReportType().equals("CUCUMBER")){
                reportPath = System.getProperty("JIRA.CUCUMBER_DIR");
            }
            reportPath = (JiraEntity.getJiraReportsDir() + "/" + JiraEntity.getJiraBuildNumber() + reportPath);
            if(reportPath.startsWith("//")){
                reportPath = "\\\\" + reportPath.substring(2);
            }
            JiraEntity.setJiraInputLocation(reportPath.replace("//", "/"));

            LOG.info("jiraCycleMap list: " + jiraCycleMap.toString());
            createJiraCyclesFoldersAndAddTests();
        }
    }

    public static void createJiraCyclesFoldersAndAddTests(){
        LinkedHashMap<String, LinkedHashMap> newCycleFoldersMap = new LinkedHashMap<>();
        Map<String,String> updatedResult = new HashMap<>();
        HashMap<String,ArrayList<HashMap>> cycleFoldersMap = new HashMap<>();

        LOG.info("JIRA START TIME - createJiraCyclesFoldersAndAddTests: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        for (Map.Entry<String, LinkedHashMap> entry : jiraCycleMap.entrySet()) {
            LinkedHashMap currentTestCase = entry.getValue();
            if("Y".equals(currentTestCase.get("CREATE_CYCLE_FOLDER"))) {
                newCycleFoldersMap.put(entry.getKey(), currentTestCase);
            }
        }

        for (Map.Entry<String, LinkedHashMap> entry : newCycleFoldersMap.entrySet()) {
            //new Thread("" + entry.getKey()){
                //public void run(){
                    String key = entry.getKey();
                    LinkedHashMap currentTestCase = entry.getValue();
                    String projectKey = currentTestCase.get("PROJECT_NAME").toString();
                    String versionKey = currentTestCase.get("PROJECT_NAME").toString() + ":" + currentTestCase.get("VERSION").toString();
                    if(!jiraversionIDMap.containsKey(versionKey)){
                        getJiraProjectAndVersionID(currentTestCase.get("PROJECT_NAME").toString(), currentTestCase.get("VERSION").toString());
                    }
                    String projectId = jiraProjectIDMap.get(projectKey), versionId = jiraversionIDMap.get(versionKey), cycleId="", folderId="", executionId="";

                    String createCycle = "";
                    if("Y".equals(currentTestCase.get("CREATE_CYCLE_FOLDER")) && !currentTestCase.get("CYCLE_NAME").equals("")) {
                        cycleId = createTestCycles(currentTestCase.get("CYCLE_NAME").toString(), projectId, versionId);
                        if(!"".equals(currentTestCase.get("FOLDER_NAME").toString()) && !cycleId.equals("")){
                            folderId = createNewFolderIntoTestCycles(currentTestCase.get("FOLDER_NAME").toString(), projectId, versionId, cycleId);
                        }
                        createCycle = "done";
                    }
                    currentTestCase.put("PROJECT_ID", projectId);
                    currentTestCase.put("VERSION_ID", versionId);
                    currentTestCase.put("CYCLE_ID", cycleId);
                    currentTestCase.put("FOLDER_ID", folderId);
                    currentTestCase.put("EXECUTION_ID", executionId);
                    currentTestCase.put("CREATE_CYCLE_FOLDER", createCycle);
                    jiraCycleMap.put(key, currentTestCase);
                    updatedResult.put(key, "Updated");

                    if(folderId.equals("")){
                        if(cycleFoldersMap.get(cycleId)==null){
                            ArrayList list = new ArrayList();
                            list.add(currentTestCase);
                            cycleFoldersMap.put(cycleId, list);
                        }else{
                            ArrayList list = cycleFoldersMap.get(cycleId);
                            list.add(currentTestCase);
                            cycleFoldersMap.put(cycleId, list);
                        }

                    }else{
                        if(cycleFoldersMap.get(folderId)==null){
                            ArrayList<HashMap> list = new ArrayList();
                            list.add(currentTestCase);
                            cycleFoldersMap.put(folderId, list);
                        }else{
                            ArrayList<HashMap> list = cycleFoldersMap.get(folderId);
                            list.add(currentTestCase);
                            cycleFoldersMap.put(folderId, list);
                        }
                    }
                //}
            //}.start();
        }

        LOG.info("JIRA CURR TIME - createJiraCyclesFoldersAndAddTests: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));

        if(cycleFoldersMap.size()!=0){
            ExecutorService executor = Executors.newFixedThreadPool(jiraEntity.getJiraThreadsCount());
            Collection<Callable<Void>> tasks = new ArrayList<>();
            for (Map.Entry<String, ArrayList<HashMap>> entry : cycleFoldersMap.entrySet()) {
                Callable<Void> task = () -> {
                    String issueKeys = "", projectId="", versionId="", cycleId="", folderId="";
                    ArrayList<HashMap> list = cycleFoldersMap.get(entry.getKey());
                    Iterator iter = list.iterator();
                    while (iter.hasNext()) {
                        HashMap currentTestCase = (HashMap) iter.next();
                        if(issueKeys.equals("")){
                            issueKeys = issueKeys + currentTestCase.get("TESTCASE_KEY").toString();
                        }else {
                            issueKeys = issueKeys + "::" + currentTestCase.get("TESTCASE_KEY").toString();
                        }
                        projectId=currentTestCase.get("PROJECT_ID").toString();
                        versionId=currentTestCase.get("VERSION_ID").toString();
                        cycleId=currentTestCase.get("CYCLE_ID").toString();
                        folderId=currentTestCase.get("FOLDER_ID").toString();
                    }
                    addTestsIntoTestCycles(issueKeys, projectId, versionId, cycleId, folderId);

                    return null;
                };

                tasks.add(task);
            }
            try {
                executor.invokeAll(tasks);
            } catch(InterruptedException ex) {
                LOG.info(ex.getLocalizedMessage());
            }
        }

        LOG.info(cycleFoldersMap.toString());

        LOG.info("JIRA END TIME - createJiraCyclesFoldersAndAddTests: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
    }


    /* To be called from After Scenario Hook*/
    public static void prepareExecutionResults(String currentScenario, String scenarioTitle2Description, boolean isStepFailed, String failedReason){
        LOG.info("Method: prepareExecutionResults - Accessing for test case: " + currentScenario);
        if(jiraEntity.getJiraUpdate().equals("true")){
            LinkedHashMap<String, Object> scenarioResult = new LinkedHashMap<>();
            scenarioResult.put("Scenario", currentScenario);
            scenarioResult.put("Description", scenarioTitle2Description);
            scenarioResult.put("Comments", "Report Location: " + JiraEntity.getJiraInputLocation());
            scenarioResult.put("Status", isStepFailed != false ? 2 : 1);
            scenarioResult.put("failedCause", isStepFailed != false ? failedReason : "");
            scenarioResult.put("executionId", getJiraCycleExecutionID(jiraCycleMap, currentScenario));

            //scenarioResult = updateJiraExecutionStatus(scenarioResult);

            executionResults.add(scenarioResult);
        }
    }

    /* To be called from After Stories*/
    public static void updateExecutionResults(){
        if(jiraEntity.getJiraUpdate().equals("true")){
            LOG.info("JIRA START TIME - updateExecutionResults: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
            ExecutorService executor = Executors.newFixedThreadPool(jiraEntity.getJiraThreadsCount());
            Collection<Callable<Void>> tasks = new ArrayList<>();   // our task do not need a returned value
            List<HashMap<String, Object>> results = new ArrayList<>();

            for (int counter = 0; counter < executionResults.size(); counter++) {
                LinkedHashMap<String, Object> execResult = (LinkedHashMap)executionResults.get(counter);
                Callable<Void> task = () -> {
                    LinkedHashMap<String, Object> scenarioResult = new LinkedHashMap<>();
                    scenarioResult = updateJiraExecutionStatus(execResult);
                    results.add(scenarioResult);
                    return null;
                };
                tasks.add(task);
            }

            try {
                executor.invokeAll(tasks);
            } catch(InterruptedException ex) {
                System.out.println(ex);
            }
            LOG.info(results.toString());
            LOG.info("JIRA END TIME - updateExecutionResults: " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }
    }

    public static String getJiraCustomHeader(String headerName) {
        LOG.info("Method: getJiraCustomHeader - Accessing method for: " + headerName);
        String customHeader = "";
        try {
            if(jiraEntity.getJiraUpdate().equals("true")){
                RestAssured.baseURI = jiraEntity.getJiraUrl();
                RequestSpecification request = RestAssured.given();

                BASE64Encoder base = new BASE64Encoder();
                String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
                request.header("Authorization", "Basic " + encoding);

                Response response = request.get();
                int statusCode = response.getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    String[] jiraHtmlArray = response.asString().split(headerName);
                    jiraHtmlArray = jiraHtmlArray[1].split("zEncKeyVal = \"");
                    customHeader = jiraHtmlArray[1].substring(0, jiraHtmlArray[1].indexOf("\";"));
                    LOG.info("Fetched the AO-7DEABF custom header value: " + customHeader);
                } else {
                    LOG.info("Error Response in method getJiraCustomHeader: " + statusCode);
                }
            }
        } catch (Exception e) {
            LOG.info("Method: getJiraCustomHeader - Error in method getJiraCustomHeader: " + e.getStackTrace());
        }
        LOG.info("Method: getJiraCustomHeader - Jira custom header value: " + customHeader);
        return customHeader;
    }

    public static String getJiraCycleExecutionID(LinkedHashMap<String, LinkedHashMap> jiraCycleMap, String testcaseName) {
        LOG.info("Method: getJiraCycleExecutionID - Accessing method for test case: " + testcaseName);
        String executionId = "";
        try {
            RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zephyr/latest/zql/executeSearch";
            RequestSpecification request = RestAssured.given();

            BASE64Encoder base = new BASE64Encoder();
            String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
            request.header("Authorization", "Basic " + encoding);
            request.header("Content-Type", "application/json");
            request.header("X-Requested-With", "XMLHttpRequest");
            request.header("ao-7deabf", jiraEntity.getHeaderAo7deabf());

            String query = generateZQLQueryParam(jiraCycleMap, testcaseName);
            request.queryParam("zqlQuery", query);

            Response response = request.get();
            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                ArrayList<LinkedHashMap> executionsList = response.jsonPath().get("executions");
                if(executionsList.size()!=0)
                    executionId = executionsList.get(0).get("id").toString();
            } else {
                LOG.info("Method: getJiraCycleExecutionID - Error Response in method getJiraCycleExecutionID: " + statusCode + " error: " + response.jsonPath().get().toString());
            }

        } catch (Exception e) {
            LOG.info("Method: getJiraCycleExecutionID - Error in method getJiraCycleExecutionID: " + e.getStackTrace());
        }
        LOG.info("Method: getJiraCycleExecutionID - Jira Cycle Execution id for test case: " + testcaseName +" = " + executionId);
        return executionId;
    }

    private static String generateZQLQueryParam(LinkedHashMap<String, LinkedHashMap> jiraCycleMap, String testcaseName){
        String query = "";

        LinkedHashMap currentTestCase = jiraCycleMap.get(testcaseName);

        query = "project = \"" + currentTestCase.get("PROJECT_NAME") + "\" AND cycleName in (\"" + currentTestCase.get("CYCLE_NAME") + "\") AND folderName = \"" + currentTestCase.get("FOLDER_NAME") + "\" AND fixVersion = \"" + currentTestCase.get("VERSION") + "\" AND issue = \"" + currentTestCase.get("TESTCASE_KEY") + "\" AND summary ~ \"" + testcaseName + "\"";
        query = query.replaceAll("= \"\"", "is null");

        return query;
    }

    public static void getJiraProjectAndVersionID(String projectKey, String versionKey) {
        LOG.info("Method: getJiraProjectAndVersionID - Accessing method for Project Key: " + projectKey + " Version Key: " + versionKey);
        String projectId = "", versionId = "";
        try {
            RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zephyr/latest/zql/executeSearch";
            RequestSpecification request = RestAssured.given();

            BASE64Encoder base = new BASE64Encoder();
            String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
            request.header("Authorization", "Basic " + encoding);
            request.header("Content-Type", "application/json");
            request.header("X-Requested-With", "XMLHttpRequest");
            request.header("ao-7deabf", jiraEntity.getHeaderAo7deabf());

            String query = "project = \"" + projectKey + "\" AND fixVersion = \"" + versionKey + "\"";
            request.queryParam("zqlQuery", query);

            Response response = request.get();
            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                ArrayList<LinkedHashMap> executionsList = response.jsonPath().get("executions");
                if(executionsList.size()!=0) {
                    projectId = executionsList.get(0).get("projectId").toString();
                    versionId = executionsList.get(0).get("versionId").toString();
                    jiraProjectIDMap.put(projectKey, projectId);
                    jiraversionIDMap.put(projectKey + ":" + versionKey, versionId);
                    //LOG.info("Fetched Jira ProjectID And VersionID details Successfully");
                } else {
                    jiraProjectIDMap.put(projectKey, "");
                    jiraversionIDMap.put(projectKey + ":" + versionKey, "");
                    LOG.info("Method: getJiraProjectAndVersionID - Project Id and Version Id not found for Project: " + projectKey + " & Version: " + versionKey);
                }
            } else {
                LOG.info("Method: getJiraProjectAndVersionID - Error Response in method: " + statusCode + " error: " + response.jsonPath().get().toString());
            }
        } catch (Exception e) {
            LOG.info("Method: getJiraProjectAndVersionID - Error in method: " + e.getStackTrace());
        }
        LOG.info("Method: getJiraProjectAndVersionID - Jira Project Id: " + projectId +" and " + versionId);
    }

    public static LinkedHashMap<String, Object> updateJiraExecutionStatus(LinkedHashMap<String, Object> scenarioResult) {
        LOG.info("Method: updateJiraExecutionStatus - Accessing method");
        String updateStatus = "";
        try {
            RestAssured.baseURI = jiraEntity.getJiraUrl() + "/rest/zapi/latest/execution/";
            RequestSpecification request = RestAssured.given();

            BASE64Encoder base = new BASE64Encoder();
            String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
            request.header("Authorization", "Basic " + encoding);
            request.header("Content-Type", "application/json");
            request.header("X-Requested-With", "XMLHttpRequest");

            JSONObject requestParams = new JSONObject();
            requestParams.put("status", scenarioResult.get("Status"));

            String comment = scenarioResult.get("Comments").toString();
            if (Integer.parseInt(scenarioResult.get("Status").toString()) == 2) {
                comment = comment + "\n" + "Failed Reason: " + scenarioResult.get("failedCause");
            }
            requestParams.put("comment", comment);
            request.body(requestParams.toString());
            Response response = request.put(scenarioResult.get("executionId") + "/execute");

            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                updateStatus = "Success: " + statusCode;
            } else {
                updateStatus = "Error Response: " + statusCode;
            }
        } catch (Exception e) {
            updateStatus = "Error Response: " + e.getMessage();
        }
        scenarioResult.put("JiraUpdateStatus", updateStatus);
        LOG.info("Method: updateJiraExecutionStatus - Jira update status for execution id: " + scenarioResult.get("executionId") + " = " + updateStatus);

        return scenarioResult;
    }

    public static String checkJiraCycleExists(String projectId, String versionId, String checkCycleName) {
        LOG.info("Method: checkJiraCycleExists - Accessing method for Cycle Name: " + checkCycleName);
        String cycleId = "";
        try {
            RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zapi/latest/cycle?";
            RequestSpecification request = RestAssured.given();

            BASE64Encoder base = new BASE64Encoder();
            String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
            request.header("Authorization", "Basic " + encoding);
            request.header("Content-Type", "application/json");
            request.header("X-Requested-With", "XMLHttpRequest");
            request.header("ao-7deabf", jiraEntity.getHeaderAo7deabf());

            request.param("projectId", projectId);
            request.param("versionId", versionId);

            Response response = request.get();
            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                LinkedHashMap<String, Object> responseMap = response.jsonPath().get();
                for (Map.Entry entry : responseMap.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    if(!key.equals ("recordsCount") && ((LinkedHashMap) entry.getValue()).containsKey("name")){
                        if(checkCycleName.equals(((LinkedHashMap) entry.getValue()).get("name").toString())){
                            cycleId = key;
                            LOG.info("Method: checkJiraCycleExists - Cycle Value of "+((LinkedHashMap) entry.getValue()).get("name").toString()+" is: "+ key);
                            break;
                        }
                    }
                }
            } else {
                LOG.info("Method: checkJiraCycleExists - Error Response in method getAllJiraCycles: " + statusCode);
            }
        } catch (Exception e) {
            LOG.info("Method: checkJiraCycleExists - Error in method getAllJiraCycles: " + e.getStackTrace());
        }

        return cycleId;
    }

    public static String createTestCycles(String cycleName, String projectId, String versionId) {
        LOG.info("Method: createTestCycles - Accessing method for Cycle Name: " + cycleName);
        String cycleId = "";
        try {
            if(cyclePresenceMap.containsKey(cycleName)){
                cycleId = cyclePresenceMap.get(cycleName);
            }else{
                cycleId = checkJiraCycleExists(projectId, versionId, cycleName);
                cyclePresenceMap.put(cycleName, cycleId);
            }

            if(cycleId.equals("")){
                RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zapi/latest/cycle";
                RequestSpecification request = RestAssured.given();

                BASE64Encoder base = new BASE64Encoder();
                String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
                request.header("Authorization", "Basic " + encoding);
                request.header("Content-Type", "application/json");

                JSONObject requestParams = new JSONObject();
                requestParams.put("name", cycleName);
                requestParams.put("description", "New Cycle created using automation script");
                requestParams.put("projectId", projectId);
                requestParams.put("versionId", versionId);
                requestParams.put("sprintId", 1);
                String dateString = new SimpleDateFormat("d/MMM/yy").format(new Date());
                requestParams.put("startDate", dateString);
                requestParams.put("endDate", dateString);
                requestParams.put("build", "");
                requestParams.put("environment", "");
                requestParams.put("clonedCycleId", "");

                request.body(requestParams.toString());
                Response response = request.post();

                int statusCode = response.getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    LinkedHashMap executionsList = response.jsonPath().get();
                    cycleId = executionsList.get("id").toString();
                    cyclePresenceMap.put(cycleName, cycleId);
                    LOG.info("Method: createTestCycles - Success: " + statusCode + " cycleId: " + cycleId);
                } else {
                    LOG.info("Method: createTestCycles - Error Response: " + statusCode);
                }
            } else {
                LOG.info("Method: createTestCycles - Cycle " + cycleName + " Exists cycleId: " + cycleId);
            }
        } catch (Exception e) {
            LOG.info("Method: createTestCycles - Error Response: " + e.getMessage());
        }

        return cycleId;
    }

    public static String checkJiraCycleFolderExists(String projectId, String versionId, String cycleId, String checkFolderName) {
        LOG.info("Method: checkJiraCycleFolderExists - Accessing method for folder name: " + checkFolderName);
        String folderId = "";
        try {
            RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zapi/latest/cycle/" + cycleId + "/folders?";
            RequestSpecification request = RestAssured.given();

            BASE64Encoder base = new BASE64Encoder();
            String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
            request.header("Authorization", "Basic " + encoding);
            request.header("Content-Type", "application/json");
            request.header("X-Requested-With", "XMLHttpRequest");
            request.header("ao-7deabf", jiraEntity.getHeaderAo7deabf());

            request.param("projectId", projectId);
            request.param("versionId", versionId);
            request.param("cycleId", cycleId);

            Response response = request.get();
            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                ArrayList<LinkedHashMap<String, Object>> responsArrayList = response.jsonPath().get();
                for(int i=0; i<responsArrayList.size();i++){
                    LinkedHashMap<String, Object> map = responsArrayList.get(i);
                    if(map.containsKey("folderName") && map.get("folderName").equals(checkFolderName)){
                        folderId = map.get("folderId").toString();
                        LOG.info("Method: checkJiraCycleFolderExists - Folder Id for the folderName of "+map.get("folderName")+" is: "+ map.get("folderId").toString());
                        break;
                    }
                }
            } else {
                LOG.info("Method: checkJiraCycleFolderExists - Error Response in method: " + statusCode);
            }

        } catch (Exception e) {
            LOG.info("Method: checkJiraCycleFolderExists - Error in method: " + e.getStackTrace());
        }

        return folderId;
    }

    public static String createNewFolderIntoTestCycles(String folderName, String projectId, String versionId, String cycleId) {
        LOG.info("Method: createNewFolderIntoTestCycles - Accessing method for folder name: " + folderName);
        String folderId = "";
        try {
            if(folderPresenceMap.containsKey(cycleId+":"+folderName)){
                folderId = folderPresenceMap.get(cycleId+":"+folderName);
            }else {
                folderId = checkJiraCycleFolderExists(projectId, versionId, cycleId, folderName);
                folderPresenceMap.put(cycleId+":"+folderName, folderId);
            }

            if(folderId.equals("")){
                RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zapi/latest";
                RequestSpecification request = RestAssured.given();

                BASE64Encoder base = new BASE64Encoder();
                String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
                request.header("Authorization", "Basic " + encoding);
                request.header("Content-Type", "application/json");

                JSONObject requestParams = new JSONObject();
                requestParams.put("name", folderName);
                requestParams.put("description", "New Folder created using automation script");
                requestParams.put("projectId", projectId);
                requestParams.put("versionId", versionId);
                requestParams.put("cycleId", cycleId);

                request.body(requestParams.toString());
                Response response = request.post("/folder/create");

                int statusCode = response.getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    LinkedHashMap executionsList = response.jsonPath().get();
                    folderId = executionsList.get("id").toString();
                    folderPresenceMap.put(cycleId+":"+folderName, folderId);
                    LOG.info("Method: createNewFolderIntoTestCycles - Success: " + statusCode + " folderId: " + folderId);
                } else {
                    LOG.info("Method: createNewFolderIntoTestCycles - Error Response: " + statusCode);
                }
            }else{
                LOG.info("Method: createNewFolderIntoTestCycles - Folder "+ folderName +" Exists folderId: " + folderId);
            }
        } catch (Exception e) {
            LOG.info("Method: createNewFolderIntoTestCycles - Error Response: " + e.getMessage());
        }

        return folderId;
    }

    public static String addTestsIntoTestCycles(String issueKey, String projectId, String versionId, String cycleId, String folderId) {
        LOG.info("Method: addTestsIntoTestCycles - Accessing method for testcase name: " + issueKey);
        String execId = "";
        try {
                RestAssured.baseURI = jiraEntity.getJiraUrl() + "rest/zapi/latest";
                RequestSpecification request = RestAssured.given();

                BASE64Encoder base = new BASE64Encoder();
                String encoding = base.encode((jiraEntity.getJiraUsername() + ":" + jiraEntity.getJiraPassword()).getBytes());
                request.header("Authorization", "Basic " + encoding);
                request.header("Content-Type", "application/json");

                JSONArray issueList = new JSONArray();
                String[] keyList = issueKey.split("::");
                for(String key : keyList){
                    issueList.put(key);
                }

                JSONObject requestParams = new JSONObject();
                requestParams.put("issues", issueList);
                requestParams.put("assigneeType", "currentUser");
                requestParams.put("method", "1");
                requestParams.put("projectId", projectId);
                requestParams.put("versionId", versionId);
                requestParams.put("cycleId", cycleId);
                if(!folderId.equals("")){
                    requestParams.put("folderId", folderId);
                }

                request.body(requestParams.toString());
                Response response = request.post("/execution/addTestsToCycle");

                int statusCode = response.getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    LinkedHashMap executionsList = response.jsonPath().get();
                    LOG.info("Method: addTestsIntoTestCycles - Success: " + statusCode + " & Progress token: " + executionsList);
                } else {
                    LOG.info("Method: addTestsIntoTestCycles - Error Response: " + statusCode);
                }
        } catch (Exception e) {
            LOG.info("Method: addTestsIntoTestCycles - Error Response: " + e.getMessage());
        }

        return execId;
    }

}