package nanodegree.gemma.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nanodegree.gemma.alexandria.R;
import nanodegree.gemma.alexandria.data.AlexandriaContract;
import nanodegree.gemma.alexandria.services.DownloadImage;

/**
 * Created by saj on 11/01/15.
 * Updated by Gemma Lara 02/2016 for Udacity Android Developer Nanodegree
 */
public class BookListAdapter extends CursorAdapter {


    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // a11y
        // Tested with TalkBack and decided that best option is to have book title
        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        new DownloadImage(viewHolder.bookCover).execute(imgUrl);

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        // a11y
        String bookTitleDescription = context.getResources().getString(R.string.book_title) + " " + bookTitle;
        viewHolder.bookTitle.setContentDescription(bookTitleDescription);
//        Log.d("A11y", bookTitleDescription);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);

        // a11y Decided this makes the list too long, this info will be show inside book detail
//        viewHolder.bookSubTitle.setContentDescription(bookSubTitle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
