/*
 * TestByteSupplier.java
 *
 * Copyright (C) 2026  Eric Mintz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ooarchitect.statemachine;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

/**
 * Validates {@link TransitionTable} using a state table that
 * removes leading and trailing whitespace from a string and
 * replaces internal whitespace with a single space.
 */

class TransitionTableTest {
  /**
   * Events characterize the incoming character type.
   */
  enum Event {
    /**
     * Received a printing character: letter,
     * number, punctuation, etc.*/
    PRINTING_CHARACTER,
    /**
     * Received a whitespace character: blank, tab,
     * form feed, etc.
     */
    WHITESPACE,
    /**
     * Received an end of line character.
     */
    END_OF_LINE,
  }

  /**
   * States determine behavior on entry.
   */
  enum State {
    /**
     * Initial state, indicates a newly created state
     * machine that has processed no input. Valid state
     * models never transition into their initial state.
     */
    CREATED,
    /**
     * At the start of a line.
     */
    START_OF_LINE,
    /**
     * Whitespace at the start of a line
     */
    LEADING_WHITESPACE,
    /**
     * In the middle of a word break -- whitespace
     * following one or more printing characters.
     */
    WITHIN_WHITESPACE,
    /**
     * Received a printing character that ends a
     * word break.
     */
    WORD_START,
    /**
     * Within a word, a block of one or more printing
     * characters.
     */
    WITHIN_WORD,
  }

  record ExpectedTransition(
      State initial,
      Event event,
      State expected) {
  }

  /**
   * Minimal table wherein all transitions yield {@link State#CREATED}
   */

  @Test
  public void buildAllDefault() {
    TransitionTable<State, Event> table =
        TransitionTable.builder(State.CREATED, Event.class)
            .build();

    for (State initial : State.values()) {
      for (Event event : Event.values()) {
        Truth.assertWithMessage(
            "Transitioning from (%s, %s)", initial, event)
            .that(table.onReceipt(initial, event)).isEqualTo(State.CREATED);
      }
    }
  }

  /**
   * Validate against a state model that trims leading and
   * trailing whitespace and consolidates interposed
   * whitespace to a single space.
   */
  @Test
  public void whitespaceProcessingTable() {

    final ExpectedTransition[] expectedTransitions = {
        new ExpectedTransition(State.CREATED, Event.WHITESPACE, State.LEADING_WHITESPACE),
        new ExpectedTransition(State.CREATED, Event.PRINTING_CHARACTER, State.WITHIN_WORD),
        new ExpectedTransition(State.CREATED, Event.END_OF_LINE, State.START_OF_LINE),

        new ExpectedTransition(State.START_OF_LINE, Event.WHITESPACE, State.LEADING_WHITESPACE),
        new ExpectedTransition(State.START_OF_LINE, Event.PRINTING_CHARACTER, State.WITHIN_WORD),
        new ExpectedTransition(State.START_OF_LINE, Event.END_OF_LINE, State.START_OF_LINE),

        new ExpectedTransition(State.LEADING_WHITESPACE, Event.WHITESPACE, State.LEADING_WHITESPACE),
        new ExpectedTransition(State.LEADING_WHITESPACE, Event.PRINTING_CHARACTER, State.WITHIN_WORD),
        new ExpectedTransition(State.LEADING_WHITESPACE, Event.END_OF_LINE, State.START_OF_LINE),

        new ExpectedTransition(State.WORD_START, Event.WHITESPACE, State.WITHIN_WHITESPACE),
        new ExpectedTransition(State.WORD_START, Event.PRINTING_CHARACTER, State.WITHIN_WORD),
        new ExpectedTransition(State.WORD_START, Event.END_OF_LINE, State.START_OF_LINE),

        new ExpectedTransition(State.WITHIN_WORD, Event.WHITESPACE, State.WITHIN_WHITESPACE),
        new ExpectedTransition(State.WITHIN_WORD, Event.PRINTING_CHARACTER, State.WITHIN_WORD),
        new ExpectedTransition(State.WITHIN_WORD, Event.END_OF_LINE, State.START_OF_LINE),

        new ExpectedTransition(State.WITHIN_WHITESPACE, Event.WHITESPACE, State.WITHIN_WHITESPACE),
        new ExpectedTransition(State.WITHIN_WHITESPACE, Event.PRINTING_CHARACTER, State.WORD_START),
        new ExpectedTransition(State.WITHIN_WHITESPACE, Event.END_OF_LINE, State.START_OF_LINE),
    };

    TransitionTable<State, Event> table =
        TransitionTable.builder(State.CREATED, Event.class)
            .add(State.CREATED, Event.WHITESPACE, State.LEADING_WHITESPACE)
            .add(State.CREATED, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.CREATED, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.LEADING_WHITESPACE, Event.WHITESPACE, State.LEADING_WHITESPACE)
            .add(State.LEADING_WHITESPACE, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.LEADING_WHITESPACE, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.START_OF_LINE, Event.WHITESPACE, State.LEADING_WHITESPACE)
            .add(State.START_OF_LINE, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.START_OF_LINE, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.WITHIN_WORD, Event.WHITESPACE, State.WITHIN_WHITESPACE)
            .add(State.WITHIN_WORD, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.WITHIN_WORD, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.WITHIN_WHITESPACE, Event.WHITESPACE, State.WITHIN_WHITESPACE)
            .add(State.WITHIN_WHITESPACE, Event.PRINTING_CHARACTER, State.WORD_START)
            .add(State.WITHIN_WHITESPACE, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.WORD_START, Event.WHITESPACE, State.WITHIN_WHITESPACE)
            .add(State.WORD_START, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.WORD_START, Event.END_OF_LINE, State.START_OF_LINE)

            .add(State.WITHIN_WORD, Event.WHITESPACE, State.WITHIN_WHITESPACE)
            .add(State.WITHIN_WORD, Event.PRINTING_CHARACTER, State.WITHIN_WORD)
            .add(State.WITHIN_WORD, Event.END_OF_LINE, State.START_OF_LINE)

            .build();

    for (var expected : expectedTransitions) {
      Truth.assertWithMessage(
              "Received event %s in state %s ",
              expected.event,
              expected.initial)
          .that(table.onReceipt(expected.initial, expected.event))
          .isEqualTo(expected.expected);
    }
  }
}
