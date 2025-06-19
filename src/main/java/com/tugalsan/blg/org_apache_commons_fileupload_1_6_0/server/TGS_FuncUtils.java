package com.tugalsan.blg.org_apache_commons_fileupload_1_6_0.server;

public class TGS_FuncUtils {

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void _throwAsUncheckedException(Throwable exception) throws T {
        throw (T) exception;
    }

    @Deprecated //only internalUse
    private static void throwAsUncheckedException(Throwable exception) {
        TGS_FuncUtils.<RuntimeException>_throwAsUncheckedException(exception);
    }

    public static <R> R throwIfInterruptedException(Throwable t) {
        if (isInterruptedException(t)) {
            Thread.currentThread().interrupt();
            throwAsUncheckedException(t);
        }
        return null;
    }
    
    public static boolean isInterruptedException(Throwable t) {
        if (t instanceof InterruptedException) {
            return true;
        }
        if (t.getCause() != null) {
            return isInterruptedException(t.getCause());
        }
        return false;
    }
}
