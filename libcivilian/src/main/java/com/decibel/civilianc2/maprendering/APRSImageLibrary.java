package com.decibel.civilianc2.maprendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.decibel.civilianc2.model.entities.Symbol;
import com.example.libcivilian.R;

/**
 * Created by dburnett on 1/12/2018.
 */

public class APRSImageLibrary {
    public APRSImageLibrary(Context context){
        table1 = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.aprs_enhanced1);
        table2 = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.aprs_symbols_24_1_2x);
    }

    public Bitmap getImage(String table, int index, Typeface typeface){
        Bitmap symbolImages = null;
        switch (table){
            case Symbol.PRIMARY_SYMBOL_TABLE:
                symbolImages = table1;
                break;
            default:
                symbolImages = table2;
        }
        int x = (index % RowWidth);
        int y = (index / RowWidth);
        if(x < 0 || y < 0){ //error, show red X. We need to fix Lukrhb
            x= 13;
            y= 0;
        }
        Bitmap symbol = Bitmap.createBitmap(symbolImages, x * SymbolWidth, y * SymbolHeight, SymbolWidth, SymbolHeight);
        if(!table.equals(Symbol.PRIMARY_SYMBOL_TABLE)){
            Canvas canvas = new Canvas(symbol);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(36);
            paint.setTypeface(Typeface.create(typeface, Typeface.BOLD));

            Rect rect = new Rect();
            paint.getTextBounds(table, 0, table.length(), rect);
            canvas.drawText(table, symbol.getWidth() / 2 - rect.centerX(), symbol.getHeight() / 2 - rect.centerY(), paint);
        }
        return symbol;
    }


    private Bitmap table1;
    private Bitmap table2;
    private final static int SymbolWidth = 48;
    private final static int SymbolHeight = 48;
    private final static int RowWidth = 16;
}
