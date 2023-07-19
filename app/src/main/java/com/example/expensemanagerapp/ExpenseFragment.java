package com.example.expensemanagerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpenseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseFragment newInstance(String param1, String param2) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //Firebase DB
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //recyclerView
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    //TextView
    private TextView expenseTotalSum;

    //Edit data item
    private EditText editAmt;
    private EditText editType;
    private EditText editNote;

    private Button btnUpdate;
    private Button btnDelete;

    //Data variables
    private String type;
    private String note;
    private int amt;
    private String postKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView= inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid= mUser.getUid();

        mExpenseDatabase= FirebaseDatabase.getInstance("https://expense-manager-35479-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("ExpenseDatabase").child(uid);

        expenseTotalSum=myView.findViewById(R.id.expense_txt_result);

        recyclerView=myView.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                int totalValue=0;
                for (DataSnapshot mySnapShot: datasnapshot.getChildren()){
                    Data data=mySnapShot.getValue(Data.class);
                    totalValue+=data.getAmt();
                    String strTotalValue="₹"+String.valueOf(totalValue);
                    expenseTotalSum.setText(strTotalValue+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options=new FirebaseRecyclerOptions.Builder<Data>().setQuery(mExpenseDatabase, Data.class).build();
        adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position, @NonNull Data model) {
                viewHolder.setAmt(model.getAmt());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postKey=getRef(position).getKey();
                        type= model.getType();
                        note= model.getNote();
                        amt= model.getAmt();
                        updateDataItem();
                    }
                });
            }


            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler, parent, false));
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }

        private void setType(String type){
            TextView mType=mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }

        private void setNote(String note){
            TextView mNote=mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }

        private void setDate(String date){
            TextView mDate=mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }

        private void setAmt(int amt){
            TextView mAmt=mView.findViewById(R.id.amt_txt_expense);
            String strAmt=String.valueOf(amt);
            mAmt.setText(strAmt);
        }
    }

    private void updateDataItem(){

        AlertDialog.Builder myDialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myView=inflater.inflate(R.layout.update_data_item, null);
        myDialog.setView(myView);

        editAmt=myView.findViewById(R.id.amount_edit);
        editType=myView.findViewById(R.id.type_edit);
        editNote=myView.findViewById(R.id.note_edit);

        //set data to edit text..
        editType.setText(type);
        editType.setSelection(type.length());

        editNote.setText(note);
        editNote.setSelection(note.length());

        editAmt.setText(String.valueOf(amt));
        editAmt.setSelection(String.valueOf(amt).length());

        btnUpdate=myView.findViewById(R.id.btnUpdate);
        btnDelete=myView.findViewById(R.id.btnDelete);

        AlertDialog dialog=myDialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type=editType.getText().toString().trim();
                note=editNote.getText().toString().trim();
                String strAmt=String.valueOf(amt);
                strAmt=editAmt.getText().toString().trim();
                int myAmt=Integer.parseInt(strAmt);
                String mDate= DateFormat.getDateInstance().format(new Date());
                Data data=new Data(myAmt, type, note, postKey, mDate);
                mExpenseDatabase.child(postKey).setValue(data);

                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpenseDatabase.child(postKey).removeValue();
                dialog.dismiss();
            }
        });

         dialog.show();

    }
}