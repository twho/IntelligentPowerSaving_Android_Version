package com.tsungweiho.intelligentpowersaving.fragments;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tsungweiho.intelligentpowersaving.IPowerSaving;
import com.tsungweiho.intelligentpowersaving.MainActivity;
import com.tsungweiho.intelligentpowersaving.R;
import com.tsungweiho.intelligentpowersaving.constants.DBConstants;
import com.tsungweiho.intelligentpowersaving.constants.FragmentTags;
import com.tsungweiho.intelligentpowersaving.constants.PubNubAPIConstants;
import com.tsungweiho.intelligentpowersaving.databases.EventDBHelper;
import com.tsungweiho.intelligentpowersaving.databases.MessageDBHelper;
import com.tsungweiho.intelligentpowersaving.databinding.FragmentMessageBinding;
import com.tsungweiho.intelligentpowersaving.objects.Message;
import com.tsungweiho.intelligentpowersaving.tools.FirebaseManager;
import com.tsungweiho.intelligentpowersaving.utils.ImageUtils;
import com.tsungweiho.intelligentpowersaving.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Fragment for user to view message content and details
 *
 * This fragment is the child fragment of InboxFragment. It is the user interface that user can view message content and details
 *
 * @author Tsung Wei Ho
 * @version 1223.2017
 * @since 1.0.0
 */
public class MessageFragment extends Fragment implements FragmentTags, DBConstants, PubNubAPIConstants {
    private String TAG = "MessageFragment";

    // Settings Fragment View
    private View view;

    // UI views
    private ImageButton ibBack, ibDelete, ibRead, ibStar;
    private ImageView ivSender;
    private static FrameLayout imgLayout;
    private static ProgressBar pbImg;

    // Functions
    private FragmentMessageBinding binding;
    private Context context;
    private ArrayList<String> messageInfo;
    private int position;

    private Message currentMessage;
    private String currentBox;
    private MessageDBHelper msgDBHelper;
    private MessageFragmentListener messageFragmentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        view = binding.getRoot();

        context = IPowerSaving.getContext();
        this.messageInfo = this.getArguments().getStringArrayList(MESSAGE_FRAGMENT_KEY);

        init();

        return view;
    }

    /**
     * Init functions and UIs in InboxFragment
     */
    private void init() {
        msgDBHelper = new MessageDBHelper(context);
        messageFragmentListener = new MessageFragmentListener();

        // Mark message as read when getting in the message screen
        currentMessage = msgDBHelper.getMessageByUnId(messageInfo.get(0));
        msgDBHelper.markMailByLabel(currentMessage, LABEL_MSG_READ);
        this.position = Integer.parseInt(messageInfo.get(1));
        this.currentBox = currentMessage.getInboxLabel().split(SEPARATOR_MSG_LABEL)[2];

        binding.setMessage(currentMessage);

        findViews();

        setImageView(ivSender, currentMessage.getSender(), currentMessage.getInboxLabel());
        setAllListeners();
    }

    /**
     * Link views in layout file to view controller, no need to cast views since compile with SDK 26
     */
    private void findViews() {
        ibBack = view.findViewById(R.id.fragment_message_ib_back);
        ibDelete = view.findViewById(R.id.fragment_message_ib_delete);
        ibRead = view.findViewById(R.id.fragment_message_ib_read);
        ibStar = view.findViewById(R.id.fragment_home_ib_following);
        ivSender = view.findViewById(R.id.fragment_message_iv_sender);
        imgLayout = view.findViewById(R.id.fragment_message_layout_img);
        pbImg = view.findViewById(R.id.fragment_message_pb_img);
    }

    /**
     * Set event image from message object
     *
     * @param imageView the imageView to set event image to
     * @param uniqueId the uniqueId of the message
     * @param inboxLabel the inbox label of the message
     */
    @BindingAdapter({"bind:eventImg", "bind:inboxLabel"})
    public static void loadImage(ImageView imageView, String uniqueId, String inboxLabel) {
        if (inboxLabel.split(",")[3].matches(".*\\d+.*")) {
            imgLayout.setVisibility(View.VISIBLE);
            EventDBHelper eventDBHelper = new EventDBHelper(IPowerSaving.getContext());

            String url = eventDBHelper.getEventByUnId(uniqueId).getImage();
            ImageUtils.getInstance().setImageViewFromUrl(url, imageView, pbImg);
        } else {
            imgLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Set if mail is starred from message object using data binding
     *
     * @param ibStar the imageButton to be set
     * @param inboxLabel the inbox label of the message
     */
    @BindingAdapter({"bind:star"})
    public static void loadStar(final ImageButton ibStar, final String inboxLabel) {
        ibStar.setImageDrawable(IPowerSaving.getContext().getResources().getDrawable(inboxLabel.split(SEPARATOR_MSG_LABEL)[1].equalsIgnoreCase(LABEL_MSG_STAR) ? R.mipmap.ic_follow : R.mipmap.ic_unfollow));
    }

    /**
     *
     *
     * @param tvTime
     * @param time
     */
    @BindingAdapter({"bind:time"})
    public static void loadTime(final TextView tvTime, final String time) {
        tvTime.setText(time.split(SEPARATOR_MSG_LABEL)[time.split(SEPARATOR_MSG_LABEL)[0].equalsIgnoreCase(TimeUtils.getInstance().getDate()) ? 0 : 1]);
    }

    /**
     * Set all listeners within MessageFragment
     */
    private void setAllListeners() {
        ibBack.setOnClickListener(messageFragmentListener);
        ibDelete.setOnClickListener(messageFragmentListener);
        ibRead.setOnClickListener(messageFragmentListener);
        ibStar.setOnClickListener(messageFragmentListener);
    }

    // TODO duplicate function in MessageListAdapter
    /**
     * Set the icon of the mail sender or resource
     *
     * @param imageView  the icon of the mail sender or resource
     * @param inboxLabel the label of the mail
     */
    private void setImageView(ImageView imageView, String sender, String inboxLabel){
        Context context = IPowerSaving.getContext();
        String label = inboxLabel.split(SEPARATOR_MSG_LABEL)[3];

        switch (label) {
            case MESSAGE_LABEL_ANNOUNCEMENT:
                setAdminSenderIcon(imageView, R.mipmap.ic_label_announcement, R.drawable.background_circle_teal);
                break;
            case MESSAGE_LABEL_WARNING:
                setAdminSenderIcon(imageView, R.mipmap.ic_label_warning, R.drawable.background_circle_yellow);
                break;
            case MESSAGE_LABEL_EMERGENCY:
                setAdminSenderIcon(imageView, R.mipmap.ic_label_emergency, R.drawable.background_circle_red);
                break;
            default:
                loadImgFromFirebase(label, imageView);
                imageView.setBackground(context.getResources().getDrawable(R.drawable.background_circle_lightred));
                break;
        }
    }

    private void loadImgFromFirebase(String imgUrl, final ImageView imageView){
        FirebaseManager.getInstance().downloadProfileImg(imgUrl + "/" + imgUrl, new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageUtils.getInstance().setRoundedCornerImageViewFromUrl(uri.toString(), imageView, ImageUtils.getInstance().IMG_CIRCULAR);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageView.setImageDrawable(IPowerSaving.getContext().getResources().getDrawable(R.mipmap.ic_preload_profile));
            }
        });
    }

    private void setAdminSenderIcon(ImageView imageView, int icon, int background){
        imageView.setImageDrawable(context.getResources().getDrawable(icon));
        imageView.setBackground(context.getResources().getDrawable(background));
    }

    /**
     * Duplicate functions ends here
     */

    /**
     * All listeners used in InboxFragment
     */
    private class MessageFragmentListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fragment_message_ib_back:
                    ((MainActivity) getActivity()).setFragment(MainFragment.INBOX);
                    break;
                case R.id.fragment_message_ib_delete:
                    msgDBHelper.moveDirByLabel(currentMessage, LABEL_MSG_TRASH);
                    if (position + 1 < msgDBHelper.getMessageListByLabel(currentBox).size()) {
                        currentMessage = msgDBHelper.getMessageListByLabel(currentBox).get(position + 1);
                        setImageView(ivSender, currentMessage.getSender(), currentMessage.getInboxLabel());
                        binding.setMessage(currentMessage);
                        position = position + 1;
                    } else {
                        ((MainActivity) getActivity()).setFragment(MainFragment.INBOX);
                    }
                    break;
                case R.id.fragment_message_ib_read:
                    msgDBHelper.markMailByLabel(currentMessage, LABEL_MSG_UNREAD);
                    ((MainActivity) getActivity()).setFragment(MainFragment.INBOX);
                    break;
                case R.id.fragment_home_ib_following:
                    currentMessage = msgDBHelper.getMessageByUnId(messageInfo.get(0));
                    Boolean isStarred = currentMessage.getInboxLabel().split(SEPARATOR_MSG_LABEL)[1].equalsIgnoreCase(LABEL_MSG_STAR);

                    // Set star label
                    ibStar.setImageDrawable(context.getResources().getDrawable(isStarred ? R.mipmap.ic_unfollow : R.mipmap.ic_follow));
                    msgDBHelper.starMailByLabel(currentMessage, isStarred ? LABEL_MSG_UNSTAR : LABEL_MSG_STAR);
                    break;
            }
        }
    }
}