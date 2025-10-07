package utils;

import com.google.gson.Gson;

public class Constants {

    public final static String ANONYMOUS = "<Anonymous>";
    public static final String XML_FILE = "xmlFile";
    public final static int REFRESH_RATE = 500;

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainApp/mainApp.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public final static String DASHBOARD_PAGE_FXML_RESOURCE_LOCATION = "/components/dashboard/mainDashboard/dashboard.fxml";

    // Server resources locations
    private final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/S-Emulator";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String DASHBOARD_PAGE = FULL_SERVER_PATH + "/dashboard";
    public final static String FILE_UPLOAD_PATH = FULL_SERVER_PATH + "/upload-file";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";

    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();
}

// http://localhost:8080/S-Emulator
// http://localhost:8080/S-Emulator/upload-file