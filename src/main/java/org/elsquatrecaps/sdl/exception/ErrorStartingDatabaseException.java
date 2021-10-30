/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elsquatrecaps.sdl.exception;

/**
 *
 * @author josep
 */
public class ErrorStartingDatabaseException extends RuntimeException {

    /**
     * Creates a new instance of <code>ErrorStartingDatabaseException</code>
     * without detail message.
     */
    public ErrorStartingDatabaseException() {
    }

    public ErrorStartingDatabaseException(Throwable th) {
        super(th);
    }

    /**
     * Constructs an instance of <code>ErrorStartingDatabaseException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ErrorStartingDatabaseException(String msg) {
        super(msg);
    }

    public ErrorStartingDatabaseException(String msg, Throwable th) {
        super(msg, th);
    }
}
