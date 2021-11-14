package com.feng.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feng.pojo.Information;
import com.feng.pojo.ResponseData;
import com.feng.pojo.Type;
import com.feng.util.CryptoUtil;
import com.feng.util.NetworkUtil;
import com.feng.util.StreamUtil;
import lombok.SneakyThrows;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

@WebServlet("/send")
@MultipartConfig
public class SenderServlet extends HttpServlet {

    private String[] blackSuffix = new String[]{"jsp","asp","aspx","war","jar","php","go"};


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @SneakyThrows
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Part namePart = req.getPart("name");
        Part typeNamePart = req.getPart("type");
        Part contentPart = req.getPart("content");
        Part avatarIdPart = req.getPart("avatarid");

        InputStream nameStream = namePart.getInputStream();
        InputStream typeNameStream = typeNamePart.getInputStream();
        InputStream contentStream = contentPart.getInputStream();
        InputStream avatarIdStream = avatarIdPart.getInputStream();

        ByteArrayOutputStream nameOutputStream = StreamUtil.getByteArrayOutputStream(nameStream);
        ByteArrayOutputStream typeNameOutputStream = StreamUtil.getByteArrayOutputStream(typeNameStream);
        ByteArrayOutputStream contentOutputStream = StreamUtil.getByteArrayOutputStream(contentStream);
        ByteArrayOutputStream avatarIdOutputStream = StreamUtil.getByteArrayOutputStream(avatarIdStream);

        Date date = new Date();
        Type type = Type.valueOf(typeNameOutputStream.toString().toUpperCase(Locale.ROOT));

        Information information;
        //注意别忘了close流
        if (type==Type.TEXT ||type == Type.TAP|| type == Type.HEARTBEAT||type == Type.VIDEOCHAT) {
            //information = new Information(nameOutputStream.toString(), date, type, Integer.parseInt(avatarIdOutputStream.toString()) ,new String(Base64.getEncoder().encode(contentOutputStream.toByteArray())),"");
            information = new Information(0,nameOutputStream.toString(), date, type, Integer.parseInt(avatarIdOutputStream.toString()) ,new String(Base64.getEncoder().encode(contentOutputStream.toString("utf-8").getBytes(StandardCharsets.UTF_8))),"");

        }else {
            String header = contentPart.getHeader("content-disposition");
            String fileName = getFileName(header);
            String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
            if(Arrays.asList(blackSuffix).contains(suffix)){
                ResponseData responseData = new ResponseData("error", "forbidden suffix", "");
                resp.getWriter().print(JSON.toJSONString(responseData));
                return;
            }else{
                information = new Information(0,nameOutputStream.toString(),date,type, Integer.parseInt(avatarIdOutputStream.toString()) ,new String(Base64.getEncoder().encode(contentOutputStream.toByteArray())),suffix);
            }
        }
        String jsonData = JSON.toJSONString(information, SerializerFeature.WriteClassName);
        System.out.println(jsonData);
        NetworkUtil.multicastClient(jsonData);
        resp.getWriter().print("{type:\"success\"}");
    }

    public String getFileName(String header) {
        String[] split = header.split(";");
        split = split[2].split("=");
        String fileName = split[1].replaceAll("\"","");
        return fileName;
    }
}
