package com.appsinventiv.noorenikah.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.PayoutsHistoryAdapter;
import com.appsinventiv.noorenikah.Models.ReferralCodePaidModel;
import com.appsinventiv.noorenikah.Models.RequestPayoutModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InviteFragment extends Fragment {
    private View rootView;
    Button invite, payout;
    int totalEarning = 0;
    private DatabaseReference mDatabase;
    private String payoutOption;

    RadioButton easypaisa, jazzcash;
    EditText phone;
    TextView totalInstalls, totalInstallsPaid, totalEarningTv;
    int countInstalls = 0;
    int countPaidInstalls = 0;
    private String adminFcmkey;
    RecyclerView recyclerView;
    private List<RequestPayoutModel> itemList = new ArrayList<>();
    PayoutsHistoryAdapter adapter;
    EditText name;

    private RewardedAd mRewardedAd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_invite_history, container, false);
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(getContext(), getResources().getString(R.string.reward_ad_unit_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }
                });

        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

        phone = rootView.findViewById(R.id.phone);
        name = rootView.findViewById(R.id.name);
        totalEarningTv = rootView.findViewById(R.id.totalEarningTv);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        easypaisa = rootView.findViewById(R.id.easypaisa);
        totalInstalls = rootView.findViewById(R.id.totalInstalls);
        totalInstallsPaid = rootView.findViewById(R.id.totalInstallsPaid);
        jazzcash = rootView.findViewById(R.id.jazzcash);
        invite = rootView.findViewById(R.id.invite);
        payout = rootView.findViewById(R.id.payout);
        getAdminFcm();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new PayoutsHistoryAdapter(getContext(), itemList);
        recyclerView.setAdapter(adapter);

        easypaisa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    payoutOption = "Easy Paisa";
                }
            }
        });
        jazzcash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    payoutOption = "Jazz Cash";
                }
            }
        });
        phone.setText("0" + SharedPrefs.getUser().getPhone());
        name.setText(SharedPrefs.getUser().getName());
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReward();
                showAlert();
            }
        });
        payout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payoutOption != null) {
                    if (totalEarning >= 1000) {

                        showPayoutAlert();

                    } else {
                        CommonUtils.showToast("Minimum payout is Rs 1000/-");
                    }
                } else {
                    CommonUtils.showToast("Please select payout option");
                }
            }
        });

        getMyReferalData();
        getPayoutsData();
        return rootView;
    }

    public void showReward() {
        if (mRewardedAd != null) {
            mRewardedAd.show(getActivity(), new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                }
            });
        } else {
        }
    }

    private void showPayoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Request Payout");
        builder.setMessage("Make sure the information you provided is correct");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showReward();
                submitForPayout();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getPayoutsData() {
        mDatabase.child("RequestPayout").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RequestPayoutModel model = snapshot.getValue(RequestPayoutModel.class);
                        itemList.add(model);
                    }
                    adapter.setItemList(itemList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMyReferalData() {
        if(SharedPrefs.getUser().getMyReferralCode()!=null){
            mDatabase.child("ReferralCodesHistory").child(SharedPrefs.getUser().getMyReferralCode()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            ReferralCodePaidModel referralCodePaidModel = snapshot.getValue(ReferralCodePaidModel.class);
                            if (referralCodePaidModel != null) {
                                countInstalls++;
                                if (referralCodePaidModel.isPaid()) {
                                    countPaidInstalls++;
                                }
                            }
                        }
                        totalInstalls.setText("" + countInstalls);
                        totalInstallsPaid.setText("" + countPaidInstalls);
                        totalEarning = (countPaidInstalls * Constants.PAYOUT_AMOUNT);
                        totalEarningTv.setText("" + totalEarning);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getAdminFcm() {
        mDatabase.child("Admin").child("fcmKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    adminFcmkey = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void submitForPayout() {
        sendNotitifcation();
        String key = "" + System.currentTimeMillis();
        RequestPayoutModel model = new RequestPayoutModel(
                key, phone.getText().toString().substring(1), payoutOption, name.getText().toString(),
                totalEarning, System.currentTimeMillis()
        );
        mDatabase.child("RequestPayout").child(SharedPrefs.getUser().getPhone()).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                CommonUtils.showToast("Request submitted");
//                finish();
            }
        });
    }

    private void sendNotitifcation() {
        NotificationAsync notificationAsync = new NotificationAsync(getContext());
        String NotificationTitle = "New payout request";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                adminFcmkey,
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "payout");

    }

    private void showAlert() {
        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.alert_dialog_invite, null);
        dialog.setContentView(layout);
        TextView link = layout.findViewById(R.id.link);
        LinearLayout copyLink = layout.findViewById(R.id.copyLink);
        LinearLayout share = layout.findViewById(R.id.share);
        String url = "http://noorenikah.com/refer?id=" + SharedPrefs.getUser().getMyReferralCode();
        link.setText(url);

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("noorenikahurl", url);
                clipboard.setPrimaryClip(clip);
                CommonUtils.showToast("Link copied!");
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String msg = "*Noor-E-Nikah Marriage Bureau\n" +
                        "(اچھے رشتوں کے لئے یہ نورنکاح میرج بیورو کی ایپ ڈاون لوڈ کریں)\n" +
                        "\"Your Privacy is our Priority\"\n" +
                        "\n" +
                        "Download Now\n" +
                        url;
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(shareIntent, "Share link via.."));
                dialog.dismiss();
            }
        });


        dialog.show();
    }

}