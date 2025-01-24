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
package javax.usb3.event;

import javax.usb3.IUsbControlIrp;
import javax.usb3.IUsbDevice;

/**
 * Indicates data was successfully transferred over the Default Control Pipe.
 * <p>
 * This event will be fired on all successful transfers of data over the DCP.
 *
 * @author Dan Streetman
 * @author Jesse Caulfield
 * @author E. Michael Maximilien
 */
public class UsbDeviceDataEvent extends UsbDeviceEvent {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param source The IUsbDevice.
   * @param irp    The IUsbControlIrp.
   */
  public UsbDeviceDataEvent(IUsbDevice source, IUsbControlIrp irp) {
    super(source);
    usbControlIrp = irp;
  }

  /**
   * Get the data.
   * <p>
   * This is a new byte[] whose length is the actual amount of transferred data.
   * The contents is a copy of the transferred data.
   *
   * @return The transferred data.
   */
  public byte[] getData() {
    byte[] data = new byte[getUsbControlIrp().getActualLength()];
    System.arraycopy(getUsbControlIrp().getData(), getUsbControlIrp().getOffset(), data, 0, data.length);
    return data;
  }

  /**
   * Get the IUsbControlIrp associated with this event.
   *
   * @return The IUsbControlIrp.
   */
  public IUsbControlIrp getUsbControlIrp() {
    return usbControlIrp;
  }

  private transient IUsbControlIrp usbControlIrp = null;

}
