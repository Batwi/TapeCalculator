package cz.batwi.tapecalculator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class CalculatorEngineTest {
    private CalculatorEngine engine(List<HistoryEntry> history) {
        return new CalculatorEngine(history::add);
    }

    private void type(CalculatorEngine engine, String digits) {
        for (char digit : digits.toCharArray()) engine.digit(String.valueOf(digit));
    }

    @Test
    public void dividesLikeReferenceScreenshot() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "700000");
        engine.operator("÷");
        type(engine, "48");
        engine.equalsPressed();
        assertEquals("14583.333333333333333", engine.getInput());
        assertEquals(1, history.size());
        assertEquals("700000", history.get(0).getTokens().get(0));
    }

    @Test
    public void computesPowers() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "2");
        engine.operator("^");
        type(engine, "10");
        engine.equalsPressed();
        assertEquals("1024", engine.getInput());
    }

    @Test
    public void computesSquareRootAndRecordsTapeEntry() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "144");
        engine.squareRoot();
        assertEquals("12", engine.getInput());
        assertEquals(1, history.size());
        assertEquals(Arrays.asList("√", "144"), history.get(0).getTokens());
        assertEquals("12", history.get(0).getResult());
    }

    @Test
    public void squareRootWorksInsideAChainedCalculation() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "2");
        engine.operator("+");
        type(engine, "9");
        engine.squareRoot();
        engine.equalsPressed();
        assertEquals("5", engine.getInput());
        assertEquals(2, history.size());
    }

    @Test
    public void squareRootKeepsTwentySignificantDigits() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "2");
        engine.squareRoot();
        assertEquals("1.4142135623730950488", engine.getInput());
    }

    @Test
    public void squareRootOfNegativeNumberShowsError() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "9");
        engine.toggleSign();
        engine.squareRoot();
        assertTrue(engine.isError());
        assertTrue(history.isEmpty());
    }

    @Test
    public void pastedTapeNumberBecomesCurrentOperand() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "1");
        engine.operator("+");
        engine.pasteValue("7");
        engine.equalsPressed();
        assertEquals("8", engine.getInput());
    }

    @Test
    public void divisionByZeroShowsErrorWithoutHistoryEntry() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "8");
        engine.operator("÷");
        engine.digit("0");
        engine.equalsPressed();
        assertTrue(engine.isError());
        assertTrue(history.isEmpty());
    }

    @Test
    public void chainedOperationsUseCalculatorStyleImmediateEvaluation() {
        List<HistoryEntry> history = new ArrayList<>();
        CalculatorEngine engine = engine(history);
        type(engine, "2");
        engine.operator("+");
        type(engine, "3");
        engine.operator("×");
        type(engine, "4");
        engine.equalsPressed();
        assertEquals("20", engine.getInput());
    }
}
