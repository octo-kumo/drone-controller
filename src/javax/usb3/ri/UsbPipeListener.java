/*
 * Copyright (C) 2011 Klaus Reimer
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
package javax.usb3.ri;

import javax.usb3.event.IUsbPipeListener;
import javax.usb3.event.UsbPipeDataEvent;
import javax.usb3.event.UsbPipeErrorEvent;

/**
 * USB pipe listener list.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class UsbPipeListener extends UsbEventListener<IUsbPipeListener> implements IUsbPipeListener {

  /**
   * Constructs a new USB pipe listener list.
   */
  public UsbPipeListener() {
    super();
  }

  @Override
  public IUsbPipeListener[] toArray() {
    return getListeners().toArray(new IUsbPipeListener[getListeners().size()]);
  }

  @Override
  public void errorEventOccurred(final UsbPipeErrorEvent event) {
    for (final IUsbPipeListener listener : toArray()) {
      listener.errorEventOccurred(event);
    }
  }

  @Override
  public void dataEventOccurred(final UsbPipeDataEvent event) {
    for (final IUsbPipeListener listener : toArray()) {
      listener.dataEventOccurred(event);
    }
  }
}
