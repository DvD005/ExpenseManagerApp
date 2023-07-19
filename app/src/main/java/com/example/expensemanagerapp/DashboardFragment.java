package com.example.expensemanagerapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
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

    //floating button
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;


    //floating btn textview
    private TextView fab_income_txt;
    private TextView fab_expense_txt;

    //BOOLEAN
     private boolean isOpen=false;

     //animation class objects
    private Animation FadeOpen, FadeClose;

    //Dashboard income and expense results
    private TextView totalIncomeResult;
    private TextView totalExpenseResult;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    //Recycler View
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;
    private FirebaseRecyclerAdapter incomeAdapter;
    private FirebaseRecyclerAdapter expenseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid=mUser.getUid();

        mIncomeDatabase= FirebaseDatabase.getInstance("https://expense-manager-35479-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("IncomeDatabase").child(uid); //uid added so that we get unique id for diff users
        mExpenseDatabase= FirebaseDatabase.getInstance("https://expense-manager-35479-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("ExpenseDatabase").child(uid);

        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        //connect floating btn to layout
        fab_main_btn=myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn=myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn=myview.findViewById(R.id.expense_ft_btn);

        //connect floating txt to layout
        fab_income_txt=myview.findViewById(R.id.income_ft_text);//in dashboard section I can find the id
        fab_expense_txt=myview.findViewById(R.id.expense_ft_text);

        //total income and expense results
        totalIncomeResult=myview.findViewById(R.id.income_set_result);
        totalExpenseResult=myview.findViewById(R.id.expense_set_result);

        //Recycler
        mRecyclerIncome=myview.findViewById(R.id.recycler_Income);
        mRecyclerExpense=myview.findViewById(R.id.recycler_Expense);

        //connecting animation
        FadeOpen=AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        FadeClose=AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();

                if ((isOpen)){
                    fab_income_btn.startAnimation(FadeClose);
                    fab_expense_btn.startAnimation(FadeClose);
                    fab_income_btn.setClickable(false);
                    fab_expense_btn.setClickable(false);

                    fab_income_txt.startAnimation(FadeClose);
                    fab_expense_txt.startAnimation(FadeClose);
                    fab_income_txt.setClickable(false);
                    fab_expense_txt.setClickable(false);

                    isOpen=false;
                }
                else {
                    fab_income_btn.startAnimation(FadeOpen);
                    fab_expense_btn.startAnimation(FadeOpen);
                    fab_income_btn.setClickable(true);
                    fab_expense_btn.setClickable(true);

                    fab_income_txt.startAnimation(FadeOpen);
                    fab_expense_txt.startAnimation(FadeOpen);
                    fab_income_txt.setClickable(true);
                    fab_expense_txt.setClickable(true);

                    isOpen=true;
                }
            }
        });

        //Calculate total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalSum=0;
                for(DataSnapshot mySnap: snapshot.getChildren()){
                    Data data=mySnap.getValue(Data.class);
                    totalSum+= data.getAmt();
                    String strResult="₹"+String.valueOf(totalSum);
                    totalIncomeResult.setText(strResult+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Calculate total expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalSum=0;
                for(DataSnapshot mySnap: snapshot.getChildren()){
                    Data data=mySnap.getValue(Data.class);
                    totalSum+= data.getAmt();
                    String strResult="₹"+String.valueOf(totalSum);
                    totalExpenseResult.setText(strResult+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Recycler
        LinearLayoutManager layoutManagerIncome=new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);//this will help us scroll the data horizontally
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense=new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);//this will help us scroll the data horizontally
        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);

        return myview;
    }

    //Floating Button Animation
    private void floatAnimation(){
        if ((isOpen)){
            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);

            isOpen=false;
        }
        else {
            fab_income_btn.startAnimation(FadeOpen);
            fab_expense_btn.startAnimation(FadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadeOpen);
            fab_expense_txt.startAnimation(FadeOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);

            isOpen=true;
        }
    }

    private void addData(){
        //FAB income button
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeDataInsert();
            }
        });

        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseDataInsert();
            }
        });
    }

    public void incomeDataInsert(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myview=inflater.inflate(R.layout.custom_layout_insert_data,null);
        myDialog.setView(myview);

        final AlertDialog dialog=myDialog.create();

        dialog.setCancelable(false);//means if we click outside the box then our dialog box will not collapse

        final EditText edtAmt=myview.findViewById(R.id.amount_edit);
        final EditText edtType=myview.findViewById(R.id.type_edit);
        final EditText edtNote=myview.findViewById(R.id.note_edit);

        Button btnSave=myview.findViewById(R.id.btnSave);
        Button btnCancel=myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type=edtType.getText().toString().trim();
                String amt=edtAmt.getText().toString().trim();
                String note=edtNote.getText().toString().trim();

                if(TextUtils.isEmpty(amt)){
                    edtAmt.setError("*Required Field...");
                    return;
                }
                int intAmt=Integer.parseInt(amt);
                if(TextUtils.isEmpty(type)){
                    edtType.setError("*Required Field...");
                    return;
                }
                if(TextUtils.isEmpty(note)){
                    edtNote.setError("*Required Field...");
                    return;
                }

                String id=mIncomeDatabase.push().getKey();//generate random id
                String mDate= DateFormat.getDateInstance().format(new Date());//will get current date
                Data data=new Data(intAmt, type, note, id, mDate);//connecting to firebase, model-->data
                mIncomeDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data Added!!", Toast.LENGTH_SHORT).show();
                floatAnimation();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatAnimation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void expenseDataInsert(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myView=inflater.inflate(R.layout.custom_layout_insert_data, null);
        myDialog.setView(myView);

        final AlertDialog dialog=myDialog.create();

        dialog.setCancelable(false);

        EditText amt=myView.findViewById(R.id.amount_edit);
        EditText type=myView.findViewById(R.id.type_edit);
        EditText note=myView.findViewById(R.id.note_edit);

        Button btnSave=myView.findViewById(R.id.btnSave);
        Button btnCancel=myView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmAmt=amt.getText().toString().trim();
                String tmType=type.getText().toString().trim();
                String tmNote=note.getText().toString().trim();

                if(TextUtils.isEmpty(tmAmt)){
                    amt.setError("*Required Field...");
                    return;
                }

                int intAmt=Integer.parseInt(tmAmt);

                if (TextUtils.isEmpty(tmType)){
                    type.setError("*Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(tmNote)){
                    note.setError("*Required Field...");
                    return;
                }

                String id=mExpenseDatabase.push().getKey();
                String mDate=DateFormat.getDateInstance().format(new Date());
                Data data=new Data(intAmt, tmType,tmNote, id, mDate);
                mExpenseDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data Added!!", Toast.LENGTH_SHORT).show();

                floatAnimation();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatAnimation();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> optionsIncome=new FirebaseRecyclerOptions.Builder<Data>().setQuery(mIncomeDatabase, Data.class).build();
        incomeAdapter= new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(optionsIncome) {
            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new IncomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                holder.setIncomeType(model.getType());
                holder.setIncomeAmt(model.getAmt());
                holder.setIncomeDate(model.getDate());
            }
        };
        mRecyclerIncome.setAdapter(incomeAdapter);
        incomeAdapter.startListening();

        FirebaseRecyclerOptions<Data> optionsExpense=new FirebaseRecyclerOptions.Builder<Data>().setQuery(mExpenseDatabase, Data.class).build();
        expenseAdapter= new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsExpense) {
            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                holder.setExpenseType(model.getType());
                holder.setExpenseAmt(model.getAmt());
                holder.setExpenseDate(model.getDate());
            }
        };
        mRecyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.startListening();
    }

    //For income data
    public static class IncomeViewHolder extends RecyclerView.ViewHolder{

        View mIncomeView;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mIncomeView=itemView;
        }

        public void setIncomeType(String type){
            TextView mType=mIncomeView.findViewById(R.id.type_Income_dash);
            mType.setText(type);
        }

        public void setIncomeAmt(int amt){
            TextView mAmt=mIncomeView.findViewById(R.id.amt_Income_dash);
            String strAmt="₹"+String.valueOf(amt);
            mAmt.setText(strAmt);
        }

        public void setIncomeDate(String date){
            TextView mDate=mIncomeView.findViewById(R.id.date_Income_dash);
            mDate.setText(date);
        }
    }

    //For expense data
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

        View mExpenseView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mExpenseView=itemView;
        }

        public void setExpenseType(String type){
            TextView mType=mExpenseView.findViewById(R.id.type_Expense_dash);
            mType.setText(type);
        }

        public void setExpenseAmt(int amt){
            TextView mAmt=mExpenseView.findViewById(R.id.amt_Expense_dash);
            String strAmt="₹"+String.valueOf(amt);
            mAmt.setText(strAmt);
        }

        public void setExpenseDate(String date){
            TextView mDate=mExpenseView.findViewById(R.id.date_Expense_dash);
            mDate.setText(date);
        }
    }
}