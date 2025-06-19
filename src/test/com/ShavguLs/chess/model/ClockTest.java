//package test.com.ShavguLs.chess.model;
//
//import main.com.ShavguLs.chess.model.*;
//import org.junit.Test;
//
//import static junit.framework.TestCase.*;
//
//public class ClockTest {
//
//    @Test
//    public void testClockInitialization() {
//        Clock clock = new Clock(1, 30, 0);
//        assertEquals("01:30:00", clock.getTime());
//
//        Clock shortClock = new Clock(0, 5, 0);
//        assertEquals("00:05:00", shortClock.getTime());
//    }
//
//    @Test
//    public void testClockDecrement() {
//        Clock clock = new Clock(0, 1, 0);
//        assertEquals("00:01:00", clock.getTime());
//
//        clock.decr();
//        assertEquals("00:00:59", clock.getTime());
//
//        for (int i = 0; i < 59; i++) {
//            clock.decr();
//        }
//        assertEquals("00:00:00", clock.getTime());
//
//        assertTrue(clock.outOfTime());
//    }
//
//    @Test
//    public void testHourRollover() {
//        Clock clock = new Clock(1, 0, 0);
//        assertEquals("01:00:00", clock.getTime());
//        for (int i = 0; i < 60; i++) {
//            clock.decr();
//        }
//        assertEquals("00:59:00", clock.getTime());
//    }
//}