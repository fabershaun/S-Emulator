package engine.logic.programData.program;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Class the holds all the Main Programs and Sub Functions
public class ProgramsHolder implements Serializable {

    private final Map<String, Program> nameToFunction = new HashMap<>();
    private final Map<String, Program> nameToMainProgram = new HashMap<>();
    private final Map<String, String> userStringToName = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    public void addMainProgram(String programName, String UserString, Program mainProgram) {
        lock.writeLock().lock();
        try {
            this.nameToMainProgram.put(programName.toUpperCase(Locale.ROOT), mainProgram);
            this.userStringToName.put(UserString.toUpperCase(Locale.ROOT), programName.toUpperCase(Locale.ROOT));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Program getMainProgramByName(String name) {    // To UPPER
        lock.readLock().lock();
        try {
            String nameToUpper = name.toUpperCase(Locale.ROOT);
            return this.nameToMainProgram.get(nameToUpper);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addFunction(String functionName, String UserString, Program function) {
        lock.writeLock().lock();
        try {
            this.nameToFunction.put(functionName.toUpperCase(Locale.ROOT), function);
            this.userStringToName.put(UserString.toUpperCase(Locale.ROOT), functionName.toUpperCase(Locale.ROOT));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Program getFunctionByName(String name) {    // To UPPER
        lock.readLock().lock();
        try {
            String nameToUpper = name.toUpperCase(Locale.ROOT);
            return this.nameToFunction.get(nameToUpper);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getNameByUserString(String UserString) {    // To UPPER
        lock.readLock().lock();
        try {
            String UserStringToUpper = UserString.toUpperCase(Locale.ROOT);
            return userStringToName.get(UserStringToUpper);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Program> getFunctions() {
        lock.readLock().lock();
        try {
            return new HashSet<>(nameToFunction.values());
        } finally {
            lock.readLock().unlock();
        }    }

    public Set<Program> getMainPrograms() {
        lock.readLock().lock();
        try {
            return new HashSet<>(nameToMainProgram.values());
        } finally {
            lock.readLock().unlock();
        }    }

    public Program getMainProgramForConsoleModuleOnly() {
        lock.readLock().lock();
        try {
            return nameToMainProgram.values().stream().findFirst().orElse(null);
        } finally {
            lock.readLock().unlock();
        }    }
}
