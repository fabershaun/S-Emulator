package constants;

import com.google.gson.Gson;

public class Constants {

    public static final Gson GSON = new Gson();

    public static final String USERNAME = "username";

    // Login:
    public static final String LOGIN_SERVLET_NAME = "LoginServlet";
    public static final String LOGIN_SERVLET_URL = "/login";

    // Upload File:
    public static final String FILE_UPLOAD_SERVLET_NAME = "UploadFileServlet";
    public static final String FILE_UPLOAD_SERVLET_URL = "/upload-file";
    public static final String XML_FILE = "xmlFile";


}
