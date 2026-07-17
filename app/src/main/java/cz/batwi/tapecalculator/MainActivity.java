package cz.batwi.tapecalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
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
    private static final int KEY_INK = Color.rgb(27, 31, 28);
    private static final String[] PALETTE_NAMES = {
            "Pastel orange and green",
            "Warm apricot",
            "Calm teal",
            "Original dark"
    };
    private static final int[][] PALETTES = {
            {Color.rgb(232, 187, 128), Color.rgb(164, 201, 169), Color.rgb(121, 171, 136),
                    Color.rgb(220, 235, 221), Color.rgb(244, 220, 183), Color.rgb(54, 91, 67),
                    Color.rgb(224, 166, 105)},
            {Color.rgb(239, 188, 151), Color.rgb(215, 196, 144), Color.rgb(190, 143, 87),
                    Color.rgb(239, 226, 196), Color.rgb(248, 221, 199), Color.rgb(119, 83, 52),
                    Color.rgb(225, 151, 105)},
            {Color.rgb(157, 201, 205), Color.rgb(165, 205, 181), Color.rgb(105, 165, 144),
                    Color.rgb(217, 235, 229), Color.rgb(211, 232, 230), Color.rgb(45, 91, 82),
                    Color.rgb(112, 172, 173)},
            {Color.rgb(54, 61, 68), Color.rgb(65, 76, 86), Color.rgb(145, 184, 162),
                    Color.rgb(220, 230, 222), Color.rgb(231, 228, 219), Color.rgb(50, 76, 62),
                    Color.rgb(48, 54, 61)}
    };

    private static final String[] COLOR_TARGET_NAMES = {
            "Number keys",
            "Operator keys",
            "Equals key",
            "Memory buttons",
            "Tape values",
            "Tape results",
            "Header buttons"
    };

    private static final int PALETTE_NUMBER_KEY = 0;
    private static final int PALETTE_OPERATOR_KEY = 1;
    private static final int PALETTE_EQUAL_KEY = 2;
    private static final int PALETTE_MEMORY = 3;
    private static final int PALETTE_TAPE_NUMBER = 4;
    private static final int PALETTE_TAPE_RESULT = 5;
    private static final int PALETTE_HEADER_ACTION = 6;

    private final ArrayList<HistoryEntry> history = new ArrayList<>();
    private final String[] memories = new String[5];
    private final ArrayList<Button> memoryButtons = new ArrayList<>();
    private CalculatorEngine engine;
    private SharedPreferences preferences;
    private LinearLayout historyContainer;
    private ScrollView historyScroll;
    private TextView display;
    private TextView expressionPreview;
    private int selectedPalette;
    private int[] palette;
    private boolean customPalette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("calculator", MODE_PRIVATE);
        loadPalette();
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
        root.addView(buildHeader(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

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

        root.addView(buildDisplay(), marginLp(ViewGroup.LayoutParams.MATCH_PARENT, dp(68), 12, 0, 12, 7));
        root.addView(buildMemoryStrip(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(72)));
        root.addView(buildKeyboard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(310)));
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), 0, dp(8), 0);
        LinearLayout branding = new LinearLayout(this);
        branding.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Tape Calculator", 18, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        branding.addView(title, lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView author = text("Autor: MiK", 10, Color.rgb(190, 198, 205));
        author.setPadding(dp(7), dp(4), 0, 0);
        branding.addView(author, lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.addView(branding, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        Button colors = compactButton("Colors");
        colors.setContentDescription("Choose a color palette");
        colors.setOnClickListener(v -> showPaletteDialog());
        LinearLayout.LayoutParams colorsParams = lp(dp(64), dp(40));
        colorsParams.setMargins(0, 0, dp(5), 0);
        header.addView(colors, colorsParams);
        Button clearHistory = compactButton("Clear");
        clearHistory.setContentDescription("Clear the entire calculation tape");
        clearHistory.setOnClickListener(v -> confirmClearHistory());
        header.addView(clearHistory, lp(dp(64), dp(40)));
        return header;
    }

    private View buildDisplay() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        box.setPadding(dp(16), dp(4), dp(16), dp(4));
        box.setBackground(makeBackground(SURFACE, 14, Color.rgb(63, 71, 79)));
        expressionPreview = text("", 12, MUTED);
        expressionPreview.setGravity(Gravity.END);
        expressionPreview.setSingleLine(true);
        expressionPreview.setEllipsize(TextUtils.TruncateAt.START);
        box.addView(expressionPreview, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(16)));
        display = text("0", 36, Color.WHITE);
        display.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        display.setSingleLine(true);
        display.setEllipsize(TextUtils.TruncateAt.START);
        display.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        box.addView(display, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        return box;
    }

    private View buildMemoryStrip() {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        TextView hint = text("Memory — tap: recall  •  hold: store", 12, MUTED);
        hint.setPadding(dp(16), 0, dp(12), 0);
        wrapper.addView(hint, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(20)));
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(dp(8), 0, dp(8), dp(6));
        for (int i = 0; i < memories.length; i++) {
            final int slot = i;
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            button.setTextColor(contrastTextColor(palette[PALETTE_MEMORY]));
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(8), 0, dp(8), 0);
            button.setBackground(makeBackground(palette[PALETTE_MEMORY], 12, 0));
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
        scroll.addView(row, lp(ViewGroup.LayoutParams.WRAP_CONTENT, dp(52)));
        wrapper.addView(scroll, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
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
        key.setGravity(Gravity.CENTER);
        key.setPadding(0, 0, 0, 0);
        int color = label.matches("[0-9,±⌫C]") ? palette[PALETTE_NUMBER_KEY] : palette[PALETTE_OPERATOR_KEY];
        if (label.equals("=")) color = palette[PALETTE_EQUAL_KEY];
        key.setTextColor(contrastTextColor(color));
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
            TextView empty = text("Your calculations will appear here.\nTap any number to reuse it.", 15, Color.rgb(95, 101, 104));
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

        WrappingTokenLayout tokens = new WrappingTokenLayout();
        tokens.setPadding(0, 0, 0, dp(2));
        for (String token : entry.getTokens()) {
            if (CalculatorEngine.isOperator(token)) {
                TextView operator = text(token, 20, INK);
                operator.setGravity(Gravity.CENTER);
                tokens.addView(operator, marginLp(dp(30), dp(40), 1, 1, 1, 1));
            } else {
                tokens.addView(numberChip(token, false), marginLp(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40), 2, 1, 2, 1));
            }
        }
        TextView equals = text("=", 20, INK);
        equals.setGravity(Gravity.CENTER);
        tokens.addView(equals, marginLp(dp(30), dp(40), 1, 1, 1, 1));
        row.addView(tokens, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
        int chipColor = result ? palette[PALETTE_TAPE_RESULT] : palette[PALETTE_TAPE_NUMBER];
        chip.setTextColor(contrastTextColor(chipColor));
        chip.setTypeface(Typeface.create("sans-serif", result ? Typeface.BOLD : Typeface.NORMAL));
        chip.setBackground(makeBackground(chipColor, 18, 0));
        chip.setContentDescription("Insert number " + NumberFormatter.format(value));
        chip.setOnClickListener(v -> {
            engine.pasteValue(value);
            refreshDisplay();
            Toast.makeText(this, "Number inserted", Toast.LENGTH_SHORT).show();
        });
        return chip;
    }

    private void refreshDisplay() {
        display.setText(NumberFormatter.format(engine.getInput()));
        String preview = engine.getExpressionPreview();
        boolean showHint = TextUtils.isEmpty(preview);
        expressionPreview.setText(showHint
                ? "Tap any tape value to reuse it in a calculation."
                : preview);
        expressionPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, showHint ? 10 : 12);
    }

    private void recallMemory(int slot) {
        if (memories[slot] == null) {
            Toast.makeText(this, "M" + (slot + 1) + " is empty. Hold it to store a number.", Toast.LENGTH_SHORT).show();
            return;
        }
        engine.pasteValue(memories[slot]);
        refreshDisplay();
        Toast.makeText(this, "Recalled from M" + (slot + 1), Toast.LENGTH_SHORT).show();
    }

    private void storeMemory(int slot) {
        if (engine.isError()) return;
        memories[slot] = engine.getInput().endsWith(".")
                ? engine.getInput().substring(0, engine.getInput().length() - 1)
                : engine.getInput();
        preferences.edit().putString("memory_" + slot, memories[slot]).apply();
        updateMemoryButtons();
        Toast.makeText(this, "Stored in M" + (slot + 1), Toast.LENGTH_SHORT).show();
    }

    private void updateMemoryButtons() {
        for (int i = 0; i < memoryButtons.size(); i++) {
            String value = memories[i];
            memoryButtons.get(i).setText("M" + (i + 1) + "\n" + (value == null ? "empty" : abbreviate(NumberFormatter.format(value))));
            memoryButtons.get(i).setContentDescription(value == null
                    ? "Memory M" + (i + 1) + " is empty"
                    : "Memory M" + (i + 1) + ", " + NumberFormatter.format(value));
        }
    }

    private String abbreviate(String value) {
        return value.length() <= 12 ? value : value.substring(0, 11) + "…";
    }

    private void confirmClearHistory() {
        if (history.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Clear the entire tape?")
                .setMessage("Stored memories M1–M5 will be preserved.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    history.clear();
                    saveHistory();
                    renderHistory();
                })
                .show();
    }

    private void showPaletteDialog() {
        String[] choices = Arrays.copyOf(PALETTE_NAMES, PALETTE_NAMES.length + 1);
        choices[choices.length - 1] = "Custom colors…";
        int checked = customPalette ? PALETTES.length : selectedPalette;
        new AlertDialog.Builder(this)
                .setTitle("Color palette")
                .setSingleChoiceItems(choices, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == PALETTES.length) {
                        showColorTargetDialog();
                    } else if (customPalette || which != selectedPalette) {
                        preferences.edit()
                                .putInt("palette", which)
                                .putBoolean("custom_palette", false)
                                .apply();
                        recreate();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showColorTargetDialog() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), dp(8), dp(18), dp(8));
        for (int i = 0; i < COLOR_TARGET_NAMES.length; i++) {
            final int target = i;
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(COLOR_TARGET_NAMES[i]);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            button.setTextColor(contrastTextColor(palette[i]));
            button.setBackground(makeBackground(palette[i], 12, 0));
            button.setOnClickListener(v -> showColorPickerDialog(target));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
            params.setMargins(0, dp(5), 0, dp(5));
            list.addView(button, params);
        }
        scroll.addView(list, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(this)
                .setTitle("Customize colors")
                .setMessage("Choose the part you want to recolor.")
                .setView(scroll)
                .setNegativeButton("Close", null)
                .show();
    }

    private void showColorPickerDialog(int target) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(6), dp(20), 0);

        TextView help = text("Choose a shade in the large field, then adjust its base color on the rainbow bar.", 12, Color.rgb(85, 90, 94));
        content.addView(help, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView value = text(formatColor(palette[target]), 14, INK);
        value.setGravity(Gravity.CENTER);
        value.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(value, marginLp(ViewGroup.LayoutParams.MATCH_PARENT, dp(34), 0, 6, 0, 4));

        ColorPickerView picker = new ColorPickerView(palette[target]);
        picker.setContentDescription("Color picker for " + COLOR_TARGET_NAMES[target]);
        picker.setOnColorChangedListener(color -> value.setText(formatColor(color)));
        content.addView(picker, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(270)));

        new AlertDialog.Builder(this)
                .setTitle(COLOR_TARGET_NAMES[target])
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", (dialog, which) -> saveCustomColor(target, picker.getColor()))
                .show();
    }

    private void loadPalette() {
        selectedPalette = Math.max(0, Math.min(preferences.getInt("palette", 0), PALETTES.length - 1));
        palette = Arrays.copyOf(PALETTES[selectedPalette], PALETTES[selectedPalette].length);
        customPalette = preferences.getBoolean("custom_palette", false);
        if (customPalette) {
            for (int i = 0; i < palette.length; i++) {
                palette[i] = preferences.getInt("custom_color_" + i, palette[i]);
            }
        }
    }

    private void saveCustomColor(int target, int color) {
        SharedPreferences.Editor editor = preferences.edit().putBoolean("custom_palette", true);
        for (int i = 0; i < palette.length; i++) {
            editor.putInt("custom_color_" + i, i == target ? color : palette[i]);
        }
        editor.apply();
        recreate();
    }

    private String formatColor(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private int contrastTextColor(int background) {
        double luminance = (0.299 * Color.red(background)
                + 0.587 * Color.green(background)
                + 0.114 * Color.blue(background)) / 255.0;
        return luminance > 0.58 ? KEY_INK : Color.WHITE;
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
            Toast.makeText(this, "The tape could not be saved", Toast.LENGTH_SHORT).show();
        }
    }

    private Button compactButton(String label) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setTextColor(contrastTextColor(palette[PALETTE_HEADER_ACTION]));
        button.setPadding(dp(10), 0, dp(10), 0);
        button.setBackground(makeBackground(palette[PALETTE_HEADER_ACTION], 10, 0));
        return button;
    }

    private interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    /** A native HSV picker: saturation/value field with a separate rainbow hue bar. */
    private final class ColorPickerView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] hsv = new float[3];
        private final int hueBarHeight = dp(28);
        private final int gap = dp(14);
        private OnColorChangedListener listener;
        private int fieldBottom;

        ColorPickerView(int color) {
            super(MainActivity.this);
            Color.colorToHSV(color, hsv);
            setFocusable(true);
        }

        void setOnColorChangedListener(OnColorChangedListener listener) {
            this.listener = listener;
        }

        int getColor() {
            return Color.HSVToColor(hsv);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            fieldBottom = Math.max(1, getHeight() - hueBarHeight - gap);
            int hueColor = Color.HSVToColor(new float[]{hsv[0], 1f, 1f});

            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, width, 0,
                    Color.WHITE, hueColor, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, fieldBottom, paint);
            paint.setShader(new LinearGradient(0, 0, 0, fieldBottom,
                    Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, fieldBottom, paint);

            int barTop = fieldBottom + gap;
            int[] rainbow = {
                    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                    Color.BLUE, Color.MAGENTA, Color.RED
            };
            paint.setShader(new LinearGradient(0, barTop, width, barTop,
                    rainbow, null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(0, barTop, width, getHeight(), dp(7), dp(7), paint);
            paint.setShader(null);

            float colorX = hsv[1] * width;
            float colorY = (1f - hsv[2]) * fieldBottom;
            drawMarker(canvas, colorX, colorY, dp(9));
            float hueX = (hsv[0] / 360f) * width;
            drawMarker(canvas, hueX, barTop + hueBarHeight / 2f, dp(8));
        }

        private void drawMarker(Canvas canvas, float x, float y, float radius) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(4));
            paint.setColor(Color.rgb(32, 35, 38));
            canvas.drawCircle(x, y, radius, paint);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, radius, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                getParent().requestDisallowInterceptTouchEvent(true);
                float x = Math.max(0, Math.min(event.getX(), getWidth()));
                float y = event.getY();
                if (y <= fieldBottom) {
                    hsv[1] = x / Math.max(1, getWidth());
                    hsv[2] = 1f - Math.max(0, Math.min(y, fieldBottom)) / Math.max(1, fieldBottom);
                } else if (y >= fieldBottom + gap) {
                    hsv[0] = 360f * x / Math.max(1, getWidth());
                    if (hsv[0] >= 360f) hsv[0] = 0f;
                }
                invalidate();
                if (listener != null) listener.onColorChanged(getColor());
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(false);
                performClick();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return super.onTouchEvent(event);
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }

    /**
     * A small dependency-free flow layout for tape tokens. Long calculations wrap onto
     * additional lines while every number remains an individually clickable button.
     */
    private final class WrappingTokenLayout extends ViewGroup {
        WrappingTokenLayout() {
            super(MainActivity.this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int rightEdge = width - getPaddingRight();
            int x = getPaddingLeft();
            int y = getPaddingTop();
            int lineHeight = 0;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
                if (x > getPaddingLeft() && x + childWidth > rightEdge) {
                    x = getPaddingLeft();
                    y += lineHeight;
                    lineHeight = 0;
                }
                x += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            int desiredHeight = y + lineHeight + getPaddingBottom();
            setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int rightEdge = right - left - getPaddingRight();
            int x = getPaddingLeft();
            int y = getPaddingTop();
            int lineHeight = 0;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
                if (x > getPaddingLeft() && x + childWidth > rightEdge) {
                    x = getPaddingLeft();
                    y += lineHeight;
                    lineHeight = 0;
                }
                int childLeft = x + params.leftMargin;
                int childTop = y + params.topMargin;
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
                x += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        @Override
        protected LayoutParams generateLayoutParams(LayoutParams params) {
            return new MarginLayoutParams(params);
        }

        @Override
        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new MarginLayoutParams(getContext(), attrs);
        }

        @Override
        protected boolean checkLayoutParams(LayoutParams params) {
            return params instanceof MarginLayoutParams;
        }
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
