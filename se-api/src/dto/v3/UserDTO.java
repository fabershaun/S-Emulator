package dto.v3;

public class UserDTO {

    public static String DEFAULT_NAME = "MANAGER";

    private final String userName;
    private int mainProgramsCount;
    private int subFunctionsCount;
    private long currentCredits;
    private int usedCredits;
    private int executionsCount;


    public UserDTO(String userName) {
        this.userName = userName;
        this.mainProgramsCount = 0;
        this.subFunctionsCount = 0;
        this.currentCredits = 0;
        this.usedCredits = 0;
        this.executionsCount = 0;
    }

    public String getUserName() {
        return userName;
    }

    public int getMainProgramsCount() {
        return mainProgramsCount;
    }

    public int getSubFunctionsCount() {
        return subFunctionsCount;
    }

    public long getCurrentCredits() {
        return currentCredits;
    }

    public int getUsedCredits() {
        return usedCredits;
    }

    public int getExecutionsCount() {
        return executionsCount;
    }


    public void setMainProgramsCount(int mainProgramsCount) {
        this.mainProgramsCount = mainProgramsCount;
    }

    public void setSubFunctionsCount(int subFunctionsCount) {
        this.subFunctionsCount = subFunctionsCount;
    }

    public void setCurrentCredits(long currentCredits) {
        this.currentCredits = currentCredits;
    }

    public void setUsedCredits(int usedCredits) {
        this.usedCredits = usedCredits;
    }

    public void setExecutionsCount(int executionsCount) {
        this.executionsCount = executionsCount;
    }
}
