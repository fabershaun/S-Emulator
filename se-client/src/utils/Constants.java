package utils;

import com.google.gson.Gson;

public class Constants {

    public final static String ANONYMOUS = "<Anonymous>";
    public static final String XML_FILE = "xmlFile";
    public final static int REFRESH_RATE = 500;

    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainAppV3/mainApp.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public final static String DASHBOARD_PAGE_FXML_RESOURCE_LOCATION = "/components/dashboard/mainDashboard/dashboard.fxml";
    public final static String EXECUTION_PAGE_FXML_RESOURCE_LOCATION = "/components/execution/mainExecution/mainExecution.fxml";
    public final static String HISTORY_ROW_POP_UP_LOCATION = "/components/dashboard/usersHistory/historyRowPopUp/historyRowPopUp.fxml";

    // Query parameters:
    public static final String USERNAME_QUERY_PARAM = "username";
    public static final String PROGRAM_NAME_QUERY_PARAM = "username";
    public static final String TARGET_DEGREE_QUERY_PARAM = "targetDegree";

    // Server resources locations
    private final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/S-Emulator";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    // Home Page:
    public final static String DASHBOARD_PAGE = FULL_SERVER_PATH + "/dashboard";

    // Login:
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";

    // Upload File:
    public final static String FILE_UPLOAD_PAGE = FULL_SERVER_PATH + "/upload-file";

    // Get Users List:
    public final static String USERS_LIST_PAGE = FULL_SERVER_PATH + "/users-list";

    // Get Users History List:
    public final static String USER_HISTORY_LIST_PAGE = FULL_SERVER_PATH + "/users-list";

    // Get Available Programs List:
    public final static String AVAILABLE_PROGRAMS_LIST_PAGE = FULL_SERVER_PATH + "/available-programs-list";

    // Get Available Functions List:
    public final static String AVAILABLE_FUNCTIONS_LIST_PAGE = FULL_SERVER_PATH + "/available-functions-list";

    // Get current programDTO:
    public final static String CURRENT_PROGRAM_DATA_PATH = FULL_SERVER_PATH + "/current-program-data";

    // Get Max Degree Of Selected Program:
    public final static String MAX_DEGREE_PATH = FULL_SERVER_PATH + "/max-degree";

    // Jump To Degree:
    public final static String JUMP_TO_DEGREE_PATH = FULL_SERVER_PATH + "/jump-to-degree";

    // Get Architecture Types Available:
    public final static String ARCHITECTURE_TYPES_PATH = FULL_SERVER_PATH + "/architecture-types";
}

// http://localhost:8080/S-Emulator
// http://localhost:8080/S-Emulator/upload-file