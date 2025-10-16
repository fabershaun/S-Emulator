package utils;

import com.google.gson.Gson;
import okhttp3.MediaType;

public class Constants {

    public final static String ANONYMOUS = "<Anonymous>";
    public static final String XML_FILE = "xmlFile";
    public final static int REFRESH_RATE = 500;

    public static final String STATE = "state";
    public static final String ERROR = "error";
    public static final String DETAILS = "details";
    // CSS:
    public final static String DEBUGGER_CSS = "/components/execution/debuggerExecutionMenu/debuggerMenu.css";

    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();
    public static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainAppV3/mainApp.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public final static String DASHBOARD_PAGE_FXML_RESOURCE_LOCATION = "/components/dashboard/mainDashboard/dashboard.fxml";
    public final static String EXECUTION_PAGE_FXML_RESOURCE_LOCATION = "/components/execution/mainExecution/mainExecution.fxml";
    public final static String HISTORY_ROW_POP_UP_LOCATION = "/components/dashboard/usersHistory/historyRowPopUp/historyRowPopUp.fxml";

    // Query parameters:
    public static final String USERNAME_QUERY_PARAM = "username";
    public static final String CREDITS_TO_CHARGE_QUERY_PARAM = "creditsToCharge";
    public static final String PROGRAM_NAME_QUERY_PARAM = "programName";
    public static final String TARGET_DEGREE_QUERY_PARAM = "targetDegree";
    public static final String DEGREE_QUERY_PARAM = "degree";
    public static final String CHOSEN_ARCHITECTURE_STR_QUERY_PARAM = "architecture";
    public static final String INPUTS_VALUES_QUERY_PARAM = "inputsValues";
    public static final String RUN_ID_QUERY_PARAM = "runId";

    // Server resources locations
    private final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/S-Emulator";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    // Home Page:
    public final static String DASHBOARD_PAGE = FULL_SERVER_PATH + "/dashboard";

    // Login:
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";

    // Credits To Charge:
    public final static String CHARGE_CREDITS_PATH = FULL_SERVER_PATH + "/charge-credits";

    // Get Updated User's Credits:
    public final static String FETCH_CREDITS_PATH = FULL_SERVER_PATH + "/fetch-credits";

    // Get if user has enough credits to play a program:
    public static final String CREDIT_CHECK_PATH = FULL_SERVER_PATH + "/credit-check";

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

    // Server resource
    public static final String PROGRAM_STATUS_PATH = FULL_SERVER_PATH + "/program-status";

    // Run Program:
    public final static String RUN_PROGRAM_PATH = FULL_SERVER_PATH + "/run-program";

    // Get Program After Run:
    public final static String PROGRAM_AFTER_RUN_PATH = FULL_SERVER_PATH + "/program-after-run";

    // Initialize debugger:
    public final static String INITIALIZE_DEBUGGER_PATH = FULL_SERVER_PATH + "/initialize-debugger";

    // Step Over:
    public final static String STEP_OVER_DEBUGGER_PATH = FULL_SERVER_PATH + "/step-over";

    // Step Over:
    public final static String RESUME_DEBUGGER_PATH = FULL_SERVER_PATH + "/resume";

    // Step Back:
    public final static String STEP_BACK_DEBUGGER_PATH = FULL_SERVER_PATH + "/step-back";

    // Stop debug step:
    public final static String STOP_DEBUGGER_PATH = FULL_SERVER_PATH + "/stop-debug";
}

// http://localhost:8080/S-Emulator

