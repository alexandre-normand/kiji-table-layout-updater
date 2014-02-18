package com.opower.updater.operation;

/**
 * Exception thrown when there is a missing update. The tool enforces that all updates are present.
 *
 * @author felix.trepanier
 */
public class MissingUpdateException extends RuntimeException {
    private final Integer expectedId;
    private final Integer idFound;

    public MissingUpdateException(Integer expectedId, Integer idFound) {
        this.expectedId = expectedId;
        this.idFound = idFound;
    }

    public Integer getExpectedId() {
        return expectedId;
    }

    public Integer getIdFound() {
        return idFound;
    }
}
