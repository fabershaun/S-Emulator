package dto.v2;

import java.util.Locale;

public class InstructionDataDTO {
    private final String name;
    private final int cycles;

    public InstructionDataDTO(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }

    public String getName() {
        return name;
    }

    public int getCycles() {
        return cycles;
    }

    @Override
    public String toString() {
        return name.toLowerCase(Locale.ROOT); // So ComboBox will show the command name
    }
}
