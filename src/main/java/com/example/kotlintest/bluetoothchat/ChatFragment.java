package com.example.kotlintest.bluetoothchat;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kotlintest.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BluetoothMainActivity bluetoothMainActivity;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    private MessageItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothMainActivity = (BluetoothMainActivity) getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private static final String TAG = "ChatFragment";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.removeAllItems();
        if (view != null && bluetoothMainActivity != null) {
            bluetoothMainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            InputMethodManager imm = (InputMethodManager) bluetoothMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private View view;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        TextInputLayout textInputLayout = view.findViewById(R.id.chatContentTextInput);
        RecyclerView recyclerView = view.findViewById(R.id.chatContentMsg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bluetoothMainActivity) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };

        linearLayoutManager.requestSimpleAnimationsInNextLayout();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        EditText editText = textInputLayout.getEditText();
        textInputLayout.setEndIconOnClickListener(v -> {
            if (editText.getText().toString().trim().length() > 0) {
                bluetoothMainActivity.transferText(editText.getText().toString());
                editText.setText("");
            }
        });

        getParentFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENT_RECEIVED_REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d(TAG, "onFragmentResult: " + result.getString(Constants.KEY_MESSAGE_RECEIVED + " " + result));
                adapter.addToListItem(result.getString(Constants.KEY_MESSAGE_RECEIVED), Constants.MESSAGE_WRITE);
            }
        });
        getParentFragmentManager().setFragmentResultListener(Constants.KEY_FRAGMENT_SENT_REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d(TAG, "onFragmentResult: " + result.getString(Constants.KEY_MESSAGE_SENT + " " + result));
                adapter.addToListItem(result.getString(Constants.KEY_MESSAGE_SENT), Constants.MESSAGE_READ);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        adapter = new MessageItemAdapter();

        bluetoothMainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    private static class MessageItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Pair<String, Integer>> pairList = new ArrayList<>();

        public void removeAllItems() {
            pairList.clear();
        }

        @Override
        public int getItemViewType(int position) {
            return pairList.get(position).second;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Constants.MESSAGE_WRITE)
                return new ReceivedItemsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_outgoing_textview, parent, false));
            else
                return new SentItemsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_incoming_textview, parent, false));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: " + pairList.get(position) + " " + holder);
            if (holder instanceof ReceivedItemsHolder) {
                ReceivedItemsHolder h = (ReceivedItemsHolder) holder;
                h.textView.setText(pairList.get(position).first);
            } else if (holder instanceof SentItemsHolder) {
                SentItemsHolder h = (SentItemsHolder) holder;
                h.textView.setText(pairList.get(position).first);
            }
        }

        public void addToListItem(String s, Integer i) {
            pairList.add(new Pair<>(s, i));
            notifyItemInserted(pairList.size() - 1);
        }

        @Override
        public int getItemCount() {
            return pairList.size();
        }

        public static class SentItemsHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            public SentItemsHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.commonText);
            }
        }

        public static class ReceivedItemsHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            public ReceivedItemsHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.commonText);
            }
        }
    }
}