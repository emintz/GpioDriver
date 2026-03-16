package com.ooarchitect.gpioclient;

import com.google.common.truth.Truth;
import org.junit.Rule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Validate {@link OutputPin}
 */
@ExtendWith(MockitoExtension.class)
class OutputPinTest {

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private OutputPinProxy pinProxy;
}
