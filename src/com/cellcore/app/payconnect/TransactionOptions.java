package com.cellcore.app.payconnect;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;

public class TransactionOptions extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transactionoptions);
		
		Button credit = (Button)findViewById(R.id.credit);
		credit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TransactionOptions.this, CreditActivity.class);
				startActivity(intent);
			}
		});
		
		Button balance = (Button)findViewById(R.id.balance);
		balance.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TransactionOptions.this, BalanceActivity.class);
				startActivity(intent);
			}
		});
		
		Button debit = (Button)findViewById(R.id.debit);
		debit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TransactionOptions.this, WithdrawalActivity.class);
				startActivity(intent);
			}
		});
		
		Button history = (Button)findViewById(R.id.history);
		history.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TransactionOptions.this, HistoryActivity.class);
				startActivity(intent);
			}
		});
		
		Button createNewButton = (Button)findViewById(R.id.newaccount);
		createNewButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TransactionOptions.this, newaccount.class);
				 startActivity(intent);
			}
		});
		
	}
}
