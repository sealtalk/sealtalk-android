package io.rong.callkit.util;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.rong.callkit.R;


public class CallKitSearchBarView extends RelativeLayout {

    private EditText editSearch;
    private View clearBtn;
    private ImageView searchIV;
    private CallKitSearchBarListener listener;
    private Handler handler;
    private boolean searchContentCleared;

    public CallKitSearchBarView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.callkit_view_search_bar_layout, this);
        searchIV = findViewById(R.id.iv_icon);
        editSearch = findViewById(R.id.et_search);
        handler = new Handler();
        editSearch.addTextChangedListener(new TextWatcher() {
            Runnable searchRunnable = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    searchIV.setImageDrawable(getResources().getDrawable(R.drawable.callkit_ic_search_focused_x));
                    clearBtn.setVisibility(VISIBLE);
                } else {
                    searchIV.setImageDrawable(getResources().getDrawable(R.drawable.callkit_ic_search_x));
                    clearBtn.setVisibility(GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (listener == null) {
                    return;
                }
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                final String keywords = editSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(keywords)) {
                    searchContentCleared = false;
                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            listener.onSearchStart(keywords);
                        }
                    };
                    handler.postDelayed(searchRunnable, 500);
                } else {
                    if (!searchContentCleared) {
                        listener.onClearButtonClick();
                    }
                }
            }
        });
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    listener.onSoftSearchKeyClick();
                }
                return false;
            }
        });
        clearBtn = findViewById(R.id.iv_clear);
        clearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearch.setText("");
                searchIV.setImageDrawable(getResources().getDrawable(R.drawable.callkit_ic_search_x));
                clearBtn.setVisibility(GONE);
                listener.onClearButtonClick();
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editSearch, InputMethodManager.HIDE_NOT_ALWAYS);
                listener.onSoftSearchKeyClick();
            }
        });
    }

    boolean isSearchTextEmpty() {
        return editSearch.getText().toString().equals("");
    }

    public void setSearchBarListener(CallKitSearchBarListener listener) {
        this.listener = listener;
    }

    public void clearSearchContent() {
        searchContentCleared = true;
        editSearch.setText("");
    }

    public void setSearchHint(String text) {
        editSearch.setHint(text);
    }

    void setSearchText(String content) {
        if (TextUtils.isEmpty(content)) {
            clearBtn.setVisibility(GONE);
            return;
        }
        editSearch.setText(content);
        editSearch.setSelection(content.length());
        searchIV.setImageDrawable(getResources().getDrawable(R.drawable.callkit_ic_search_focused_x));
        clearBtn.setVisibility(VISIBLE);
    }

    public EditText getEditText() {
        return editSearch;
    }
}
