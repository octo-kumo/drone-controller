/*
 * Copyright (C) 1999 - 2001, International Business Machines
 * Corporation. All Rights Reserved. Provided and licensed under the terms and
 * conditions of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 *
 * Copyright (C) 2014 Key Bridge LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package javax.usb3.ri;

import javax.usb3.IUsbIrp;
import javax.usb3.exception.UsbException;
import javax.usb3.exception.UsbShortPacketException;
import javax.usb3.utility.ByteUtility;

/**
 * A basic, abstract USB I/O Request Packet (IRP) implementation (IUsbIrp). This
 * class implements minimum required functionality for the UsbIrp interface.
 * <p>
 * The behavior and defaults follow those defined in the
 * {@link javax.usb.IUsbIrp interface}. Any of the fields may be updated if the
 * default is not appropriate; in most cases the {@link #getData() data} will be
 * the only field that needs to be {@link #setData(byte[]) set}.
 *
 * @author Dan Streetman
 * @author Jesse Caulfield
 */
@SuppressWarnings("ProtectedField")
public class AUsbIrp implements IUsbIrp {

  /**
   * The I/O Request Packet data buffer.
   */
  protected byte[] data = new byte[0];
  /**
   * Indicator that the UsbIrp data read/write transaction is complete or not.
   */
  protected boolean complete = false;
  /**
   * The policy can be set to either accept or reject short packets.
   * <p>
   * Short packets will happen if the device transfers less data than the host
   * was expecting. Normally this will happen only if the pipe direction is
   * device-to-host, and the data buffer provided is larger than the amount of
   * data that the device has to send to the host during a specific
   * communication. If short packets are accepted, and a short packet occurs,
   * the communication will complete successfully and the actual length of
   * transferred data will be less than the size of the provided data buffer. If
   * short packets are not accepted, and a short packet occurs, the UsbIrp will
   * complete with an error.
   * <p>
   * Default is TRUE (Accept short packets).
   * <p>
   * Developer note: Only set this to FALSE if you have a well behaved USB
   * device and want/need to tightly control the exchange of data between the
   * HOST and DEVICE.
   */
  protected boolean acceptShortPacket = true;
  /**
   * The starting offset of the data. This indicates the starting byte in the
   * data, or what offset into the data buffer the implementation should use
   * when transferring data. If the offset is zero, data will be transferred
   * starting at the beginning of the byte[], if the offset is above zero, data
   * starting at the offset into the byte[] will be used when communicating with
   * the device.
   */
  protected int offset = 0;
  /**
   * The length of data in the data buffer to transfer with the device. (e.g.
   * that should be read by the device.)
   * <p>
   * The direction of data transfer (host-to-device or device-to-host) is
   * indicated by the Direction bit of the bmRequestType field. If this field is
   * zero, there is no data transfer phase.
   * <p>
   * On an input request, a device must never return more data than is indicated
   * by the wLength value; it may return less. On an output request, wLength
   * will always indicate the exact amount of data to be sent by the host.
   * Device behavior is undefined if the host should send more data than is
   * specified in wLength.
   */
  protected int length = 0;
  /**
   * The actual length of of data written into the data buffer by the device.
   */
  protected int actualLength = 0;
  /**
   * Any UsbException that occurred during communication with the device on the
   * pipe.
   */
  protected UsbException usbException = null;
  /**
   * Internal lock object used for a synchronized read/write transaction.
   */
  private final Object waitLock = new Object();

  /**
   * Empty constructor. The data array must be set before use.
   */
  public AUsbIrp() {
  }

  /**
   * Generic USB IRP Constructor providing a data array to read from or write in
   * to.
   *
   * @param data The data array.
   * @exception IllegalArgumentException If the data is null.
   */
  public AUsbIrp(byte[] data) {
    setData(data);
  }

  /**
   * Generic USB IRP Constructor. This is overwritten from the UsbIrp and
   * ControlUspIrp implementations.
   *
   * @param data        The data.
   * @param offset      The offset.
   * @param length      The length.
   * @param shortPacket The Short Packet policy.
   * @exception IllegalArgumentException If the data is null, or the offset
   *                                     and/or length is negative.
   */
  public AUsbIrp(byte[] data, int offset, int length, boolean shortPacket) {
    setData(data, offset, length);
    setAcceptShortPacket(shortPacket);
  }

  /**
   * Get the data.
   *
   * @return The data.
   */
  @Override
  public byte[] getData() {
    return data;
  }

  /**
   * Set the data.
   *
   * @param d The data.
   * @exception IllegalArgumentException If the data is null.
   */
  @Override
  public final void setData(byte[] d) throws IllegalArgumentException {
    if (null == d) {
      throw new IllegalArgumentException("Data cannot be null.");
    }
    setData(d, 0, d.length);
  }

  /**
   * Set the data, offset, and length.
   *
   * @param d The data.
   * @param o The offset.
   * @param l The length.
   * @exception IllegalArgumentException If the data is null, or the offset
   *                                     and/or length is negative.
   */
  @Override
  @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
  public final void setData(byte[] d, int o, int l) throws IllegalArgumentException {
    if (null == d) {
      throw new IllegalArgumentException("Data cannot be null.");
    }
    this.data = d;
    setOffset(o);
    setLength(l);
  }

  /**
   * Get the offset.
   *
   * @return The offset.
   */
  @Override
  public int getOffset() {
    return offset;
  }

  /**
   * Set the offset.
   *
   * @param o The offset.
   * @exception IllegalArgumentException If the offset is negative.
   */
  @Override
  public void setOffset(int o) throws IllegalArgumentException {
    if (0 > o) {
      throw new IllegalArgumentException("Offset cannot be negative.");
    }
    offset = o;
  }

  /**
   * Get the length.
   *
   * @return The length.
   */
  @Override
  public int getLength() {
    return length;
  }

  /**
   * Set the length.
   *
   * @param l The length.
   * @exception IllegalArgumentException If the length is negative.
   */
  @Override
  public void setLength(int l) throws IllegalArgumentException {
    if (0 > l) {
      throw new IllegalArgumentException("Length cannot be negative");
    }
    length = l;
  }

  /**
   * Get the actual length.
   *
   * @return The actual length.
   */
  @Override
  public int getActualLength() {
    return actualLength;
  }

  /**
   * Set the actual length.
   *
   * @param l The actual length.
   * @exception IllegalArgumentException If the length is negative.
   */
  @Override
  public void setActualLength(int l) throws IllegalArgumentException {
    if (0 > l) {
      throw new IllegalArgumentException("Actual length cannot be negative");
    }
    actualLength = l;
  }

  /**
   * If a UsbException occurred.
   *
   * @return If a UsbException occurred.
   */
  @Override
  public boolean isUsbException() {
    return (null != getUsbException());
  }

  /**
   * Get the UsbException.
   *
   * @return The UsbException, or null.
   */
  @Override
  public UsbException getUsbException() {
    return usbException;
  }

  /**
   * Set the UsbException.
   *
   * @param exception The UsbException.
   */
  @Override
  public void setUsbException(UsbException exception) {
    usbException = exception;
  }

  /**
   * Get the Short Packet policy. Default is TRUE (Accept short packets).
   *
   * @return The Short Packet policy.
   */
  @Override
  public boolean getAcceptShortPacket() {
    return acceptShortPacket;
  }

  /**
   * Set the Short Packet policy.
   * <p>
   * Developer note: Only set this to FALSE if you have a well behaved USB
   * device and want/need to tightly control the exchange of data between the
   * HOST and DEVICE.
   *
   * @param accept The Short Packet policy.
   */
  @Override
  public final void setAcceptShortPacket(boolean accept) {
    acceptShortPacket = accept;
  }

  /**
   * If this is complete.
   *
   * @return If this is complete.
   */
  @Override
  public boolean isComplete() {
    return complete;
  }

  /**
   * Set this as complete (or not).
   *
   * @param b If this is complete (or not).
   */
  @Override
  public void setComplete(boolean b) {
    complete = b;
  }

  /**
   * Complete this submission.
   * <p>
   * This will:
   * <ul>
   * <li>{@link #setComplete(boolean) Set} {@link #isComplete() complete} to
   * TRUE.</li>
   * <li>Notify all {@link #waitUntilComplete() waiting Threads}.</li>
   * </ul>
   */
  @Override
  public void complete() {
    setComplete(true);
    /**
     * Check for a Short Packet error condition.
     */
    if (!acceptShortPacket && getLength() > getActualLength()) {
      setUsbException(new UsbShortPacketException("Short packet: actual [" + getActualLength() + "] length [" + getLength() + "]"));
    }
    synchronized (waitLock) {
      waitLock.notifyAll();
    }
  }

  /**
   * Wait until {@link #isComplete() complete}.
   * <p>
   * This will block until this is {@link #isComplete() complete}.
   */
  @Override
  public void waitUntilComplete() {
    synchronized (waitLock) {
      while (!isComplete()) {
        try {
          waitLock.wait();
        } catch (InterruptedException iE) {
        }
      }
    }
  }

  /**
   * Wait until {@link #isComplete() complete}, or the timeout has expired.
   * <p>
   * This will block until this is {@link #isComplete() complete}, or the
   * timeout has expired. If the timeout is 0 or less, this behaves as the
   * {@link #waitUntilComplete() no-timeout method}.
   *
   * @param timeout The maximum number of milliseconds to wait.
   */
  @Override
  public void waitUntilComplete(long timeout) {
    if (0 >= timeout) {
      waitUntilComplete();
      return;
    }

    long startTime = System.currentTimeMillis();

    synchronized (waitLock) {
      if (!isComplete() && ((System.currentTimeMillis() - startTime) < timeout)) {
        try {
          waitLock.wait(timeout);
        } catch (InterruptedException iE) {
        }
      }
    }
  }

  /**
   * Get a pretty-print string output for this UsbIrp implementation.
   *
   * @return the bean configuration
   */
  @Override
  public String toString() {
    return "complete [" + complete
      + "] acceptShortPacket [" + acceptShortPacket
      + "] offset [" + offset
      + "] length [" + length
      + "] actualLength [" + actualLength
      + "] data [" + ByteUtility.toString(data).trim()
      + ']';
  }
}
