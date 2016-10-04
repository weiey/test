package com.yibei.xkm.vo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 日程
 */

public class ScheduleListVo extends  BaseVo implements Serializable {
    private int size;
    private String point;
    private ArrayList<ScheduleResVo> schedules;

    public ArrayList<ScheduleResVo> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<ScheduleResVo> schedules) {
        this.schedules = schedules;
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
