/*
 *
 * Based on libusb <http://libusb.info/>:
 *
 * Copyright 2001 Johannes Erdfelt <johannes@erdfelt.com>
 * Copyright 2007-2009 Daniel Drake <dsd@gentoo.org>
 * Copyright 2010-2012 Peter Stuge <peter@stuge.se>
 * Copyright 2008-2013 Nathan Hjelm <hjelmn@users.sourceforge.net>
 * Copyright 2009-2013 Pete Batard <pete@akeo.ie>
 * Copyright 2009-2013 Ludovic Rousseau <ludovic.rousseau@gmail.com>
 * Copyright 2010-2012 Michael Plante <michael.plante@gmail.com>
 * Copyright 2011-2013 Hans de Goede <hdegoede@redhat.com>
 * Copyright 2012-2013 Martin Pieuchot <mpi@openbsd.org>
 * Copyright 2012-2013 Toby Gray <toby.gray@realvnc.com>
 * Copyright 2013 Klaus Reimer
 * Copyright 2014-2016 Jesse Caulfield
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
package org.usb4java;

import java.util.Objects;

/**
 * Structure providing the version of the (native) libusb runtime.
 *
 * @author Klaus Reimer
 * @author Jesse Caulfield
 */
public final class Version {
  // Maps to JNI native class

  /**
   * The native pointer to the version structure.
   */
  private long versionPointer;

  /**
   * Package-private constructor to prevent manual instantiation. An instance is
   * only returned by the JNI method {@link LibUsb#getVersion()}.
   */
  public Version() {
    // Empty
  }

  /**
   * Returns the native pointer.
   *
   * @return The native pointer.
   */
  public long getPointer() {
    return this.versionPointer;
  }

  /**
   * Returns the library major version.
   *
   * @return The library major version.
   */
  public native int major();

  /**
   * Returns the library minor version.
   *
   * @return The library minor version.
   */
  public native int minor();

  /**
   * Returns the library micro version.
   *
   * @return The library micro version.
   */
  public native int micro();

  /**
   * Returns the library nano version.
   *
   * @return The library nano version.
   */
  public native int nano();

  /**
   * Returns the release candidate suffix string, e.g. "-rc4".
   *
   * @return The release candidate suffix string.
   */
  public native String rc();

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + (int) (this.versionPointer ^ (this.versionPointer >>> 32));
    hash += this.major();
    hash += this.minor();
    hash += this.micro();
    hash += this.nano();
    hash += Objects.hashCode(this.rc());
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Version other = (Version) obj;
    return this.hashCode() == other.hashCode();
  }

  @Override
  public String toString() {
    return this.major() + "."
      + this.minor() + "."
      + this.micro() + "."
      + this.nano()
      + this.rc();
  }
}
