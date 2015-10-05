package com.ndhunju.dailyjournal.util;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;

import java.util.Date;

/**
 * This class has methods that can be used to create header, footer and row of a Ledger
 */
public class UtilsLedger {
	
	//Table layout constants
	private static float LAYOUT_WT_SUM = 4.0f;
	private static float COL_WT = 1.0f;
	private static int PADDING = 5;
	private static int TEXT_SIZE = 15;

	/**
	 * This method creates Header of a Ledger
	 * @param activity
	 * @return
	 */
	public static TableRow createLedgerHeader(Activity activity){

		TableRow ledgerHeader = new TableRow(activity);
		ledgerHeader.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		ledgerHeader.setWeightSum(LAYOUT_WT_SUM);

		TextView dateTV = new TextView(activity);
		dateTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		dateTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateTV.setGravity(Gravity.CENTER);
		dateTV.setText(activity.getString(R.string.str_date));
		dateTV.setTextSize(TEXT_SIZE);
		dateTV.setTypeface(null, Typeface.BOLD);
		ledgerHeader.addView(dateTV);

		TextView noteTV = new TextView(activity);
		noteTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		noteTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		Point size = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(size);
		noteTV.setMaxWidth(size.x);
		noteTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		noteTV.setGravity(Gravity.CENTER);
		noteTV.setText(activity.getString(R.string.str_note));
		noteTV.setTextSize(TEXT_SIZE);
		noteTV.setTypeface(null, Typeface.BOLD);
		ledgerHeader.addView(noteTV);

		TextView drTV = new TextView(activity);
		drTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		drTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		drTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		drTV.setGravity(Gravity.CENTER);
		drTV.setTextSize(TEXT_SIZE);
		drTV.setText(activity.getString(R.string.str_dr));
		drTV.setTypeface(null, Typeface.BOLD);
		ledgerHeader.addView(drTV);
		
		TextView crTV = new TextView(activity);
		crTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		crTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		crTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		crTV.setGravity(Gravity.CENTER);
		crTV.setTextSize(TEXT_SIZE);
		crTV.setText(activity.getString(R.string.str_cr));
		crTV.setTypeface(null, Typeface.BOLD);
		ledgerHeader.addView(crTV);
		
		return ledgerHeader;
	}

	/**
	 * * This method creates Footer for a Ledger with passed balance
	 * @param activity
	 * @param debit : Total debit amount to show at the end of the Ledger
	 * @param credit : Total credit amount to show at the end of the Ledger
	 * @return
	 */
	public static TableRow createLedgerFooter(Activity activity, double debit, double credit){
		TableRow balanceRow = new TableRow(activity);
		balanceRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		balanceRow.setWeightSum(LAYOUT_WT_SUM);

		TextView dateTV = new TextView(activity);
		dateTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		dateTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateTV.setGravity(Gravity.CENTER);
		dateTV.setTextSize(TEXT_SIZE);
		dateTV.setText(UtilsFormat.formatDate(new Date(), activity));
		dateTV.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(dateTV);

		TextView totalTv = new TextView(activity);
		totalTv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		totalTv.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		totalTv.setPadding(PADDING, PADDING, PADDING, PADDING);
		totalTv.setGravity(Gravity.CENTER);
		totalTv.setText(activity.getString(R.string.str_total));
		totalTv.setTextSize(TEXT_SIZE);
		totalTv.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(totalTv);

		TextView debitTotal = new TextView(activity);
		debitTotal.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		debitTotal.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		debitTotal.setPadding(PADDING, PADDING, PADDING, PADDING);
		debitTotal.setGravity(Gravity.END);
		debitTotal.setTextSize(TEXT_SIZE);
		debitTotal.setTypeface(null, Typeface.BOLD);
		debitTotal.setText(UtilsFormat.formatCurrency(debit, activity ));
		balanceRow.addView(debitTotal);

		TextView creditTotal = new TextView(activity);
		creditTotal.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		creditTotal.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		creditTotal.setPadding(PADDING, PADDING, PADDING, PADDING);
		creditTotal.setGravity(Gravity.END);
		creditTotal.setTextSize(TEXT_SIZE);
		creditTotal.setText(UtilsFormat.formatCurrency(credit, activity));
		creditTotal.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(creditTotal);
		
		return balanceRow;
	}

	/**
	 * This method creates a row of a ledger based on passed journal
	 * @param activity
	 * @param journal
	 * @param rowOnClickListener
	 * @return
	 */
	public static TableRow createLedgerRow(Activity activity, Journal journal, View.OnClickListener rowOnClickListener){
		TableRow r = new TableRow(activity);
		r.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		r.setWeightSum(LAYOUT_WT_SUM);

		TextView dateCol = new TextView(activity);
		dateCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateCol.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.cell_shape));
		dateCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateCol.setGravity(Gravity.CENTER);
		dateCol.setText(UtilsFormat.formatDate(new Date(journal.getDate()), activity));
		dateCol.setTextSize(TEXT_SIZE);
		r.addView(dateCol);

		Point size = new Point();
		TextView noteCol = new TextView(activity);
		noteCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		noteCol.setMaxWidth(size.x / 3);
		noteCol.setMaxLines(1);
		noteCol.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.cell_shape));
		noteCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		noteCol.setGravity(Gravity.CENTER);
		noteCol.setText(journal.getNote());
		noteCol.setTextSize(TEXT_SIZE);
		r.addView(noteCol);

		TextView drCol = new TextView(activity);
		drCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		drCol.setBackgroundDrawable(activity.getResources().getDrawable(	R.drawable.cell_shape));
		drCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		drCol.setGravity(Gravity.END);
		drCol.setTextSize(TEXT_SIZE);
		drCol.setText(UtilsFormat.formatCurrency(journal.getAmount(), activity));

		TextView crCol = new TextView(activity);
		crCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		crCol.setBackgroundDrawable(activity.getResources().getDrawable(	R.drawable.cell_shape));
		crCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		crCol.setGravity(Gravity.END);
		crCol.setTextSize(TEXT_SIZE);

		if (journal.getType().equals(Journal.Type.Debit)) {
			r.addView(drCol);
			r.addView(crCol);
		} else {
			r.addView(crCol);
			r.addView(drCol);
		}

		r.setOnClickListener(rowOnClickListener);
		
		return r;
	}

}
