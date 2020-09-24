package space.mira.ratecalc;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends ListActivity {

    public EditText etRate, etTime, etIncome;
    public Button btnCount;

    public SharedPreferences showTable; //contains names of months and sums
    public SharedPreferences savedRate; //just useful thing - saves your usual rate
    public ArrayList<String> months; //contains names of months and sums needed for adapter
    public ArrayAdapter<String> adapter;


    final String SAVED_TABLE = "saved_table"; //key for saving data
    final String SAVED_RATE = "rate"; //key for saving data
    final String PUT_MONTH = "month";  //key for connection between two intents

    public static int tableCount;  //serial number of month


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIncome = (EditText) findViewById(R.id.etIncome);
        etRate = (EditText) findViewById(R.id.etRate);
        etTime = (EditText) findViewById(R.id.etTime);
        btnCount = (Button) findViewById(R.id.btnCount);

        showTable = getSharedPreferences(SAVED_TABLE, MODE_PRIVATE);
        savedRate = getSharedPreferences(SAVED_RATE, MODE_PRIVATE);
        etRate.setText(savedRate.getString(SAVED_RATE, ""));

        months = new ArrayList<>(showTable.getAll().keySet());
        //sorting array in reverse order, cuz i want that last month stays on top
        Collections.sort(months, Collections.<String>reverseOrder());
        tableCount = months.size() + 1;

        adapter = new ArrayAdapter<>(this, R.layout.list_item, months);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(clickOnListItem);
        getListView().setOnItemLongClickListener(clickOnListItemLonger);
        btnCount.setOnClickListener(clickOnButton);
        etRate.addTextChangedListener(setPermanentRate);
    }

    //saves last change EditText "Rate" for convenience
    TextWatcher setPermanentRate = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            SharedPreferences.Editor editor = savedRate.edit();
            editor.putString(SAVED_RATE, etRate.getText().toString());
            editor.apply();
        }
    };

    //on item click opens new activity, there you can see list of days when you added data
    AdapterView.OnItemClickListener clickOnListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, ShiftsView.class);

            String key = ((TextView)view).getText().toString();
            HashSet<String> set = (HashSet<String>) showTable.getStringSet(key, null);

            String[] temp = new String[set.size()];
            set.toArray(temp);

            intent.putExtra(PUT_MONTH, temp);
            startActivity(intent);
        }
    };

    //on long item click you can edit field in new activity shown as dialog window
    // or delete item after following confirmed
    AdapterView.OnItemLongClickListener clickOnListItemLonger = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final String item = ((TextView)view).getText().toString();
            final String[] month = item.split(" ");
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.deleteEdit, (month[1] + " " + month[2].substring(0, month[2].length() - 1))));

            builder.setItems(R.array.alertDialogItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                            String[] extraForIntent = new String[]{item, ""};
                            intent.putExtra("result", extraForIntent);
                            startActivityForResult(intent, 1);
                            break;
                        case 1:
                            deleteFromTable(item, (month[1] + " " + month[2].substring(0, month[2].length() - 1)));
                            break;
                    }
                }
            });

            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.create().show();
            return true;
        }
    };

    //after your back from editor of data you can put new data in table
    //edited things mark as Edited (date)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        String[] extraFromEditor = data.getStringArrayExtra("result");
        if (((int)Integer.parseInt(extraFromEditor[1])) == 0) {
            Toast.makeText(MainActivity.this, R.string.toastNeedlessNumber, Toast.LENGTH_LONG).show();
            return;
        }
        plusEdit(extraFromEditor[0], (int)Integer.parseInt(extraFromEditor[1]));
    }

    //remove items from table
    public void deleteFromTable(final String key, String month) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(getString(R.string.deleteMessage, month));

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeItem(key);
            }
        });

        builder.create().show();
    }

    //remove data from shared preferences and reset adapter
    public void removeItem(String key) {
        months.remove(key);
        SharedPreferences.Editor editor = showTable.edit();
        editor.remove(key);
        editor.apply();

        adapter.notifyDataSetChanged();
    }

    //add data to shared preferences and reset adapter
    public void putItem(String key, HashSet<String> set) {
        months.add(key);
        Collections.sort(months, Collections.<String>reverseOrder());

        SharedPreferences.Editor editorTable = showTable.edit();
        editorTable.putStringSet(key, set);
        editorTable.apply();

        adapter.notifyDataSetChanged();
    }

    //checks status of EditTexts and add new month in table or just edits sums
    //i let to users create month with 0 sum
    public View.OnClickListener clickOnButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etIncome.getText().toString().equals("") & etTime.getText().toString().equals("")
            & etRate.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, R.string.toastFillFields,
                        Toast.LENGTH_LONG).show();
                return;
            }

            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    etIncome.getWindowToken(),0);


            plus(addMonth());
        }
    };

    //i could place tableCount here but i didnt
    public String getExtraNumber(){
        if (tableCount < 10) {
            return "0" + tableCount + ")";
        }
        return  tableCount + ")";
    }

    //in fact adds new month, if exist just out
    public String addMonth() {
        Calendar calendar = Calendar.getInstance();
        String key = (new SimpleDateFormat("yyyy, MMMM:", Locale.ENGLISH).format(calendar.getTime()));

        for (String x : months) {
            String[] words = x.split(" ");
            if ((words[1] + " " + words[2]).equals(key)) {
                return x;
            }
        }

        String extraNumber = getExtraNumber();
        key = extraNumber + " " + key + " 0";

        HashSet<String> shifts = new HashSet<>();
        String day = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.getTime());
        day += " " + calendar.get(Calendar.DAY_OF_MONTH) + ": 0";
        shifts.add(day);

        putItem(key, shifts);
        tableCount++; // i could but i didnt
        return key;
    }

    //adds new days, before day is gone you can change sum without editing
    public void plus(String key) {
        Calendar calendar = Calendar.getInstance();

        String[] words = key.split(" ");
        String month = words[0] + " " + words[1] + " " + words[2];
        int summMajor = Integer.parseInt(words[3]);

        HashSet<String> shifts = (HashSet<String>) showTable.getStringSet(key, null);

        String day = (new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.getTime()));
        day += " " + calendar.get(Calendar.DAY_OF_MONTH) + ":";

        boolean dayIsNotExist = true;
        String resultDaySumm = "";
        int summMinor = 0;
        for (String x : shifts) {
            words = x.split(" ");
            if ((words[0] + " " + words[1]).equals(day)) {
                day = x;
                summMinor = Integer.parseInt(words[2]);
                resultDaySumm = words[0] + " " + words[1] + " ";
                dayIsNotExist = false;
                break;
            }
        }

        int total = 0;
        if (!etTime.getText().toString().equals("") & !etRate.getText().toString().equals("")) {
            total = Integer.parseInt(etTime.getText().toString());
            total *= Integer.parseInt(etRate.getText().toString());
        }
        if (!etIncome.getText().toString().equals("")) {
            total += Integer.parseInt(etIncome.getText().toString());
        }

        summMajor += total - summMinor;

        if (dayIsNotExist) {
            day += " " + total;
            resultDaySumm = day;
        }
        else if (shifts.contains(day)) {
            shifts.remove(day);
            resultDaySumm += "" + total;
        }

        shifts.add(resultDaySumm);

        String resultMonth = month + " " + summMajor;

        removeItem(key);
        putItem(resultMonth, shifts);
    }

    //adds edited lines
    public void plusEdit(String key, int editedSumm) {
        Calendar calendar = Calendar.getInstance();
        String line = "Edited " + (new SimpleDateFormat("dd/MM/yyyy",
                Locale.ENGLISH).format(calendar.getTime())) + ": ";

        HashSet<String> shifts = (HashSet<String>) showTable.getStringSet(key, null);
        String[] words = key.split(" ");
        int totalSumm = Integer.parseInt(words[3]) + editedSumm;

        shifts.add(line + editedSumm);

        String duplicateKey = words[0] + " " + words[1] + " " + words[2] + " " + totalSumm;
        removeItem(key);
        putItem(duplicateKey, shifts);
    }
}