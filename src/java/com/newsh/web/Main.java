/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newsh.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import com.newsh.TemplateEngine;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.http.Cookie;

/**
 *
 * @author fgss
 */
@MultipartConfig
@WebServlet(name = "MainServlet", urlPatterns = {"/upload", "/status", "/get"})
public class Main extends HttpServlet {

    TemplateEngine tempEngine;

    public Main() {
        tempEngine = new TemplateEngine();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/get")) {
            String filename = request.getParameter("file");
            response.setHeader("Content-Disposition", "attachment;filename=".concat(filename.concat(".zip")));
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream("/tmp/" + filename + ".zip");
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.flush();

        } else {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("group_param")) {
                    if (tempEngine.groupName.equals(c.getValue()) && tempEngine.isReady()) {
                        response.getWriter().print("<p>Your job is done, link to download is <a href=\"/WebNewSupp/get?file=" + c.getValue() + "\">here</a></p>");
                        return;
                    } else {
                        response.getWriter().print("<p>Your job is running now</p>");
                        response.getWriter().print("<br/>");
                        response.getWriter().print("<p>Progress is "+ String.valueOf(tempEngine.getProgress()) +"/"+ String.valueOf(tempEngine.getMaxProgress()) +"</p>");
                        return;
                    }
                }
            }

            response.getWriter().print("<p>You don\'t have running job</p>");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if(!request.getServletPath().equals("/upload"))
        {
            return;
        }
        
        String nameOfDiplom = request.getParameter("nameofdip");
        String st = request.getParameter("st");
        String group = request.getParameter("group");
        Cookie cookie = new Cookie("group_param", group);
        response.addCookie(cookie);

        Part filePart = request.getPart("xmlfile");
        String fileNameXML = filePart.getSubmittedFileName();
        File fXML = File.createTempFile(group, fileNameXML);
        Files.copy(filePart.getInputStream(), fXML.toPath(), StandardCopyOption.REPLACE_EXISTING);

        filePart = request.getPart("wordfile");
        String fileNameWord = filePart.getSubmittedFileName();
        File fWord = File.createTempFile(group, fileNameWord);
        Files.copy(filePart.getInputStream(), fWord.toPath(), StandardCopyOption.REPLACE_EXISTING);

        tempEngine.work(fWord.getAbsolutePath(), fXML.getAbsolutePath(), Files.createTempDirectory(group).toString(), group, nameOfDiplom, Boolean.parseBoolean(st));

        try (PrintWriter pw = response.getWriter()) {
            pw.print("<!DOCTYPE html><html><body><script>window.location.replace(\"/WebNewSupp/result.html\");</script></body></html>");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "com.newsh.web servlet";
    }

}
