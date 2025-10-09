package dto.v3;

public class FunctionDTO {
    private final String functionName;
    private final String mainProgramName;
    private final String uploaderName;
    private final int instructionsAmount;
    private final int maxDegree;

    public FunctionDTO(String functionName, String mainProgramName, String uploaderName, int instructionsAmount, int maxDegree) {
        this.functionName = functionName;
        this.mainProgramName = mainProgramName;
        this.uploaderName = uploaderName;
        this.instructionsAmount = instructionsAmount;
        this.maxDegree = maxDegree;

    }

    public String getFunctionName() {
        return functionName;
    }

    public String getMainProgramName() {
        return mainProgramName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public int getInstructionsAmount() {
        return instructionsAmount;
    }

    public int getMaxDegree() {
        return maxDegree;
    }
}

