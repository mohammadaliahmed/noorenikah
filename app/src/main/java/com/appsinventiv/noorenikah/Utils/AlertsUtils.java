package com.appsinventiv.noorenikah.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.appsinventiv.noorenikah.R;

public class AlertsUtils {
    public static void showPrivacyAlert(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View layout = layoutInflater.inflate(R.layout.alert_dialog_terms, null);
        dialog.setContentView(layout);
        TextView alertTitle = layout.findViewById(R.id.alertTitle);
        alertTitle.setText("Privacy Policy");
        TextView textHere = layout.findViewById(R.id.textHere);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String html =
                    "<!DOCTYPE html>\n" +
                            "    <html>\n" +
                            "\n" +
                            "    <body>\n" +
                            "    <strong>Privacy Policy</strong> <p>\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau built the Noor-E-Nikah app as\n" +
                            "                  an Ad Supported app. This SERVICE is provided by\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau at no cost and is intended for use as\n" +
                            "                  is.\n" +
                            "                </p> <p>\n" +
                            "                  This page is used to inform visitors regarding our\n" +
                            "                  policies with the collection, use, and disclosure of Personal\n" +
                            "                  Information if anyone decided to use our Service.\n" +
                            "                </p> <p>\n" +
                            "                  If you choose to use our Service, then you agree to\n" +
                            "                  the collection and use of information in relation to this\n" +
                            "                  policy. The Personal Information that we collect is\n" +
                            "                  used for providing and improving the Service. We will not use or share your information with\n" +
                            "                  anyone except as described in this Privacy Policy.\n" +
                            "                </p> <p>\n" +
                            "                  The terms used in this Privacy Policy have the same meanings\n" +
                            "                  as in our Terms and Conditions, which are accessible at\n" +
                            "                  Noor-E-Nikah unless otherwise defined in this Privacy Policy.\n" +
                            "                </p> <p><strong>Information Collection and Use</strong></p> <p>\n" +
                            "                  For a better experience, while using our Service, we\n" +
                            "                  may require you to provide us with certain personally\n" +
                            "                  identifiable information, including but not limited to Camera, Audio, Gallery, Pictures, Network Information, Name, Phone, Email. The information that\n" +
                            "                  we request will be retained by us and used as described in this privacy policy.\n" +
                            "                </p> <div><p>\n" +
                            "                    The app does use third-party services that may collect\n" +
                            "                    information used to identify you.\n" +
                            "                  </p> <p>\n" +
                            "                    Link to the privacy policy of third-party service providers used\n" +
                            "                    by the app\n" +
                            "                  </p> <ul><li><a href=\"https://www.google.com/policies/privacy/\" target=\"_blank\" rel=\"noopener noreferrer\">Google Play Services</a></li><li><a href=\"https://support.google.com/admob/answer/6128543?hl=en\" target=\"_blank\" rel=\"noopener noreferrer\">AdMob</a></li><li><a href=\"https://firebase.google.com/policies/analytics\" target=\"_blank\" rel=\"noopener noreferrer\">Google Analytics for Firebase</a></li><li><a href=\"https://firebase.google.com/support/privacy/\" target=\"_blank\" rel=\"noopener noreferrer\">Firebase Crashlytics</a></li><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----></ul></div> <p><strong>Log Data</strong></p> <p>\n" +
                            "                  We want to inform you that whenever you\n" +
                            "                  use our Service, in a case of an error in the app\n" +
                            "                  we collect data and information (through third-party\n" +
                            "                  products) on your phone called Log Data. This Log Data may\n" +
                            "                  include information such as your device Internet Protocol\n" +
                            "                  (“IP”) address, device name, operating system version, the\n" +
                            "                  configuration of the app when utilizing our Service,\n" +
                            "                  the time and date of your use of the Service, and other\n" +
                            "                  statistics.\n" +
                            "                </p> <p><strong>Cookies</strong></p> <p>\n" +
                            "                  Cookies are files with a small amount of data that are\n" +
                            "                  commonly used as anonymous unique identifiers. These are sent\n" +
                            "                  to your browser from the websites that you visit and are\n" +
                            "                  stored on your device's internal memory.\n" +
                            "                </p> <p>\n" +
                            "                  This Service does not use these “cookies” explicitly. However,\n" +
                            "                  the app may use third-party code and libraries that use\n" +
                            "                  “cookies” to collect information and improve their services.\n" +
                            "                  You have the option to either accept or refuse these cookies\n" +
                            "                  and know when a cookie is being sent to your device. If you\n" +
                            "                  choose to refuse our cookies, you may not be able to use some\n" +
                            "                  portions of this Service.\n" +
                            "                </p> <p><strong>Service Providers</strong></p> <p>\n" +
                            "                  We may employ third-party companies and\n" +
                            "                  individuals due to the following reasons:\n" +
                            "                </p> <ul><li>To facilitate our Service;</li> <li>To provide the Service on our behalf;</li> <li>To perform Service-related services; or</li> <li>To assist us in analyzing how our Service is used.</li></ul> <p>\n" +
                            "                  We want to inform users of this Service\n" +
                            "                  that these third parties have access to their Personal\n" +
                            "                  Information. The reason is to perform the tasks assigned to\n" +
                            "                  them on our behalf. However, they are obligated not to\n" +
                            "                  disclose or use the information for any other purpose.\n" +
                            "                </p> <p><strong>Security</strong></p> <p>\n" +
                            "                  We value your trust in providing us your\n" +
                            "                  Personal Information, thus we are striving to use commercially\n" +
                            "                  acceptable means of protecting it. But remember that no method\n" +
                            "                  of transmission over the internet, or method of electronic\n" +
                            "                  storage is 100% secure and reliable, and we cannot\n" +
                            "                  guarantee its absolute security.\n" +
                            "                </p> <p><strong>Links to Other Sites</strong></p> <p>\n" +
                            "                  This Service may contain links to other sites. If you click on\n" +
                            "                  a third-party link, you will be directed to that site. Note\n" +
                            "                  that these external sites are not operated by us.\n" +
                            "                  Therefore, we strongly advise you to review the\n" +
                            "                  Privacy Policy of these websites. We have\n" +
                            "                  no control over and assume no responsibility for the content,\n" +
                            "                  privacy policies, or practices of any third-party sites or\n" +
                            "                  services.\n" +
                            "                </p> <p><strong>Children’s Privacy</strong></p> <div><p>\n" +
                            "                    These Services do not address anyone under the age of 13.\n" +
                            "                    We do not knowingly collect personally\n" +
                            "                    identifiable information from children under 13 years of age. In the case\n" +
                            "                    we discover that a child under 13 has provided\n" +
                            "                    us with personal information, we immediately\n" +
                            "                    delete this from our servers. If you are a parent or guardian\n" +
                            "                    and you are aware that your child has provided us with\n" +
                            "                    personal information, please contact us so that\n" +
                            "                    we will be able to do the necessary actions.\n" +
                            "                  </p></div> <!----> <p><strong>Changes to This Privacy Policy</strong></p> <p>\n" +
                            "                  We may update our Privacy Policy from\n" +
                            "                  time to time. Thus, you are advised to review this page\n" +
                            "                  periodically for any changes. We will\n" +
                            "                  notify you of any changes by posting the new Privacy Policy on\n" +
                            "                  this page.\n" +
                            "                </p> <p>This policy is effective as of 2022-08-01</p> <p><strong>Contact Us</strong></p> <p>\n" +
                            "                  If you have any questions or suggestions about our\n" +
                            "                  Privacy Policy, do not hesitate to contact us at naatshamds@gmail.com.\n" +
                            "                </p> \n" +
                            "<strong>Note</strong><br>" +
                            "<br>1-Noor e nikah is a platform designed to gather users, who are searching for their soulmates by their own will. We are just a platform.\n" +
                            "<br>2- Noor e Nikah will not took the responsible of any user, it is just a medium of comunication.\n" +
                            "<br>3- Noor e Nikah not visited any user's home to inquire them.\n" +
                            "<br>4- The proper investigation of a User, is family's prior responsibility, Noor e Nikah will not took any user's responsibilty.\n" +
                            "<br>5-Noor e nikah, provided user an option to block or report user, who are not fulling their requirement, it is totaly upto user." +
                            " <br><br><br>   </body>\n" +
                            "    </html>\n" +
                            "      ";
            textHere.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        }

        dialog.show();

    }

    public static void customTextView(Context context, TextView view) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "I agree to the ");
        spanTxt.append("Term of services");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                AlertsUtils.showTermsAlert(context);


            }
        }, spanTxt.length() - "Term of services".length(), spanTxt.length(), 0);
        spanTxt.append(" and");
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), 32, spanTxt.length(), 0);
        spanTxt.append(" Privacy Policy");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                AlertsUtils.showPrivacyAlert(context);

            }
        }, spanTxt.length() - " Privacy Policy".length(), spanTxt.length(), 0);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }


    public static void showTermsAlert(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View layout = layoutInflater.inflate(R.layout.alert_dialog_terms, null);
        dialog.setContentView(layout);
        TextView alertTitle = layout.findViewById(R.id.alertTitle);
        alertTitle.setText("Terms and conditions");
        TextView textHere = layout.findViewById(R.id.textHere);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String html =
                    "<!DOCTYPE html>\n" +
                            "    <html>\n" +
                            "    <head>\n" +
                            "   </head>\n" +
                            "    <body>\n" +
                            "    <strong>Terms &amp; Conditions</strong> <p>\n" +
                            "                  By downloading or using the app, these terms will\n" +
                            "                  automatically apply to you – you should make sure therefore\n" +
                            "                  that you read them carefully before using the app. You’re not\n" +
                            "                  allowed to copy or modify the app, any part of the app, or\n" +
                            "                  our trademarks in any way. You’re not allowed to attempt to\n" +
                            "                  extract the source code of the app, and you also shouldn’t try\n" +
                            "                  to translate the app into other languages or make derivative\n" +
                            "                  versions. The app itself, and all the trademarks, copyright,\n" +
                            "                  database rights, and other intellectual property rights related\n" +
                            "                  to it, still belong to Noor e Nikah - Muslims Matrimonial Marriage Bureau.\n" +
                            "                </p> <p>\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau is committed to ensuring that the app is\n" +
                            "                  as useful and efficient as possible. For that reason, we\n" +
                            "                  reserve the right to make changes to the app or to charge for\n" +
                            "                  its services, at any time and for any reason. We will never\n" +
                            "                  charge you for the app or its services without making it very\n" +
                            "                  clear to you exactly what you’re paying for.\n" +
                            "                </p> <p>\n" +
                            "                  The Noor-E-Nikah app stores and processes personal data that\n" +
                            "                  you have provided to us, to provide our\n" +
                            "                  Service. It’s your responsibility to keep your phone and\n" +
                            "                  access to the app secure. We therefore recommend that you do\n" +
                            "                  not jailbreak or root your phone, which is the process of\n" +
                            "                  removing software restrictions and limitations imposed by the\n" +
                            "                  official operating system of your device. It could make your\n" +
                            "                  phone vulnerable to malware/viruses/malicious programs,\n" +
                            "                  compromise your phone’s security features and it could mean\n" +
                            "                  that the Noor-E-Nikah app won’t work properly or at all.\n" +
                            "                </p> <div><p>\n" +
                            "                    The app does use third-party services that declare their\n" +
                            "                    Terms and Conditions.\n" +
                            "                  </p> <p>\n" +
                            "                    Link to Terms and Conditions of third-party service\n" +
                            "                    providers used by the app\n" +
                            "                  </p> <ul><li><a href=\"https://policies.google.com/terms\" target=\"_blank\" rel=\"noopener noreferrer\">Google Play Services</a></li><li><a href=\"https://developers.google.com/admob/terms\" target=\"_blank\" rel=\"noopener noreferrer\">AdMob</a></li><li><a href=\"https://firebase.google.com/terms/analytics\" target=\"_blank\" rel=\"noopener noreferrer\">Google Analytics for Firebase</a></li><li><a href=\"https://firebase.google.com/terms/crashlytics\" target=\"_blank\" rel=\"noopener noreferrer\">Firebase Crashlytics</a></li><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----></ul></div> <p>\n" +
                            "                  You should be aware that there are certain things that\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau will not take responsibility for. Certain\n" +
                            "                  functions of the app will require the app to have an active\n" +
                            "                  internet connection. The connection can be Wi-Fi or provided\n" +
                            "                  by your mobile network provider, but Noor e Nikah - Muslims Matrimonial Marriage Bureau\n" +
                            "                  cannot take responsibility for the app not working at full\n" +
                            "                  functionality if you don’t have access to Wi-Fi, and you don’t\n" +
                            "                  have any of your data allowance left.\n" +
                            "                </p> <p></p> <p>\n" +
                            "                  If you’re using the app outside of an area with Wi-Fi, you\n" +
                            "                  should remember that the terms of the agreement with your\n" +
                            "                  mobile network provider will still apply. As a result, you may\n" +
                            "                  be charged by your mobile provider for the cost of data for\n" +
                            "                  the duration of the connection while accessing the app, or\n" +
                            "                  other third-party charges. In using the app, you’re accepting\n" +
                            "                  responsibility for any such charges, including roaming data\n" +
                            "                  charges if you use the app outside of your home territory\n" +
                            "                  (i.e. region or country) without turning off data roaming. If\n" +
                            "                  you are not the bill payer for the device on which you’re\n" +
                            "                  using the app, please be aware that we assume that you have\n" +
                            "                  received permission from the bill payer for using the app.\n" +
                            "                </p> <p>\n" +
                            "                  Along the same lines, Noor e Nikah - Muslims Matrimonial Marriage Bureau cannot always take\n" +
                            "                  responsibility for the way you use the app i.e. You need to\n" +
                            "                  make sure that your device stays charged – if it runs out of\n" +
                            "                  battery and you can’t turn it on to avail the Service,\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau cannot accept responsibility.\n" +
                            "                </p> <p>\n" +
                            "                  With respect to Noor e Nikah - Muslims Matrimonial Marriage Bureau’s responsibility for your\n" +
                            "                  use of the app, when you’re using the app, it’s important to\n" +
                            "                  bear in mind that although we endeavor to ensure that it is\n" +
                            "                  updated and correct at all times, we do rely on third parties\n" +
                            "                  to provide information to us so that we can make it available\n" +
                            "                  to you. Noor e Nikah - Muslims Matrimonial Marriage Bureau accepts no liability for any\n" +
                            "                  loss, direct or indirect, you experience as a result of\n" +
                            "                  relying wholly on this functionality of the app.\n" +
                            "                </p> <p>\n" +
                            "                  At some point, we may wish to update the app. The app is\n" +
                            "                  currently available on Android – the requirements for the \n" +
                            "                  system(and for any additional systems we\n" +
                            "                  decide to extend the availability of the app to) may change,\n" +
                            "                  and you’ll need to download the updates if you want to keep\n" +
                            "                  using the app. Noor e Nikah - Muslims Matrimonial Marriage Bureau does not promise that it\n" +
                            "                  will always update the app so that it is relevant to you\n" +
                            "                  and/or works with the Android version that you have\n" +
                            "                  installed on your device. However, you promise to always\n" +
                            "                  accept updates to the application when offered to you, We may\n" +
                            "                  also wish to stop providing the app, and may terminate use of\n" +
                            "                  it at any time without giving notice of termination to you.\n" +
                            "                  Unless we tell you otherwise, upon any termination, (a) the\n" +
                            "                  rights and licenses granted to you in these terms will end;\n" +
                            "                  (b) you must stop using the app, and (if needed) delete it\n" +
                            "                  from your device.\n" +
                            "                </p> <p><strong>Changes to This Terms and Conditions</strong></p> <p>\n" +
                            "                  We may update our Terms and Conditions\n" +
                            "                  from time to time. Thus, you are advised to review this page\n" +
                            "                  periodically for any changes. We will\n" +
                            "                  notify you of any changes by posting the new Terms and\n" +
                            "                  Conditions on this page.\n" +
                            "                </p> <p>\n" +
                            "                  These terms and conditions are effective as of 2022-08-01\n" +
                            "                </p> <p><strong>Contact Us</strong></p> <p>\n" +
                            "                  If you have any questions or suggestions about our\n" +
                            "                  Terms and Conditions, do not hesitate to contact us\n" +
                            "                  at naatshamds@gmail.com.\n" +
                            "                </p> \n" +
                            "    </body>\n" +
                            "    </html>\n" +
                            "      ";
            textHere.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        }

        dialog.show();

    }

    public static void showTermsAlert2(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View layout = layoutInflater.inflate(R.layout.alert_dialog_terms2, null);
        dialog.setContentView(layout);
        Button start = layout.findViewById(R.id.start);

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    Intent serviceIntent = new Intent(context, MyNotificationListenerService.class);
                    context.startService(serviceIntent);
                    // We have permission to access the user's notifications
                } else {
                    // Open the notification policy access screen
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    context.startActivity(intent);
                }

            }
        });
        TextView alertTitle = layout.findViewById(R.id.alertTitle);
        alertTitle.setText("Terms and conditions");
        TextView textHere = layout.findViewById(R.id.textHere);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String html =
                    "<!DOCTYPE html>\n" +
                            "    <html>\n" +
                            "    <head>\n" +
                            "   </head>\n" +
                            "    <body>\n" +
                            "    <strong>Terms &amp; Conditions</strong> <p>\n" +
                            "                  By downloading or using the app, these terms will\n" +
                            "                  automatically apply to you – you should make sure therefore\n" +
                            "                  that you read them carefully before using the app. You’re not\n" +
                            "                  allowed to copy or modify the app, any part of the app, or\n" +
                            "                  our trademarks in any way. You’re not allowed to attempt to\n" +
                            "                  extract the source code of the app, and you also shouldn’t try\n" +
                            "                  to translate the app into other languages or make derivative\n" +
                            "                  versions. The app itself, and all the trademarks, copyright,\n" +
                            "                  database rights, and other intellectual property rights related\n" +
                            "                  to it, still belong to Noor e Nikah - Muslims Matrimonial Marriage Bureau.\n" +
                            "                </p> <p>\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau is committed to ensuring that the app is\n" +
                            "                  as useful and efficient as possible. For that reason, we\n" +
                            "                  reserve the right to make changes to the app or to charge for\n" +
                            "                  its services, at any time and for any reason. We will never\n" +
                            "                  charge you for the app or its services without making it very\n" +
                            "                  clear to you exactly what you’re paying for.\n" +
                            "                </p> <p>\n" +
                            "                  The Noor-E-Nikah app stores and processes personal data that\n" +
                            "                  you have provided to us, to provide our\n" +
                            "                  Service. It’s your responsibility to keep your phone and\n" +
                            "                  access to the app secure. We therefore recommend that you do\n" +
                            "                  not jailbreak or root your phone, which is the process of\n" +
                            "                  removing software restrictions and limitations imposed by the\n" +
                            "                  official operating system of your device. It could make your\n" +
                            "                  phone vulnerable to malware/viruses/malicious programs,\n" +
                            "                  compromise your phone’s security features and it could mean\n" +
                            "                  that the Noor-E-Nikah app won’t work properly or at all.\n" +
                            "                </p> <div><p>\n" +
                            "                    The app does use third-party services that declare their\n" +
                            "                    Terms and Conditions.\n" +
                            "                  </p> <p>\n" +
                            "                    Link to Terms and Conditions of third-party service\n" +
                            "                    providers used by the app\n" +
                            "                  </p> <ul><li><a href=\"https://policies.google.com/terms\" target=\"_blank\" rel=\"noopener noreferrer\">Google Play Services</a></li><li><a href=\"https://developers.google.com/admob/terms\" target=\"_blank\" rel=\"noopener noreferrer\">AdMob</a></li><li><a href=\"https://firebase.google.com/terms/analytics\" target=\"_blank\" rel=\"noopener noreferrer\">Google Analytics for Firebase</a></li><li><a href=\"https://firebase.google.com/terms/crashlytics\" target=\"_blank\" rel=\"noopener noreferrer\">Firebase Crashlytics</a></li><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----></ul></div> <p>\n" +
                            "                  You should be aware that there are certain things that\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau will not take responsibility for. Certain\n" +
                            "                  functions of the app will require the app to have an active\n" +
                            "                  internet connection. The connection can be Wi-Fi or provided\n" +
                            "                  by your mobile network provider, but Noor e Nikah - Muslims Matrimonial Marriage Bureau\n" +
                            "                  cannot take responsibility for the app not working at full\n" +
                            "                  functionality if you don’t have access to Wi-Fi, and you don’t\n" +
                            "                  have any of your data allowance left.\n" +
                            "                </p> <p></p> <p>\n" +
                            "                  If you’re using the app outside of an area with Wi-Fi, you\n" +
                            "                  should remember that the terms of the agreement with your\n" +
                            "                  mobile network provider will still apply. As a result, you may\n" +
                            "                  be charged by your mobile provider for the cost of data for\n" +
                            "                  the duration of the connection while accessing the app, or\n" +
                            "                  other third-party charges. In using the app, you’re accepting\n" +
                            "                  responsibility for any such charges, including roaming data\n" +
                            "                  charges if you use the app outside of your home territory\n" +
                            "                  (i.e. region or country) without turning off data roaming. If\n" +
                            "                  you are not the bill payer for the device on which you’re\n" +
                            "                  using the app, please be aware that we assume that you have\n" +
                            "                  received permission from the bill payer for using the app.\n" +
                            "                </p> <p>\n" +
                            "                  Along the same lines, Noor e Nikah - Muslims Matrimonial Marriage Bureau cannot always take\n" +
                            "                  responsibility for the way you use the app i.e. You need to\n" +
                            "                  make sure that your device stays charged – if it runs out of\n" +
                            "                  battery and you can’t turn it on to avail the Service,\n" +
                            "                  Noor e Nikah - Muslims Matrimonial Marriage Bureau cannot accept responsibility.\n" +
                            "                </p> <p>\n" +
                            "                  With respect to Noor e Nikah - Muslims Matrimonial Marriage Bureau’s responsibility for your\n" +
                            "                  use of the app, when you’re using the app, it’s important to\n" +
                            "                  bear in mind that although we endeavor to ensure that it is\n" +
                            "                  updated and correct at all times, we do rely on third parties\n" +
                            "                  to provide information to us so that we can make it available\n" +
                            "                  to you. Noor e Nikah - Muslims Matrimonial Marriage Bureau accepts no liability for any\n" +
                            "                  loss, direct or indirect, you experience as a result of\n" +
                            "                  relying wholly on this functionality of the app.\n" +
                            "                </p> <p>\n" +
                            "                  At some point, we may wish to update the app. The app is\n" +
                            "                  currently available on Android – the requirements for the \n" +
                            "                  system(and for any additional systems we\n" +
                            "                  decide to extend the availability of the app to) may change,\n" +
                            "                  and you’ll need to download the updates if you want to keep\n" +
                            "                  using the app. Noor e Nikah - Muslims Matrimonial Marriage Bureau does not promise that it\n" +
                            "                  will always update the app so that it is relevant to you\n" +
                            "                  and/or works with the Android version that you have\n" +
                            "                  installed on your device. However, you promise to always\n" +
                            "                  accept updates to the application when offered to you, We may\n" +
                            "                  also wish to stop providing the app, and may terminate use of\n" +
                            "                  it at any time without giving notice of termination to you.\n" +
                            "                  Unless we tell you otherwise, upon any termination, (a) the\n" +
                            "                  rights and licenses granted to you in these terms will end;\n" +
                            "                  (b) you must stop using the app, and (if needed) delete it\n" +
                            "                  from your device.\n" +
                            "                </p> <p><strong>Changes to This Terms and Conditions</strong></p> <p>\n" +
                            "                  We may update our Terms and Conditions\n" +
                            "                  from time to time. Thus, you are advised to review this page\n" +
                            "                  periodically for any changes. We will\n" +
                            "                  notify you of any changes by posting the new Terms and\n" +
                            "                  Conditions on this page.\n" +
                            "                </p> <p>\n" +
                            "                  These terms and conditions are effective as of 2022-08-01\n" +
                            "                </p> <p><strong>Contact Us</strong></p> <p>\n" +
                            "                  If you have any questions or suggestions about our\n" +
                            "                  Terms and Conditions, do not hesitate to contact us\n" +
                            "                  at naatshamds@gmail.com.\n" +
                            "                </p> \n" +
                            "    </body>\n" +
                            "    </html>\n" +
                            "      ";
            textHere.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        }

        dialog.show();

    }

}
