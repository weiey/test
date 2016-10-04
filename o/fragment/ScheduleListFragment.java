package com.yibei.xkm.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.yibei.net.RestService;
import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ScheduleAdapter;
import com.yibei.xkm.constants.CmnConstants;
import com.yibei.xkm.manager.WebService;
import com.yibei.xkm.network.Action;
import com.yibei.xkm.network.BaseCallEntry;
import com.yibei.xkm.network.BaseNetwork;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.ui.activity.ScheduleDetailActivity;
import com.yibei.xkm.ui.activity.ScheduleOtherUserListActivity;
import com.yibei.xkm.util.SPUtil;
import com.yibei.xkm.util.ToastUtils;
import com.yibei.xkm.vo.ScheduleListVo;
import com.yibei.xkm.vo.ScheduleResVo;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import wekin.com.tools.listener.DialogController;

/**
 * 日程列表
 */
public class ScheduleListFragment extends Fragment {

    private static final String TAG = ScheduleListFragment.class.getSimpleName();
    public static final String TYPE = "type";
    private ListView listView;
    private MaterialRefreshLayout refreshLayout;


    private WebService webService;
    private String userId;
    public ScheduleAdapter mAdapter;


    private DialogController dialogController;

    private int type = 0; //0 --我收到 1 --我安排
    private String point;

    private ArrayList<ScheduleResVo> listdatas;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            type = arguments.getInt(TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        listView = (ListView) view.findViewById(R.id.lv_list);
        View emptyView = view.findViewById(R.id.list_empty_view);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getNetDatas();
            }
        });
        listView.setEmptyView(emptyView);
        mAdapter = new ScheduleAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduleResVo p = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                intent.putExtra("id", p.getId());
                startActivity(intent);
            }
        });
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
        View topView = view.findViewById(R.id.ll_other_user);
        if (type == 1) {
            topView.setVisibility(View.VISIBLE);
            topView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ScheduleOtherUserListActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            topView.setVisibility(View.GONE);
        }
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNetDatas();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
        refreshLayout = null;
        mAdapter = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dialogController = (DialogController) activity;

    }


    public void doFilter(String type) {
        List<ScheduleResVo> list = new ArrayList<>();
        if (listdatas != null) {
            for (ScheduleResVo vo : listdatas) {
                if (!"全部".equals(type) && !vo.getType().equals(type)) {
                    continue;
                }
                list.add(vo);
            }
        }
        mAdapter.update(list);

    }

    private void getNetDatas() {
        if (webService == null) {
            webService = RestService.getInstance().getCommonService(getActivity(), WebService.class);
        }
        if (userId == null) {
            userId = SPUtil.get(getActivity()).getString(CmnConstants.KEY_USERID, null);
        }

        if (type == 1) {//我安排的
            getScheduleSendList();
        } else {//我收到的
            getScheduleList();
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
            getScheduleSendMoreList();
        } else {
            getScheduleMoreList();
        }

    }

    /**
     * 获取
     */
    private void getScheduleSendList() {
        Call<ScheduleListVo> call = webService.getScheduleSendList(userId, CmnConstants.PAGE_SIZE);
        BaseCallEntry<ScheduleListVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnResponseError(new Action<ScheduleListVo>() {
                    @Override
                    public void onAction(ScheduleListVo body) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                }).doOnException(new Action<Throwable>() {
                    @Override
                    public void onAction(Throwable throwable) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo vo) {
                if (getActivity().isFinishing()) return;
                if (vo == null) {
                    handleResponseError();
                    return;
                }
                point = vo.getPoint();
                if (vo.getResponseMsg().equals("1")) {
                    onLoadFiniah(vo.getSchedules());
                }

            }
        });

    }

    /**
     * 获取
     */
    private void getScheduleSendMoreList() {
        if(TextUtils.isEmpty(point)){
            handleResponseError();
            ToastUtils.toast(getActivity(), "没有更多了");
            return;
        }
        Call<ScheduleListVo> call = webService.getScheduleSendMoreList(userId, point, CmnConstants.PAGE_SIZE);
        BaseCallEntry<ScheduleListVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnResponseError(new Action<ScheduleListVo>() {
                    @Override
                    public void onAction(ScheduleListVo body) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                }).doOnException(new Action<Throwable>() {
                    @Override
                    public void onAction(Throwable throwable) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo vo) {
                if (getActivity().isFinishing()) return;
                if (vo == null) {
                    handleResponseError();
                    return;
                }
                point = vo.getPoint();
                finishRefreshLoadMore(vo.getSchedules());

            }
        });

    }

    /**
     * 获取
     */
    private void getScheduleList() {
        Call<ScheduleListVo> call = webService.getScheduleList(userId, CmnConstants.PAGE_SIZE);
        BaseCallEntry<ScheduleListVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnResponseError(new Action<ScheduleListVo>() {
                    @Override
                    public void onAction(ScheduleListVo body) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                }).doOnException(new Action<Throwable>() {
                    @Override
                    public void onAction(Throwable throwable) {
                        if (getActivity().isFinishing()) return;
                        handleResponseError();
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo vo) {
                if (getActivity().isFinishing()) return;
                if (vo == null) {
                    handleResponseError();
                    return;
                }
                point = vo.getPoint();
                if (vo.getResponseMsg().equals("1")) {
                    onLoadFiniah(vo.getSchedules());
                }

            }
        });

    }

    /**
     * 获取
     */
    private void getScheduleMoreList() {

        if(TextUtils.isEmpty(point)){
            handleResponseError();
            ToastUtils.toast(getActivity(), "没有更多了");
            return;
        }
        Call<ScheduleListVo> call = webService.getScheduleMoreList(userId, point, CmnConstants.PAGE_SIZE);
        BaseCallEntry<ScheduleListVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnError(new Runnable() {
                    @Override
                    public void run() {
                        handleResponseError();
                    }
                })
                .build();
        network.callNetwork(callEntry, new ResponseCallback<ScheduleListVo>() {
            @Override
            public void onResponse(ScheduleListVo vo) {
                if (getActivity().isFinishing()) return;
                if (vo == null) {
                    handleResponseError();
                    return;
                }
                point = vo.getPoint();
                finishRefreshLoadMore(vo.getSchedules());

            }
        });

    }


    private void onLoadFiniah(ArrayList<ScheduleResVo> list) {
        listdatas = list;
        mAdapter.update(listdatas);
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    private void finishRefreshLoadMore(ArrayList<ScheduleResVo> list) {
        if (list != null) {
            //mAdapter.add(list);
            listdatas.addAll(list);
            mAdapter.update(listdatas);
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
    }
}
