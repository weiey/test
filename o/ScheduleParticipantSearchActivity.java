package com.yibei.xkm.ui.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yibei.xkm.R;
import com.yibei.xkm.adapter.ChoosePaticipantAdapter;
import com.yibei.xkm.entity.SchUserInfo;
import com.yibei.xkm.network.ResponseCallback;
import com.yibei.xkm.vo.PatientVo;
import com.yibei.xkm.vo.SchUserVo;
import com.yibei.xkm.vo.WorkmateVo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit.Call;
import wekin.com.tools.Utils;

/**
 * 搜索参与人
 */
public class ScheduleParticipantSearchActivity extends XkmBasicTemplateActivity implements View.OnClickListener {

    public static final String KEY_PATIENTS = "patients";
    public static final String KEY_COLLEAGUES = "colleagues";

    private ListView listView;
    private TextView hintView;
    private RadioGroup radioGroup;
    private ChoosePaticipantAdapter choosePaticipantAdapter;
    Set<SchUserInfo> workmateList;
    Set<SchUserInfo> patientList;

    private int type = 0;//0--

    //private String input ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_search);
        type = getIntent().getIntExtra("type", 0);

        workmateList = new LinkedHashSet<>();
        patientList = new LinkedHashSet<>();
        initView();

    }

    /**
     * 初始化
     */
    private void initView() {


        findViewById(R.id.iv_back).setOnClickListener(this);

        listView = $(R.id.lv_list);
        listView.setDivider(new ColorDrawable());
        listView.setSelector(new ColorDrawable());
        hintView = $(R.id.tv_hint);
        final EditText etSearch = $(R.id.et_search);
        etSearch.setHint("请输入关键字");
        View headerView = getLayoutInflater().inflate(R.layout.header_search, null);
        //headerView.findViewById(R.id.tv_type).setVisibility(View.GONE);
        radioGroup = (RadioGroup) headerView.findViewById(R.id.radioGroup);
        RadioButton radioButton = (RadioButton) radioGroup.findViewById(R.id.rb_my);
        RadioButton radioButton2 = (RadioButton) radioGroup.findViewById(R.id.rb_sub);
        radioButton.setText(getResources().getString(R.string.colleagues));
        radioButton2.setText(getResources().getString(R.string.patients));

        if (type != 0) {
            radioGroup.check(R.id.rb_sub);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_my:
                        type = 0;
                        //doSearch(input);
                        break;
                    case R.id.rb_sub:
                        type = 1;
                        //doSearch(input);
                        break;
                }
            }
        });

        listView.addHeaderView(headerView, null, false);
        choosePaticipantAdapter = new ChoosePaticipantAdapter(this);
        listView.setAdapter(choosePaticipantAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SchUserInfo p = choosePaticipantAdapter.getItem(position - 1);
                if (p.isChooosed()) {
                    p.setChooosed(false);
                    if (type == 0) {
                        workmateList.remove(p);
                    } else {
                        patientList.remove(p);
                    }
                } else {
                    p.setChooosed(true);
                    if (type == 0) {
                        workmateList.add(p);
                    } else {
                        patientList.add(p);
                    }
                }
                choosePaticipantAdapter.notifyDataSetChanged();
            }
        });


        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                doSearch(s.toString());
            }
        });
    }


    private long lastSearchTime;

    private void doSearch(String input) {
        long timeMillis = System.currentTimeMillis();
        if (timeMillis - lastSearchTime < 250) {
            return;
        }
        lastSearchTime = timeMillis;
        input = Utils.filterIllegalChars(input);
        if (TextUtils.isEmpty(input)) {
//            choosePaticipantAdapter.c;
            return;
        }
        if (type == 0) {
            searchColleagues(input);
        } else {
            searchPatients(input);
        }

    }

    /**
     * 获取患者列表
     */
    private void searchColleagues(String input) {

        Call<WorkmateVo> call = getWebService().searchColleagues(getCurrentUserId(), input);
        requestNetwork(call, false, new ResponseCallback<WorkmateVo>() {
            @Override
            public void onResponse(WorkmateVo body) {
                onLoadFinish(body.getDoctors());
            }
        });
    }

    /**
     * 获取患者列表
     */
    private void searchPatients(String input) {
        Call<SchUserVo> call = getWebService().searchPatients(getCurrentUserId(), input);
        requestNetwork(call, false, new ResponseCallback<SchUserVo>() {
            @Override
            public void onResponse(SchUserVo body) {
                onLoadFinish(body.getUsers());
            }
        });
    }

    private void onLoadFinish(List<SchUserInfo> list) {
        choosePaticipantAdapter.update(list);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideInputMethod();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            onBackPressed();
        } else {

        }
    }


}
