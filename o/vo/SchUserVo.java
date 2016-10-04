package com.yibei.xkm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yibei.xkm.entity.SchUserInfo;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchUserVo  extends BaseVo{
    private int size;
    private String point;
    private ArrayList<SchUserInfo> users;


    public ArrayList<SchUserInfo> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<SchUserInfo> users) {
        this.users = users;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
