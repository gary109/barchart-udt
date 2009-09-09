/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_00-33-23'
 *
 * Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
 */
package com.barchart.udt;

public class LingerUDT extends Number implements Comparable<LingerUDT> {

	// measured in seconds
	final int timeout;

	public LingerUDT(int seconds) {
		this.timeout = seconds;
	}

	private static final long serialVersionUID = 3414455799823407217L;

	@Override
	public double doubleValue() {
		return timeout;
	}

	@Override
	public float floatValue() {
		return timeout;
	}

	@Override
	public int intValue() {
		return timeout;
	}

	@Override
	public long longValue() {
		return timeout;
	}

	boolean isLingerOn() {
		return timeout > 0;
	}

	int timeout() {
		return timeout > 0 ? timeout : 0;
	}

	@Override
	public boolean equals(Object otherLinger) {
		if (otherLinger instanceof LingerUDT) {
			LingerUDT other = (LingerUDT) otherLinger;
			return other.timeout == this.timeout;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return timeout;
	}

	@Override
	public int compareTo(LingerUDT other) {
		return other.timeout - this.timeout;
	}

	@Override
	public String toString() {
		return String.valueOf(timeout);
	}

}
