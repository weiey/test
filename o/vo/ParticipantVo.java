package com.yibei.xkm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yibei.xkm.entity.SchUserInfo;

import java.util.ArrayList;



public class ParticipantVo extends BaseVo {
    private int size;
    private ArrayList<SchUserInfo> users;
    private String point;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArrayList<SchUserInfo> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<SchUserInfo> users) {
        this.users = users;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
