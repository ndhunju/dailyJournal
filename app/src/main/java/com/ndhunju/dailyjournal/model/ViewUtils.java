package com.ndhunju.dailyjournal.model;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;

import java.util.Date;

public class ViewUtils {
	
	//Table layout constants
	private static float LAYOUT_WT_SUM = 4.0f;
	private static float COL_WT = 1.0f;
	private static int PADDING = 5;
	private static int TEXT_SIZE = 15;
	
	
	@SuppressWarnings("deprecation")
	public static TableRow createLedgerHeader(Activity activity){
		TableRow headingRow = new TableRow(activity);
		headingRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		headingRow.setWeightSum(LAYOUT_WT_SUM);

		TextView dateTV = new TextView(activity);
		dateTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		dateTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateTV.setGravity(Gravity.CENTER);
		dateTV.setText(activity.getString(R.string.str_date));
		dateTV.setTextSize(TEXT_SIZE);
		dateTV.setTypeface(null, Typeface.BOLD);
		headingRow.addView(dateTV);

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
		headingRow.addView(noteTV);

		TextView drTV = new TextView(activity);
		drTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		drTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		drTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		drTV.setGravity(Gravity.CENTER);
		drTV.setTextSize(TEXT_SIZE);
		drTV.setText(activity.getString(R.string.str_dr));
		drTV.setTypeface(null, Typeface.BOLD);
		headingRow.addView(drTV);
		
		TextView crTV = new TextView(activity);
		crTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		crTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		crTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		crTV.setGravity(Gravity.CENTER);
		crTV.setTextSize(TEXT_SIZE);
		crTV.setText(activity.getString(R.string.str_cr));
		crTV.setTypeface(null, Typeface.BOLD);
		headingRow.addView(crTV);
		
		return headingRow;
	}
	
	@SuppressWarnings("deprecation")
	public static TableRow createLedgerFooter(Activity activity, double balance){
		TableRow balanceRow = new TableRow(activity);
		balanceRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		balanceRow.setWeightSum(LAYOUT_WT_SUM);

		TextView dateTV = new TextView(activity);
		dateTV.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateTV.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		dateTV.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateTV.setGravity(Gravity.CENTER);
		dateTV.setTextSize(TEXT_SIZE);
		dateTV.setText(Utils.parseDate(new Date(), Utils.DATE_FORMAT));
		dateTV.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(dateTV);

		TextView balanceTv = new TextView(activity);
		balanceTv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		balanceTv.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		balanceTv.setPadding(PADDING, PADDING, PADDING, PADDING);
		balanceTv.setGravity(Gravity.CENTER);
		balanceTv.setText(activity.getString(R.string.str_balance));
		balanceTv.setTextSize(TEXT_SIZE);
		balanceTv.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(balanceTv);

		TextView drCrTv = new TextView(activity);
		drCrTv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		drCrTv.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		drCrTv.setPadding(PADDING, PADDING, PADDING, PADDING);
		drCrTv.setGravity(Gravity.END);
		drCrTv.setTextSize(TEXT_SIZE);
		drCrTv.setTypeface(null, Typeface.BOLD);
		drCrTv.setText(balance < 0 ? "Cr" : "Dr");
		balanceRow.addView(drCrTv);

		TextView balanceAmtTv = new TextView(activity);
		balanceAmtTv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		balanceAmtTv.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.heading_shape));
		balanceAmtTv.setPadding(PADDING, PADDING, PADDING, PADDING);
		balanceAmtTv.setGravity(Gravity.CENTER);
		balanceAmtTv.setTextSize(TEXT_SIZE);
		balanceAmtTv.setText(Utils.formatCurrency(balance));
		balanceAmtTv.setTypeface(null, Typeface.BOLD);
		balanceRow.addView(balanceAmtTv);
		
		return balanceRow;
	}
	
	@SuppressWarnings("deprecation")
	public static TableRow createJournalRow(Activity activity, Journal journal, View.OnClickListener rowOnClickListener){
		TableRow r = new TableRow(activity);
		r.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		r.setWeightSum(LAYOUT_WT_SUM);

		TextView dateCol = new TextView(activity);
		dateCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		dateCol.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.cell_shape));
		dateCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		dateCol.setGravity(Gravity.CENTER);
		dateCol.setText(Utils.parseDate(new Date(journal.getDate()),Utils.DATE_FORMAT));
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
		drCol.setGravity(Gravity.CENTER);
		drCol.setTextSize(TEXT_SIZE);
		drCol.setText(Utils.formatCurrency(journal.getAmount()));

		TextView crCol = new TextView(activity);
		crCol.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, COL_WT));
		crCol.setBackgroundDrawable(activity.getResources().getDrawable(	R.drawable.cell_shape));
		crCol.setPadding(PADDING, PADDING, PADDING, PADDING);
		crCol.setGravity(Gravity.CENTER);
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
