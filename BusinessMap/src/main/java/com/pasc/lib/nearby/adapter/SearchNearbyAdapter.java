package com.pasc.lib.nearby.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import com.amap.api.services.core.PoiItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.pasc.lib.nearby.R;
import com.pasc.lib.nearby.utils.NearByUtil;
import com.pasc.lib.widget.dialog.OnCloseListener;
import com.pasc.lib.widget.dialog.OnConfirmListener;
import com.pasc.lib.widget.dialog.common.ConfirmDialogFragment;

import java.text.DecimalFormat;
import java.util.List;

public class SearchNearbyAdapter extends BaseQuickAdapter<PoiItem, BaseViewHolder> {

    private DecimalFormat df;
    private Context mContext;
    public SearchNearbyAdapter (Context context, @Nullable List<PoiItem> data) {
        super(R.layout.nearby_item_search_nearby, data);
        mContext = context;
        df = new DecimalFormat("0.0");//格式化小数
    }
    public SearchNearbyAdapter (@Nullable List<PoiItem> data) {
        super(R.layout.nearby_item_search_nearby, data);
        df = new DecimalFormat("0.0");//格式化小数
    }

    @Override protected void convert(BaseViewHolder helper, PoiItem item) {
        helper.addOnClickListener(R.id.nearby_rl_middle).addOnClickListener(R.id
                .nearby_tv_near_loc);

        final String tel = item.getTel();
        String title = item.getTitle();
        String distance =
                item.getDistance() > 1000 ? df.format((float) item.getDistance() / 1000) + "km"
                        : item.getDistance() + "m";
        helper.setText(R.id.nearby_tv_item_title, title)
                .setText(R.id.nearby_tv_item_distance, distance)
                .setText(R.id.nearby_tv_item_address, item.getSnippet());

        if (isPhoneValid(tel)) {
            helper.getView(R.id.nearby_tv_near_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPhoneValid(tel)){
                        showChooseDialog(mContext, tel.split(";")[0]);
                    }
                }
            });
            helper.setVisible(R.id.nearby_tv_near_call, true);
            helper.setVisible(R.id.nearby_view_divider, true);
        } else {
            helper.getView(R.id.nearby_tv_near_call).setOnClickListener(null);
            helper.setVisible(R.id.nearby_tv_near_call, false);
            helper.setVisible(R.id.nearby_view_divider, false);
        }
    }

    public boolean isPhoneValid(String phoneNo) {
        return !TextUtils.isEmpty(phoneNo) && phoneNo.trim().length() >= 8;
    }

    private void showChooseDialog(final Context context, final String phone) {
        final ConfirmDialogFragment commonDialog = new ConfirmDialogFragment.Builder()
                .setDesc(phone)
                .setCloseText("取消")
                .setConfirmText("呼叫")
                .setOnConfirmListener(new OnConfirmListener<ConfirmDialogFragment>() {
                    @Override
                    public void onConfirm(ConfirmDialogFragment confirmDialogFragment) {
                        confirmDialogFragment.dismiss();
                        NearByUtil.call(context, phone);
                    }
                })
                .setOnCloseListener(new OnCloseListener<ConfirmDialogFragment>() {
                    @Override
                    public void onClose(ConfirmDialogFragment confirmDialogFragment) {
                        confirmDialogFragment.dismiss();
                    }
                }).build();
        commonDialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "");
    }
}
