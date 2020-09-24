package space.mira.ratecalc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditorActivity extends AppCompatActivity {

    EditText etTimeEditor, etRateEditor, etIncomeEditor;
    Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        etIncomeEditor = (EditText) findViewById(R.id.etIncomeEditor);
        etRateEditor = (EditText) findViewById(R.id.etRateEditor);
        etTimeEditor = (EditText) findViewById(R.id.etTimeEditor);
        btnEdit = (Button) findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(clickOnButton);
    }

    //click on button to sent data in main activity
    View.OnClickListener clickOnButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int result = 0;
            if (!etIncomeEditor.getText().toString().equals("")) {
                result = Integer.parseInt(etIncomeEditor.getText().toString());
            }
            if (!(etRateEditor.getText().toString().equals("") | etTimeEditor.getText().toString().equals(""))) {
                result += Integer.parseInt(etTimeEditor.getText().toString()) * Integer.parseInt(etRateEditor.getText().toString());
            }
            Intent intentFrom = getIntent();
            String[] extraForEditor = intentFrom.getStringArrayExtra("result");
            extraForEditor[1] = String.valueOf(result);
            Intent intentTo = new Intent();
            intentTo.putExtra("result", extraForEditor);
            setResult(RESULT_OK, intentTo);
            finish();
        }
    };
}