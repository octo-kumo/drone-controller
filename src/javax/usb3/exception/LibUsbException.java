/*
 * Copyright 2014 Klaus Reimer
 * Copyright (C) 2014 Jesse Caulfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javax.usb3.exception;

import org.usb4java.LibUsb;

/**
 * A runtime exception which automatically outputs the {@code libusb} error
 * string.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class LibUsbException extends RuntimeException {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The libusb error code.
   */
  private final int errorCode;

  /**
   * Constructs a libusb exception which just outputs the error code and the
   * error message from libusb.
   *
   * @param errorCode The error code.
   */
  public LibUsbException(final int errorCode) {
    super(String.format("USB error %d: %s", -errorCode,
                        LibUsb.strError(errorCode)));
    this.errorCode = errorCode;
  }

  /**
   * Constructs a libusb exception which outputs the error code and the error
   * message from libusb together with a custom error message.
   *
   * @param message   The error message.
   * @param errorCode The error code.
   */
  public LibUsbException(final String message, final int errorCode) {
    super(String.format("USB error %d: %s: %s", -errorCode, message,
                        LibUsb.strError(errorCode)));
    this.errorCode = errorCode;
  }

  /**
   * Returns the error code.
   *
   * @return The error code
   */
  public int getErrorCode() {
    return this.errorCode;
  }
}
