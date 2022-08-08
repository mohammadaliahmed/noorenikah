package com.appsinventiv.noorenikah.Activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.PayoutsHistoryAdapter;
import com.appsinventiv.noorenikah.Models.ReferralCodePaidModel;
import com.appsinventiv.noorenikah.Models.RequestPayoutModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InviteHistory extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_history);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
            this.setTitle("Invite");
        }
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        totalEarningTv = findViewById(R.id.totalEarningTv);
        recyclerView = findViewById(R.id.recyclerView);
        easypaisa = findViewById(R.id.easypaisa);
        totalInstalls = findViewById(R.id.totalInstalls);
        totalInstallsPaid = findViewById(R.id.totalInstallsPaid);
        jazzcash = findViewById(R.id.jazzcash);
        invite = findViewById(R.id.invite);
        payout = findViewById(R.id.payout);
        getAdminFcm();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new PayoutsHistoryAdapter(this, itemList);
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

    }

    private void showPayoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request Payout");
        builder.setMessage("Make sure the information you provided is correct");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
                    totalEarning = (countPaidInstalls * 200);
                    totalEarningTv.setText("" + totalEarning);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                key, phone.getText().toString(), payoutOption, name.getText().toString(),
                totalEarning, System.currentTimeMillis()
        );
        mDatabase.child("RequestPayout").child(SharedPrefs.getUser().getPhone()).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                CommonUtils.showToast("Request submitted");
                finish();
            }
        });
    }

    private void sendNotitifcation() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
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
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}