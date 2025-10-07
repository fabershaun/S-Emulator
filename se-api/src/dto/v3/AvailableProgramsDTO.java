package dto.v3;

import java.util.List;
import java.util.Map;

public class AvailableProgramsDTO {
    private final String programName;
    private final String uploaderName;
    private final int instructionsAmount;
    private final int maxDegree;
    private final int timesPlayed;
    private final long averageCreditCost;

    public AvailableProgramsDTO(String programName, String uploaderName, int instructionsAmount, int maxDegree, int timesPlayed, long averageCreditCost) {
        this.programName = programName;
        this.uploaderName = uploaderName;
        this.instructionsAmount = instructionsAmount;
        this.maxDegree = maxDegree;
        this.timesPlayed = timesPlayed;
        this.averageCreditCost = averageCreditCost;
    }

    public String getProgramName() {
        return programName;
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

   public int getTimesPlayed() {
        return timesPlayed;
   }

   public long getAverageCreditCost() {
        return averageCreditCost;
   }
}

