package dto.v3;

public class UserDTO {

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

    public void addToCurrentCredits(int count) {
        this.currentCredits += count;
    }

    public void addToUsedCredits(int usedCredits) {
        this.usedCredits += usedCredits;
    }

    public void addOneToExecutionsCount() {
        this.executionsCount += 1;
    }

//    public synchronized void addUser(String username) {
//        usersSet.add(username);
//    }
//
//    public synchronized void removeUser(String username) {
//        usersSet.remove(username);
//    }
//
//    public synchronized Set<String> getUsers() {
//        return Collections.unmodifiableSet(usersSet);
//    }
//
//    public boolean isUserExists(String username) {
//        return usersSet.contains(username);
//    }
}
