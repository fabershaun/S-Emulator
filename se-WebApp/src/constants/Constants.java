package constants;

import com.google.gson.Gson;

public class Constants {

    public static final Gson GSON_INSTANCE = new Gson();

    public static final String USERNAME_QUERY_PARAM = "username";
    public static final String PROGRAM_NAME_QUERY_PARAM = "username";
    public static final String TARGET_DEGREE_QUERY_PARAM = "targetDegree";

    // Login:
    public static final String LOGIN_SERVLET_NAME = "LoginServlet";
    public static final String LOGIN_SERVLET_URL = "/login";

    // Upload File:
    public static final String FILE_UPLOAD_SERVLET_NAME = "UploadFileServlet";
    public static final String FILE_UPLOAD_SERVLET_URL = "/upload-file";
    public static final String XML_FILE = "xmlFile";

    // Get Users List:
    public static final String USERS_LIST_NAME = "UsersListServlet";
    public static final String USERS_LIST_URL = "/users-list";

    // Get Users History List:
    public static final String USER_HISTORY_LIST_NAME = "UserHistoryListServlet";
    public static final String USER_HISTORY_LIST_URL = "/user-history-list";

    // Get Available Programs List:
    public static final String AVAILABLE_PROGRAMS_LIST_NAME = "AvailableProgramsListServlet";
    public static final String AVAILABLE_PROGRAMS_LIST_URL = "/available-programs-list";

    // Get Available Functions List:
    public static final String AVAILABLE_FUNCTIONS_LIST_NAME = "AvailableFunctionsListServlet";
    public static final String AVAILABLE_FUNCTIONS_LIST_URL = "/available-functions-list";

    // Get ProgramDTO:
    public final static String CURRENT_PROGRAM_DATA_NAME = "ProgramDtoServlet";
    public final static String CURRENT_PROGRAM_DATA_URL = "/current-program-data";

    // Get Max Degree:
    public final static String MAX_DEGREE_NAME = "MaxDegreeServlet";
    public final static String MAX_DEGREE_URL = "/max-degree";

    // Get ProgramDTO in taget degree:
    public final static String JUMP_TO_DEGREE_NAME = "JumpToDegreeServlet";
    public final static String JUMP_TO_DEGREE_URL = "/jump-to-degree";

    // Get Architecture Types Available:
    public final static String ARCHITECTURE_TYPES_NAME = "ArchitectureTypes";
    public final static String ARCHITECTURE_TYPES_URL = "/architecture-types";

}
