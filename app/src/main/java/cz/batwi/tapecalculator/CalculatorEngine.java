package cz.batwi.tapecalculator;

import android.os.Bundle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public final class CalculatorEngine {
    public interface HistoryListener {
        void onHistoryEntry(HistoryEntry entry);
    }

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final String ERROR = "Error";
    private static final String LEGACY_ERROR = "Chyba";

    private String input = "0";
    private BigDecimal accumulated;
    private String pendingOperator;
    private final ArrayList<String> expressionTokens = new ArrayList<>();
    private boolean waitingForOperand;
    private boolean justEvaluated;
    private final HistoryListener historyListener;

    public CalculatorEngine(HistoryListener historyListener) {
        this.historyListener = historyListener;
    }

    public void digit(String digit) {
        if (isError() || waitingForOperand || justEvaluated) {
            if (justEvaluated) resetChain();
            input = digit;
            waitingForOperand = false;
            justEvaluated = false;
            return;
        }
        String unsigned = input.startsWith("-") ? input.substring(1) : input;
        if (unsigned.replace(".", "").length() >= 18) return;
        if (input.equals("0")) input = digit;
        else if (input.equals("-0")) input = "-" + digit;
        else input += digit;
    }

    public void decimalPoint() {
        if (isError() || waitingForOperand || justEvaluated) {
            if (justEvaluated) resetChain();
            input = "0.";
            waitingForOperand = false;
            justEvaluated = false;
        } else if (!input.contains(".")) {
            input += ".";
        }
    }

    public void toggleSign() {
        if (isError()) return;
        if (input.startsWith("-")) input = input.substring(1);
        else if (!input.equals("0")) input = "-" + input;
    }

    public void backspace() {
        if (isError() || waitingForOperand || justEvaluated) return;
        if (input.length() <= 1 || (input.startsWith("-") && input.length() == 2)) input = "0";
        else input = input.substring(0, input.length() - 1);
    }

    public void clear() {
        input = "0";
        resetChain();
    }

    public void operator(String operator) {
        if (isError()) {
            clear();
            return;
        }
        if (pendingOperator != null && waitingForOperand) {
            pendingOperator = operator;
            expressionTokens.set(expressionTokens.size() - 1, operator);
            return;
        }

        BigDecimal operand = parseInput();
        if (pendingOperator == null) {
            accumulated = operand;
            expressionTokens.clear();
            expressionTokens.add(canonical(operand));
        } else {
            expressionTokens.add(canonical(operand));
            try {
                accumulated = calculate(accumulated, operand, pendingOperator);
                input = canonical(accumulated);
            } catch (ArithmeticException exception) {
                setError();
                return;
            }
        }
        pendingOperator = operator;
        expressionTokens.add(operator);
        waitingForOperand = true;
        justEvaluated = false;
    }

    public void equalsPressed() {
        if (isError() || pendingOperator == null || waitingForOperand) return;
        BigDecimal operand = parseInput();
        expressionTokens.add(canonical(operand));
        try {
            BigDecimal result = calculate(accumulated, operand, pendingOperator);
            String canonicalResult = canonical(result);
            historyListener.onHistoryEntry(new HistoryEntry(expressionTokens, canonicalResult));
            input = canonicalResult;
            accumulated = result;
            pendingOperator = null;
            expressionTokens.clear();
            waitingForOperand = false;
            justEvaluated = true;
        } catch (ArithmeticException exception) {
            setError();
        }
    }

    public void squareRoot() {
        if (isError()) {
            clear();
            return;
        }

        BigDecimal operand = parseInput();
        try {
            BigDecimal result = squareRootValue(operand);
            String canonicalResult = canonical(result);
            ArrayList<String> tokens = new ArrayList<>();
            tokens.add("√");
            tokens.add(canonical(operand));
            historyListener.onHistoryEntry(new HistoryEntry(tokens, canonicalResult));
            input = canonicalResult;
            waitingForOperand = false;

            if (pendingOperator == null) {
                accumulated = null;
                expressionTokens.clear();
                justEvaluated = true;
            } else {
                justEvaluated = false;
            }
        } catch (ArithmeticException exception) {
            setError();
        }
    }

    public void pasteValue(String value) {
        try {
            BigDecimal parsed = new BigDecimal(value, MC);
            if (justEvaluated && pendingOperator == null) resetChain();
            input = canonical(parsed);
            waitingForOperand = false;
            justEvaluated = false;
        } catch (NumberFormatException ignored) {
            // Invalid persisted values are ignored safely.
        }
    }

    public String getInput() {
        return input;
    }

    public String getExpressionPreview() {
        if (expressionTokens.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        for (String token : expressionTokens) {
            if (result.length() > 0) result.append(' ');
            result.append(isOperator(token) ? token : NumberFormatter.format(token));
        }
        if (!waitingForOperand && pendingOperator != null) {
            result.append(' ').append(NumberFormatter.format(input));
        }
        return result.toString();
    }

    public boolean isError() {
        return ERROR.equals(input) || LEGACY_ERROR.equals(input);
    }

    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putString("input", input);
        state.putString("accumulated", accumulated == null ? null : canonical(accumulated));
        state.putString("operator", pendingOperator);
        state.putStringArrayList("tokens", new ArrayList<>(expressionTokens));
        state.putBoolean("waiting", waitingForOperand);
        state.putBoolean("evaluated", justEvaluated);
        return state;
    }

    public void restoreState(Bundle state) {
        if (state == null) return;
        input = state.getString("input", "0");
        if (LEGACY_ERROR.equals(input)) input = ERROR;
        String savedAccumulated = state.getString("accumulated");
        accumulated = savedAccumulated == null ? null : new BigDecimal(savedAccumulated, MC);
        pendingOperator = state.getString("operator");
        expressionTokens.clear();
        ArrayList<String> savedTokens = state.getStringArrayList("tokens");
        if (savedTokens != null) expressionTokens.addAll(savedTokens);
        waitingForOperand = state.getBoolean("waiting", false);
        justEvaluated = state.getBoolean("evaluated", false);
    }

    private BigDecimal parseInput() {
        String normalized = input.endsWith(".") ? input.substring(0, input.length() - 1) : input;
        return new BigDecimal(normalized, MC);
    }

    private BigDecimal calculate(BigDecimal left, BigDecimal right, String operator) {
        switch (operator) {
            case "+": return left.add(right, MC);
            case "−": return left.subtract(right, MC);
            case "×": return left.multiply(right, MC);
            case "÷":
                if (right.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("Division by zero");
                return left.divide(right, MC);
            case "^": return power(left, right);
            default: throw new ArithmeticException("Unknown operator");
        }
    }

    private BigDecimal power(BigDecimal base, BigDecimal exponent) {
        try {
            int integerExponent = exponent.intValueExact();
            if (Math.abs((long) integerExponent) <= 9999) {
                if (integerExponent >= 0) return base.pow(integerExponent, MC);
                if (base.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("Zero to negative power");
                return BigDecimal.ONE.divide(base.pow(-integerExponent, MC), MC);
            }
        } catch (ArithmeticException ignored) {
            // Fractional/large powers use Math.pow below.
        }
        double powered = Math.pow(base.doubleValue(), exponent.doubleValue());
        if (!Double.isFinite(powered)) throw new ArithmeticException("Out of range");
        return new BigDecimal(Double.toString(powered), MC);
    }

    private BigDecimal squareRootValue(BigDecimal value) {
        if (value.signum() < 0) throw new ArithmeticException("Square root of a negative number");
        if (value.signum() == 0) return BigDecimal.ZERO;

        int magnitude = value.precision() - value.scale();
        BigDecimal guess = BigDecimal.ONE.scaleByPowerOfTen(Math.floorDiv(magnitude, 2));
        BigDecimal two = BigDecimal.valueOf(2);
        for (int iteration = 0; iteration < MC.getPrecision() + 10; iteration++) {
            BigDecimal next = guess.add(value.divide(guess, MC), MC).divide(two, MC);
            if (next.compareTo(guess) == 0) return next;
            guess = next;
        }
        return guess;
    }

    private void setError() {
        input = ERROR;
        resetChain();
    }

    private void resetChain() {
        accumulated = null;
        pendingOperator = null;
        expressionTokens.clear();
        waitingForOperand = false;
        justEvaluated = false;
    }

    public static boolean isOperator(String token) {
        return token.equals("+") || token.equals("−") || token.equals("×") || token.equals("÷")
                || token.equals("^") || token.equals("√");
    }

    private static String canonical(BigDecimal value) {
        BigDecimal clean = value.round(MC).stripTrailingZeros();
        return clean.compareTo(BigDecimal.ZERO) == 0 ? "0" : clean.toPlainString();
    }
}
