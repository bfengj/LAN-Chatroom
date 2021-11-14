package com.feng.filter;

import com.alibaba.fastjson.parser.ParserConfig;
import com.feng.pojo.Information;
import com.feng.util.NetworkUtil;
import lombok.SneakyThrows;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebFilter("/*")
public class ReceiverFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ParserConfig.getGlobalInstance().addAccept("com.feng.pojo.Information");
        ParserConfig.getGlobalInstance().addAccept("com.feng.pojo.Type");
        ParserConfig.getGlobalInstance().addAccept("com.feng.pojo.HeartResult");
        ServletContext context = filterConfig.getServletContext();

        List<String> infoList = Collections.synchronizedList(new ArrayList<String>());
        List<Information> heartList = Collections.synchronizedList(new ArrayList<Information>());

        context.setAttribute("infoList",infoList);
        context.setAttribute("heartList",heartList);
        context.setAttribute("order",0);
        Thread t1 = new Thread(){
            @SneakyThrows
            @Override
            public void run() {
                NetworkUtil.multicastServer(context);
            }
        };
        Thread t2 = new Thread(){
            @SneakyThrows
            @Override
            public void run() {
                NetworkUtil.heartCheck(context);
            }
        };
        t1.start();
        t2.start();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
