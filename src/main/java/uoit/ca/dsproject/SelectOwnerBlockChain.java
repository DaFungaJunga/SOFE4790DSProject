package uoit.ca.dsproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SelectOwnerBlockChain extends AppCompatActivity {
    TextView messageTv;
    EditText editHash;
    Spinner spinner;
    EditText ownerTaskData;
    public static User clientTaskChains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_owner_block_chain);
    }
}
