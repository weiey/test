package com.yibei.xkm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yibei.xkm.entity.SchUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/11.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkmateVo extends BaseVo {
    private int size;
    private String point;

    private ArrayList<SchUserInfo> doctors;

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

    public ArrayList<SchUserInfo> getDoctors() {
        return doctors;
    }

    public void setDoctors(ArrayList<SchUserInfo> doctors) {
        this.doctors = doctors;
    }
}
