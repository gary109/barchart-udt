package com.barchart.udt;

public enum StatusUDT {

	/* non UDT value */
	UNKNOWN(0), //

	//

	/* keep in sync with api.h CUDTSocket::UDTSTATUS */
	INIT(1), //
	OPENED(2), //
	LISTENING(3), //
	CONNECTED(4), //
	BROKEN(5), //
	CLOSED(6), //

	;

	public final int code;

	private StatusUDT(final int code) {
		this.code = code;
	}

	private static final StatusUDT[] ENUM_VALS = values();

	public static final StatusUDT fromCode(final int code) {
		for (final StatusUDT status : ENUM_VALS) {
			if (status.code == code) {
				return status;
			}
		}
		return UNKNOWN;
	}

}
