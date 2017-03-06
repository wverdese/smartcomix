package com.shockdom.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Set di metodi per operazioni di base sul fragment manager per la gestione delle transactions dei fragment.
 *
 */
public class BaseFragmentManager {

    public static void addFragment(FragmentManager fm, int idContainer, Fragment f, boolean addToBackStack, boolean animated) {
        FragmentTransaction ft = fm.beginTransaction();
        if (animated)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.add(idContainer, f);
        if (addToBackStack)
            ft.addToBackStack(null);
        ft.commit();
        fm.executePendingTransactions();
    }

    public static void replaceFragment(FragmentManager fm, int idContainer, Fragment f, boolean addToBackStack, boolean animated) {
        FragmentTransaction ft = fm.beginTransaction();
        if (animated)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(idContainer, f);
        if (addToBackStack)
            ft.addToBackStack(null);
        ft.commit();
        fm.executePendingTransactions();
    }


	
}
