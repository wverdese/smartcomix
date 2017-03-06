package com.shockdom.api.json;

import java.io.Serializable;

/**
 * Created by Walt on 23/05/2015.
 */
public class Volume implements Serializable {

    private String _id;
    private String google_play_id;
    private String title;
    private String subtitle;
    private String number;
    private int pages_size;
    //private List<Author> author;
    private String authors_compact;
    private String authors;
    private String type;
    private String genre;
    private int isSmartSonix;
    private String year;
    private String description;
    private String picture;

    private boolean isPurchased;
    private double price = -1; //not in JSON
    private String priceText;
    private String transactionId;

    public class Author implements Serializable {
        private String first_name;
        private String last_name;

        public String getFirstName() {
            return first_name;
        }

        public String getLastName() {
            return last_name;
        }
    }

    public String getId() {
        return _id;
    }

    public String getGooglePlayId() {
        return google_play_id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getNumber() {
        return number;
    }

    public int getPagesSize() {
        return pages_size;
    }

//    public List<Author> getAuthors() {
//        return author;
//    }


    public String getAuthorsCompact() {
        return authors_compact;
    }

    public String getAuthors() {
        return authors;
    }

    public String getType() {
        return type;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isSmartSonix() {
        return isSmartSonix == 1;
    }

    public String getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public String getPicture() {
        return picture;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceText() {
        return priceText;
    }

    public void setPriceText(String priceText) {
        this.priceText = priceText;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setIsPurchased(boolean isPurchased) {
        this.isPurchased = isPurchased;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Page getCoverPreview() {
        return new Page(getId() + "_cover", getPicture(), null, 0);
    }
}
