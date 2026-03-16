package com.ooarchitect.gpioclient;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Validates {@link InputPin}
 */
class InputPinTest {

  /**
   * Pin levels returned to the user
   */
  private final byte HIGH = 1;
  private final byte LOW = 0;

  /**
   * Incoming pin mutations
   */
  private static final byte HIGH_MUTATION = (byte) 0x8F;
  private static final byte LOW_MUTATION = (byte) 0x0A;

  private InputPin inputPin;

  @BeforeEach
  void beforeEachTest() {
    inputPin = new InputPin();
  }

  @Test
  public void construction() {
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);
    Truth.assertThat(inputPin.offline()).isFalse();
  }

  @Test
  public void setWhenClosed_ignored() {
    inputPin.receiveMutation(HIGH);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);
    Truth.assertThat(inputPin.offline()).isFalse();
  }

  @Test
  public void requestOpenWhenInactive() {
    Truth.assertThat(inputPin.openRequested()).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.PENDING);
    Truth.assertThat(inputPin.offline()).isFalse();
  }

  @Test
  public void requestOpenFromUnsupportedState() {
    inputPin.setState(PinState.PENDING);
    Truth.assertThat(inputPin.openRequested()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.PENDING);
    Truth.assertThat(inputPin.offline()).isFalse();

    inputPin.setState(PinState.ACTIVE);
    Truth.assertThat(inputPin.openRequested()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);
    Truth.assertThat(inputPin.offline()).isFalse();

    inputPin.setState(PinState.OFFLINE);
    Truth.assertThat(inputPin.openRequested()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.OFFLINE);
    Truth.assertThat(inputPin.offline()).isTrue();
  }

  @Test
  public void openSucceededFromPending() {
    inputPin.setState(PinState.PENDING);
    Truth.assertThat(inputPin.openSucceeded(HIGH)).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(HIGH);
  }

  @Test
  public void openSucceededFromUnsupportedState() {
    // Newly constructed instance is INACTIVE
    Truth.assertThat(inputPin.openSucceeded(HIGH)).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);

    inputPin.setState(PinState.OFFLINE);
    Truth.assertThat(inputPin.openSucceeded(HIGH)).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.OFFLINE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);


    inputPin.setState(PinState.INACTIVE);
    Truth.assertThat(inputPin.openSucceeded(HIGH)).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);

    inputPin.setState(PinState.ACTIVE);
    Truth.assertThat(inputPin.openSucceeded(HIGH)).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);
  }

  @Test
  public void openSucceededAfterReceiveMutationInPending() {
    inputPin.setState(PinState.PENDING);
    inputPin.receiveMutation(HIGH_MUTATION);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(HIGH);
    Truth.assertThat(inputPin.openSucceeded(LOW_MUTATION)).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(HIGH);
  }

  @Test
  public void reset() {
    inputPin.setState(PinState.OFFLINE);
    Truth.assertThat(inputPin.offline()).isTrue();
    inputPin.setValue(HIGH);
    inputPin.reset();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);
  }

  @Test
  public void close() {
    inputPin.setState(PinState.ACTIVE);
    inputPin.setValue(HIGH);
    Truth.assertThat(inputPin.close()).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);

    inputPin.setState(PinState.INACTIVE);
    inputPin.setValue(HIGH);
    Truth.assertThat(inputPin.close()).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);

    inputPin.setState(PinState.PENDING);
    inputPin.setValue(HIGH);
    Truth.assertThat(inputPin.close()).isTrue();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.INACTIVE);
    Truth.assertThat(inputPin.value()).isEqualTo(LOW);

    inputPin.setState(PinState.OFFLINE);
    inputPin.setValue(HIGH);
    Truth.assertThat(inputPin.close()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.OFFLINE);
    Truth.assertThat(inputPin.value()).isEqualTo(HIGH);
  }
}
