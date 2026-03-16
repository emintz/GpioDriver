/*
 * TransitionTable.java
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

import java.util.EnumMap;

/**
 * State transition table for a DFA having states {@code S}
 * and events {@code E}, where {@code S} and {@code E} are
 * enumerations. The table maps (Initial State, Event) ->
 * Resulting State.
 *
 * A {@code TransitionTable} is stateless and immutable. To
 * achieve the latter, it employs an auxiliary {@link Builder}
 * for the user to populate.
 *
 * @param <S> State enumeration
 * @param <E> Event enumeration
 */
public class TransitionTable<S extends Enum<S>, E extends Enum<E>> {
  private final EnumMap<S, EnumMap<E, S>> table;

  /**
   * Creates a new {@link Builder} for the caller to populate.
   * Unspecified
   *
   * @param defaultResult result state of unspecified transitions
   * @param eventType event enumeration class
   * @return a {@link Builder} as described above
   * @param <S> state enumeration
   * @param <E> event enumeration
   */
  public static <
      S extends Enum<S>,
      E extends Enum<E>> Builder<S, E> builder(
          S defaultResult,
          Class<E> eventType) {
    return new Builder<>(defaultResult, eventType);
  }

  /**
   * Constructor, invoked only by the builder.
   *
   * @param table state transition table.
   */
  private TransitionTable(
      EnumMap<S, EnumMap<E, S>> table) {
    this.table = table;
  }

  /**
   * Provides the state that results when a machine in
   * {@code currentState} receives {@code event}.
   *
   * @param currentState the client machine's current state
   * @param event the received event
   * @return the resulting state
   */
  public S onReceipt(S currentState, E event) {
    return table.get(currentState).get(event);
  }

  /**
   * Builder for {@link TransitionTable} instances. Since
   * transition tables are immutable, the {@code Builder}
   * provides the mutability required to populate the
   * table.
   *
   * @param <S> state enumeration
   * @param <E> event enumeration
   */
  public static class Builder<S extends Enum<S>, E extends Enum<E>> {
    private final Class<E> eventType;
    private final EnumMap<S, EnumMap<E, S>> table;

    /**
     * Creates a {@code Builder} containing a candidate
     * {@link TransitionTable} for the user to populate.
     * The candidate is configured so that transitions
     * will produce {@code defaultResult} unless configured
     * to do otherwise.
     *
     * @param defaultResult produced for unspecified transitions
     * @param eventType event enumeration
     */
    @SuppressWarnings("unchecked")
    private Builder(
        S defaultResult,
        Class<E> eventType) {
      this.eventType = eventType;
      Class<S> stateType = (Class<S>) defaultResult.getClass();
      table = new EnumMap<S, EnumMap<E, S>>((Class<S>) defaultResult.getClass());

      for (var initialState : stateType.getEnumConstants()) {
        EnumMap<E, S> transitions = new EnumMap<>(eventType);
        for (var receivedEvent : eventType.getEnumConstants()) {
          transitions.put(receivedEvent, defaultResult);
        }
        table.put(initialState, transitions);
      }
    }

    /**
     * Specifies that the built {@link TransitionTable} should
     * map {@code (initial, received) -> result}.
     *
     * @param initial initial state
     * @param received received event
     * @param result resulting state
     * @return {code this}, for chaining.
     */
    public Builder<S, E> add(S initial, E received, S result) {
      if (!table.containsKey(initial)) {
        table.put(initial, new EnumMap<>(eventType));
      }
      table.get(initial).put(received, result);
      return this;
    }

    /**
     * Creates a {@link TransitionTable} from a <em>copy</em>
     * of the contained candidate table. The returned table isw
     * independent of this {@code Builder}. Subsequent
     * {@link #add} invocations will <em>not</em>
     * affect it. This {@code Builder} remains usable. Users
     * Users can make further changes and build another
     * {@link TransitionTable} if desired.
     *
     * @return a newly created {@link TransitionTable} as
     * described above.
     */
    public TransitionTable<S, E> build() {
       return new TransitionTable<>(new EnumMap<>(table));  // TODO
    }
  }
}
