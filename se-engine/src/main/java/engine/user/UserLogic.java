package engine.user;

import dto.v3.UserDTO;
import engine.logic.exceptions.CreditsException;

public class UserLogic {


    public static void incrementMainPrograms(UserDTO user) {
        user.setMainProgramsCount(user.getMainProgramsCount() + 1);
    }

    public static void incrementSubFunctions(UserDTO user) {
        user.setSubFunctionsCount(user.getSubFunctionsCount() + 1);
    }

    public static void addCredits(UserDTO user, long creditsToAdd) {
        user.setCurrentCredits(user.getCurrentCredits() + creditsToAdd);
    }

    public static void subtractCredits(UserDTO user, int creditsToSubtract) {
        user.setCurrentCredits(user.getCurrentCredits() - creditsToSubtract);
        user.setUsedCredits(user.getUsedCredits() + creditsToSubtract);

        if (user.getCurrentCredits() < 0) {
            user.setCurrentCredits(0);

            String errorMessage = "Execution isn't finished." + System.lineSeparator() +
                    "You don't have enough credits." + System.lineSeparator() +
                    "Current credits amount: " + user.getCurrentCredits();

            throw new CreditsException(errorMessage);
        }
    }

    public static void incrementExecutions(UserDTO user) {
        user.setExecutionsCount(user.getExecutionsCount() + 1);
    }

    public static boolean hasEnoughCredits(UserDTO user, int requiredCredits) {
        return user.getCurrentCredits() >= requiredCredits;
    }
}
