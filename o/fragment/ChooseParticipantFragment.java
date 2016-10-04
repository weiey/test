package com.yibei.xkm.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.yibei.net.RestService;
import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ChoosePaticipantAdapter;
import com.yibei.xkm.adapter.RecentPaticipantAdapter;
import com.yibei.xkm.constants.CmnConstants;
import com.yibei.xkm.entity.SchUserInfo;
import com.yibei.xkm.manager.WebService;
import com.yibei.xkm.network.Action;
import com.yibei.xkm.network.BaseCallEntry;
import com.yibei.xkm.network.BaseNetwork;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.util.CommonUtil;
import com.yibei.xkm.util.SPUtil;
import com.yibei.xkm.util.ToastUtils;
import com.yibei.xkm.vo.MembersVo;
import com.yibei.xkm.vo.SchUserVo;
import com.yibei.xkm.vo.WorkmateVo;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import wekin.com.tools.image.widget.CircleImageView;
import wekin.com.tools.listener.DialogController;

/**
 * 选择我的患者.
 */
public class ChooseParticipantFragment extends Fragment {

    private static final String TAG = ChooseParticipantFragment.class.getSimpleName();
    public static final String TYPE = "type";
    private ListView listView;
    private MaterialRefreshLayout refreshLayout;

    private View content_rl;
    private View ll_recent_user;
    private TextView check_box_tv;
    private GridView mGv;
    private RecentPaticipantAdapter recentPaticipantAdapter;
    private WebService webService;
    private String userId;
    private ChoosePaticipantAdapter choosePaticipantAdapter;
    private SchUserInfo participantInfo = new SchUserInfo();

    private int mSelectionType = 0;


    private DialogController dialogController;

    private Bundle bundle;
    private int type = 0; //0 --同事 1 --患者

    private String icon;
    private int user_type;
    private String user_name;
    private List<String> listdata;
    private String point;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = SPUtil.get(getActivity()).getString(CmnConstants.KEY_USERID, "");
        bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt(TYPE, 0);
            listdata = bundle.getStringArrayList("listdata");
//            listdata = (ArrayList<String>) bundle.getSerializable("listdata");
        }
        String[] message = SPUtil.get(getActivity()).getMainshowMessage();
        user_name = message[0];

        icon = message[1];
        user_type = Integer.parseInt(message[3]);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_participant, container, false);
        listView = (ListView) view.findViewById(R.id.lv_list);
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view, null);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNetDatas();
            }
        });
        listView.setEmptyView(emptyView);
        refreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setLoadMore(true);
        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                getNetDatas();
                if (type == 0) {
                    getRecentPaticipant();
                }
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                getNetDatasMore();
            }
        });

        if (type == 0) {
            View top = inflater.inflate(R.layout.participant_top_view, null);
            ll_recent_user = top.findViewById(R.id.ll_recent_user);
            TextView item_name_tv = (TextView) top.findViewById(R.id.item_name_tv);
            ll_recent_user.setVisibility(View.GONE);
            content_rl = top.findViewById(R.id.content_rl);
            CircleImageView scaleImage = (CircleImageView) top.findViewById(R.id.item_name_img);
            check_box_tv = (TextView) top.findViewById(R.id.check_box_tv);

            if (listdata != null) {
                check_box_tv.setSelected(false);
                participantInfo.setChooosed(false);
                participantInfo.setName(user_name);
                participantInfo.setId(userId);
                for (String info : listdata) {
                    if (info.equals(userId)) {
                        check_box_tv.setSelected(true);
                        participantInfo.setChooosed(true);
                        break;
                    }
                }
            } else {
                check_box_tv.setSelected(true);
                participantInfo.setId(userId);
                participantInfo.setName(user_name);
                participantInfo.setChooosed(true);
            }
            content_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //选择框.
                    check_box_tv.setSelected(!check_box_tv.isSelected());
                    participantInfo.setChooosed(check_box_tv.isSelected());
                    participantInfo.setId(userId);
                    onChooosedChanged(participantInfo);
                }
            });


            CommonUtil.inflatHeaderIcon(icon, user_name, item_name_tv, scaleImage, getActivity(), user_type);
            mGv = (GridView) top.findViewById(R.id.recent_user);
            recentPaticipantAdapter = new RecentPaticipantAdapter(getActivity());
            mGv.setAdapter(recentPaticipantAdapter);
            mGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final SchUserInfo p = recentPaticipantAdapter.getItem(position);
                    TextView tvName = (TextView) view.findViewById(R.id.tv_name);
                    if (p.isChooosed()) {
                        p.setChooosed(false);
                    } else {
                        p.setChooosed(true);
                    }
                    tvName.setSelected(true);
                    onChooosedChanged(p);


                }
            });
            listView.addHeaderView(top);
        }

        choosePaticipantAdapter = new ChoosePaticipantAdapter(getActivity());
        choosePaticipantAdapter.update(new ArrayList<SchUserInfo>());
        listView.setAdapter(choosePaticipantAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - listView.getHeaderViewsCount();
                final SchUserInfo p = choosePaticipantAdapter.getItem(pos);
                TextView check_box_tv = (TextView) view.findViewById(R.id.check_box_tv);
                if (p.isChooosed()) {
                    p.setChooosed(false);

                } else {
                    p.setChooosed(true);
                }
                check_box_tv.setSelected(p.isChooosed());
                onChooosedChanged(p);

            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getNetDatas();
        if (type == 0) {
            getRecentPaticipant();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        if (type == 1) {
            getPatientPageList();
        } else {
            getWorkmatePageList();
        }
    }

    private void getNetDatasMore() {
        if (webService == null) {
            webService = RestService.getInstance().getCommonService(getActivity(), WebService.class);
        }

        if (type == 1) {
            getPatientePageMoreList();
        } else {
            getWorkmatePageMoreList();
        }
    }


    /**
     * 获取患者列表 首页
     */
    private void getPatientPageList() {
        Call<SchUserVo> call = webService.getPatientPageList(userId, CmnConstants.PAGE_SIZE);
        BaseCallEntry<SchUserVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .build();
        network.callNetwork(callEntry, new ResponseCallback<SchUserVo>() {
            @Override
            public void onResponse(SchUserVo body) {
                if (body.getResponseMsg().equals("1")) {
                    point = body.getPoint();
                    onLoadFiniah(body.getUsers());
                }
            }
        });
    }

    /**
     * 获取患者列表 历史
     */
    private void getPatientePageMoreList() {
        if(TextUtils.isEmpty(point)){
            handleResponseError();
            ToastUtils.toast(getActivity(), "没有更多了");
            return;
        }
        Call<SchUserVo> call = webService.getPatientePageMoreList(userId, point, CmnConstants.PAGE_SIZE);
        BaseCallEntry<SchUserVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .build();
        network.callNetwork(callEntry, new ResponseCallback<SchUserVo>() {
            @Override
            public void onResponse(SchUserVo body) {
                if (body.getUsers() != null) {
                    point = body.getPoint();
                    finishRefreshLoadMore(body.getUsers());
                } else {
                    ToastUtils.toast(getActivity(), "没有更多了");
                    handleResponseError();
                }
            }
        });
    }

    /**
     * 获取同事列表
     */
    private void getWorkmatePageList() {
        Call<WorkmateVo> call = webService.getWorkmatePageList(userId, CmnConstants.PAGE_SIZE);
        BaseCallEntry<WorkmateVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, true);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .build();
        network.callNetwork(callEntry, new ResponseCallback<WorkmateVo>() {
            @Override
            public void onResponse(WorkmateVo body) {
                if (body != null) {
                    point =  body.getPoint();
                    onLoadFiniah(body.getDoctors());
                } else {
                    handleResponseError();
                }

            }
        });
    }

    /**
     * 获取更多
     */
    private void getWorkmatePageMoreList() {

        if(TextUtils.isEmpty(point)){
            ToastUtils.toast(getActivity(), "没有更多了");
            return;
        }

        Call<WorkmateVo> call = webService.getWorkmatePageMoreList(userId, point, CmnConstants.PAGE_SIZE);
        BaseCallEntry<WorkmateVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, true);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .doOnResponseError(new Action<WorkmateVo>() {
                    @Override
                    public void onAction(WorkmateVo body) {
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
        network.callNetwork(callEntry, new ResponseCallback<WorkmateVo>() {
            @Override
            public void onResponse(WorkmateVo body) {

                if (body!= null) {
                    point =  body.getPoint();
                    finishRefreshLoadMore(body.getDoctors());
                } else {
                    ToastUtils.toast(getActivity(), "没有更多了");
                    handleResponseError();
                }
            }
        });
    }


    private boolean isExist(SchUserInfo info) {

        if (participantInfo != null && participantInfo.isChooosed() && info.getId().equals(participantInfo.getId())) {
            return true;
        }
        if (listdata == null) return false;
        for (String p : listdata) {
            if (info.getId().equals(p)) return true;
        }
        return false;
    }

    private boolean isExist(SchUserInfo info, List<SchUserInfo> list) {

        if (list == null) return false;

        for (SchUserInfo p : list) {
            if (info.getId().equals(p.getId())) return true;
        }
        return false;
    }

    private void onLoadFiniah(List<SchUserInfo> list) {

        if (list != null && list.size() > 0) {
            for (SchUserInfo info : list) {
                if (isExist(info)) {
                    info.setChooosed(true);
                }
            }
            choosePaticipantAdapter.update(list);
        }
        refreshLayout.finishRefresh();
        refreshLayout.finishRefreshLoadMore();
    }

    public List<SchUserInfo> getModels() {

        return choosePaticipantAdapter.getModels();
    }

    public List<SchUserInfo> getRecentModels() {
        return recentPaticipantAdapter.getModels();
    }

    public List<SchUserInfo> getChoosedItems() {


        List<SchUserInfo> participantInfoList = new ArrayList<SchUserInfo>();
        List<SchUserInfo> list = choosePaticipantAdapter.getModels();
        boolean isChoosed = false;
        if (list != null) {
            for (SchUserInfo info : list) {
                if (info.isChooosed()) {
                    if (participantInfo.getId() != null && participantInfo.getId().equals(info.getId())) {
                        isChoosed = true;
                    }
                    participantInfoList.add(info);
                }
            }
        }


        if (type == 0 && !isChoosed && participantInfo.isChooosed()) {
            participantInfoList.add(participantInfo);
        }
        if (type == 0 && recentPaticipantAdapter != null) {
            List<SchUserInfo> models = recentPaticipantAdapter.getModels();

            if (models != null) {
                for (SchUserInfo info : models) {

                    if (!info.isChooosed()) continue;
                    if (isExist(info, participantInfoList)) continue;
                    participantInfoList.add(info);
                }
            }
        }
        return participantInfoList;
    }


    public void myDataChanged(SchUserInfo info) {

        if (info != null && info.getName().equals(userId)) {
            check_box_tv.setSelected(info.isChooosed());
            participantInfo.setChooosed(info.isChooosed());
        }

    }


    public void getRecentPaticipant() {

        if (webService == null) {
            webService = RestService.getInstance().getCommonService(getActivity(), WebService.class);
        }
        Call<MembersVo> call = webService.getRecentPaticipant(userId);
        BaseCallEntry<MembersVo> callEntry = new BaseCallEntry<>(getActivity().getApplicationContext(), TAG, call, false);
        BaseNetwork network = new BaseNetwork.Builder()
                .setDialogController(dialogController)
                .build();
        network.callNetwork(callEntry, new ResponseCallback<MembersVo>() {
            @Override
            public void onResponse(MembersVo body) {
                if (body.getResponseMsg().equals("1")) {


                    List<SchUserInfo> users = body.getUsers();
                    if (users != null) {
                        ll_recent_user.setVisibility(View.VISIBLE);
                        for (SchUserInfo info : body.getUsers()) {
                            if (isExist(info)) {
                                info.setChooosed(true);
                            }
                        }
                        if (recentPaticipantAdapter != null) {
                            recentPaticipantAdapter.update(users);
                        }
                    }

                }

            }
        });


    }

    private void onChooosedChanged(SchUserInfo participantInfo) {

        // long start =System.currentTimeMillis();

        // LogUtil.i("start=====>",(System.currentTimeMillis() -start) +"");
        if (participantInfo != null) {//通知其他页面数据改变
            List<SchUserInfo> models = getModels();
            if (models != null) {
                int pos = 0;
                boolean isUpdate = false;
                for (SchUserInfo p : models) {
                    if (participantInfo.getId().equals(p.getId())) {//存在
                        p.setChooosed(participantInfo.isChooosed());
                        isUpdate = true;
                        break;
                    }
                    pos++;
                }
                if (choosePaticipantAdapter != null && isUpdate) {
                    choosePaticipantAdapter.updataView(pos + listView.getHeaderViewsCount(), listView, participantInfo.isChooosed());
                }
            }
            if (type == 0) {
                List<SchUserInfo> models0 = getRecentModels();
                if (models0 != null) {
                    for (SchUserInfo p : models0) {
                        if (participantInfo.getId().equals(p.getId())) {//存在
                            p.setChooosed(participantInfo.isChooosed());
                            break;
                        }
                    }
                }
                myDataChanged(participantInfo);
                if (recentPaticipantAdapter != null) {
                    recentPaticipantAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    private boolean isCheck(SchUserInfo info, List<SchUserInfo> models) {

        if (participantInfo.isChooosed() && participantInfo.getId().equals(info.getId())) {//存在
            return true;
        }
        if (models != null) {
            for (SchUserInfo p : models) {
                if (info.getId().equals(p.getId()) && p.isChooosed()) {//存在
                    return true;
                }
            }
        }

        return false;
    }

    private void finishRefreshLoadMore(ArrayList<SchUserInfo> list) {

        if (list != null) {
            if (type == 0) {
                List<SchUserInfo> models0 = getRecentModels();
                for (SchUserInfo info : list) {
                    if (isCheck(info, models0)) {
                        info.setChooosed(true);
                    }
                }
            }
            choosePaticipantAdapter.addList(list);
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

//    private ChooosedChangedCallBack changedCallBack;
//
//    public interface ChooosedChangedCallBack {
//        void onChooosedChanged(SchUserInfo info);
//    }
//
//    public void setChangedCallBack(ChooosedChangedCallBack changedCallBack) {
//        this.changedCallBack = changedCallBack;
//    }
}
