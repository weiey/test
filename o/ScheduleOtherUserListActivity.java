package com.yibei.xkm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.yibei.net.RestService;
import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ScheduleUserGroupAdapter;
import com.yibei.xkm.constants.CmnConstants;
import com.yibei.xkm.entity.Creator;
import com.yibei.xkm.entity.SchUserInfo;
import com.yibei.xkm.manager.WebService;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.util.SPUtil;
import com.yibei.xkm.util.ToastUtils;
import com.yibei.xkm.vo.ParticipantVo;
import com.yibei.xkm.vo.ReceivedUserListVo;

import java.util.ArrayList;

import retrofit.Call;

/**
 *
 */
public class ScheduleOtherUserListActivity extends XkmBasicTemplateActivity implements View.OnClickListener{

    private ListView listView;
    private MaterialRefreshLayout refreshLayout;
    private ScheduleUserGroupAdapter scheduleUserGroupAdapter;
    private String point;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_user_list);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        listView = $(R.id.lv_list);
        refreshLayout =$(R.id.refresh_layout);
        View emptyView =$(R.id.list_empty_view);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 getNetDatas();
            }
        });
        listView.setEmptyView(emptyView);
        scheduleUserGroupAdapter = new ScheduleUserGroupAdapter(this);
        listView.setAdapter(scheduleUserGroupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Creator p = scheduleUserGroupAdapter.getItem(position);
                Intent intent = new Intent(ScheduleOtherUserListActivity.this,ScheduleOtherListActivity.class);
                intent.putExtra("otherUserId",p.getId());
                startActivity(intent);
            }
        });
        refreshLayout.setLoadMore(true);
        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                getNetDatas();
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                getNetDatasMore();
            }
        });
        getNetDatas();
    }

    /**
     * 获取
     */
    private void getNetDatas() {
        Call<ReceivedUserListVo> call = getWebService().getScheduleOtherUserList(getCurrentUserId(),CmnConstants.PAGE_SIZE);
        requestNetwork(call, false, new ResponseCallback<ReceivedUserListVo>() {
            @Override
            public void onResponse(ReceivedUserListVo body) {
                if (body ==null){
                    handleResponseError();
                    return;
                }
                if(body.getResponseMsg().equals("1")) {
                    point = body.getPoint();
                    onLoadFinish(body.getUsers());
                }
            }
        });
    }
    private void getNetDatasMore() {
        if(TextUtils.isEmpty(point)){
            handleResponseError();
            ToastUtils.toast(this, "没有更多了");
            return;
        }
        Call<ReceivedUserListVo> call = getWebService().getScheduleOtherUserMoreList(getCurrentUserId(),point,CmnConstants.PAGE_SIZE);
        requestNetwork(call, false, new ResponseCallback<ReceivedUserListVo>() {
            @Override
            public void onResponse(ReceivedUserListVo body) {
                point = body.getPoint();
                finishRefreshLoadMore(body.getUsers());
            }
        });
    }

    private void onLoadFinish(ArrayList<Creator> list){
        refreshLayout.finishRefresh();
        scheduleUserGroupAdapter.update(list);
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    private void finishRefreshLoadMore(ArrayList<Creator> list) {
        if(list != null){
            scheduleUserGroupAdapter.add(list);
        }
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }
    @Override
    protected void handleResponseError() {
        super.handleResponseError();
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    @Override
    protected void handleHttpError(Throwable e) {
        super.handleHttpError(e);
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideInputMethod();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            finish();
        }
    }


}
