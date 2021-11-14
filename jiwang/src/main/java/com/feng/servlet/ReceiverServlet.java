package com.feng.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.feng.pojo.Information;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet("/receive")
public class ReceiverServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int order = Integer.parseInt(req.getParameter("order"));
        ServletContext context = this.getServletContext();
        List<String> infoList = (List<String>) context.getAttribute("infoList");
        List<String> temps = new ArrayList<String>();
        for (String i:infoList){
            Information info = (Information) JSON.parse(i);
            if (info.getOrder()>order){
                //i = i.replaceAll("\\w{32}\\.png","1.png");
                //i = i.replaceAll("\\w{32}\\.mp3","1.mp3");
                //i = i.replaceAll("\\w{32}\\.mp4","1.mp4");
                //i = i.replaceAll("\\w{32}\\.txt","1.txt");
                temps.add(i);
            }
        }
        resp.getWriter().print(temps);

    }
}
