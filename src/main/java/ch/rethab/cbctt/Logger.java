package ch.rethab.cbctt;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Reto Habluetzel, 2015
 */
public final class Logger {

    public static boolean verbose = false;

    public static void info(String msg) {
        log("INFO", msg);
    }

    public static void trace(String msg) {
        if (verbose) {
            log("TRACE", msg);
        }
    }

    private static void log(String level, String msg) {
        System.out.printf("%s [%s / %s / %s]: %s\n", level, Thread.currentThread().getName(), dateTime(), methodInfo(), msg);
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
