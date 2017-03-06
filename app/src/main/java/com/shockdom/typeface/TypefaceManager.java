package com.shockdom.typeface;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Manager che provvede a caricare e mantenere in una cache i custom font.
 * I custom font è necessario siano situati nella directory in asset/fonts,
 */
public class TypefaceManager {

    private static final String FONT_PATH = "fonts/";   //subfolder dentro asset/ in cui mettere i font

    private static TypefaceManager instance;

    /**
     * Resistuisce l'instanza del TypefaceManager
     * @param context
     * @return
     */
    public static TypefaceManager getInstance(Context context) {
        if(instance == null) {
            instance = new TypefaceManager(context.getApplicationContext());
        }

        return instance;
    }

    private TypefaceManager(Context context) {
        this.context = context;
        typefaces = new HashMap<String, Typeface>();
    }

    private Context context;
    private HashMap<String, Typeface> typefaces;

    /**
     * Carica il custom font dalla directory asset/font
     * @param name nome per esteso del file del font
     * @return typeface
     * @throws Exception in caso di errore nel caricamento
     */
    public Typeface getTypefaceFromAsset(String name) throws Exception {
        Typeface t;

        if(typefaces.containsKey(name)) {
            t = typefaces.get(name);
        } else {
            t = Typeface.createFromAsset(context.getAssets(), new StringBuilder(FONT_PATH).append(name).toString());
            typefaces.put(name, t);
        }

        return t;
    }

    /**
     * Restituisce il path in cui il TypefaceManager si aspetta di trovare i font
     * @return
     */
    public static String getFontAssetPath() {
        return FONT_PATH;
    }
}
