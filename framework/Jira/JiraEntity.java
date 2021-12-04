package net.boigroup.bdd.framework.Jira;

public class JiraEntity {

    private static String jiraUpdate = System.getProperty("JIRA.UPDATE");
    private static String JIRA_URL = System.getProperty("JIRA_URL");
    private static String JIRA_USERNAME = System.getProperty("JIRA_USERNAME");
    private static String JIRA_PASSWORD = System.getProperty("JIRA_PASSWORD");
    private static String jiraInputFormat = System.getProperty("JIRA.INPUT_FORMAT");
    private static String jiraInputLocation = System.getProperty("JIRA.INPUT_LOCATION");
    private static String HEADER_AO7DEABF = JiraZephyrTestCaseManagement.getJiraCustomHeader("AO-7DEABF");

    private static String jiraProject = System.getProperty("JIRA.PROJECT_NAME");
    private static String jiraCycleName = System.getProperty("JIRA.CYCLE_NAME");
    private static String jiraFolderName = System.getProperty("JIRA.FOLDER_NAME");
    private static String jiraVersion = System.getProperty("JIRA.VERSION");
    private static String jiraReportType = System.getProperty("JIRA.REPORT_TYPE");
    private static String jiraReportsDir = System.getProperty("JIRA.REPORTS_DIR");
    private static String jiraBuildNumber = System.getProperty("JIRA.BUILD_NUMBER");
    private static int jiraThreadsCount = Integer.parseInt(System.getProperty("JIRA.THREADS_COUNT", "1"));

    public String getJiraUpdate() {
        return jiraUpdate;
    }

    public String getJiraUrl() {
        return JIRA_URL;
    }

    public String getJiraUsername() {
        return JIRA_USERNAME;
    }

    public String getJiraPassword() {
        return JIRA_PASSWORD;
    }

    public static String getJiraInputFormat() {
        return jiraInputFormat;
    }

    public static String getJiraInputLocation() {
        return jiraInputLocation;
    }

    public static String getHeaderAo7deabf() {
        return HEADER_AO7DEABF;
    }

    public static void setJiraUpdate(String jiraUpdate) {
        JiraEntity.jiraUpdate = jiraUpdate;
    }

    public static void setJiraUrl(String jiraUrl) {
        JIRA_URL = jiraUrl;
    }

    public static void setJiraUsername(String jiraUsername) {
        JIRA_USERNAME = jiraUsername;
    }

    public static void setJiraPassword(String jiraPassword) {
        JIRA_PASSWORD = jiraPassword;
    }

    public static void setJiraInputFormat(String jiraInputFormat) {
        JiraEntity.jiraInputFormat = jiraInputFormat;
    }

    public static void setJiraInputLocation(String jiraInputLocation) {
        JiraEntity.jiraInputLocation = jiraInputLocation;
    }

    public static void setHeaderAo7deabf(String headerAo7deabf) {
        HEADER_AO7DEABF = headerAo7deabf;
    }

    public static String getJiraProject() {
        return jiraProject;
    }

    public static void setJiraProject(String jiraProject) {
        JiraEntity.jiraProject = jiraProject;
    }

    public static String getJiraCycleName() {
        return jiraCycleName;
    }

    public static void setJiraCycleName(String jiraCycleName) {
        JiraEntity.jiraCycleName = jiraCycleName;
    }

    public static String getJiraFolderName() {
        return jiraFolderName;
    }

    public static void setJiraFolderName(String jiraFolderName) {
        JiraEntity.jiraFolderName = jiraFolderName;
    }

    public static String getJiraVersion() {
        return jiraVersion;
    }

    public static void setJiraVersion(String jiraVersion) {
        JiraEntity.jiraVersion = jiraVersion;
    }

    public static String getJiraReportsDir() {
        return jiraReportsDir;
    }

    public static void setJiraReportsDir(String jiraReportsDir) {
        JiraEntity.jiraReportsDir = jiraReportsDir;
    }

    public static String getJiraBuildNumber() {
        return jiraBuildNumber;
    }

    public static void setJiraBuildNumber(String jiraBuildNumber) {
        JiraEntity.jiraBuildNumber = jiraBuildNumber;
    }

    public static String getJiraReportType() {
        return jiraReportType;
    }

    public static void setJiraReportType(String jiraReportType) {
        JiraEntity.jiraReportType = jiraReportType;
    }

    public static int getJiraThreadsCount() {
        return jiraThreadsCount;
    }

    public static void setJiraThreadsCount(int jiraThreadsCount) {
        JiraEntity.jiraThreadsCount = jiraThreadsCount;
    }
}
