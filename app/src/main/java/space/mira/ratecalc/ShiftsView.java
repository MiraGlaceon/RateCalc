package space.mira.ratecalc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class ShiftsView extends AppCompatActivity {

    GridLayout ll;

    final String GET_MONTH = "month";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts_view);

        ll = (GridLayout) findViewById(R.id.ll);

        print();
    }

    public void print(){
        Intent intent = getIntent();
        String[] shifts = intent.getStringArrayExtra(GET_MONTH);
        Arrays.sort(shifts);
        for (int i = 0; i < shifts.length; i++) {
            create(shifts[i]);
        }
    }

    public void create(String shift) {
        String[] words = shift.split(" ");
        String line;
        if (words[0].equals("Edited")){
            line = " " + words[0] + " " + words[1] + " ";
        }
        else {
            line = words[0] + ", " + words[1] + " ";
        }
        String number = words[2];

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(700, 115);
        TextView tvLine = new TextView(this);
        tvLine.setText(line);
        tvLine.setTextSize(20);
        tvLine.setTextColor(Color.BLACK);
        tvLine.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        tvLine.setPadding(8,0,0,0);
        ll.addView(tvLine,lp);

        TextView tvNumber = new TextView(this);
        tvNumber.setText(number);
        tvNumber.setTextSize(20);
        tvNumber.setTextColor(Color.BLACK);
        tvNumber.setGravity(Gravity.CENTER_VERTICAL);
        tvNumber.setPadding(8,0,0,0);
        ll.addView(tvNumber,lp);
    }

}