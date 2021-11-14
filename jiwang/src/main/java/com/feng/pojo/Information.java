package com.feng.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Information {
    private int order;
    private String name;
    private Date date;
    private Type type;
    private int avatarId;
    private String content;
    private String suffix;
}
