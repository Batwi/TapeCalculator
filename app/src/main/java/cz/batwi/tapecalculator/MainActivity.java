package cz.batwi.tapecalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(17, 20, 24);
    private static final int SURFACE = Color.rgb(29, 34, 40);
    private static final int TAPE = Color.rgb(246, 243, 237);
    private static final int INK = Color.rgb(30, 34, 36);
    private static final int MUTED = Color.rgb(151, 160, 169);
    private static final int NUMBER_KEY = Color.rgb(54, 61, 68);
    private static final int OPERATOR_KEY = Color.rgb(65, 76, 86);
    private static final int ACCENT = Color.rgb(145, 184, 162);
    private static final int ACCENT_DARK = Color.rgb(50, 76, 62);
    private static final int MEMORY = Color.rgb(220, 230, 222);

    private final ArrayList<HistoryEntry> history = new ArrayList<>();
    private final String[] memories = new String[5];
    private final ArrayList<Button> memoryButtons = new ArrayList<>();
    private CalculatorEngine engine;
    private SharedPreferences preferences;
    private LinearLayout historyContainer;
    private ScrollView historyScroll;
    private TextView display;
    private TextView expressionPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("calculator", MODE_PRIVATE);
        loadPersistedData();
        engine = new CalculatorEngine(this::addHistoryEntry);
        if (savedInstanceState != null) engine.restoreState(savedInstanceState.getBundle("engine"));
        View root = buildScreen();
        root.setFitsSystemWindows(true);
        setContentView(root);
        renderHistory();
        refreshDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("engine", engine.saveState());
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.addView(buildHeader(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        historyScroll = new ScrollView(this);
        historyScroll.setFillViewport(true);
        historyScroll.setBackground(makeBackground(TAPE, 16, 0));
        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        historyContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        historyScroll.addView(historyContainer, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams historyParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        historyParams.setMargins(dp(12), 0, dp(12), dp(10));
        root.addView(historyScroll, historyParams);

        root.addView(buildDisplay(), marginLp(ViewGroup.LayoutParams.MATCH_PARENT, dp(92), 12, 0, 12, 8));
        root.addView(buildMemoryStrip(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(76)));
        root.addView(buildKeyboard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(350)));
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), 0, dp(8), 0);
        TextView title = text("Pásková kalkulačka", 19, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button clearHistory = compactButton("Smazat pásku");
        clearHistory.setContentDescription("Smazat celou historii výpočtů");
        clearHistory.setOnClickListener(v -> confirmClearHistory());
        header.addView(clearHistory, lp(dp(122), dp(42)));
        return header;
    }

    private View buildDisplay() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        box.setBackground(makeBackground(SURFACE, 14, Color.rgb(63, 71, 79)));
        expressionPreview = text("", 14, MUTED);
        expressionPreview.setGravity(Gravity.END);
        expressionPreview.setSingleLine(true);
        expressionPreview.setEllipsize(TextUtils.TruncateAt.START);
        box.addView(expressionPreview, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(24)));
        display = text("0", 36, Color.WHITE);
        display.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        display.setSingleLine(true);
        display.setEllipsize(TextUtils.TruncateAt.START);
        display.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        box.addView(display, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        return box;
    }

    private View buildMemoryStrip() {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        TextView hint = text("Paměť — klepnout: vložit  •  podržet: uložit", 12, MUTED);
        hint.setPadding(dp(16), 0, dp(12), 0);
        wrapper.addView(hint, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(22)));
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(dp(8), 0, dp(8), dp(6));
        for (int i = 0; i < memories.length; i++) {
            final int slot = i;
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            button.setTextColor(INK);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(8), 0, dp(8), 0);
            button.setBackground(makeBackground(MEMORY, 12, 0));
            button.setOnClickListener(v -> recallMemory(slot));
            button.setOnLongClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                storeMemory(slot);
                return true;
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(116), dp(48));
            params.setMargins(dp(4), 0, dp(4), 0);
            row.addView(button, params);
            memoryButtons.add(button);
        }
        scroll.addView(row, lp(ViewGroup.LayoutParams.WRAP_CONTENT, dp(54)));
        wrapper.addView(scroll, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        updateMemoryButtons();
        return wrapper;
    }

    private View buildKeyboard() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(5);
        grid.setPadding(dp(8), 0, dp(8), dp(8));
        List<String> labels = Arrays.asList(
                "⌫", "C", "^", "÷",
                "7", "8", "9", "×",
                "4", "5", "6", "−",
                "1", "2", "3", "+",
                "±", "0", ",", "="
        );
        for (String label : labels) addKey(grid, label);
        return grid;
    }

    private void addKey(GridLayout grid, String label) {
        Button key = new Button(this);
        key.setText(label);
        key.setTextSize(TypedValue.COMPLEX_UNIT_SP, label.equals("=") ? 28 : 24);
        key.setTextColor(label.equals("=") ? Color.rgb(19, 31, 24) : Color.WHITE);
        key.setGravity(Gravity.CENTER);
        key.setPadding(0, 0, 0, 0);
        int color = label.matches("[0-9,±⌫C]") ? NUMBER_KEY : OPERATOR_KEY;
        if (label.equals("=")) color = ACCENT;
        key.setBackground(makeBackground(color, 12, 0));
        key.setOnClickListener(v -> {
            handleKey(label);
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        });
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        grid.addView(key, params);
    }

    private void handleKey(String label) {
        if (label.matches("[0-9]")) engine.digit(label);
        else if (label.equals(",")) engine.decimalPoint();
        else if (label.equals("±")) engine.toggleSign();
        else if (label.equals("⌫")) engine.backspace();
        else if (label.equals("C")) engine.clear();
        else if (label.equals("=")) engine.equalsPressed();
        else engine.operator(label);
        refreshDisplay();
    }

    private void addHistoryEntry(HistoryEntry entry) {
        history.add(entry);
        boolean trimmed = false;
        while (history.size() > 100) {
            history.remove(0);
            trimmed = true;
        }
        saveHistory();
        if (trimmed) renderHistory();
        else addHistoryRow(entry, history.size());
        historyScroll.post(() -> historyScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void renderHistory() {
        historyContainer.removeAllViews();
        if (history.isEmpty()) {
            TextView empty = text("Tady se bude zapisovat postup výpočtu.\nKlepnutím na kterékoliv číslo ho znovu vložíš.", 15, Color.rgb(95, 101, 104));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20), dp(36), dp(20), dp(36));
            historyContainer.addView(empty, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        for (int i = 0; i < history.size(); i++) addHistoryRow(history.get(i), i + 1);
        historyScroll.post(() -> historyScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addHistoryRow(HistoryEntry entry, int index) {
        if (historyContainer.getChildCount() == 1 && history.size() == 1) historyContainer.removeAllViews();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(10));
        TextView number = text(Integer.toString(index), 11, Color.rgb(118, 122, 122));
        row.addView(number, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));

        HorizontalScrollView expressionScroll = new HorizontalScrollView(this);
        expressionScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tokens = new LinearLayout(this);
        tokens.setGravity(Gravity.CENTER_VERTICAL);
        for (String token : entry.getTokens()) {
            if (CalculatorEngine.isOperator(token)) {
                TextView operator = text(token, 20, INK);
                operator.setGravity(Gravity.CENTER);
                tokens.addView(operator, lp(dp(34), dp(42)));
            } else {
                tokens.addView(numberChip(token, false), marginLp(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40), 2, 1, 2, 1));
            }
        }
        TextView equals = text("=", 20, INK);
        equals.setGravity(Gravity.CENTER);
        tokens.addView(equals, lp(dp(34), dp(42)));
        expressionScroll.addView(tokens, lp(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)));
        row.addView(expressionScroll, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        Button result = numberChip(entry.getResult(), true);
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        resultParams.gravity = Gravity.END;
        row.addView(result, resultParams);
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(218, 215, 208));
        row.addView(divider, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        historyContainer.addView(row, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private Button numberChip(String value, boolean result) {
        Button chip = new Button(this);
        chip.setAllCaps(false);
        chip.setMinWidth(0);
        chip.setMinimumWidth(0);
        chip.setMinHeight(0);
        chip.setMinimumHeight(0);
        chip.setPadding(dp(12), 0, dp(12), 0);
        chip.setText(NumberFormatter.format(value));
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, result ? 21 : 18);
        chip.setTextColor(result ? Color.WHITE : INK);
        chip.setTypeface(Typeface.create("sans-serif", result ? Typeface.BOLD : Typeface.NORMAL));
        chip.setBackground(makeBackground(result ? ACCENT_DARK : Color.rgb(231, 228, 219), 18, 0));
        chip.setContentDescription("Vložit číslo " + NumberFormatter.format(value));
        chip.setOnClickListener(v -> {
            engine.pasteValue(value);
            refreshDisplay();
            Toast.makeText(this, "Číslo vloženo", Toast.LENGTH_SHORT).show();
        });
        return chip;
    }

    private void refreshDisplay() {
        display.setText(NumberFormatter.format(engine.getInput()));
        expressionPreview.setText(engine.getExpressionPreview());
    }

    private void recallMemory(int slot) {
        if (memories[slot] == null) {
            Toast.makeText(this, "M" + (slot + 1) + " je prázdná. Podržením do ní uložíš číslo.", Toast.LENGTH_SHORT).show();
            return;
        }
        engine.pasteValue(memories[slot]);
        refreshDisplay();
        Toast.makeText(this, "Vloženo z M" + (slot + 1), Toast.LENGTH_SHORT).show();
    }

    private void storeMemory(int slot) {
        if (engine.isError()) return;
        memories[slot] = engine.getInput().endsWith(".")
                ? engine.getInput().substring(0, engine.getInput().length() - 1)
                : engine.getInput();
        preferences.edit().putString("memory_" + slot, memories[slot]).apply();
        updateMemoryButtons();
        Toast.makeText(this, "Uloženo do M" + (slot + 1), Toast.LENGTH_SHORT).show();
    }

    private void updateMemoryButtons() {
        for (int i = 0; i < memoryButtons.size(); i++) {
            String value = memories[i];
            memoryButtons.get(i).setText("M" + (i + 1) + "\n" + (value == null ? "prázdná" : abbreviate(NumberFormatter.format(value))));
            memoryButtons.get(i).setContentDescription(value == null
                    ? "Paměť M" + (i + 1) + " je prázdná"
                    : "Paměť M" + (i + 1) + ", " + NumberFormatter.format(value));
        }
    }

    private String abbreviate(String value) {
        return value.length() <= 12 ? value : value.substring(0, 11) + "…";
    }

    private void confirmClearHistory() {
        if (history.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Smazat celou pásku?")
                .setMessage("Uložené paměti M1–M5 zůstanou zachované.")
                .setNegativeButton("Zrušit", null)
                .setPositiveButton("Smazat", (dialog, which) -> {
                    history.clear();
                    saveHistory();
                    renderHistory();
                })
                .show();
    }

    private void loadPersistedData() {
        for (int i = 0; i < memories.length; i++) memories[i] = preferences.getString("memory_" + i, null);
        try {
            JSONArray array = new JSONArray(preferences.getString("history", "[]"));
            int start = Math.max(0, array.length() - 100);
            for (int i = start; i < array.length(); i++) history.add(HistoryEntry.fromJson(array.getJSONObject(i)));
        } catch (JSONException ignored) {
            preferences.edit().remove("history").apply();
        }
    }

    private void saveHistory() {
        JSONArray array = new JSONArray();
        try {
            for (HistoryEntry entry : history) array.put(entry.toJson());
            preferences.edit().putString("history", array.toString()).apply();
        } catch (JSONException ignored) {
            Toast.makeText(this, "Pásku se nepodařilo uložit", Toast.LENGTH_SHORT).show();
        }
    }

    private Button compactButton(String label) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setTextColor(Color.WHITE);
        button.setPadding(dp(10), 0, dp(10), 0);
        button.setBackground(makeBackground(Color.rgb(48, 54, 61), 10, 0));
        return button;
    }

    private TextView text(String value, int sp, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        text.setTextColor(color);
        return text;
    }

    private GradientDrawable makeBackground(int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeColor != 0) drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private LinearLayout.LayoutParams marginLp(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = lp(width, height);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
