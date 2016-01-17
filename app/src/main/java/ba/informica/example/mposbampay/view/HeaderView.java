package ba.informica.example.mposbampay.view;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import ba.informica.example.mposbampay.R;

/**
 * Created by nedim on 12/9/15.
 */
public class HeaderView extends LinearLayout {

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(VERTICAL);

        addTitleView(context);

        addSubtitleView(context);
    }

    private void addTitleView(Context context) {
        ImageView logo = new ImageView(context);
        logo.setImageResource(R.drawable.bamcard_logo);
        MarginLayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int)getResources().getDimension(R.dimen.default_margin_doubled);


        addView(logo,params);
    }

    private void addSubtitleView(Context context) {
        TextView subtitle = new TextView(context);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        subtitle.setText(Html.fromHtml(context.getString(R.string.bampay) + "<sup><small>" +
                 " " + "</small></sup>" + year));
        subtitle.setTextAppearance(context, R.style.BlueText );
        subtitle.setGravity(Gravity.CENTER);

        addView(subtitle);
    }
}