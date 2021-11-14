package com.feng.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feng.pojo.Information;
import com.feng.pojo.Type;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class NetworkUtil {
    private final static String LOCAL_IP = "127.0.0.1";


    public static void heartCheck(ServletContext context) {
        while (true) {
            List<Information> heartList = (List<Information>) context.getAttribute("heartList");
            List<Information> delHeartList = Collections.synchronizedList(new ArrayList<Information>());
            try{
                for (int i = 0; i < heartList.size(); i ++){
                    Information info = heartList.get(i);
                    Date infoDate = info.getDate();
                    long timeDiff = (new Date()).getTime() - infoDate.getTime();
                    long secondDiff = timeDiff / 1000;
                    if (secondDiff > 10) {
                        delHeartList.add(info);
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            for (Information info : delHeartList) {
                heartList.remove(info);
            }
            context.setAttribute("heartList", heartList);

        }
    }

    public static List<NetworkInterface> getNetworkInterfaces() throws Exception {
        List<NetworkInterface> localIPlist = Collections.synchronizedList(new ArrayList<NetworkInterface>());
        Enumeration<NetworkInterface> interfs =
                NetworkInterface.getNetworkInterfaces();
        if (interfs == null) {
            return null;
        }
        while (interfs.hasMoreElements()) {
            NetworkInterface interf = interfs.nextElement();
            Enumeration<InetAddress> addres = interf.getInetAddresses();
            while (addres.hasMoreElements()) {
                InetAddress in = addres.nextElement();
                if (in instanceof Inet4Address) {
                    if (!LOCAL_IP.equals(in.getHostAddress())) {
                        localIPlist.add(interf);
                    }
                }
            }
        }
        return localIPlist;
    }

    public static void multicastServer(ServletContext context) throws Exception {
        final MulticastSocket msr = new MulticastSocket(39876);
        msr.setTimeToLive(255);
        InetAddress receiveAddress = InetAddress.getByName("239.0.0.9");
        List<NetworkInterface> addressList = getNetworkInterfaces();
        int port = 10009;
        for (NetworkInterface networkInterface : addressList) {
            InetSocketAddress inetSocketAddress = new
                    InetSocketAddress(receiveAddress, port);
            msr.joinGroup(inetSocketAddress, networkInterface);
            port++;
        }
        byte[] buffer = new byte[515];
        System.out.println("receive start , time : " + new Date() + ")");

        while (true) {


            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            try {
                msr.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }


            String s = new String(dp.getData(), 0, dp.getLength());
            if (s.equals("StartSend")){
                boolean isTimeOut = false;
                StringBuilder resultBuilder = new StringBuilder();
                Date lastReceiveTime = new Date();
                int count = 0;
                while (true){
                    buffer = new byte[515];
                    dp = new DatagramPacket(buffer,buffer.length);
                    msr.receive(dp);
                    Date nowReceiveTime = new Date();

                    long timeDiff = nowReceiveTime.getTime() - lastReceiveTime.getTime();
                    long secondDiff = timeDiff / 1000;
                    if (secondDiff>10){
                        isTimeOut = true;
                        System.out.println("translation failed!!!!!!!!!!!!!!!!!!!!!");
                        break;
                    }else{
                        lastReceiveTime = nowReceiveTime;
                    }



                    String tempData = new String(dp.getData(),0,dp.getLength());
                    if (tempData.equals("EndSend")){
                        break;
                    }
                    String pattern = "^\\{\"@type\":\"com.feng.pojo.Information\",\"avatarId\":.*,\"content\":\".*\",\"date\":.*,\"name\":\".*\",\"order\":.*,\"suffix\":\".*\",\"type\":\".*\"\\}$";
                    if(Pattern.matches(pattern,tempData)){
                        dealData(context,tempData);
                    }else {
                        System.out.println(++count);
                        resultBuilder.append(tempData);
                    }
                }dealData(context,resultBuilder.toString());


            }else{
                dealData(context,s);
            }

        }
    }
    public static void dealData(ServletContext context,String s) throws Exception{
        try {
/*            s = s.replace("\n","").replace("\r","");
            if (s.contains("IMAGE")){
                String path = context.getRealPath("/upload");
                try (OutputStream output = new FileOutputStream(path + "/" + "aaaaaaa.txt")) {
                    output.write(s.getBytes(StandardCharsets.UTF_8), 0, s.getBytes(StandardCharsets.UTF_8).length);
                }
            }
            System.out.println("===");
            System.out.println(s);
            System.out.println("===");*/
            Information info = (Information) JSON.parse(s);
            String content = info.getContent();
            Type type = info.getType();
            if (content.equals("") && type == Type.HEARTBEAT) {
                List<Information> heartList = (List<Information>) context.getAttribute("heartList");
                for (Information information : heartList) {
                    if (information.getName().equals(info.getName())) {
                        heartList.remove(information);
                        break;
                    }
                }
                heartList.add(info);
                context.setAttribute("heartList", heartList);
            } else if (type == Type.TEXT || type == Type.TAP || type == Type.VIDEOCHAT) {
                List<String> infoList = (List<String>) context.getAttribute("infoList");
                //System.out.println("base64-content:"+content);
                content = new String(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)),"utf-8");
                //System.out.println("content:"+content);
                info.setContent(content);
                int order = (Integer) context.getAttribute("order");
                order++;
                System.out.println("order:"+order);
                info.setOrder(order);
                context.setAttribute("order",order);
                infoList.add(JSON.toJSONString(info, SerializerFeature.WriteClassName));
                context.setAttribute("infoList", infoList);
            } else {
                List<String> infoList = (List<String>) context.getAttribute("infoList");
                //System.out.println("content:" + content);
                byte[] byteContent;
                byteContent = Base64.getDecoder().decode(content.replace("\r","").replace("\n","").getBytes(StandardCharsets.UTF_8));
                //content = new String(Base64.getDecoder().decode(content.getBytes()));
                String suffix = info.getSuffix();
                String trueFileName = CryptoUtil.getMD5Str((new Date()) + "feng") + "." + suffix;
                String savePath = context.getRealPath("/upload");
                try (OutputStream output = new FileOutputStream(savePath + File.separator + trueFileName)) {
                    output.write(byteContent, 0, byteContent.length);
                }
                info.setContent("/upload" + "/" + trueFileName);
                int order = (Integer) context.getAttribute("order");
                order++;
                System.out.println("order:"+order);
                info.setOrder(order);
                System.out.println(JSON.toJSONString(info,SerializerFeature.WriteClassName));
                infoList.add(JSON.toJSONString(info,SerializerFeature.WriteClassName));
                context.setAttribute("infoList", infoList);
            }
            System.out.println("the data received : \n" + s);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void multicastClient(String jsonData) throws Exception {
        InetAddress group = InetAddress.getByName("239.0.0.9");
        int port = 39876;// 组播端口
        MulticastSocket mss = null;// 创建组播套接字
        mss = new MulticastSocket(port);
        mss.joinGroup(group);
        System.out.println("send start , time : " + new Date() + ")");
        String message = jsonData;


        System.out.println("length:!!!!!!!!!"+message.getBytes(StandardCharsets.UTF_8).length);

        int messageLength = message.getBytes(StandardCharsets.UTF_8).length;
        if (messageLength<= 515){
            byte[] buffer = message.getBytes();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                    group, port);
            mss.send(dp);
            System.out.println("send  " + group + ":" + port);
        }else{

            int sendCount = messageLength/515;

            String startStr = "StartSend";
            byte[] buffer1 = startStr.getBytes();
            DatagramPacket dp = new DatagramPacket(buffer1, buffer1.length,
                    group, port);
            mss.send(dp);
            Thread.sleep(5);
            for(int i = 0; i < sendCount ; i++){
                System.out.println("send!!!!!!!!!!!!!!!!!!!!!!!!!!"+"  "+i+"     ");
                String sendStr = message.substring(i*515,(i+1)*515);
                buffer1 = sendStr.getBytes();
                dp = new DatagramPacket(buffer1, buffer1.length,
                        group, port);
                mss.send(dp);
                Thread.sleep(5);
            }
            String sendStr = message.substring(sendCount*515);
            buffer1 = sendStr.getBytes(StandardCharsets.UTF_8);
            dp = new DatagramPacket(buffer1, buffer1.length,
                    group, port);
            mss.send(dp);
            Thread.sleep(5);

            String endStr = "EndSend";
            buffer1 = endStr.getBytes(StandardCharsets.UTF_8);
            dp = new DatagramPacket(buffer1, buffer1.length,
                    group, port);
            mss.send(dp);
            Thread.sleep(5);
        }
    }
}
