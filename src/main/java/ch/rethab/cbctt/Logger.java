package ch.rethab.cbctt;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Reto Habluetzel, 2015
 */
public final class Logger {

    public enum Level {
        /** Info should always be show. It won't print too much. Promised! */
        INFO(0),

        /** We would like to know what's going on, cause we're either impatient or a little concerned. */
        GIBBER(1),

        /** Something looks fishy. I want to know all the details */
        TRACE(2);

        int level;

        Level(int level) {
            this.level = level;
        }
    }

    /**
     * Note that this variable should not be set in the middle of the program
     * if the program is multi-threaded, because of stale values. The variable
     * is not made volatile for performance reasons.
     */
    public static Level configuredLevel = Level.INFO;

    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void gibber(String msg) {
        log(Level.GIBBER, msg);
    }

    public static void trace(String msg) {
        log(Level.TRACE, msg);
    }

    private static void log(Level level, String msg) {
        if (configuredLevel.level >= level.level) {
            System.out.printf("%s [%s / %s / %s]: %s\n", level.name(), Thread.currentThread().getName(), dateTime(), methodInfo(), msg);
        }
    }

    private static String methodInfo() {
        StackTraceElement[] stack = new Exception().getStackTrace();
        String className = stack[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return String.format("%s.%s", simpleClassName, stack[3].getMethodName());
    }

    private static String dateTime() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

}
