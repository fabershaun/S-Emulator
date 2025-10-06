package utils;

public class Constants {

    public final static String ANONYMOUS = "<Anonymous>";

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/mainApp/mainApp.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public final static String DASHBOARD_PAGE_FXML_RESOURCE_LOCATION = "/components/dashboard/dashboard.fxml";

    // Server resources locations
    private final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/S-Emulator";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String DASHBOARD_PAGE = FULL_SERVER_PATH + "/dashboard";
    public final static String FILE_UPLOAD_PATH = FULL_SERVER_PATH + "/upload-file";
    public static final String XML_FILE = "xmlFile";

}

// http://localhost:8080/S-Emulator
// http://localhost:8080/S-Emulator/upload-file