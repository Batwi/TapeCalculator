package cz.batwi.tapecalculator;

public final class NumberFormatter {
    private NumberFormatter() {}

    public static String format(String canonical) {
        if (canonical == null || canonical.equals("Chyba")) return canonical;
        boolean negative = canonical.startsWith("-");
        String unsigned = negative ? canonical.substring(1) : canonical;
        String[] parts = unsigned.split("\\.", -1);
        String integer = parts[0];
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < integer.length(); i++) {
            if (i > 0 && (integer.length() - i) % 3 == 0) grouped.append('\u00A0');
            grouped.append(integer.charAt(i));
        }
        if (negative) grouped.insert(0, '-');
        if (parts.length == 2) grouped.append(',').append(parts[1]);
        return grouped.toString();
    }
}
