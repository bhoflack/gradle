package org.gradle.cache.internal;

import org.gradle.internal.Factory;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * By Szczepan Faber on 5/16/13
 */
public class DummyLock implements FileLock {
    private File lockFile;
    private final FileLockManager.LockMode lockMode;
    private final String cacheDiplayName;

    public DummyLock(File lockFile, FileLockManager.LockMode lockMode, String cacheDiplayName) {
        this.lockFile = lockFile;
        this.lockMode = lockMode;
        this.cacheDiplayName = cacheDiplayName;
    }

    public boolean getUnlockedCleanly() {
        return true;
    }

    public boolean isLockFile(File file) {
        return lockFile.equals(file);
    }

    public void close() {}

    public FileLockManager.LockMode getMode() {
        return lockMode;
    }

    public <T> T readFile(Callable<? extends T> action) throws LockTimeoutException, FileIntegrityViolationException, InsufficientLockModeException {
        try {
            return action.call();
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T readFile(Factory<? extends T> action) throws LockTimeoutException, FileIntegrityViolationException, InsufficientLockModeException {
        try {
            return action.create();
        } catch (Exception e) {
            return null;
        }
    }

    public void updateFile(Runnable action) throws LockTimeoutException, FileIntegrityViolationException, InsufficientLockModeException {
        action.run();
    }

    public void writeFile(Runnable action) throws LockTimeoutException, InsufficientLockModeException {
        action.run();
    }
}
