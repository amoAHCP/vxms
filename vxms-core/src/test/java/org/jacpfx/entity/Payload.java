package org.jacpfx.entity;

import java.io.Serializable;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class Payload<T extends Serializable> implements Serializable {

  private final T value;

  public Payload(T value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Payload)) {
      return false;
    }

    Payload<?> payload = (Payload<?>) o;

    return !(value != null ? !value.equals(payload.value) : payload.value != null);

  }

  public T getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}