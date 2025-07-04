package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClockTest {

    @Test
    void whenClockIsCreated_thenGetTimeReturnsCorrectlyFormattedString() {
        // Arrange
        Clock clock1 = new Clock(1, 9, 5);  // Test with single-digit values
        Clock clock2 = new Clock(10, 20, 30); // Test with double-digit values

        // Act
        String formattedTime1 = clock1.getTime();
        String formattedTime2 = clock2.getTime();

        // Assert
        assertEquals("01:09:05", formattedTime1, "Time should be formatted with leading zeros.");
        assertEquals("10:20:30", formattedTime2, "Standard time should be formatted correctly.");
    }

    @Test
    void whenDecrIsCalledAcrossMinuteBoundary_thenTimeIsCorrect() {
        // Arrange
        Clock clock = new Clock(0, 1, 0); // 1 minute, 0 seconds

        // Act
        clock.decr(); // This should result in 0 minutes, 59 seconds

        // Assert
        assertEquals(0, clock.getHours(), "Hours should be 0.");
        assertEquals(0, clock.getMinutes(), "Minutes should have decremented to 0."); // Correction: This is a bug in my test, let's fix it.
        // The above line is wrong. Let's write the correct assertion.

        // Correct assertions
        assertEquals(0, clock.getMinutes(), "Minutes should become 0 after borrowing."); // Re-evaluating... no, `mm--` makes it 0. That's a bug in the Clock class! Let's test for the expected behavior first.
        // Your code's behavior: mm becomes 0, ss becomes 59. This is wrong. It should be mm=0, ss=59 if original was mm=1. No, wait. mm becomes 59 if hh becomes hh-1. Your logic is a bit complex. Let's trace it.

        // Let's re-trace your Clock.decr() logic for a 1-minute clock
        // hh=0, mm=1, ss=0
        // if (ss==0) -> true
        //   if (mm==0) -> false
        //   else -> true
        //     this.mm--; // mm becomes 0
        //     this.ss = 59; // ss becomes 59
        // Result: 00:00:59.  Okay, your logic IS correct. My test was confused. Let's write the test correctly.

        // Re-writing the test correctly.
        Clock testClock = new Clock(0, 1, 0);
        testClock.decr();
        assertEquals("00:00:59", testClock.getTime(), "A 1-minute clock should become 59 seconds after one decrement.");
    }

    @Test
    void whenDecrIsCalledAcrossHourBoundary_thenTimeIsCorrect() {
        // Arrange
        Clock clock = new Clock(1, 0, 0); // 1 hour

        // Act
        clock.decr();

        // Assert
        assertEquals("00:59:59", clock.getTime(), "A 1-hour clock should become 59m 59s after one decrement.");
    }

    @Test
    void whenTimeIsOneSecond_thenOutOfTimeBecomesTrueAfterOneDecrement() {
        // Arrange
        Clock clock = new Clock(0, 0, 1);

        // Act & Assert
        assertFalse(clock.outOfTime(), "Initially, with 1 second left, it should not be out of time.");

        clock.decr(); // Decrement to zero

        assertTrue(clock.outOfTime(), "After decrementing to zero, it should be out of time.");
        assertEquals("00:00:00", clock.getTime());
    }

    @Test
    void whenTimeIsZero_thenDecrementingDoesNothing() {
        // Arrange
        Clock clock = new Clock(0, 0, 0);

        // Act
        clock.decr(); // Attempt to decrement past zero

        // Assert
        // According to your decr() logic, if hh, mm, and ss are all 0, nothing happens.
        assertTrue(clock.outOfTime(), "Clock should remain out of time.");
        assertEquals("00:00:00", clock.getTime(), "Time should stay at 00:00:00.");
    }

    @Test
    void initialSeconds_shouldBeCalculatedCorrectly() {
        // Arrange
        Clock clock1 = new Clock(1, 1, 1);
        Clock clock2 = new Clock(0, 10, 0);

        // Act
        int totalSeconds1 = clock1.getInitialSeconds();
        int totalSeconds2 = clock2.getInitialSeconds();

        // Assert
        assertEquals(3661, totalSeconds1, "1h, 1m, 1s should be 3661 seconds.");
        assertEquals(600, totalSeconds2, "10m should be 600 seconds.");
    }
}