package com.tugalsan.blg.org_apache_commons_fileupload_1_6_0.server;

import com.tugalsan.blg.org_apache_commons_fileupload_1_6_0.client.*;
import javax.servlet.http.*;
import java.io.File;
import static java.lang.System.out;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/u")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_MEMORY,
        maxFileSize = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_FILE,
        maxRequestSize = 1024 * 1024 * TS_SUploadWebServlet.UPLOAD_MB_LIMIT_REQUESTBALL,
        location = "/" + TGS_SUploadUtils.LOC_NAME//means C:/bin/tomcat/home/work/Catalina/localhost/spi-xxx/upload (do create it)
)
public class TS_SUploadWebServlet extends HttpServlet {

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
            var appPath = rq.getServletContext().getRealPath("");// constructs path of the directory to save uploaded file
            var savePath = appPath + File.separator + TGS_SUploadUtils.LOC_NAME;// creates the save directory if it does not exists
            var fileSaveDir = new File(savePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdir();
            }

            if (!ServletFileUpload.isMultipartContent(rq)) {
                println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_NOT_MULTIPART());
                return;
            }

            //GETING ITEMS
            //WARNING: Dont touch request before this, like execution getParameter or such!
            var items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(rq);

            //GETTING PROFILE
            var profile = items.stream().filter(item -> item.isFormField()).findFirst().orElse(null);
            if (profile == null) {
                println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_PROFILE_NULL());
                return;
            }
            println(rs, "profile_selected");

            var profileValue = profile.getString();
            if (profileValue == null) {
                println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_PROFILEVALUE_NULL());
                return;
            }
            println(rs, "profileValue: " + profileValue);

            //GETING SOURCEFILE
            var sourceFile = items.stream().filter(item -> !item.isFormField()).findFirst().orElse(null);
            if (sourceFile == null) {
                println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_SOURCEFILE_NULL());
                return;
            }
            println(rs, "sourceFile_selected");

            var sourceFileName = sourceFile.getName();
            if (sourceFileName == null) {
                println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_SOURCEFILENAME_NULL());
                return;
            }
            println(rs, "sourceFileName: " + sourceFileName);

            //COMPILING TARGET FILE
            var targetFile = Path.of(profileValue);

            //CREATE DIRECTORIES
            targetFile.getParent().toFile().mkdirs();
            Files.createFile(targetFile);
            sourceFile.write(targetFile.toFile());
            rs.setStatus(HttpServletResponse.SC_CREATED);
            println(rs, TGS_SUploadUtils.RESULT_UPLOAD_USER_SUCCESS());
        } catch (Exception e) {
            TGS_FuncUtils.throwIfInterruptedException(e);
            e.printStackTrace();
        }
    }

    private static void println(HttpServletResponse rs, String msg) {
        try {
            out.println("println: " + msg);
            rs.getWriter().println(msg);
        } catch (Exception e) {
            TGS_FuncUtils.throwIfInterruptedException(e);
        }
    }
}
