package com.yibei.xkm.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ScheduleAdapter;
import com.yibei.xkm.constants.BroadcastAction;
import com.yibei.xkm.constants.CmnConstants;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.util.ToastUtils;
import com.yibei.xkm.vo.ScheduleListVo;
import com.yibei.xkm.vo.ScheduleResVo;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;

/**
 * 我查看我发给其他人的日程 单个人的日程
 */
public class ScheduleOtherListActivity extends XkmBasicTemplateActivity implements View.OnClickListener {

    private ListView listView;
    private MaterialRefreshLayout refreshLayout;
    private ScheduleAdapter mAdapter;

    String otherUserId;

    private String point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        otherUserId = getIntent().getStringExtra("otherUserId");
        setContentView(R.layout.activity_schedule_user_list);
        initView();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_SCHEDULE_DELETE);
        filter.addAction(BroadcastAction.ACTION_SCHEDULE_UPDATE_STATUS);
        registerReceiver(changeReceiver, filter);
    }

    /**
     * 初始化
     */
    private void initView() {
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        listView = $(R.id.lv_list);
        refreshLayout = $(R.id.refresh_layout);
        View emptyView = $(R.id.list_empty_view);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNetDatas();
            }
        });
        listView.setEmptyView(emptyView);
        mAdapter = new ScheduleAdapter(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduleResVo p = mAdapter.getItem(position);
                Intent intent = new Intent(ScheduleOtherListActivity.this, ScheduleDetailActivity.class);
                intent.putExtra("id", p.getId());
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
        Call<ScheduleListVo> call = getWebService().getOtherUserScheduleList(getCurrentUserId(), otherUserId, CmnConstants.PAGE_SIZE);
        requestNetwork(call, false, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo body) {
                if (body.getResponseMsg().equals("1")) {
                    point = body.getPoint();
                    onLoadFinish(body.getSchedules());
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
        Call<ScheduleListVo> call = getWebService().getOtherUserScheduleMoreList(getCurrentUserId(), otherUserId, point, CmnConstants.PAGE_SIZE);
        requestNetwork(call, false, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo body) {

                point = body.getPoint();
                finishRefreshLoadMore(body.getSchedules());
            }
        });
    }

    private void onLoadFinish(ArrayList<ScheduleResVo> list) {
        refreshLayout.finishRefresh();
        if (list == null || list.isEmpty()) return;
        mAdapter.update(list);
    }

    private void finishRefreshLoadMore(ArrayList<ScheduleResVo> list) {
        if (list != null) {
            mAdapter.add(list);
        }
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    @Override
    protected void handleResponseError() {
        super.handleResponseError();
        refreshLayout.finishRefresh();
    }

    @Override
    protected void handleHttpError(Throwable e) {
        super.handleHttpError(e);
        refreshLayout.finishRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeReceiver);
        hideInputMethod();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            onBackPressed();
        } else {

        }
    }

    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_SCHEDULE_DELETE)) {
                getNetDatas();

            }
            if (intent.getAction().equals(BroadcastAction.ACTION_SCHEDULE_UPDATE_STATUS)) {

                String id = intent.getStringExtra("id");
                int status = intent.getIntExtra("status", 0);
                if (mAdapter != null) {
                    List<ScheduleResVo> list = mAdapter.getModels();
                    if (list != null) {
                        for (ScheduleResVo m : list) {
                            if (m.getId().equals(id)) {
                                m.setStatus(status);
                            }
                        }

                    }
                    mAdapter.notifyDataSetChanged();

                }
            }
        }
    };


}
