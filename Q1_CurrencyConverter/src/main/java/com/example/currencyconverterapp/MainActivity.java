package com.example.currencyconverterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    EditText amount;
    Spinner fromCurrency, toCurrency;
    Button convertBtn, themeBtn;
    TextView result;

    String[] currencies = {"INR", "USD", "EUR", "JPY"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🔥 APPLY THEME BEFORE UI LOAD
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("darkMode", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Linking UI components
        amount = findViewById(R.id.amount);
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);
        convertBtn = findViewById(R.id.convertBtn);
        result = findViewById(R.id.result);
        themeBtn = findViewById(R.id.themeBtn);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                currencies
        );

        fromCurrency.setAdapter(adapter);
        toCurrency.setAdapter(adapter);

        // Convert button
        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (amount.getText().toString().isEmpty()) {
                    result.setText("Enter amount");
                    return;
                }

                double input = Double.parseDouble(amount.getText().toString());

                String from = fromCurrency.getSelectedItem().toString();
                String to = toCurrency.getSelectedItem().toString();

                double output = convertCurrency(from, to, input);

                result.setText("Converted: " + output);
            }
        });

        // Theme toggle button
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                boolean current = prefs.getBoolean("darkMode", false);

                if (current) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("darkMode", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("darkMode", true);
                }

                editor.apply();

                recreate(); // 🔥 refresh activity
            }
        });
    }

    // conversion logic
    private double convertCurrency(String from, String to, double amount) {

        if (from.equals(to)) return amount;

        if (from.equals("INR") && to.equals("USD")) return amount / 83;
        if (from.equals("USD") && to.equals("INR")) return amount * 83;

        if (from.equals("INR") && to.equals("EUR")) return amount / 90;
        if (from.equals("EUR") && to.equals("INR")) return amount * 90;

        if (from.equals("INR") && to.equals("JPY")) return amount / 0.55;
        if (from.equals("JPY") && to.equals("INR")) return amount * 0.55;

        return amount;
    }
}