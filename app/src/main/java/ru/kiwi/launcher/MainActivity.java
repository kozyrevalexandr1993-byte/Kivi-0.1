package ru.kiwi.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(9,10,12);
    private final int panel = Color.rgb(22,24,28);
    private final int accent = Color.rgb(214,255,95);
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildHome();
    }

    private void buildHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(44), dp(28), dp(44), dp(26));
        root.setBackgroundColor(bg);
        setContentView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("kiwi.", 34, Color.WHITE, true);
        TextView subtitle = text("  YOUR TV. YOUR RULES.", 11, 0xFF8A8E96, true);
        top.addView(logo);
        top.addView(subtitle);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView date = text("МИНИМАЛИЗМ. НИЧЕГО ЛИШНЕГО.", 12, 0xFF777C85, true);
        date.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams dpms = new LinearLayout.LayoutParams(-1, dp(32));
        dpms.topMargin = dp(12);
        root.addView(date, dpms);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, 0, 1f);
        rp.topMargin = dp(12);
        root.addView(row, rp);

        addTile(row, "01", "Приложения", "Все установленные приложения", true, this::showApps);
        addTile(row, "02", "Избранное", "Быстрый доступ к нужному", false, this::showApps);
        addTile(row, "03", "Настройки", "Система и параметры ТВ", false, this::openSettings);

        TextView footer = text("KIWI LAUNCHER   •   VERSION 0.1", 10, 0xFF555A63, true);
        footer.setLetterSpacing(0.13f);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(24));
        fp.topMargin = dp(10);
        root.addView(footer, fp);
    }

    private interface Action { void run(); }
    private void addTile(LinearLayout row, String num, String title, String detail, boolean selected, Action action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.BOTTOM);
        tile.setPadding(dp(20), dp(18), dp(20), dp(22));
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(selected ? 0xFF252A2E : panel);
        shape.setCornerRadius(dp(18));
        if (selected) shape.setStroke(dp(2), accent);
        tile.setBackground(shape);
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setClickable(true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -1, 1f);
        tp.setMargins(dp(0), dp(0), dp(14), dp(0));
        row.addView(tile, tp);

        TextView number = text(num, 12, selected ? accent : 0xFF737983, true);
        tile.addView(number);
        TextView heading = text(title, 22, Color.WHITE, true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(12);
        tile.addView(heading, hp);
        TextView desc = text(detail, 12, 0xFF969BA3, false);
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(-1, -2);
        xp.topMargin = dp(8);
        tile.addView(desc, xp);
        tile.setOnClickListener(v -> action.run());
        tile.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable s = new GradientDrawable();
            s.setColor(hasFocus ? 0xFF2B3035 : (selected ? 0xFF252A2E : panel));
            s.setCornerRadius(dp(18));
            s.setStroke(dp(hasFocus ? 3 : (selected ? 2 : 0)), hasFocus ? accent : (selected ? accent : panel));
            tile.setBackground(s);
            tile.setScaleX(hasFocus ? 1.025f : 1f);
            tile.setScaleY(hasFocus ? 1.025f : 1f);
        });
    }

    private void showApps() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<ApplicationInfo> launchable = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null && !app.packageName.equals(getPackageName())) launchable.add(app);
        }
        Collections.sort(launchable, Comparator.comparing(a -> pm.getApplicationLabel(a).toString().toLowerCase()));
        String[] labels = new String[launchable.size()];
        for (int i=0; i<launchable.size(); i++) labels[i] = pm.getApplicationLabel(launchable.get(i)).toString();
        if (labels.length == 0) {
            Toast.makeText(this, "Не нашёл приложений для запуска", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Приложения").setItems(labels, (d, which) -> {
            Intent launch = pm.getLaunchIntentForPackage(launchable.get(which).packageName);
            if (launch != null) try { startActivity(launch); } catch (Exception e) { Toast.makeText(this, "Не удалось открыть приложение", Toast.LENGTH_SHORT).show(); }
        }).setNegativeButton("Закрыть", null).create();
        dialog.setOnShowListener(d -> dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_dark));
        dialog.show();
    }

    private void openSettings() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
        catch (Exception e) { Toast.makeText(this, "Настройки недоступны", Toast.LENGTH_SHORT).show(); }
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { showApps(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}
