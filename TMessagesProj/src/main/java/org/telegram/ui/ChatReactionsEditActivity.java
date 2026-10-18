package org.telegram.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.core.presenter.ChatReactionsPresenter;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AvailableReactionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SimpleThemeDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ChatReactionsEditActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ChatReactionsPresenter.View {
    private final static int TYPE_INFO = 0, TYPE_HEADER = 1, TYPE_REACTION = 2, TYPE_CONTROLS_CONTAINER = 3;

    public final static String KEY_CHAT_ID = "chat_id";

    private ChatReactionsPresenter presenter;
    private long chatId;

    private LinearLayout contentView;
    private RecyclerListView listView;
    private RecyclerView.Adapter listAdapter;

    private TextCheckCell enableReactionsCell;
    LinearLayout contorlsLayout;

    private RadioCell allReactions;
    private RadioCell someReactions;
    private RadioCell disableReactions;
    ArrayList<RadioCell> radioCells = new ArrayList();
    boolean isChannel;


    public ChatReactionsEditActivity(Bundle args) {
        super(args);

        chatId = args.getLong(KEY_CHAT_ID, 0);
        presenter = new ChatReactionsPresenter(new ChatRepositoryImpl(currentAccount), chatId);
    }

    @Override
    public boolean onFragmentCreate() {
        presenter.attachView(this);
        if (!presenter.onCreate()) {
            return false;
        }
        getNotificationCenter().addObserver(this, NotificationCenter.reactionsDidLoad);
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        isChannel = presenter.isChannel();

        actionBar.setTitle(LocaleController.getString("Reactions", R.string.Reactions));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        presenter.loadAvailableReactions();

        if (isChannel) {
            enableReactionsCell = new TextCheckCell(context);
            enableReactionsCell.setHeight(56);
            enableReactionsCell.setTextAndCheck(LocaleController.getString("EnableReactions", R.string.EnableReactions), !presenter.getChatReactions().isEmpty(), false);
            enableReactionsCell.setBackgroundColor(Theme.getColor(enableReactionsCell.isChecked() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked));
            enableReactionsCell.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            enableReactionsCell.setOnClickListener(v -> {
                setCheckedEnableReactionCell(enableReactionsCell.isChecked() ? ChatReactionsPresenter.SELECT_TYPE_NONE : ChatReactionsPresenter.SELECT_TYPE_SOME, true);
            });
            ll.addView(enableReactionsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        HeaderCell headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("AvailableReactions", R.string.AvailableReactions));

        contorlsLayout = new LinearLayout(context);
        contorlsLayout.setOrientation(LinearLayout.VERTICAL);
        allReactions = new RadioCell(context);
        allReactions.setText(LocaleController.getString("AllReactions", R.string.AllReactions), false, true);
        someReactions = new RadioCell(context);
        someReactions.setText(LocaleController.getString("SomeReactions", R.string.SomeReactions), false, true);
        disableReactions = new RadioCell(context);
        disableReactions.setText(LocaleController.getString("NoReactions", R.string.NoReactions), false, false);
        contorlsLayout.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        contorlsLayout.addView(allReactions, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        contorlsLayout.addView(someReactions, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        contorlsLayout.addView(disableReactions, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        radioCells.clear();
        radioCells.add(allReactions);
        radioCells.add(someReactions);
        radioCells.add(disableReactions);
        allReactions.setOnClickListener(v -> AndroidUtilities.runOnUIThread(() -> setCheckedEnableReactionCell(ChatReactionsPresenter.SELECT_TYPE_ALL, true)));
        someReactions.setOnClickListener(v -> AndroidUtilities.runOnUIThread(() -> setCheckedEnableReactionCell(ChatReactionsPresenter.SELECT_TYPE_SOME, true)));
        disableReactions.setOnClickListener(v -> AndroidUtilities.runOnUIThread(() -> setCheckedEnableReactionCell(ChatReactionsPresenter.SELECT_TYPE_NONE, true)));

        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        allReactions.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
        someReactions.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
        disableReactions.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));


        setCheckedEnableReactionCell(presenter.getStartFromType(), false);

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(listAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    default:
                    case TYPE_REACTION: {
                        return new RecyclerListView.Holder(new AvailableReactionCell(context, false, false));
                    }
                    case TYPE_INFO: {
                        TextInfoPrivacyCell infoCell = new TextInfoPrivacyCell(context);
                        return new RecyclerListView.Holder(infoCell);
                    }
                    case TYPE_HEADER: {
                        return new RecyclerListView.Holder(new HeaderCell(context, 23));
                    }
                    case TYPE_CONTROLS_CONTAINER:
                        FrameLayout frameLayout = new FrameLayout(context);
                        if (contorlsLayout.getParent() != null) {
                            ((ViewGroup) contorlsLayout.getParent()).removeView(contorlsLayout);
                        }
                        frameLayout.addView(contorlsLayout);
                        frameLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        return new RecyclerListView.Holder(frameLayout);

                }
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                switch (getItemViewType(position)) {
                    case TYPE_INFO:
                        TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                        infoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
                        if (isChannel) {
                            infoCell.setText(ChatObject.isChannelAndNotMegaGroup(presenter.getCurrentChat()) ? LocaleController.getString("EnableReactionsChannelInfo", R.string.EnableReactionsChannelInfo) :
                                    LocaleController.getString("EnableReactionsGroupInfo", R.string.EnableReactionsGroupInfo));
                        } else {
                            if (presenter.getSelectedType() == ChatReactionsPresenter.SELECT_TYPE_SOME) {
                                infoCell.setText(LocaleController.getString("EnableSomeReactionsInfo", R.string.EnableSomeReactionsInfo));
                            } else if (presenter.getSelectedType() == ChatReactionsPresenter.SELECT_TYPE_ALL) {
                                infoCell.setText(LocaleController.getString("EnableAllReactionsInfo", R.string.EnableAllReactionsInfo));
                            } else if (presenter.getSelectedType() == ChatReactionsPresenter.SELECT_TYPE_NONE) {
                                infoCell.setText(LocaleController.getString("DisableReactionsInfo", R.string.DisableReactionsInfo));
                            }
                        }
                        break;
                    case TYPE_HEADER:
                        HeaderCell headerCell = (HeaderCell) holder.itemView;
                        headerCell.setText(LocaleController.getString("OnlyAllowThisReactions", R.string.OnlyAllowThisReactions));
                        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                        break;
                    case TYPE_REACTION:
                        AvailableReactionCell reactionCell = (AvailableReactionCell) holder.itemView;
                        TLRPC.TL_availableReaction react = presenter.getAvailableReactions().get(position - (isChannel ? 2 : 3));
                        reactionCell.bind(react, presenter.getChatReactions().contains(react.reaction), currentAccount);
                        break;
                }
            }

            @Override
            public int getItemCount() {
                if (isChannel) {
                    return 1 + (!presenter.getChatReactions().isEmpty() ? 1 + presenter.getAvailableReactions().size() : 0);
                }
                return 1 + 1 + (!presenter.getChatReactions().isEmpty() ? 1 + presenter.getAvailableReactions().size() : 0);
            }

            @Override
            public int getItemViewType(int position) {
                if (isChannel) {
                    return position == 0 ? TYPE_INFO : position == 1 ? TYPE_HEADER : TYPE_REACTION;
                }
                if (position == 0) {
                    return TYPE_CONTROLS_CONTAINER;
                }
                return position == 1 ? TYPE_INFO : position == 2 ? TYPE_HEADER : TYPE_REACTION;
            }
        });
        listView.setOnItemClickListener((view, position) -> {
            if (position <= (isChannel ? 1 : 2)) return;

            AvailableReactionCell cell = (AvailableReactionCell) view;
            TLRPC.TL_availableReaction react = presenter.getAvailableReactions().get(position - (isChannel ? 2 : 3));
            boolean nc = presenter.toggleReaction(react.reaction);
            
            if (!nc && presenter.getChatReactions().isEmpty()) {
                setCheckedEnableReactionCell(ChatReactionsPresenter.SELECT_TYPE_NONE, true);
            }

            cell.setChecked(nc, true);
        });
        ll.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));
        fragmentView = contentView = ll;

        updateColors();

        return contentView;
    }

    private void setCheckedEnableReactionCell(int selectType, boolean animated) {
        if (presenter.getSelectedType() == selectType) {
            return;
        }
        if (enableReactionsCell != null) {
            boolean checked = selectType == ChatReactionsPresenter.SELECT_TYPE_SOME;
            enableReactionsCell.setChecked(checked);
            int clr = Theme.getColor(checked ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
            if (checked) {
                enableReactionsCell.setBackgroundColorAnimated(checked, clr);
            } else {
                enableReactionsCell.setBackgroundColorAnimatedReverse(clr);
            }
        }

        for (int i = 0; i < radioCells.size(); i++) {
            radioCells.get(i).setChecked(selectType == i, animated);
        }

        presenter.setSelectedType(selectType, animated, isChannel);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.onDestroy();
            presenter.detachView();
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.reactionsDidLoad);
    }


    public void setInfo(TLRPC.ChatFull info) {
        presenter.setInfo(info);
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return SimpleThemeDescription.createThemeDescriptions(this::updateColors,
                Theme.key_windowBackgroundWhite,
                Theme.key_windowBackgroundWhiteBlackText,
                Theme.key_windowBackgroundWhiteGrayText2,
                Theme.key_listSelector,
                Theme.key_windowBackgroundGray,
                Theme.key_windowBackgroundWhiteGrayText4,
                Theme.key_windowBackgroundWhiteRedText4,
                Theme.key_windowBackgroundChecked,
                Theme.key_windowBackgroundCheckText,
                Theme.key_switchTrackBlue,
                Theme.key_switchTrackBlueChecked,
                Theme.key_switchTrackBlueThumb,
                Theme.key_switchTrackBlueThumbChecked
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateColors() {
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        if (enableReactionsCell != null) {
            enableReactionsCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        }
        listAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (account != currentAccount) return;
        if (id == NotificationCenter.reactionsDidLoad) {
            presenter.onReactionsLoaded();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyItemChanged(int position) {
        if (listAdapter != null) {
            listAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        if (listAdapter != null) {
            listAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyItemRangeRemoved(int position, int count) {
        if (listAdapter != null) {
            listAdapter.notifyItemRangeRemoved(position, count);
        }
    }
}