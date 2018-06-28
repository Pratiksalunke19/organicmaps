package com.mapswithme.maps.subway;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapswithme.maps.R;
import com.mapswithme.maps.adapter.BottomSheetItem;
import com.mapswithme.maps.adapter.SpanningLinearLayoutManager;
import com.mapswithme.maps.bookmarks.OnItemClickListener;
import com.mapswithme.maps.traffic.widget.OnTrafficModeSelectListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ToggleMapLayerDialog extends DialogFragment
{
  @NonNull
  @SuppressWarnings("NullableProblems")
  private View mRoot;

  @NonNull
  @SuppressWarnings("NullableProblems")
  private ModeAdapter mAdapter;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    mRoot = inflater.inflate(R.layout.fragment_toggle_map_layer, null, false);
    dialog.setContentView(mRoot);
    initChildren();
    return dialog;
  }

  private void initChildren()
  {
    initCloseBtn();
    initRecycler();
  }

  private void initCloseBtn()
  {
    View closeBtn = mRoot.findViewById(R.id.сlose_btn);
    closeBtn.setOnClickListener(v -> dismiss());
  }

  private void initRecycler()
  {
    RecyclerView recycler = mRoot.findViewById(R.id.recycler);
    RecyclerView.LayoutManager layoutManager = new SpanningLinearLayoutManager(getContext(),
                                                                               LinearLayoutManager.HORIZONTAL,
                                                                               false);
    recycler.setLayoutManager(layoutManager);
    mAdapter = new ModeAdapter(createItems());
    recycler.setAdapter(mAdapter);
  }

  @NonNull
  private List<Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>>> createItems()
  {
    SubwayItemClickListener subwayListener = new SubwayItemClickListener();
    Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>> subway
        = new Pair<>(BottomSheetItem.Subway.makeInstance(), subwayListener);

    TrafficItemClickListener trafficListener = new TrafficItemClickListener();
    Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>> traffic
        = new Pair<>(BottomSheetItem.Traffic.makeInstance(), trafficListener);

    return Arrays.asList(subway, traffic);
  }

  public static void show(@NonNull AppCompatActivity activity)
  {
    ToggleMapLayerDialog frag = new ToggleMapLayerDialog();
    String tag = frag.getClass().getCanonicalName();
    FragmentManager fm = activity.getSupportFragmentManager();

    Fragment oldInstance = fm.findFragmentByTag(tag);
    if (oldInstance != null)
      fm.beginTransaction().remove(oldInstance).commit();

    frag.show(fm, tag);
    fm.executePendingTransactions();
  }

  private static class ModeAdapter extends RecyclerView.Adapter<ModeHolder>
  {
    @NonNull
    private final List<Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>>> mItems;

    private ModeAdapter(@NonNull List<Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>>> modes)
    {
      mItems = modes;
    }

    @Override
    public ModeHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View root = inflater.inflate(R.layout.item_bottomsheet_dialog, parent, false);
      return new ModeHolder(root);
    }

    @Override
    public void onBindViewHolder(ModeHolder holder, int position)
    {
      Context context = holder.itemView.getContext();
      Pair<BottomSheetItem, OnItemClickListener<BottomSheetItem>> pair = mItems.get(position);
      BottomSheetItem item = pair.first;
      holder.mItem = item;

      boolean isEnabled = item.getMode().isEnabled(context);

      holder.mButton.setSelected(isEnabled);
      holder.mTitle.setText(item.getTitle());
      holder.mButton.setImageResource(isEnabled ? item.getEnabledStateDrawable()
                                                : item.getDisabledStateDrawable());
      holder.mListener = pair.second;
    }

    @Override
    public int getItemCount()
    {
      return mItems.size();
    }
  }
  private static class ModeHolder extends RecyclerView.ViewHolder
  {
    @NonNull
    private final ImageButton mButton;
    @NonNull
    private final TextView mTitle;
    @Nullable
    private BottomSheetItem mItem;
    @Nullable
    private OnItemClickListener<BottomSheetItem> mListener;

    ModeHolder(@NonNull View root)
    {
      super(root);
      mButton = root.findViewById(R.id.btn);
      mTitle = root.findViewById(R.id.name);
      mButton.setOnClickListener(this::onItemClicked);
    }

    @NonNull
    public BottomSheetItem getItem()
    {
      return Objects.requireNonNull(mItem);
    }

    @NonNull
    public OnItemClickListener<BottomSheetItem> getListener()
    {
      return Objects.requireNonNull(mListener);
    }

    private void onItemClicked(@NonNull View v)
    {
      getListener().onItemClick(v, getItem());
    }
  }

  private abstract class DefaultClickListener implements OnItemClickListener<BottomSheetItem>
  {
    @Override
    public final void onItemClick(@NonNull View v, @NonNull BottomSheetItem item)
    {
      onItemClickInternal(v, item);
      mAdapter.notifyDataSetChanged();
    }

    abstract void onItemClickInternal(@NonNull View v, @NonNull BottomSheetItem item);
  }

  private class SubwayItemClickListener extends DefaultClickListener
  {
    @Override
    void onItemClickInternal(@NonNull View v, @NonNull BottomSheetItem item)
    {
      OnSubwayModeSelectListener listener = (OnSubwayModeSelectListener) getActivity();
      listener.onSubwayModeSelected();
    }
  }

  private class TrafficItemClickListener extends DefaultClickListener
  {
    @Override
    void onItemClickInternal(@NonNull View v, @NonNull BottomSheetItem item)
    {
      OnTrafficModeSelectListener listener = (OnTrafficModeSelectListener) getActivity();
      listener.onTrafficModeSelected();
    }
  }
}
