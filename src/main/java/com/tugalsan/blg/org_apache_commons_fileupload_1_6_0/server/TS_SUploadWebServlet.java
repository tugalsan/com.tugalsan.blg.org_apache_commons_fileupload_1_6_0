package com.tugalsan.blg.org_apache_commons_fileupload_1_6_0.server;

import javax.servlet.http.*;
import static java.lang.System.out;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.javax.JavaxFileCleaner;
import org.apache.commons.fileupload2.javax.JavaxServletFileUpload;

@WebServlet("/u")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_MEMORY,
        maxFileSize = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_FILE,
        maxRequestSize = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_REQUESTBALL,
        location = "/" + TS_SUploadWebServlet.UPLOAD_DIR_NAME//means C:/bin/tomcat/home/work/Catalina/localhost/spi-xxx/upload (do create it when implementing)
)
public class TS_SUploadWebServlet extends HttpServlet {

    final public static String UPLOAD_DIR_NAME = "p";
    final public static int UPLOAD_MB_LIMIT_MEMORY = 10;
    final public static int UPLOAD_MB_LIMIT_FILE = 25;
    final public static int UPLOAD_MB_LIMIT_REQUESTBALL = 50;

    @Override
    public void doGet(HttpServletRequest rq, HttpServletResponse rs) {
        call(this, rq, rs);
    }

    @Override
    protected void doPost(HttpServletRequest rq, HttpServletResponse rs) {
        call(this, rq, rs);
    }

    public static void call(HttpServlet servlet, HttpServletRequest rq, HttpServletResponse rs) {
        try {
            //CHECK IF REQUEST IS MULTIPART
            if (!JavaxServletFileUpload.isMultipartContent(rq)) {
                println(rs, "USER_NOT_MULTIPART");
                return;
            }

            //GETING ITEMS
            //WARNING: Dont touch request before this, like execution getParameter or such!
            var fileFactory = DiskFileItemFactory.builder().get();
            var fileUpload = new JavaxServletFileUpload(fileFactory);
            var fileItems = fileUpload.parseRequest(rq);

            //GETTING PROFILE OBJECT
            var profile = fileItems.stream().filter(item -> item.isFormField()).findFirst().orElse(null);
            if (profile == null) {
                println(rs, "RESULT_UPLOAD_USER_PROFILE_NULL");
                return;
            }
            println(rs, "profile_selected");

            //GETTING PROFILE VALUE
            var profileValue = profile.getString();
            if (profileValue == null) {
                println(rs, "RESULT_UPLOAD_USER_PROFILEVALUE_NULL");
                return;
            }
            println(rs, "profileValue: " + profileValue);

            //GETING SOURCEFILE OBJECT
            var sourceFile = fileItems.stream().filter(item -> !item.isFormField()).findFirst().orElse(null);
            if (sourceFile == null) {
                println(rs, "RESULT_UPLOAD_USER_SOURCEFILE_NULL");
                return;
            }
            println(rs, "sourceFile_selected");

            //GETING SOURCEFILE NAME
            var sourceFileName = sourceFile.getName();
            if (sourceFileName == null) {
                println(rs, "RESULT_UPLOAD_USER_SOURCEFILENAME_NULL");
                return;
            }
            println(rs, "sourceFileName: " + sourceFileName);

            //COMPILING TARGET FILE
            var pathDirApp = Path.of(rq.getServletContext().getRealPath(""));
            var pathDirSave = pathDirApp.resolve(TS_SUploadWebServlet.UPLOAD_DIR_NAME);
            var pathFileTarget = pathDirSave.resolve(profileValue).resolve(sourceFileName);

            //STORE TARGET FILE
            pathFileTarget.getParent().toFile().mkdirs();
            Files.createFile(pathFileTarget);
            sourceFile.write(pathFileTarget.toFile());

            //RETURN SUCCESS FLAG
            rs.setStatus(HttpServletResponse.SC_CREATED);
            println(rs, "RESULT_UPLOAD_USER_SUCCESS");
        } catch (Exception e) {
            throwIfInterruptedException(e);
            e.printStackTrace();
        }
    }

    private static void println(HttpServletResponse rs, String msg) {
        try {
            out.println("println: " + msg);
            rs.getWriter().println(msg);
        } catch (Exception e) {
            throwIfInterruptedException(e);
        }
    }

    //---------------------------- LISTENER ----------------
    @WebListener
    public class ApacheFileCleanerCleanup extends JavaxFileCleaner {

    }
    
    //----------------------------- UTILS -----------------------
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void _throwAsUncheckedException(Throwable exception) throws T {
        throw (T) exception;
    }

    @Deprecated //only internalUse
    private static void throwAsUncheckedException(Throwable exception) {
        TS_SUploadWebServlet.<RuntimeException>_throwAsUncheckedException(exception);
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
