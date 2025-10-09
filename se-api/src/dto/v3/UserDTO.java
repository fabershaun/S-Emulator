package dto.v3;

public class UserDTO {

    public static String DEFAULT_NAME = "MANAGER";

    private final String userName;
    private int mainProgramsCount;
    private int subFunctionsCount;
    private int currentCredits;
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

    public int getCurrentCredits() {
        return currentCredits;
    }

    public int getUsedCredits() {
        return usedCredits;
    }

    public int getExecutionsCount() {
        return executionsCount;
    }

    public void addOneToMainProgramsCount() {
        this.mainProgramsCount += 1;
    }

    public void addOneToSubFunctionsCount() {
        this.subFunctionsCount += 1;
    }

    public void addToCurrentCredits(int creditsToAdd) {
        this.currentCredits += creditsToAdd;
    }

    public void subtractFromCurrentCredits(int creditsToSubtract) {
        this.currentCredits -= creditsToSubtract;
        this.usedCredits += creditsToSubtract;

        assertHasPositiveCredits();
    }

    public void addOneToExecutionsCount() {
        this.executionsCount += 1;
    }

    private void assertHasPositiveCredits() {
        if (currentCredits < 0) {
            String errorMessage = "Execution isn't finished." + System.lineSeparator() +
                    "You don't have enough credits." + System.lineSeparator() +
                    "Current credits amount: " + getCurrentCredits();

            throw new IllegalStateException(errorMessage);
        }
    }

    public boolean hasEnoughCredits(int requiredCredits) {
        return currentCredits >= requiredCredits;
    }
}
