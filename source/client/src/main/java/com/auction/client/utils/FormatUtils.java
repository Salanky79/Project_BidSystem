package com.auction.client.utils;

import javafx.scene.control.TextField;
import java.util.Locale;

public class FormatUtils {

    public static void setupNumberField(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            
            // Only format if there's a change that needs formatting
            String plain = newVal.replaceAll("[^\\d]", "");
            if (plain.isEmpty()) {
                textField.setText("");
                return;
            }
            
            try {
                long val = Long.parseLong(plain);
                String formattedStr = String.format(Locale.US, "%,d", val);
                if (!formattedStr.equals(newVal)) {
                    // Remember cursor position from the right
                    int caretsFromRight = oldVal.length() - textField.getCaretPosition();
                    
                    textField.setText(formattedStr);
                    
                    int newCaretPos = formattedStr.length() - caretsFromRight;
                    if (newCaretPos < 0) newCaretPos = 0;
                    if (newCaretPos > formattedStr.length()) newCaretPos = formattedStr.length();
                    textField.positionCaret(newCaretPos);
                }
            } catch (NumberFormatException e) {
                textField.setText(oldVal);
            }
        });
    }

    public static double parseFormattedNumber(String formattedStr) throws NumberFormatException {
        if (formattedStr == null || formattedStr.trim().isEmpty()) throw new NumberFormatException("Empty input");
        String plain = formattedStr.replaceAll("[^\\d.]", "");
        if (plain.isEmpty()) throw new NumberFormatException("Empty input");
        return Double.parseDouble(plain);
    }
}
