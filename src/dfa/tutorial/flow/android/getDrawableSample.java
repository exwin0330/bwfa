package dfa.tutorial.flow.android;

import javax.annotation.Resources;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

public class getDrawableSample extends Activity {
    private int theme;

    private void getThemeResource() {
        // AppTheme のスタイル ID を theme 変数に設定
        this.theme = R.style.AppTheme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // テーマを設定
        getThemeResource();

        // Drawable リソース ID を設定
        int drawableResId = R.drawable.sample_drawable;
        Resources resources = getResources();
        Drawable drawable;

        // SDK バージョンに応じて Drawable を取得
        if (Build.VERSION.SDK_INT >= 22) {
            drawable = resources.getDrawable(drawableResId, new ContextThemeWrapper(this, theme).getTheme());
        } else {
            drawable = resources.getDrawable(drawableResId);
        }
        
        // Drawable の使用（例: View にセットなど）
    }
}
