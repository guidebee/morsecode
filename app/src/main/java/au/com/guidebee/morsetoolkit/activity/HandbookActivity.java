package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import au.com.guidebee.morsetoolkit.component.HandbookCardAdapter;
import au.com.guidebee.morsetoolkit.helper.UIHelper;


public class HandbookActivity extends MorseActivity {

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.handbook_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_handbook);
        super.onCreate(savedInstanceState);
        int gridWidth = 250;
        int screenWidth = UIHelper.getScreenWidth(this) - 10;
        int gridSize = Math.max(1, screenWidth / gridWidth);
        gridWidth = (int) (screenWidth / gridSize * UIHelper.getScreenDp(this));
        RecyclerView recycler_view_handbook = (RecyclerView) findViewById(R.id.recycler_view_handbook);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, gridSize);
        if (recycler_view_handbook != null) {
            recycler_view_handbook.setLayoutManager(mLayoutManager);
            RecyclerView.Adapter mAdapter = new HandbookCardAdapter(gridWidth, morseEncoder);
            recycler_view_handbook.setAdapter(mAdapter);
        }

    }
}
