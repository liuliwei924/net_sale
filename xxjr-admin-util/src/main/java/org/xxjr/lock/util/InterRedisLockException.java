package org.xxjr.lock.util;

public class InterRedisLockException extends RuntimeException {

	private static final long serialVersionUID = 7731354534764975532L;

	/**
     * Constructs an <code>InterruptedException</code> with no detail  message.
     */
    public InterRedisLockException() {
        super();
    }

    /**
     * Constructs an <code>InterruptedException</code> with the
     * specified detail message.
     *
     * @param   s   the detail message.
     */
    public InterRedisLockException(String s) {
        super(s);
    }
}
