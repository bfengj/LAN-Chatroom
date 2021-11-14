package com.feng.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feng.pojo.HeartResult;
import com.feng.pojo.Information;
import com.feng.pojo.Person;
import com.feng.pojo.Type;
import com.feng.util.NetworkUtil;
import lombok.SneakyThrows;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@WebServlet("/heart")
public class HeartBeatServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @SneakyThrows
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        int avatarId = Integer.parseInt(req.getParameter("avatarId"));
        Information information = new Information(0,name, new Date(), Type.HEARTBEAT, avatarId, "", "");
        String jsonData = JSON.toJSONString(information, SerializerFeature.WriteClassName);
        NetworkUtil.multicastClient(jsonData);


        ServletContext context = this.getServletContext();
        List<Information> heartList = (List<Information>) context.getAttribute("heartList");
        heartList = Collections.synchronizedList(heartList);
        int count = heartList.size();
        List<Person> list = Collections.synchronizedList(new ArrayList<Person>());

        for(Information info : heartList) {
            list.add(new Person(info.getName(),info.getAvatarId()));
        }
        HeartResult result = new HeartResult(Type.HEARTBEAT, count, list);
        jsonData = JSON.toJSONString(result);
        resp.getWriter().print(jsonData);
    }

}
