package bdv.dzne;

public	class ParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ParseException() {
        super();
    }

    public ParseException(final String s) {
        super(s);
    }

    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ParseException(final Throwable cause) {
        super(cause);
    }
}