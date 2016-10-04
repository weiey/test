package com.yibei.xkm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yibei.xkm.entity.Creator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/11.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceivedUserListVo extends BaseVo {
    private int size;
    private String point;
    private ArrayList<Creator> users;

    public ReceivedUserListVo() {
    }

    public ArrayList<Creator> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Creator> users) {
        this.users = users;
    }


    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
