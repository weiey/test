package com.yibei.xkm.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.yibei.net.RestService;
import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ShowPaticipantAdapter;
import com.yibei.xkm.constants.CmnConstants;
import com.yibei.xkm.entity.SchUserInfo;
import com.yibei.xkm.manager.WebService;
import com.yibei.xkm.network.Action;
import com.yibei.xkm.network.BaseCallEntry;
import com.yibei.xkm.network.BaseNetwork;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.util.SPUtil;
import com.yibei.xkm.util.ToastUtils;
import com.yibei.xkm.vo.ParticipantVo;
import com.yibei.xkm.vo.ScheduleListVo;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import wekin.com.tools.listener.DialogController;

/**
 * 选择我的患者.
 */
public class ShowParticipantFragment extends Fragment {

    private static final String TAG = ShowParticipantFragment.class.getSimpleName();
    public static final String TYPE = "type";
    private ListView listView;
    private DialogController dialogController;
    private MaterialRefreshLayout refreshLayout;
    public ShowPaticipantAdapter mAdapter;
    private WebService webService;
    private int type = 0; //0 --同事 1 --患者
    private String userId;
    private List<String> listdata;
    private String id;//日程id
    private String point;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt(TYPE, 0);
            id = bundle.getString("id");
            listdata = bundle.getStringArrayList("listdata");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_participant, container, false);
        listView = (ListView) view.findViewById(R.id.lv_list);
        View emptyView = view.findViewById(R.id.list_empty_view);
        listView.setEmptyView(emptyView);
        refreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.refresh_layout);
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
        mAdapter = new ShowPaticipantAdapter(getActivity());
        listView.setAdapter(mAdapter);
//        mAdapter.update(listdata);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
        mAdapter = null;
        refreshLayout = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNetDatas();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dialogController = (DialogController) activity;

    }

    private void getNetDatas() {
        if (webService == null) {
            webService = RestService.getInstance().getCommonService(getActivity(), WebService.class);
        }
        if (userId == null) {
            userId = SPUtil.get(getActivity()).getString(CmnConstants.KEY_USERID, null);
        }
        if (type == 1) {
            getScheduleDetailPaticipant(0);
        } else {
            getScheduleDetailPaticipant(1);
        }
    }

    private void getNetDatasMore() {
        if (webService == null) {
            webService = RestService.getInstance().getCommonService(getActivity(), WebService.class);
        }
        if (userId == null) {
            userId = SPUtil.get(getActivity()).getString(CmnConstants.KEY_USERID, null);
        }
        if (type == 1) {
            getScheduleDetailPaticipantMore(0);
        } else {
            getScheduleDetailPaticipantMore(1);
        }
    }

    /**
     * 获取
     */
    private void getScheduleDetailPaticipant(int status) {
        Call<ParticipantVo> call = webService.getScheduleDetailPaticipant(id, CmnConstants.PAGE_SIZE, status);
        BaseCallEntry<ParticipantVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, true);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnError(new Runnable() {
                    @Override
                    public void run() {
                        handleResponseError();
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ParticipantVo>() {
            @Override
            public void onResponse(ParticipantVo body) {
                point = body.getPoint();
                onLoadFiniah(body.getUsers());
            }
        });
    }

    /**
     * 获取更多
     */
    private void getScheduleDetailPaticipantMore(int status) {

        if(TextUtils.isEmpty(point)){
            handleResponseError();
            ToastUtils.toast(getActivity(), "没有更多了");
            return;
        }
        Call<ParticipantVo> call = webService.getScheduleDetailPaticipantMore(id, point, CmnConstants.PAGE_SIZE, status);
        BaseCallEntry<ParticipantVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, true);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnResponseError(new Action<ScheduleListVo>() {
                    @Override
                    public void onAction(ScheduleListVo body) {
                        //if (getActivity().isFinishing()) return;
                        handleResponseError();
                        if (getActivity().isFinishing()) return;
                    }
                }).doOnException(new Action<Throwable>() {
                    @Override
                    public void onAction(Throwable throwable) {
                        // if (getActivity().isFinishing()) return;
                        handleResponseError();
                        if (getActivity().isFinishing()) return;
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ParticipantVo>() {
            @Override
            public void onResponse(ParticipantVo body) {

                if (body.getResponseMsg().equals("1")) {
                    point = body.getPoint();
                    finishRefreshLoadMore(body.getUsers());
                }
            }
        });
    }

    private void onLoadFiniah(ArrayList<SchUserInfo> list) {
        if (list != null) {
            mAdapter.update(list);
        }
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    private void finishRefreshLoadMore(ArrayList<SchUserInfo> list) {
        if (list != null) {
            mAdapter.add(list);
        }
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    private void handleResponseError() {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh();
            refreshLayout.finishRefreshLoadMore();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dialogController = null;
        webService = null;
    }
}
