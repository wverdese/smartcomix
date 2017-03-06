package com.shockdom.purchase;

import com.anjlab.android.iab.v3.Constants;

/**
 * Created by Walt on 23/05/2015.
 */
public class PurchaseUtils {

    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2; //they forgot it!

    public static String getErrorExplanation(int errCode, Throwable exception) {

        StringBuilder sb = new StringBuilder("Error("+errCode+"): ");
        switch (errCode) {
            case Constants.BILLING_RESPONSE_RESULT_OK: //0
                sb.append("Success --this should not happen!--");
                break;
            case Constants.BILLING_RESPONSE_RESULT_USER_CANCELED: //1
                sb.append("User Canceled");
                break;
            case BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE: //2
                sb.append("Service Unavailable");
                break;
            case Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE: //3
                sb.append("Billing Unavailable");
                break;
            case Constants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE: //4
                sb.append("Item Unavailable");
                break;
            case Constants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR: //5
                sb.append("Developer Error");
                break;
            case Constants.BILLING_RESPONSE_RESULT_ERROR: //6
                sb.append("Result Error");
                break;
            case Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED: //7
                sb.append("Item Already Owned");
                break;
            case Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED: //8
                sb.append("Item Not Owned");
                break;
            case Constants.BILLING_ERROR_FAILED_LOAD_PURCHASES: //100
                sb.append("Failed to load purchases");
                break;
            case Constants.BILLING_ERROR_FAILED_TO_INITIALIZE_PURCHASE: //101
                sb.append("Failed to initialize purchases");
                break;
            case Constants.BILLING_ERROR_INVALID_SIGNATURE: //102
                sb.append("Invalid signature");
                break;
            case Constants.BILLING_ERROR_LOST_CONTEXT: //103
                sb.append("Lost Context");
                break;
            case Constants.BILLING_ERROR_OTHER_ERROR: //1110
                sb.append("Not specified.");
                break;
            default: //103
                sb.append("Unknown error code.");
                break;
        }

        if (exception != null) {
            sb.append(" "+exception.getLocalizedMessage());
            exception.printStackTrace();
        }

        return sb.toString();

    }

}
